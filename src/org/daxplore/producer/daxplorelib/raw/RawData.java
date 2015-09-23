/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.daxplorelib.raw;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.DaxploreFile;
import org.daxplore.producer.daxplorelib.DaxploreTable;
import org.daxplore.producer.daxplorelib.SQLTools;
import org.daxplore.producer.tools.MyTools;
import org.daxplore.producer.tools.Pair;
import org.opendatafoundation.data.spss.SPSSFile;
import org.opendatafoundation.data.spss.SPSSFileException;
import org.opendatafoundation.data.spss.SPSSNumericVariable;
import org.opendatafoundation.data.spss.SPSSStringVariable;
import org.opendatafoundation.data.spss.SPSSVariable;

import com.google.common.base.Strings;


public class RawData {
	protected final DaxploreTable table = new DaxploreTable(null, "rawdata");
	static final String tablename = "rawdata";
	Connection connection;
	
	public RawData(Connection connection) throws SQLException{
		this.connection = connection;
		if(SQLTools.tableExists(table.name, connection)){
			try (PreparedStatement stmt = connection.prepareStatement("SELECT sql FROM sqlite_master WHERE name=?")) {
				stmt.setString(1, table.name);
				try(ResultSet rs = stmt.executeQuery()) {
					rs.next();
					table.sql = rs.getString("sql");
				}
			}
		}
	}
	
	public void importSPSS(SPSSFile spssFile) throws SQLException, DaxploreException {
		if(SQLTools.tableExists("rawdata", connection)) {
			//TODO this code is/was broken, may be fixed now as the project uses try-with-resource for everything
			//java.sql.SQLException: database table is locked
			try(Statement stmt = connection.createStatement()) {
				stmt.executeUpdate("DROP TABLE " + table.name);
			}
		}
		
		LinkedHashMap<String, VariableType> columns = new LinkedHashMap<>(); //LinkedHashMap has predictable iteration order! 
		
		for(int i = 0; i < spssFile.getVariableCount(); i++){
			SPSSVariable var = spssFile.getVariable(i);
			if(!DaxploreFile.isValidColumnName(var.getName())) {
				throw new DaxploreException("\"" + var.getName() + "\" is not a valid variable name");
			}
			VariableType qtype;
			if(var instanceof SPSSNumericVariable){
				qtype = VariableType.NUMERIC;
				columns.put(var.getName(), qtype);
			} else if (var instanceof SPSSStringVariable){
				qtype = VariableType.TEXT;
				columns.put(var.getName(), qtype);
			} else throw new Error("shuoldn't happen");
		}
		
		try(Statement stmt = connection.createStatement()) {
			String createString = createRawDataTableString(columns);
			table.sql = createString;
			stmt.execute(createString);
		}
		
		try (PreparedStatement addRowStatement = addRowStatement(columns, connection)) {
			Iterator<Object[]> iter = spssFile.getDataIterator();
			while(iter.hasNext()){
				Object[] data = iter.next();
				addRow(columns, addRowStatement, data);
			}
		} catch (IOException | SPSSFileException e) {
			throw new DaxploreException("Failed to read SPSS file");
		}
	}
	
	protected static String createRawDataTableString(LinkedHashMap<String, VariableType> columns) {
		StringBuilder sb = new StringBuilder();
		Iterator<String> iter = columns.keySet().iterator();
		sb.append("create table " + tablename + " (");
		while(iter.hasNext()){
			String s = iter.next();
			sb.append("[" + s + "] ");
			sb.append(columns.get(s).sqltype());
			if(iter.hasNext()){
				sb.append(", ");
			}
		}
		sb.append(")");
		return sb.toString();
	}
	
	protected static PreparedStatement addRowStatement(LinkedHashMap<String, VariableType> columns, Connection connection) throws SQLException {
		LinkedList<String> qmarks = new LinkedList<>();
		for(int i = 0; i < columns.size(); i++){
			qmarks.add("?");
		}
		PreparedStatement ps = connection.prepareStatement("insert into " + tablename + " values("+ MyTools.join(qmarks, ", ") + ")");
		return ps;
	}
	
