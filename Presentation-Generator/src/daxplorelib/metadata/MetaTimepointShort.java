package daxplorelib.metadata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import daxplorelib.DaxploreTable;
import daxplorelib.SQLTools;

public class MetaTimepointShort {
	protected static final DaxploreTable seriesTable = new DaxploreTable(
			"CREATE TABLE timeseries (column TEXT, ", "timeseries");
	protected static final DaxploreTable pointTable = new DaxploreTable(
			"CREATE TABLE timepoints (name TEXT UNIQUE, timeindex INTEGER UNIQUE, value INTEGER UNIQUE", "timepoints");
	
	public static class MetaTimeseriesShortManager {
		private Map<String, MetaTimepointShort> pointMap = new HashMap<String, MetaTimepointShort>();
		private String column;

		private Connection connection;
		
		public MetaTimeseriesShortManager(Connection connection) {
			this.connection = connection;
		}
		
		public void init() throws SQLException {
			SQLTools.createIfNotExists(seriesTable, connection);
			SQLTools.createIfNotExists(pointTable, connection);
			
			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM timeseries");
			ResultSet rs = stmt.executeQuery();
			if(rs.first()) {
				column = rs.getString("column");
			}
			rs.close();
		}
		
		public MetaTimepointShort get(String name) throws SQLException {
			if(pointMap.containsKey(name)) {
				return pointMap.get(name);
			}
			
			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM timepoints WHERE name = ?");
			stmt.setString(1, name);
			ResultSet rs = stmt.executeQuery();
			rs.next();
			rs.close();
			int timeindex = rs.getInt("timeindex");
			int value = rs.getInt("value");
			
			MetaTimepointShort timepoint = new MetaTimepointShort(column, name, timeindex, value);
			pointMap.put(name, timepoint);
			return timepoint;
		}
		
		public MetaTimepointShort create(String name, int timeindex, int value) throws SQLException {
			PreparedStatement stmt = connection.prepareStatement("INSERT INTO timepoints (name, timeindex, value) VALUES (?, ?, ?)");
			stmt.setString(1, name);
			stmt.setInt(2, timeindex);
			stmt.setInt(3, value);
			stmt.executeUpdate();
			
			MetaTimepointShort timepoint = new MetaTimepointShort(column, name, timeindex, value);
			pointMap.put(name, timepoint);
			return timepoint;
		}
		
		public String getColumn() {
			return column;
		}

		public void setColumn(String column) {
			this.column = column;
		}
	}
	
	private String column, name;
	private int timeindex, value;
	private boolean modified = false;
	
	private MetaTimepointShort(String column, String name, int timeindex, int value) {
		this.column = column;
		this.name = name;
		this.timeindex = timeindex; 
		this.value = value;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		modified = true;
	}

	public int getTimeindex() {
		return timeindex;
	}

	public void setTimeindex(int timeindex) {
		this.timeindex = timeindex;
		modified = true;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
		modified = true;
	}

	public String getColumn() {
		return column;
	}


}
