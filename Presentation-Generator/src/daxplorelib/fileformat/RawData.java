package daxplorelib.fileformat;

import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.opendatafoundation.data.spss.SPSSFile;

import daxplorelib.DaxploreException;
import daxplorelib.SQLTools;

import tools.MyTools;


public class RawData {
	static final String tablename = "rawdata";
	Connection sqliteDatabase;
	RawMeta metadata;
	
	public RawData(RawMeta metadata, Connection sqliteDatabase){
		this.sqliteDatabase = sqliteDatabase;
		this.metadata = metadata;
	}
	
	public void importSPSS(SPSSFile spssFile, Charset charset, RawMeta metadata) throws SQLException, DaxploreException {
		this.metadata = metadata;
		Map<String, VariableType> columns = metadata.getColumnMap();
		if(SQLTools.tableExists("rawdata", sqliteDatabase)) {
			Statement statement = sqliteDatabase.createStatement();
			statement.executeUpdate("DROP TABLE " + tablename);
		}
		createRawDataTable(columns, sqliteDatabase);
		
		PreparedStatement addRowStatement = addRowStatement(columns, sqliteDatabase);
		try{
			Iterator<Object[]> iter = spssFile.getDataIterator();
			while(iter.hasNext()){
				Object[] data = iter.next();
				addRow(columns, addRowStatement, data);
			}
		} catch (Exception e){
			e.printStackTrace();
			throw new DaxploreException("Something went wrong with spss-file");
		}
	}
	
	protected static void createRawDataTable(Map<String, VariableType> columns, Connection conn) throws SQLException{
		StringBuilder sb = new StringBuilder();
		Iterator<String> iter = columns.keySet().iterator();
		sb.append("create table " + tablename + " (");
		while(iter.hasNext()){
			String s = iter.next();
			if(s.contains("\"") || s.contains("'") || s.contains("$")){
				continue;
			}
			sb.append(s);
			sb.append(" ");
			sb.append(columns.get(s).sqltype());
			if(iter.hasNext()){
				sb.append(", ");
			}
		}
		sb.append(")");
		Statement statement = conn.createStatement();
		statement.execute(sb.toString());
		statement.close();
	}
	
	protected static PreparedStatement addRowStatement(Map<String, VariableType> columns, Connection conn) throws SQLException {
		LinkedList<String> qmarks = new LinkedList<String>();
		for(int i = 0; i < columns.size(); i++){
			qmarks.add("?");
		}
		PreparedStatement ps = conn.prepareStatement("insert into " + tablename + " values("+ MyTools.join(qmarks, ", ") + ")");
		return ps;
	}
	
	protected static void addRow(Map<String, VariableType> columns, PreparedStatement statement, Object[] data) throws SQLException {
		Collection<VariableType> types= columns.values();
		int colIndex = 0;
		for(VariableType type: types){
			Object datapoint = data[colIndex];
			if(VariableType.NUMERIC.compareTo(type) == 0 && datapoint instanceof Double){
				Double ddatapoint = (Double)datapoint;
				if(ddatapoint.isNaN() || ddatapoint.isInfinite()){
					statement.setNull(colIndex+1, java.sql.Types.REAL);
				} else {
					statement.setDouble(colIndex+1 , ddatapoint);
				}
			} else if(VariableType.TEXT.compareTo(type) == 0 && datapoint instanceof String){
				String sdatapoint = (String)datapoint;
				statement.setString(colIndex+1, sdatapoint);
			} else if(VariableType.MAPPED.compareTo(type) == 0 && datapoint instanceof Double){
				Double ddatapoint = (Double)datapoint;
				if(ddatapoint.isNaN() || ddatapoint.isInfinite()){
					statement.setNull(colIndex+1, java.sql.Types.REAL);
				} else {
					statement.setDouble(colIndex+1 , ddatapoint);
				}
			} else if (datapoint == null){
				if(VariableType.NUMERIC.compareTo(type) == 0){
					statement.setNull(colIndex+1, java.sql.Types.REAL);
				} else if (VariableType.TEXT.compareTo(type) == 0){
					statement.setString(colIndex+1, "");
				}
			} else {
				System.err.println("Type is: " + type.toString());
				throw new Error("Crash and burn");
			}
			colIndex++;
		}
		statement.execute();
	}
	
	int getNumberOfRows() throws SQLException{
		Statement stmt = sqliteDatabase.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT count(*) FROM " + tablename);
		rs.next();
		return rs.getInt(1);
	}
	
	public boolean hasData() {
		if(SQLTools.tableExists("rawdata", sqliteDatabase)) {
			try {
				if(getNumberOfRows() > 0) {
					return true;
				} else {
					return false;
				}
			} catch (SQLException e) {
				return false;
			}
		} else {
			return false;
		}
	}
}


