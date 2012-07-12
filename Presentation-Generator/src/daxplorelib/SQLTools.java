package daxplorelib;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class SQLTools {
	
	public static boolean tableExists(String tablename, Connection connection){
		try{
			PreparedStatement stmt = connection.prepareStatement("SELECT name FROM sqlite_master WHERE type='table' AND name=?");
			stmt.setString(1, tablename);
			ResultSet rs = stmt.executeQuery();
			boolean first = rs.next();
			if(first) {
				String name = rs.getString("name");
				stmt.close();
				return tablename.equals(name);
			}
			return false;
		} catch (SQLException e){
			e.printStackTrace();
			System.err.println(e.getMessage());
			return false;
		}
	}
	
	public static void createIfNotExists(DaxploreTable table, Connection connection) throws SQLException {
		if(!tableExists(table.name, connection)) {
			Statement stmt = connection.createStatement();
			stmt.executeUpdate(table.sql);
			stmt.close();
		}
	}
	
	public static boolean compareTables(String table1, String table2, String idcolumn, Map<String,String> columns, Connection connection) throws SQLException{
		Statement stmt = connection.createStatement();
		stmt.execute("select * from " + table1);
		ResultSet rs1 = stmt.getResultSet();
		String idcoltype = columns.get(idcolumn);
		PreparedStatement ps = connection.prepareStatement("select * from " + table2 + " where " + idcolumn + " = ?");
		int row = 0;
		while(rs1.next()){
			String id;
			if(idcoltype.equalsIgnoreCase("text")){
				ps.setString(1, rs1.getString(idcolumn));
				id = rs1.getString(idcolumn);
			} else if(idcoltype.equalsIgnoreCase("real")){
				ps.setDouble(1, rs1.getDouble(idcolumn));
				id = new Double(rs1.getDouble(idcolumn)).toString();
			} else {
				return false;
			}
			ps.execute();
			ResultSet rs2 = ps.getResultSet();
			Iterator<Entry<String,String>> iter = columns.entrySet().iterator();
			int duplicates = 0;
			while(rs2.next()){
				while(iter.hasNext()){
					Entry<String,String> e = iter.next();
					if(e.getValue().equalsIgnoreCase("real")){
						double d1 = rs1.getDouble(e.getKey());
						double d2 = rs2.getDouble(e.getKey());
						if(rs1.wasNull() != rs2.wasNull()){
							System.out.println("NULL row = " + row + " " + idcolumn + " = " + id + " column = " + e.getKey());
							System.out.println("d: " + d1 + " != " + d2);
							return false;
						} else if(d1 != d2){
							System.out.println("row = " + row + " " + idcolumn + " = " + id + " column = " + e.getKey());
							System.out.println("d: " + d1 + " != " + d2);
							return false;
						}
					} else if(e.getValue().equalsIgnoreCase("text")){
						String s1 = rs1.getString(e.getKey());
						String s2 = rs2.getString(e.getKey());
						if(!s1.equals(s2)){
							System.out.println("s: " + s1 + " != " + s2);
							return false;
						}
					}
				}
				duplicates++;
				if(duplicates > 1){
					System.out.println("Duplicate row = " + row + " " + idcolumn + " = " + id);
					return false;
				}
			}
			row++;
		}
		ps.close();
		stmt.close();
		return true;
	}
}
