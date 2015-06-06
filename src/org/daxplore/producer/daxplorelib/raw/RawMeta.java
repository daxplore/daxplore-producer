/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.daxplorelib.raw;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.DaxploreFile;
import org.daxplore.producer.daxplorelib.DaxploreTable;
import org.daxplore.producer.daxplorelib.SQLTools;
import org.json.simple.JSONValue;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.opendatafoundation.data.spss.SPSSFile;
import org.opendatafoundation.data.spss.SPSSNumericVariable;
import org.opendatafoundation.data.spss.SPSSStringVariable;
import org.opendatafoundation.data.spss.SPSSVariable;
import org.opendatafoundation.data.spss.SPSSVariableCategory;


public class RawMeta {
	protected static final DaxploreTable table = new DaxploreTable("CREATE TABLE rawmeta (column TEXT, longname TEXT, qtext TEXT, qtype TEXT, spsstype TEXT, valuelabels TEXT, measure TEXT)", "rawmeta");
	
	public class RawMetaQuestion {
		public String column, longname, qtext, spsstype, measure;
		public VariableType qtype = null;
		public LinkedHashMap<Object, String> valuelables;
	}
	
	Connection connection;
	
	public RawMeta(Connection connection) throws SQLException{
		this.connection = connection;
		SQLTools.createIfNotExists(table, connection);
	}
	