	protected static void addRow(LinkedHashMap<String, VariableType> columns, PreparedStatement statement, Object[] data) throws SQLException {
		Collection<VariableType> types= columns.values();
		int colIndex = 0;
		for(VariableType type: types){
			Object datapoint = data[colIndex];
			
			switch (type) {
			case NUMERIC:
				Double ddatapoint = (Double)datapoint;
				if(ddatapoint == null || ddatapoint.isNaN() || ddatapoint.isInfinite()){
					statement.setNull(colIndex+1, java.sql.Types.REAL);
				} else {
					statement.setDouble(colIndex+1 , ddatapoint);
				}
				break;
			case TEXT:
				String sdatapoint = (String)datapoint;
				if(Strings.isNullOrEmpty(sdatapoint)) {
					statement.setNull(colIndex+1, java.sql.Types.VARCHAR); //TODO: varchar may be wrong. Check!
				} else {
					statement.setString(colIndex+1, sdatapoint);
				}
				break;
			default:
				throw new AssertionError("Unsupported SPSS data type: " + type.toString());
			}
			colIndex++;
		}
		statement.execute();
		//don't close statement
	}
	
	public int getNumberOfRows() throws DaxploreException {
		int ret;
		try (Statement stmt = connection.createStatement()) {
			try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM " + tablename)) {
				rs.next();
				ret = rs.getInt(1);
			}
		} catch (SQLException e) {
			throw new DaxploreException("Failed to get row count in RawData", e);
		}
		return ret;
	}
	
	public boolean hasData() {
		if(!SQLTools.tableExists("rawdata", connection)) {
			return false;
		}
		try {
			return getNumberOfRows() > 0;
		} catch (DaxploreException e) {
			return false;
		}
	}
	
	/**
	 * Fetch a list of the different values found in this column in the rawdata
	 * table and their frequencies.
	 * 
	 * <p>The returned list contains pairs of <column-value, frequency>.</p>
	 * 
	 * <p><b>Note:</b> Call {@link RawData#hasColumn(String)} first to make sure that the column exists!
	 * 
	 * @param column The column in the rawdata table
	 * @return A list of pairs containing <value, count>
	 * @throws SQLException
	 */
	public LinkedHashMap<Object, Integer> getColumnValueCount(String column) throws SQLException {
		LinkedHashMap<Object, Integer> map = new LinkedHashMap<>();
		//TODO call hasColumn automatically?
		//Prepared statement doesn't work, but hasColumn is always called first so this should be relatively injection-safe
		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(
				"select \"" + column + "\" as val, count(*) as cnt from rawdata group by val order by val")) {
			while(rs.next()) {
				Object val = rs.getObject("val");
				Integer count = rs.getInt("cnt");
				map.put(val, count);
			}
		}
		return map;
	}
	
	public LinkedList<Pair<Double, Integer>> getColumnValueCountWhere(String column, String column2) throws SQLException {
		LinkedList<Pair<Double, Integer>> map = new LinkedList<>();
		//TODO call hasColumn automatically?
		//Prepared statement doesn't work, but hasColumn is always called first so this should be relatively injection-safe
		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(
				"SELECT \""+ column + "\" AS val, count(*) AS cnt FROM rawdata WHERE \"" + column2 + "\" IS NOT NULL GROUP BY val ORDER BY val")) {
			while(rs.next()) {
				double val = rs.getDouble("val");
				boolean nullVal = rs.wasNull();
				Integer count = rs.getInt("cnt");
				if(!nullVal) {
					map.add(new Pair<>(val, count));
				} else {
					map.add(new Pair<Double, Integer>(null, count));
				}
			}
		}
		return map;
	}
}


