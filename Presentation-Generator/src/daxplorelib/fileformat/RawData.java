package daxplorelib.fileformat;

import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.opendatafoundation.data.spss.SPSSFile;

import daxplorelib.DaxploreException;

import tools.MyTools;


public class RawData {
	String tablename;
	Connection sqliteDatabase;
	RawMeta metadata;
	
	public RawData(String tablename, RawMeta metadata, Connection sqliteDatabase){
		this.tablename = tablename;
		this.sqliteDatabase = sqliteDatabase;
		this.metadata = metadata;
	}
	
	public void importSPSS(SPSSFile spssFile, Charset charset) throws SQLException, DaxploreException {
		Map<String, VariableType> columns = metadata.getColumnMap();
		
		createRawDataTable(tablename, columns, sqliteDatabase);
		PreparedStatement addRowStatement = addRowStatement(tablename, columns, sqliteDatabase);
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

	protected static void createRawDataTable(String dataTable, Map<String, VariableType> columns, Connection conn) throws SQLException{
		StringBuilder sb = new StringBuilder();
		Iterator<String> iter = columns.keySet().iterator();
		sb.append("create table " + dataTable + " (");
		while(iter.hasNext()){
			String s = iter.next();
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
	
	protected static PreparedStatement addRowStatement(String dataTable, Map<String, VariableType> columns, Connection conn) throws SQLException {
		LinkedList<String> qmarks = new LinkedList<String>();
		for(int i = 0; i < columns.size(); i++){
			qmarks.add("?");
		}
		PreparedStatement ps = conn.prepareStatement("insert into " + dataTable + " values("+ MyTools.join(qmarks, ", ") + ")");
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
}


