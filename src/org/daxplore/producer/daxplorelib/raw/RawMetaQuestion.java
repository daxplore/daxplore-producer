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
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

import com.google.common.collect.Sets;

public class RawMetaQuestion {
	private static final DaxploreTable table = new DaxploreTable(
			"CREATE TABLE rawmeta ("
			+ "column TEXT PRIMARY KEY, "
			+ "longname TEXT, "
			+ "qtext TEXT, "
			+ "qtype TEXT, "
			+ "spsstype TEXT, "
			+ "valuelabels TEXT, "
			+ "measure TEXT)",
			"rawmeta");
	
	public static class RawMetaManager {
		
		private Connection connection;
		private LinkedHashMap<String, RawMetaQuestion> columnMap = new LinkedHashMap<>();
		private HashMap<String, Integer> columnIndexMap = new HashMap<>();
		private List<RawMetaQuestion> toBeAdded = new ArrayList<>();
		private Map<String, RawMetaQuestion> toBeRemoved = new HashMap<>();
		
		public RawMetaManager(Connection connection) throws SQLException, DaxploreException {
			this.connection = connection;
			boolean created = SQLTools.createIfNotExists(table, connection);
			if (!created) {
				loadAllToMemory();
			}
		}
		
		// completely replaces in-memory version
		private void loadAllToMemory() throws DaxploreException, SQLException {
			try(ResultSet rawDataColumnResultSet = connection.createStatement().executeQuery("PRAGMA table_info('rawdata')");
				PreparedStatement stmt = connection.prepareStatement("SELECT * FROM rawmeta ORDER BY column ASC");
				ResultSet rs = stmt.executeQuery()) {
				
				columnMap = new LinkedHashMap<String, RawMetaQuestion>();
				
				HashSet<String> rawDataColumns = new HashSet<String>();
				HashSet<String> rawMetaColumns = new HashSet<String>();
				
				// insert columns in map so that columnMap's order matches the column order of rawdata
				int index = 0;
				while (rawDataColumnResultSet.next()) {
					String column = rawDataColumnResultSet.getString("name");
					rawDataColumns.add(column);
					columnMap.put(column, null);
					columnIndexMap.put(column, index++);
				}
				
				while (rs.next()) {
					String column = rs.getString("column");
					rawMetaColumns.add(column);
					
					VariableType qtype = VariableType.valueOf(rs.getString("qtype"));
					String valuelabelString = rs.getString("valuelabels");
					LinkedHashMap<Object, String> valuelabels = null;
					if(valuelabelString != null && !valuelabelString.isEmpty()) {
						switch (qtype) {
						case NUMERIC:
							valuelabels = JSONtoCategoriesDoubles(valuelabelString);
							break;
						case TEXT:
							valuelabels = JSONtoCategoriesStrings(valuelabelString);
							break;
						default:
							break;
						}
					}
					
					RawMetaQuestion rmq = new RawMetaQuestion(
						column,
						rs.getString("longname"),
						rs.getString("qtext"),
						qtype,
						rs.getString("spsstype"),
						valuelabels,
						rs.getString("measure"));
					
					columnMap.put(column, rmq);
				}
				
				if (Sets.symmetricDifference(rawDataColumns, rawMetaColumns).size() > 0) {
					throw new DaxploreException("Corrupt data file: rawdata columns doesn't match rawmeta columns");
				}
			}
		}
		
		public RawMetaQuestion get(String column) throws DaxploreException {
			if (columnMap.containsKey(column)) {
				return columnMap.get(column);
			}
			throw new DaxploreException("No raw meta question with column name '" + column + "'");
		}

		public void saveAll() throws SQLException, DaxploreException {
			int nNew = 0, nModified = 0, nRemoved = 0;
			try(
				PreparedStatement addStmt = connection.prepareStatement("INSERT INTO rawmeta values (?, ?, ?, ?, ?, ?, ?)");
				PreparedStatement updateStmt = connection.prepareStatement("UPDATE rawmeta SET "
						+ "longname = ?, qtext = ?, qtype = ?, spsstype = ?, valuelabels = ?, measure = ? WHERE column = ?");
				PreparedStatement deleteStmt = connection.prepareStatement("DELETE FROM rawmeta WHERE column = ?")) {

				for (RawMetaQuestion rmq : columnMap.values()) {
					if (rmq.modified) {
						nModified++;
						updateStmt.setString(1, rmq.longname);
						updateStmt.setString(2, rmq.qtext);
						updateStmt.setString(3, rmq.qtype.name());
						updateStmt.setString(4, rmq.spsstype);
						if (rmq.valuelabels.isEmpty()) {
							updateStmt.setNull(5, Types.VARCHAR);
						} else {
							updateStmt.setString(5, rmq.getValuelabelsJSONString());
						}
						updateStmt.setString(6, rmq.measure);
						updateStmt.setString(7, rmq.column);
						updateStmt.executeUpdate();
						
						rmq.modified = false;
					}
				}

				for (RawMetaQuestion rmq : toBeRemoved.values()) {
					nRemoved++;
					deleteStmt.setString(1, rmq.column);
					deleteStmt.addBatch();
				}
				deleteStmt.executeBatch();
				toBeRemoved.clear();
				
				for (RawMetaQuestion rmq : toBeAdded) {
					nNew++;
					addStmt.setString(1, rmq.column);
					addStmt.setString(2, rmq.longname);
					addStmt.setString(3, rmq.qtext);
					addStmt.setString(4, rmq.qtype.name());
					addStmt.setString(5, rmq.spsstype);
					if (rmq.valuelabels.isEmpty()) {
						addStmt.setNull(6, Types.VARCHAR);
					} else {
						addStmt.setString(6, rmq.getValuelabelsJSONString());
					}
					addStmt.setString(7, rmq.measure);
					addStmt.addBatch();
				}
				addStmt.executeBatch();
				toBeAdded.clear();
				
			} catch (SQLException e) {
				throw new DaxploreException("Failed to add new RawMeta row", e);
			}
			
			if (nNew != 0) {
				String logString = String.format(
						"RawMetaQuestion: Saved %d (%d new), %d removed",
						nModified+nNew, nNew, nRemoved);
				Logger.getGlobal().log(Level.INFO, logString);
			}
		}
		