	public List<String> getColumns() throws DaxploreException {
		List<String> list = new LinkedList<>();
		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT column FROM rawmeta")) {
			while(rs.next()){
				list.add(rs.getString("column"));
			}
		} catch (SQLException e) {
			throw new DaxploreException("Failed to read columns in RawMeta", e);
		}
		return list;
	}
	
	public boolean hasColumn(String column) throws SQLException {
		try (PreparedStatement stmt = connection.prepareStatement(
				"SELECT column FROM rawmeta WHERE column LIKE ?")) {
			stmt.setString(1, column);
			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next();
			}
		}
	}
	
	/*public Map<String, VariableType> getColumnMap() throws SQLException{
		Map<String, VariableType> columns = new LinkedHashMap<>();
		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT column, qtype FROM rawmeta")) {
			while(rs.next()){
				String col = rs.getString("column");
				VariableType type = VariableType.valueOf(rs.getString("qtype"));
				columns.put(col, type);
			}
		}
		return columns;
	}*/
	
	public void importSPSS(SPSSFile spssFile) throws DaxploreException {
		Map<String, String> columns = new LinkedHashMap<>();
		try {
			clearRawMetaTable(connection);
		} catch (SQLException e) {
			throw new DaxploreException("Failed to clear RawMetaTable", e);
		}
		for(int i = 0; i < spssFile.getVariableCount(); i++){
			SPSSVariable var = spssFile.getVariable(i);
			if(!DaxploreFile.isValidColumnName(var.getName())) {
				throw new DaxploreException("\"" + var.getName() + "\" is not a valid variable name");
			}
			String spsstype;
			String qtype;
			if(var instanceof SPSSNumericVariable){
				spsstype = "Numeric";
				qtype = VariableType.NUMERIC.toString();
				columns.put(var.getName(), "real");
			} else if (var instanceof SPSSStringVariable){
				spsstype = "String";
				qtype = VariableType.TEXT.toString();
				columns.put(var.getName(), "text");
			} else {
				throw new Error("shuoldn't happen");
			}
			
			String valuelabels = null;
			if(var.hasValueLabels()){ //Mapped stored implicitly as valuelabels != null
				valuelabels = categoriesToJSON(var.categoryMap);
			}
			String measure = var.getMeasureLabel();
			try(PreparedStatement stmt = connection.prepareStatement(
					"INSERT INTO rawmeta values (?, ?, ?, ?, ?, ?, ?)")) {
				addColumnMeta(
						stmt,
						var.getName(),
						var.getName(),
						var.getLabel(),
						qtype,
						spsstype,
						valuelabels,
						measure
						);
			} catch (SQLException e) {
				throw new DaxploreException("Failed to add new RawMeta row", e);
			}
		}
	}
	
	public List<RawMetaQuestion> getQuestions() throws SQLException { 
		List<RawMetaQuestion> rawQuestionList = new LinkedList<>();
		try(PreparedStatement stmt = connection.prepareStatement("SELECT * FROM rawmeta ORDER BY column ASC");
			ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				RawMetaQuestion rmq = new RawMetaQuestion();
				rmq.column = rs.getString("column");
				rmq.longname = rs.getString("longname");
				rmq.measure = rs.getString("measure");
				rmq.qtext = rs.getString("qtext");
				String qtype = rs.getString("qtype");
				rmq.qtype = VariableType.valueOf(qtype);
				rmq.spsstype = rs.getString("spsstype");
				String cats = rs.getString("valuelabels");
				if(cats != null && !cats.isEmpty()) {
					switch (rmq.qtype) {
					case NUMERIC:
						try {
							rmq.valuelables = JSONtoCategoriesDoubles(rs.getString("valuelabels"));
						} catch (NumberFormatException e) {
							//TODO: send message to client
							Logger.getGlobal().log(Level.SEVERE, "Variable \"" + rmq.column + "\" was ignored");
							continue;
						}
						break;
					case TEXT:
						rmq.valuelables = JSONtoCategoriesStrings(rs.getString("valuelabels"));
						break;
					default:
						break;
						
					}
				} else {
					rmq.valuelables = null; 
				}
				rawQuestionList.add(rmq);
			}
		}
		return rawQuestionList;
	}
	
	public RawMetaQuestion getQuestion(String column) throws DaxploreException {
		try(PreparedStatement stmt = connection.prepareStatement("SELECT * FROM rawmeta WHERE column = ?")) { 
			stmt.setString(1, column);
			try(ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					RawMetaQuestion rmq = new RawMetaQuestion();
					rmq.column = rs.getString("column");
					rmq.longname = rs.getString("longname");
					rmq.measure = rs.getString("measure");
					rmq.qtext = rs.getString("qtext");
					String qtype = rs.getString("qtype");
					rmq.qtype = VariableType.valueOf(qtype);
					rmq.spsstype = rs.getString("spsstype");
					String cats = rs.getString("valuelabels");
					if(cats != null && !cats.isEmpty()) {
						switch (rmq.qtype) {
						case NUMERIC:
							rmq.valuelables = JSONtoCategoriesDoubles(rs.getString("valuelabels"));
							break;
						case TEXT:
							rmq.valuelables = JSONtoCategoriesStrings(rs.getString("valuelabels"));
							break;
						default:
							break;
							
						}
					} else {
						rmq.valuelables = null; 
					}
					return rmq;
				}
			}
		} catch (SQLException e) {
			throw new DaxploreException("Failure to read from rawmeta", e);
		}
		return null;
	}
	
	public boolean hasData() {
		if(!SQLTools.tableExists("rawmeta", connection)) {
			return false;
		}
		try {
			return getColumns().size() > 0;
		} catch (DaxploreException e) {
			return false;
		}
	}
	
	/**
	 * Compare the columns of two different versions.
	 * 
	 * @param other ImportedData to compare to.
	 * @return Map of all columns with the values 0 if they exist in both, -1 if it only exists in other and 1 if it only exists in this
	 */
	public Map<String, Integer> compareColumns(RawMeta other) throws DaxploreException {
		Map<String, Integer> columnMap = new HashMap<>();
		List<String> columnsthis = getColumns();
		List<String> columnsother = other.getColumns();
		for(String s: columnsthis){
			if(columnsother.contains(s)) {
				columnMap.put(s, 0);
			} else {
				columnMap.put(s, 1);
			}
		}
		for(String s: columnsother){
			if(!columnsthis.contains(s)){
				columnMap.put(s, -1);
			}
		}
		return columnMap;
	}
	
	protected static void clearRawMetaTable(Connection conn) throws SQLException {
		try(Statement stmt = conn.createStatement()) {
			stmt.executeUpdate("DELETE FROM rawmeta");
		}
	}
	
	protected static void addColumnMeta(PreparedStatement stmt, String column, String longname, String qtext, String qtype, String spsstype, String valuelabels, String measure) throws SQLException {
		
		if(column != null)stmt.setString(1, column);
		else throw new NullPointerException();
		
		if(longname != null) stmt.setString(2, longname);
		else stmt.setNull(2, java.sql.Types.VARCHAR);
		
		if(qtext != null) stmt.setString(3, qtext);
		else stmt.setNull(3, java.sql.Types.VARCHAR);
		
		if(qtype != null) stmt.setString(4, qtype);
		else stmt.setNull(4, java.sql.Types.VARCHAR);
		
		if(spsstype != null) stmt.setString(5, spsstype);
		else stmt.setNull(5, java.sql.Types.VARCHAR);
		
		if(valuelabels != null) stmt.setString(6, valuelabels);
		else stmt.setNull(6, java.sql.Types.VARCHAR);
		
		if(measure != null) stmt.setString(7, measure);
		else stmt.setNull(7, java.sql.Types.VARCHAR);
		
		stmt.executeUpdate();
	}
	
	protected static String categoriesToJSON(Map<String, SPSSVariableCategory> categories){
		Set<String> keyset = categories.keySet();
		Map<Object,String> catObj = new LinkedHashMap<>();
		for(String key : keyset){
			//if(categories.get(key).value != Double.NaN){
			//	catObj.put(new Double(categories.get(key).value), categories.get(key).label);
			//} else {
			catObj.put(categories.get(key).strValue, categories.get(key).label);
			//}
		}
		return JSONValue.toJSONString(catObj);
	}
	
	@SuppressWarnings("rawtypes")
	protected static LinkedHashMap<Object, String> JSONtoCategoriesDoubles(String jsonstring) {
		LinkedHashMap<Object, String> list = new LinkedHashMap<>();
		
		JSONParser parser = new JSONParser();
		ContainerFactory containerFactory = new ContainerFactory(){
			@Override
			public List creatArrayContainer() {
				return new LinkedList();
			}
			@Override
			public Map createObjectContainer() {
				return new LinkedHashMap();
			}
		};
		Map json;
		try {
			json = (Map)parser.parse(jsonstring, containerFactory);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
		Iterator iter = json.entrySet().iterator();
		
		while(iter.hasNext()) {
			Map.Entry entry = (Map.Entry)iter.next();
			list.put(Double.parseDouble((String)entry.getKey()), (String) entry.getValue());
		}
		
		return list;
	}
	
	@SuppressWarnings("rawtypes")
	protected static LinkedHashMap<Object, String> JSONtoCategoriesStrings(String jsonstring) {
		LinkedHashMap<Object, String> list = new LinkedHashMap<>();
		
		JSONParser parser = new JSONParser();
		ContainerFactory containerFactory = new ContainerFactory(){
			@Override
			public List creatArrayContainer() {
				return new LinkedList();
			}
			@Override
			public Map createObjectContainer() {
				return new LinkedHashMap();
			}
		};
		Map json;
		try {
			json = (Map)parser.parse(jsonstring, containerFactory);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
		Iterator iter = json.entrySet().iterator();
		
		while(iter.hasNext()) {
			Map.Entry entry = (Map.Entry)iter.next();
			list.put((String)entry.getKey(), (String) entry.getValue());
		}
		
		return list;
	}
}

