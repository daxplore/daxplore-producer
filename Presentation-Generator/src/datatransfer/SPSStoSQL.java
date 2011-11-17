package datatransfer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONValue;
import org.opendatafoundation.data.FileFormatInfo;
import org.opendatafoundation.data.FileFormatInfo.ASCIIFormat;
import org.opendatafoundation.data.FileFormatInfo.Compatibility;
import org.opendatafoundation.data.spss.SPSSFile;
import org.opendatafoundation.data.spss.SPSSFileException;
import org.opendatafoundation.data.spss.SPSSNumericVariable;
import org.opendatafoundation.data.spss.SPSSStringVariable;
import org.opendatafoundation.data.spss.SPSSVariable;
import org.opendatafoundation.data.spss.SPSSVariableCategory;

import tools.MyTools;

public class SPSStoSQL {
	
	public static void loadRawData(File spssFile, Connection sqliteDatabase, String dataTable, String metaTable) throws ImportSPSSException, FileNotFoundException {
		try{
			SPSSFile sf = null;
			FileFormatInfo ffi = new FileFormatInfo();
			ffi.namesOnFirstLine = false;
			ffi.asciiFormat = ASCIIFormat.CSV;
			ffi.compatibility = Compatibility.GENERIC;
			boolean autocommit = sqliteDatabase.getAutoCommit();
			sqliteDatabase.setAutoCommit(false);
			
			sf = new SPSSFile(spssFile,Charset.forName("ISO-8859-1"));
			sf.logFlag = false;
			sf.loadMetadata();
			
			Map<String, String> columns = new LinkedHashMap<String,String>();
			createRawMetaTable(sqliteDatabase, metaTable);
			for(int i = 0; i < sf.getVariableCount(); i++){
				SPSSVariable var = sf.getVariable(i);
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
				} else throw new ImportSPSSException("Variable type unknown");
				if(var.hasValueLabels()){
					qtype = VariableType.MAPPED.toString();
					valuelabels = categoriesToJSON(var.categoryMap);
				}
				String measure = var.getMeasureLabel();
				addColumnMeta(
						metaTable,
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
			createRawDataTable(dataTable, columns, sqliteDatabase);
			PreparedStatement addRowStatement = addRowStatement(dataTable, columns, sqliteDatabase);
			
			Iterator<Object[]> iter = sf.getDataIterator();
			while(iter.hasNext()){
				Object[] data = iter.next();
				addRow(columns, addRowStatement, data);
			}
			sqliteDatabase.commit();
			
			sqliteDatabase.setAutoCommit(autocommit);
		} catch (SQLException e){
			throw new ImportSPSSException("Sql error", e);
		} catch (IOException e) {
			throw new ImportSPSSException("Error reading SPSSFile", e);
		} catch (SPSSFileException e) {
			throw new ImportSPSSException("Exception in SPSSFile", e);
		}
	}
	
	
	protected static void createRawMetaTable(Connection conn, String tablename) throws SQLException {
		String query = "create table " + tablename + " (column text, longname text, qtext text, qtype text, spsstype text, valuelabels text, measure text)";
		Statement statement = conn.createStatement();
		statement.executeUpdate(query);
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
	
	protected static void createRawDataTable(String dataTable, Map<String,String> columns, Connection conn) throws SQLException{
		StringBuilder sb = new StringBuilder();
		Iterator<String> iter = columns.keySet().iterator();
		sb.append("create table " + dataTable + " (");
		while(iter.hasNext()){
			String s = iter.next();
			sb.append(s);
			sb.append(" ");
			sb.append(columns.get(s));
			if(iter.hasNext()){
				sb.append(", ");
			}
		}
		sb.append(")");
		Statement statement = conn.createStatement();
		statement.execute(sb.toString());
		statement.close();
		/*LinkedList<String> qmarks = new LinkedList<String>();
		for(int i = 0; i < columns.size(); i++){
			qmarks.add("? ?");
		}
		String query = "create table rawdata ("+ MyTools.join(qmarks, ", ") + ")";
		System.out.println(query);
		PreparedStatement ps = conn.prepareStatement(query);
		
		Iterator<String> iter = columns.keySet().iterator();
		int i = 0;
		while(iter.hasNext()){
			String s = iter.next();
			ps.setString(i, s);
			i++;
			ps.setString(i, columns.get(s));
			i++;
		}
		ps.execute();*/
	}
	
	protected static PreparedStatement addRowStatement(String dataTable, Map<String,String> columns, Connection conn) throws SQLException {
		LinkedList<String> qmarks = new LinkedList<String>();
		for(int i = 0; i < columns.size(); i++){
			qmarks.add("?");
		}
		PreparedStatement ps = conn.prepareStatement("insert into " + dataTable + " values("+ MyTools.join(qmarks, ", ") + ")");
		return ps;
	}
	
	/* Old, but kept just in case
	protected static void addRow(Map<String,String> columns, PreparedStatement statement, String[] data) throws SQLException {
		Collection<String> types= columns.values();
		int colIndex = 0;
		for(String type: types){
			if(type.equalsIgnoreCase("real")){
				String datapoint = data[colIndex];
				double datadouble;
				try {
					datadouble = Double.parseDouble(datapoint);
					statement.setDouble(colIndex+1, datadouble);
				} catch (NumberFormatException e) {
					statement.setNull(colIndex+1, java.sql.Types.REAL);
				} catch (NullPointerException e) {
					statement.setNull(colIndex+1, java.sql.Types.REAL);
				}
			}else if(type.equalsIgnoreCase("text")){
				String datapoint = data[colIndex];
				if(datapoint == null){
					datapoint = "";
				}
				statement.setString(colIndex+1, datapoint);
			} else throw new Error("Crash and burn");
			colIndex++;
		}
		statement.execute();
	}*/
	
	protected static void addRow(Map<String,String> columns, PreparedStatement statement, Object[] data) throws SQLException {
		Collection<String> types= columns.values();
		int colIndex = 0;
		for(String type: types){
			Object datapoint = data[colIndex];
			if(type.equalsIgnoreCase("real") && datapoint instanceof Double){
				Double ddatapoint = (Double)datapoint;
				if(ddatapoint.isNaN() || ddatapoint.isInfinite()){
					statement.setNull(colIndex+1, java.sql.Types.REAL);
				} else {
					statement.setDouble(colIndex+1 , ddatapoint);
				}
			}else if(type.equalsIgnoreCase("text") && datapoint instanceof String){
				String sdatapoint = (String)datapoint;
				statement.setString(colIndex+1, sdatapoint);
			} else if (datapoint == null){
				if(type.equalsIgnoreCase("real")){
					statement.setNull(colIndex+1, java.sql.Types.REAL);
				} else if (type.equalsIgnoreCase("text")){
					statement.setString(colIndex+1, "");
				}
			} else throw new Error("Crash and burn");
			colIndex++;
		}
		statement.execute();
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
