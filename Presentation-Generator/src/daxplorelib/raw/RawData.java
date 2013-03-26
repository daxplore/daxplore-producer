package daxplorelib.raw;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.opendatafoundation.data.spss.SPSSFile;
import org.opendatafoundation.data.spss.SPSSNumericVariable;
import org.opendatafoundation.data.spss.SPSSStringVariable;
import org.opendatafoundation.data.spss.SPSSVariable;

import tools.MyTools;
import tools.Pair;
import daxplorelib.DaxploreException;
import daxplorelib.DaxploreTable;
import daxplorelib.SQLTools;


public class RawData {
	protected final DaxploreTable table = new DaxploreTable(null, "rawdata");
	static final String tablename = "rawdata";
	Connection connection;
	
	public RawData(Connection connection) throws SQLException{
		this.connection = connection;
		if(SQLTools.tableExists(table.name, connection)){
			PreparedStatement stmt = connection.prepareStatement("SELECT sql FROM sqlite_master WHERE name=?");
			stmt.setString(1, table.name);
			ResultSet rs = stmt.executeQuery();
			rs.next();
			table.sql = rs.getString("sql");
			stmt.close();
		}
	}
	
	public void importSPSS(SPSSFile spssFile) throws SQLException, DaxploreException {
		
		if(SQLTools.tableExists("rawdata", connection)) {
			Statement stmt = connection.createStatement();
			stmt.executeUpdate("DROP TABLE " + table.name);
			stmt.close();
		}
		
		Map<String, VariableType> columns = new LinkedHashMap<String, VariableType>();//metadata.getColumnMap();
		
		for(int i = 0; i < spssFile.getVariableCount(); i++){
			SPSSVariable var = spssFile.getVariable(i);
			VariableType qtype;
			if(var instanceof SPSSNumericVariable){
				qtype = VariableType.NUMERIC;
				columns.put(var.getShortName(), qtype);
			} else if (var instanceof SPSSStringVariable){
				qtype = VariableType.TEXT;
				columns.put(var.getShortName(), qtype);
			} else throw new Error("shuoldn't happen");
		}
		
		Statement stmt = connection.createStatement();
		String createString = createRawDataTableString(columns, connection);
		
		table.sql = createString;
		stmt.execute(createString);
		stmt.close();
		
		PreparedStatement addRowStatement = addRowStatement(columns, connection);
		try{
			Iterator<Object[]> iter = spssFile.getDataIterator();
			while(iter.hasNext()){
				Object[] data = iter.next();
				addRow(columns, addRowStatement, data);
			}
			addRowStatement.close();
		} catch (Exception e){
			e.printStackTrace();
			throw new DaxploreException("Something went wrong with spss-file");
		}
	}
	
	protected static String createRawDataTableString(Map<String, VariableType> columns, Connection conn) throws SQLException{
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
		return sb.toString();
	}
	
	protected static PreparedStatement addRowStatement(Map<String, VariableType> columns, Connection connection) throws SQLException {
		LinkedList<String> qmarks = new LinkedList<String>();
		for(int i = 0; i < columns.size(); i++){
			qmarks.add("?");
		}
		PreparedStatement ps = connection.prepareStatement("insert into " + tablename + " values("+ MyTools.join(qmarks, ", ") + ")");
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
		//don't close statement
	}
	
	int getNumberOfRows() throws SQLException {
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT count(*) FROM " + tablename);
		rs.next();
		int ret = rs.getInt(1);
		stmt.close();
		return ret;
	}
	
	public boolean hasData() {
		if(SQLTools.tableExists("rawdata", connection)) {
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
	
	public boolean hasColumn(String column) throws SQLException {
		ResultSet rs = connection.createStatement().executeQuery("PRAGMA table_info(rawdata)");
		while(rs.next()) {
			if(rs.getString("name").equalsIgnoreCase(column)) {
				return true;
			}
		}
		return false;
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
	public LinkedList<Pair<Double, Integer>> getColumnValueCount(String column) throws SQLException {
		//TODO call hasColumn automatically?
		//Prepared statement doesn't work, but hasColumn is always called first so this should be relatively injection-safe
		ResultSet rs = connection.createStatement().executeQuery(
				"select "+ column + " as val, count(*) as cnt from rawdata group by val order by val");
		
		LinkedList<Pair<Double, Integer>> map = new LinkedList<Pair<Double, Integer>>();
		while(rs.next()) {
			Double val = rs.getDouble(1);
			Integer count = rs.getInt("cnt");
			if(val!=null) {
				map.add(new Pair<Double, Integer>(val, count));
			} else {
				map.add(new Pair<Double, Integer>(null, count));
			}
		}
		return map;
	}
}


