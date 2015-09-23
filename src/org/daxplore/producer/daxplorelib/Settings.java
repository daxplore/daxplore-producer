package org.daxplore.producer.daxplorelib;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;

public class Settings {
	
	public enum SettingsType {
		INT, DOUBLE, STRING
	}
	
	private HashMap<String, Object> settingsMap = new HashMap<>();
	private LinkedList<String> toBeRemoved = new LinkedList<>();
	private LinkedList<String> toBeAdded = new LinkedList<>();
	private LinkedList<String> toBeUpdated = new LinkedList<>();

	private static final DaxploreTable table = new DaxploreTable("CREATE TABLE settings (key TEXT, datatype TEXT, val TEXT)", "settings");
	
	private Connection connection;

	public Settings(Connection connection) throws SQLException {
		this.connection = connection;
		SQLTools.createIfNotExists(table, connection);
	}
	
	public void save() throws SQLException {
		try (	
			PreparedStatement addStmt = connection.prepareStatement("INSERT INTO settings (key, datatype, val) VALUES (?, ?, ?)");
			PreparedStatement removeStmt = connection.prepareStatement("DELETE FROM settings WHERE key = ?");
			PreparedStatement updateStmt = connection.prepareStatement("UPDATE settings SET datatype = ?, val = ? WHERE key = ?");
		){
			for(String key : toBeRemoved) {
				removeStmt.setString(1, key);
				removeStmt.addBatch();
			}
			removeStmt.executeBatch();
			toBeRemoved.clear();
			
			for(String key : toBeAdded) {
				addStmt.setString(1, key);
				addStmt.setString(2, getTypeOf(settingsMap.get(key)).name());
				addStmt.setString(3, settingsMap.get(key).toString()); //works for current types
				addStmt.addBatch();
			}
			addStmt.executeBatch();
			toBeAdded.clear();
			
			for(String key : toBeUpdated) {
				updateStmt.setString(1, getTypeOf(settingsMap.get(key)).name());
				updateStmt.setString(2, settingsMap.get(key).toString()); //works for current types
				updateStmt.setString(3, key);
				updateStmt.addBatch();
			}
			updateStmt.executeBatch();
			toBeUpdated.clear();
			
		}
	}
	
	public void discardChanges() {
		settingsMap.clear();
		toBeAdded.clear();
		toBeRemoved.clear();
		toBeUpdated.clear();
	}
	
	private static SettingsType getTypeOf(Object object) {
		if(object instanceof Double) {
			return SettingsType.DOUBLE;
		} else if(object instanceof String) {
			return SettingsType.STRING;
		} else if(object instanceof Integer) {
			return SettingsType.INT;
		}
		return null;
	}
	
	private Object getAndLoad(String key) throws SQLException {
		try (
			PreparedStatement getStmt = connection.prepareStatement("SELECT datatype, val FROM settings WHERE key = ?")
		){
			getStmt.setString(1, key);
			ResultSet rs = getStmt.executeQuery();
			SettingsType type = SettingsType.valueOf(rs.getString("datatype"));
			switch (type) {
			case DOUBLE:
				Double d = Double.parseDouble(rs.getString("val"));
				settingsMap.put(key, d);
				return d;
			case INT:
				Integer i = Integer.parseInt(rs.getString("val"));
				settingsMap.put(key, i);
				return i;
			case STRING:
				String s = rs.getString("val");
				settingsMap.put(key, s);
				return s;
			}
		}
		return null;
	}
	
	public SettingsType getType(String key) throws SQLException, DaxploreException {
		if(!settingsMap.containsKey(key)) {
			Object obj = getAndLoad(key);
			if(obj == null) {
				throw new DaxploreException("Setting with key " + key + " does not exist");
			}
		}
		return getTypeOf(settingsMap.get(key));
	}
	
	public Double getDouble(String key) {
		return 0.0;
	}
	
	public void putDouble(String key, Double value) {
		
	}

}
