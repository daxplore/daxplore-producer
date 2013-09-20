package org.daxplore.producer.daxplorelib.raw;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.daxplore.producer.daxplorelib.DaxploreTable;
import org.daxplore.producer.daxplorelib.SQLTools;
import org.daxplore.producer.tools.MyTools;
import org.daxplore.producer.tools.Pair;
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
		public List<Pair<String, Double>> valuelables;
	}
	
	Connection connection;
	
	public RawMeta(Connection connection) throws SQLException{
		this.connection = connection;
		SQLTools.createIfNotExists(table, connection);
	}
	
	public List<String> getColumns() throws SQLException{
		List<String> list = new LinkedList<>();
		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT column FROM rawmeta")) {
			while(rs.next()){
				list.add(rs.getString("column"));
			}
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
	
	public Map<String, VariableType> getColumnMap() throws SQLException{
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
	}
	
	public void importSPSS(SPSSFile spssFile) throws SQLException {
		Map<String, String> columns = new LinkedHashMap<>();
		try {
			clearRawMetaTable(connection);
		} catch (SQLException e) {
			System.out.println("error clearing table");
			MyTools.printSQLExeption(e);
			throw e; //TODO both catching and throwing SQLException!?
		}
		for(int i = 0; i < spssFile.getVariableCount(); i++){
			SPSSVariable var = spssFile.getVariable(i);
			String spsstype;
			String valuelabels = null;
			String qtype;
			if(var instanceof SPSSNumericVariable){
				spsstype = "Numeric";
				qtype = VariableType.NUMERIC.toString();
				columns.put(var.getShortName(), "real");
			} else if (var instanceof SPSSStringVariable){
				spsstype = "String";
				qtype = VariableType.TEXT.toString();
				columns.put(var.getShortName(), "text");
			} else throw new Error("shuoldn't happen");
			if(var.hasValueLabels()){
				qtype = VariableType.MAPPED.toString();
				valuelabels = categoriesToJSON(var.categoryMap);
			}
			String measure = var.getMeasureLabel();
			try(PreparedStatement stmt = connection.prepareStatement(
					"INSERT INTO rawmeta values (?, ?, ?, ?, ?, ?, ?)")) {
				addColumnMeta(
						stmt,
						var.getShortName(),
						var.getName(),
						var.getLabel(),
						qtype,
						spsstype,
						valuelabels,
						measure
						);
			} catch (SQLException e) {
				System.out.println("Error adding row");
				MyTools.printSQLExeption(e);
				throw e; //TODO both catching and throwing SQLException!?
			}
		}
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
	protected static List<Pair<String, Double>> JSONtoCategories(String jsonstring) {
		List<Pair<String, Double>> list = new LinkedList<>();
		
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
			list.add(new Pair<>((String)entry.getValue(), Double.parseDouble((String) entry.getKey())));
	    }
	    
		return list;
	}
	
	public List<RawMetaQuestion> getQuestions() throws SQLException { 
		List<RawMetaQuestion> rawQuestionList = new LinkedList<>();
		try(PreparedStatement stmt = connection.prepareStatement("SELECT * FROM rawmeta ORDER BY column ASC");
			final ResultSet rs = stmt.executeQuery()) {
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
					rmq.valuelables = JSONtoCategories(rs.getString("valuelabels"));
				} else {
					rmq.valuelables = new LinkedList<>(); 
				}
				rawQuestionList.add(rmq);
			}
		}
		return rawQuestionList;
	}
	
	public boolean hasData() {
		if(!SQLTools.tableExists("rawmeta", connection)) {
			return false;
		}
		try {
			return getColumns().size() > 0;
		} catch (SQLException e) {
			return false;
		}
	}
}