		public void loadFromSPSS(SPSSFile spssFile) throws DaxploreException {
			for(int i = 0; i < spssFile.getVariableCount(); i++){
				SPSSVariable var = spssFile.getVariable(i);
				String column = var.getName();
				if(!DaxploreFile.isValidColumnName(column)) {
					throw new DaxploreException("'" + column + "' is not a valid variable name");
				}
				
				LinkedHashMap<Object, String> valuelabels = new LinkedHashMap<>();
				String spsstype;
				VariableType qtype;
				
				if (var instanceof SPSSNumericVariable) {
					spsstype = "Numeric";
					qtype = VariableType.NUMERIC;
				} else if (var instanceof SPSSStringVariable){
					spsstype = "String";
					qtype = VariableType.TEXT;
				} else {
					throw new Error("Unknown SPSS variable type");
				}
				
				if(var.hasValueLabels()){ //Mapped stored implicitly as valuelabels != null
					for (String key : var.categoryMap.keySet()) {
						if (qtype == VariableType.NUMERIC) {
							valuelabels.put(Double.parseDouble(key), var.categoryMap.get(key).label);
						} else {
							valuelabels.put(key, var.categoryMap.get(key).label);
						}
					}
				}
				
				RawMetaQuestion rmq = new RawMetaQuestion(
					column,
					column,
					var.getLabel(),
					qtype,
					spsstype,
					valuelabels,
					var.getMeasureLabel());
				
				// TODO handle different diff scenarios
				toBeAdded.add(rmq);
				columnMap.put(column, rmq);
			}
		}

		public boolean hasColumn(String column) {
			return columnMap.containsKey(column);
		}

		public RawMetaQuestion getQuestion(String column) {
			return columnMap.get(column);
		}

		public List<RawMetaQuestion> getQuestions() {
			return new ArrayList<RawMetaQuestion>(columnMap.values());
		}
		
		public int getIndexOfColumn(String column) {
			if (columnIndexMap.containsKey(column)) {
				return columnIndexMap.get(column);
			}
			return -1;
		}
		
		public List<String> getColumnNames() {
			List<String> columns = new ArrayList<>();
			for (RawMetaQuestion rmq : columnMap.values()) {
				columns.add(rmq.column);
			}
			return columns;
		}

		public int getQuestionCount() {
			return columnMap.size();
		}
	}
	
	private String column, longname, qtext, spsstype, measure;
	private VariableType qtype;
	private LinkedHashMap<Object, String> valuelabels;
	
	private boolean modified = false;
	
	public RawMetaQuestion(String column, String longname, String qtext, VariableType qtype,
			String spsstype, LinkedHashMap<Object, String> valuelabels, String measure) {
		this.column = column;
		this.longname = longname;
		this.qtext = qtext;
		this.spsstype = spsstype;
		this.measure = measure;
		this.qtype = qtype;
		this.valuelabels = valuelabels;
	}

	public String getColumn() {
		return column;
	}

	public String getLongname() {
		return longname;
	}

	public String getQtext() {
		return qtext;
	}

	public String getSpsstype() {
		return spsstype;
	}

	public String getMeasure() {
		return measure;
	}

	public VariableType getQtype() {
		return qtype;
	}

	public LinkedHashMap<Object, String> getValuelabels() {
		return valuelabels;
	}
	
	private String getValuelabelsJSONString() {
		return JSONValue.toJSONString(valuelabels);
	}

	public boolean isModified() {
		return modified;
	}

	
	// Help functions
	
	@SuppressWarnings("rawtypes")
	private static LinkedHashMap<Object, String> JSONtoCategoriesDoubles(String jsonstring) throws DaxploreException {
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
			throw new DaxploreException("Failed to parse json", e);
		}
		Iterator iter = json.entrySet().iterator();
		
		while(iter.hasNext()) {
			Map.Entry entry = (Map.Entry)iter.next();
			list.put(Double.parseDouble((String)entry.getKey()), (String) entry.getValue());
		}
		
		return list;
	}
	
	@SuppressWarnings("rawtypes")
	private static LinkedHashMap<Object, String> JSONtoCategoriesStrings(String jsonstring) throws DaxploreException {
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
			throw new DaxploreException("Failed to parse json", e); 
		}
		Iterator iter = json.entrySet().iterator();
		
		while(iter.hasNext()) {
			Map.Entry entry = (Map.Entry)iter.next();
			list.put((String)entry.getKey(), (String) entry.getValue());
		}
		
		return list;
	}
}

