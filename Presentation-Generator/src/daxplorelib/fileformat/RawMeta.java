package daxplorelib.fileformat;

import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONValue;
import org.opendatafoundation.data.spss.SPSSFile;
import org.opendatafoundation.data.spss.SPSSNumericVariable;
import org.opendatafoundation.data.spss.SPSSStringVariable;
import org.opendatafoundation.data.spss.SPSSVariable;
import org.opendatafoundation.data.spss.SPSSVariableCategory;

import daxplorelib.SQLTools;


public class RawMeta {
	static final String tablename = "rawmeta";
	Connection sqliteDatabase;
	
	public RawMeta(Connection sqliteDatabase) throws SQLException{
		this.sqliteDatabase = sqliteDatabase;
		if(!SQLTools.tableExists(tablename, sqliteDatabase)){
			createRawMetaTable(sqliteDatabase);
		}
	}
	
	public List<String> getColumns() throws SQLException{
		Statement stmt = sqliteDatabase.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT column FROM " + tablename);
		List<String> list = new LinkedList<String>();
		while(rs.next()){
			list.add(rs.getString("column"));
		}
		return list;
	}
	
	public Map<String, VariableType> getColumnMap() throws SQLException{
		Statement stmt = sqliteDatabase.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT column, qtype FROM " + tablename);
		Map<String, VariableType> columns = new LinkedHashMap<String,VariableType>();
		while(rs.next()){
			String col = rs.getString("column");
			VariableType type = VariableType.valueOf(rs.getString("qtype"));
			columns.put(col, type);
		}
		return columns;
	}
	
	public void importSPSS(SPSSFile spssFile, Charset charset) throws SQLException {
		Map<String, String> columns = new LinkedHashMap<String,String>();
		clearRawMetaTable(sqliteDatabase);
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
			addColumnMeta(
					tablename,
					var.getShortName(),
					var.getName(),
					var.getLabel(),
					qtype,
					spsstype,
					valuelabels,
					sqliteDatabase,
					measure
					);
		}
	}
	
	protected static void createRawMetaTable(Connection conn) throws SQLException {
		String query = "create table " + tablename + " (column text, longname text, qtext text, qtype text, spsstype text, valuelabels text, measure text)";
		Statement statement = conn.createStatement();
		statement.executeUpdate(query);
	}
	
	protected static void clearRawMetaTable(Connection conn) throws SQLException {
		Statement statement = conn.createStatement();
		statement.executeUpdate("DELETE * FROM " + tablename);
	}
	
	protected static void addColumnMeta(String metaTable, String column, String longname, String qtext, String qtype, String spsstype, String valuelabels, Connection conn, String measure) throws SQLException {
		PreparedStatement ps = conn.prepareStatement("insert into " + metaTable + " values (?, ?, ?, ?, ?, ?, ?)");
		
		if(column != null)ps.setString(1, column);
		else throw new NullPointerException();
		
		if(longname != null) ps.setString(2, longname);
		else ps.setNull(2, java.sql.Types.VARCHAR);
		
		if(qtext != null) ps.setString(3, qtext);
		else ps.setNull(3, java.sql.Types.VARCHAR);
		
		if(qtype != null) ps.setString(4, qtype);
		else ps.setNull(4, java.sql.Types.VARCHAR);
		
		if(spsstype != null) ps.setString(5, spsstype);
		else ps.setNull(5, java.sql.Types.VARCHAR);
		
		if(valuelabels != null) ps.setString(6, valuelabels);
		else ps.setNull(6, java.sql.Types.VARCHAR);

		if(measure != null) ps.setString(7, measure);
		else ps.setNull(7, java.sql.Types.VARCHAR);
		
		ps.executeUpdate();
	}
	
	protected static String categoriesToJSON(Map<String, SPSSVariableCategory> categories){
		Set<String> keyset = categories.keySet();
		Map<Object,String> catObj = new LinkedHashMap<Object, String>();
		for(String key : keyset){
			//if(categories.get(key).value != Double.NaN){
			//	catObj.put(new Double(categories.get(key).value), categories.get(key).label);
			//} else {
				catObj.put(categories.get(key).strValue, categories.get(key).label);
			//}
		}
		return JSONValue.toJSONString(catObj);
	}
}
