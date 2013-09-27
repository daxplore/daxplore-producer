package org.daxplore.producer.daxplorelib.metadata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.daxplore.producer.daxplorelib.DaxploreTable;

public class MetaCalculation {
	//TODO make table private and let MetaCaluculation to create itself?
	static final DaxploreTable table = new DaxploreTable("CREATE TABLE metacalc (id INTEGER NOT NULL, column TEXT)", "metacalc");
	private int id;
	private Connection connection;
	
	MetaCalculation(int id, Connection connection) {
		this.id = id;
		this.connection = connection;
	}
	
	MetaCalculation(String column, Connection connection) throws SQLException {
		try(PreparedStatement stmt = connection.prepareStatement("SELECT * FROM metacalc WHERE column = ?")) {
			stmt.setString(1, column);
			try (ResultSet rs = stmt.executeQuery()) {
				if(rs.next()) {
					this.id = rs.getInt("id");
				}
			}
		}
	}
	
	public String getColumn() throws SQLException{
		try (PreparedStatement stmt = connection.prepareStatement("SELECT column FROM metacalc WHERE id = ?")) {
			stmt.setInt(1, id);
			try(ResultSet rs = stmt.executeQuery()) {
				if(!rs.next()) {
					return null;
				}
				String column = rs.getString("column");
				return column;
			}
		}
	}
	
	public int getID(){
		return id;
	}
}
