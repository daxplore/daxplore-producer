package org.daxplore.producer.daxplorelib.metadata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.daxplore.producer.daxplorelib.DaxploreTable;

public class MetaCalculation {
	protected static final DaxploreTable table = new DaxploreTable("CREATE TABLE metacalc (id INTEGER NOT NULL, column TEXT)", "metacalc");
	int id;
	Connection connection;
	
	public MetaCalculation(int id, Connection connection) {
		this.id = id;
		this.connection = connection;
	}
	
	public MetaCalculation(String column, Connection connection) throws SQLException {
		PreparedStatement stmt = connection.prepareStatement("SELECT * FROM metacalc WHERE column = ?");
		stmt.setString(1, column);
		ResultSet rs = stmt.executeQuery();
		if(rs.next()) {
			this.id = rs.getInt("id");
		}
		rs.close();
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
}
