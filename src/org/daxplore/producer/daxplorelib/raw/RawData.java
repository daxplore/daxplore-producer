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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.daxplore.producer.daxplorelib.About;
import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.DaxploreTable;
import org.daxplore.producer.daxplorelib.SQLTools;
import org.daxplore.producer.daxplorelib.raw.RawMetaQuestion.RawMetaManager;
import org.daxplore.producer.tools.MyTools;
import org.opendatafoundation.data.spss.SPSSFile;
import org.opendatafoundation.data.spss.SPSSFileException;

import com.google.common.base.Strings;

public class RawData {
	private static final String tablename = "rawdata";
	
	public static class RawDataManager {
		private final DaxploreTable table;
		
		private Connection connection;
		private About about;
		private RawMetaManager rawMetaManager;
		
		private boolean modified = false;
		
		/**
		 * Indexed as data[column][row]
		 */
		private Object[][] data = new Object[0][0];
		
		public RawDataManager(Connection connection, About about, RawMetaManager rawMetaManager) throws SQLException {
			this.connection = connection;
			this.about = about;
			this.rawMetaManager = rawMetaManager;
			
			String sql = null;
			if (SQLTools.tableExists(tablename, connection)){
				try (PreparedStatement stmt = connection.prepareStatement("SELECT sql FROM sqlite_master WHERE name = ?")) {
					stmt.setString(1, tablename);
					try(ResultSet rs = stmt.executeQuery()) {
						rs.next();
						sql = rs.getString("sql");
					}
				}
				loadAllToMemory();
			}
			
			table = new DaxploreTable(sql, tablename);
		}
		
		private void loadAllToMemory() throws SQLException {
			List<RawMetaQuestion> columns = rawMetaManager.getQuestions();
			
			if (!SQLTools.tableExists(tablename, connection)) {
				data = new Object[0][columns.size()];
			}

			int colCount = columns.size();
			
			try (Statement stmt = connection.createStatement();
					ResultSet rs = stmt.executeQuery("SELECT count(*) as cnt from rawdata")) {
				rs.next();
				int rowCount = rs.getInt("cnt");
				data = new Object[colCount][rowCount];
			}
			
			try(Statement stmt = connection.createStatement();
					ResultSet rs = stmt.executeQuery("SELECT * FROM rawdata")) {
				for(int row = 0; rs.next(); row++) {
					for(int col = 0; col < columns.size(); col++) {
						switch (columns.get(col).getQtype()) {
						case NUMERIC:
							data[col][row] = rs.getDouble(col + 1);
							if(rs.wasNull()) {
								data[col][row] = null;
							}
							break;
						case TEXT:
							data[col][row] = rs.getString(col + 1);
							break;
						}
					}
				}
			}
		}
		
		public void saveAll() throws SQLException {
			if (!modified) {
				return;
			}
			if (SQLTools.tableExists(table.name, connection)) {
				try (Statement stmt = connection.createStatement()) {
					stmt.executeUpdate("DROP TABLE " + table.name);
				}
			}
			
			List<RawMetaQuestion> rawMetaQuestions = rawMetaManager.getQuestions();
			
			try(Statement stmt = connection.createStatement()) {
				String createString = createRawDataTableString(rawMetaQuestions);
				table.sql = createString;
				stmt.execute(createString);
			}
			
			int rows = data.length > 0 ? data[0].length : 0;
			
			try (PreparedStatement addRowStatement = addRowStatement(rawMetaQuestions, connection)) {
				for (int row = 0; row < rows; row++) {
					int col = 0;
					for(RawMetaQuestion rmq : rawMetaQuestions) {
						switch (rmq.getQtype()) {
						case NUMERIC:
							Double ddatapoint = (Double)data[col][row];
							if(ddatapoint == null || ddatapoint.isNaN() || ddatapoint.isInfinite()){
								addRowStatement.setNull(col+1, java.sql.Types.REAL);
							} else {
								addRowStatement.setDouble(col+1 , ddatapoint);
							}
							break;
						case TEXT:
							String sdatapoint = (String)data[col][row];
							if(Strings.isNullOrEmpty(sdatapoint)) {
								addRowStatement.setNull(col+1, java.sql.Types.VARCHAR);
							} else {
								addRowStatement.setString(col+1, sdatapoint);
							}
							break;
						default:
							throw new AssertionError("Unsupported SPSS data type: " + rmq.getQtype().toString());
						}
						col++;
					}
					addRowStatement.addBatch();
				}
				addRowStatement.executeBatch();
			}
		}
		
		
		public void loadFromSPSS(SPSSFile spssFile) throws DaxploreException {
			try {
				Iterator<Object[]> iter = spssFile.getDataIterator();
				List<Object[]> rows = new ArrayList<Object[]>();
				while(iter.hasNext()){
					rows.add(iter.next());
				}
				Object[][] loadedData = new Object[rawMetaManager.getQuestionCount()][rows.size()];
				int rowIndex = 0;
				for (Object[] row : rows) {
					for (int col = 0; col < rawMetaManager.getQuestionCount(); col++) {
						if (!(row[col] instanceof Double && Double.isNaN((Double)row[col]))) {
							loadedData[col][rowIndex] = row[col];
						}
					}
					rowIndex++;
				}
				data = loadedData;
				modified = true;
			} catch (IOException | SPSSFileException e) {
				throw new DaxploreException("Failed to read SPSS file", e);
			}
		}
		
