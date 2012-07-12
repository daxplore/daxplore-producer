package daxplorelib.metadata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.simple.JSONAware;

public class MetaCalculation implements JSONAware{
	protected static final String sqlDefinition = "CREATE TABLE metacalc (id INTEGER, column TEXT)";
	int id;
	Connection connection;
	
	public MetaCalculation(int id, Connection connection) {
		this.id = id;
		this.connection = connection;
	}
	
	public MetaCalculation(String column, Connection connection) throws SQLException{
		PreparedStatement stmt = connection.prepareStatement("SELECT * FROM metacalc WHERE column = ?");
		stmt.setString(1, column);
		ResultSet rs = stmt.executeQuery();
		if(rs.next()) {
			this.id = rs.getInt("id");
		}
		stmt.close();
	}
	
	public String getColumn() throws SQLException{
		PreparedStatement stmt = connection.prepareStatement("SELECT column FROM metacalc WHERE id = ?");
		stmt.setInt(1, id);
		ResultSet rs = stmt.executeQuery();
		if(rs.next()) {
			String column = rs.getString("column");
			stmt.close();
			return column;
		} else {
			stmt.close();
			return null;
		}
	}
	
	public int getID(){
		return id;
	}

	@Override
	public String toJSONString() {
		try {
			return '"' + getColumn() + '"';
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
