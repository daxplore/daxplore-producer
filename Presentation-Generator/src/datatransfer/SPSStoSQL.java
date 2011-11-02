package datatransfer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
import java.util.StringTokenizer;

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

	public static void loadRawData(File spssFile, Connection sqliteDatabase) throws Exception {
		File temp;
		SPSSFile sf;
		FileFormatInfo ffi = new FileFormatInfo();
		ffi.namesOnFirstLine = false;
		ffi.asciiFormat = ASCIIFormat.CSV;
		ffi.compatibility = Compatibility.GENERIC;
		boolean autocommit = sqliteDatabase.getAutoCommit();
		sqliteDatabase.setAutoCommit(false);
		try {
			sf = new SPSSFile(spssFile,Charset.forName("ISO-8859-1"));
			sf.logFlag = false;
			sf.loadMetadata();
			temp = File.createTempFile("spsscsv", ".csv.tmp");
			temp.deleteOnExit();
			sf.exportData(temp, ffi);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		} catch (SPSSFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		Map<String, String> columns = new LinkedHashMap<String,String>();
		createRawMetaTable(sqliteDatabase);
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
			} else throw new Exception("Variable type unknown");
			if(var.hasValueLabels()){
				qtype = VariableType.MAPPED.toString();
				valuelabels = categoriesToJSON(var.categoryMap);
			}
			String measure = var.getMeasureLabel();
			addColumnMeta(
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
		createRawDataTable(columns, sqliteDatabase);
		PreparedStatement addRowStatement = addRowStatement(columns, sqliteDatabase);
		try {
			BufferedReader br = new BufferedReader(new FileReader(temp));
			String line;
			while((line = br.readLine()) != null){
				String[] data = new String[sf.getVariableCount()];
				StringTokenizer st = new StringTokenizer(line, ",");
				int c = 0;
				while(st.hasMoreTokens()){
					data[c] = st.nextToken();
					c++;
				}
				addRow(columns, addRowStatement, data);
			}
			long time = System.currentTimeMillis();
			sqliteDatabase.commit();
			sqliteDatabase.setAutoCommit(autocommit);
			time = System.currentTimeMillis() - time;
			
			
		} catch (FileNotFoundException e) {
			System.out.println("FileNotFoundException");
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("IOException");
			// TODO Auto-generated catch block
			System.out.print(e.toString());
			e.printStackTrace();
		}
	}
	
	protected static void createRawMetaTable(Connection conn) throws SQLException {
		String query = "create table rawmeta (column text, longname text, qtext text, qtype text, spsstype text, valuelabels text, measure text)";
		Statement statement = conn.createStatement();
		statement.executeUpdate(query);
	}
	
	protected static void addColumnMeta(String column, String longname, String qtext, String qtype, String spsstype, String valuelabels, Connection conn, String measure) throws SQLException {
		PreparedStatement ps = conn.prepareStatement("insert into rawmeta values (?, ?, ?, ?, ?, ?, ?)");
		
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
	
	protected static void createRawDataTable(Map<String,String> columns, Connection conn) throws SQLException{
		StringBuilder sb = new StringBuilder();
		Iterator<String> iter = columns.keySet().iterator();
		sb.append("create table rawdata (");
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
	
	protected static PreparedStatement addRowStatement(Map<String,String> columns, Connection conn) throws SQLException {
		LinkedList<String> qmarks = new LinkedList<String>();
		for(int i = 0; i < columns.size(); i++){
			qmarks.add("?");
		}
		PreparedStatement ps = conn.prepareStatement("insert into rawdata values("+ MyTools.join(qmarks, ", ") + ")");
		return ps;
	}
	
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