		public SortedMap<Object, Integer> getColumnValueCount(String column) throws DaxploreException {
			SortedMap<Object, Integer> counts = new TreeMap<>(naturalOrderNullLowComparator);
			int colIndex = rawMetaManager.getIndexOfColumn(column);
			if (data.length > 0) {
				for (int row = 0; row < data[0].length; row++) {
					Object val = data[colIndex][row];
					if (counts.containsKey(val)) {
						int previous = counts.get(val);
						counts.put(val, previous+1);
					} else {
						counts.put(val, 1);
					}
				}
			}
			return counts;
		}
		
		// TODO fix the assumption that only number type is supported, requires changes in timepoint managment
		public SortedMap<Double, Integer> getColumnValueCountWhere(String column1, String column2) throws DaxploreException {
			SortedMap<Double, Integer> counts = new TreeMap<>(naturalOrderNullLowComparator);
			if (column1 != null) {
				int col1 = rawMetaManager.getIndexOfColumn(column1);
				int col2 = rawMetaManager.getIndexOfColumn(column2);
				if (data.length > 0) {
					for (int row = 0; row < data[0].length; row++) {
						Double val1 = asDouble(data[col1][row]);
						if (data[col2][row] != null) {
							if (counts.containsKey(val1)) {
								int previous = counts.get(val1);
								counts.put(val1, previous+1);
							} else {
								counts.put(val1, 1);
							}
						}
					}
				}
			}
			return counts;
		}
	}
	
	
	// helper functions
	
	private static Comparator<Object> naturalOrderNullLowComparator = new Comparator<Object>() {
		@Override
		public int compare(Object o1, Object o2) {
			if (o1 == null && o2 == null) return 0;
			if (o1 == null) return -1;
			if (o2 == null) return 1;
			return ((Comparable<Object>)o1).compareTo(o2);
		}
	};
	
	private static Double asDouble(Object o) {
		if (o == null) return null;
		if (o instanceof Double) return (Double)o;
		return Double.parseDouble((String)o);
	}
	
	private static PreparedStatement addRowStatement(List<RawMetaQuestion> columns, Connection connection) throws SQLException {
		LinkedList<String> qmarks = new LinkedList<>();
		for(int i = 0; i < columns.size(); i++){
			qmarks.add("?");
		}
		PreparedStatement ps = connection.prepareStatement("insert into " + tablename + " values("+ MyTools.join(qmarks, ", ") + ")");
		return ps;
	}
	
	private static String createRawDataTableString(List<RawMetaQuestion> columns) {
		StringBuilder sb = new StringBuilder();
		sb.append("create table " + tablename + " (");
		int index = 0;
		for (RawMetaQuestion rmq : columns) {
			sb.append("[" + rmq.getColumn() + "] ");
			sb.append(rmq.getQtype().sqltype());
			if(index < columns.size()-1){
				sb.append(", ");
			}
			index++;
		}
		sb.append(")");
		return sb.toString();
	}
}


