package org.daxplore.producer.daxplorelib;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

public class Settings {
	
	public enum SettingsType {
		INT, DOUBLE, STRING, DATE
	}
	
	private HashMap<String, Object> settingsMap = new HashMap<>();
	private LinkedList<String> toBeRemoved = new LinkedList<>();
	private LinkedList<String> toBeAdded = new LinkedList<>();
	private LinkedList<String> toBeUpdated = new LinkedList<>();

	private static final DaxploreTable table = new DaxploreTable("CREATE TABLE settings (key TEXT, datatype TEXT, val TEXT)", "settings");
	
	private Connection connection;
	
	SimpleDateFormat sdf = new SimpleDateFormat();

	public Settings(Connection connection) throws SQLException {
		this.connection = connection;
		SQLTools.createIfNotExists(table, connection);
	}
	
	public void saveAll() throws SQLException {
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
				SettingsType type = getTypeOf(settingsMap.get(key));
				addStmt.setString(2, type.name());
				switch(type) {
				case DATE:
					addStmt.setString(3, sdf.format(settingsMap.get(key)));
					break;
				default:
					addStmt.setString(3, settingsMap.get(key).toString()); //works for current types
				}
				addStmt.addBatch();
			}
			addStmt.executeBatch();
			toBeAdded.clear();
			
			for(String key : toBeUpdated) {
				SettingsType type = getTypeOf(settingsMap.get(key));
				addStmt.setString(1, type.name());
				switch(type) {
				case DATE:
					addStmt.setString(2, sdf.format(settingsMap.get(key)));
					break;
				default:
					addStmt.setString(2, settingsMap.get(key).toString()); //works for current types
				}
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
		} else if(object instanceof Date) {
			return SettingsType.DATE;
		}
		return null;
	}
	
	private Object getAndLoad(String key) {
		if(settingsMap.containsKey(key)) {
			return settingsMap.get(key);
		} else {
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
				case DATE:
					Date date = sdf.parse(rs.getString("val"));
					settingsMap.put(key, date);
					return date;
				}
			} catch (SQLException|ParseException e) {
				// couldn't find a value or valid value, treat it as if it's an empty hashmap and return null
			}
			return null;
		}
	}
	
	public SettingsType getType(String key) throws DaxploreException {
		if(!settingsMap.containsKey(key)) {
			Object obj = getAndLoad(key);
			if(obj == null) {
				throw new DaxploreException("Setting with key " + key + " does not exist");
			}
		}
		return getTypeOf(settingsMap.get(key));
	}
	
	public Double getDouble(String key) {
		return (Double)getAndLoad(key);
	}
	
	public String getString(String key) {
		return (String)getAndLoad(key);
	}
	
	public Integer getInteger(String key) {
		return (Integer)getAndLoad(key);
	}
	
	public Date getDate(String key) {
		return (Date)getAndLoad(key);
	}
	
	public void putSetting(String key, Object value) {
		if(settingsMap.containsKey(key)) {
			toBeUpdated.add(key);
		} else {
			toBeAdded.add(key);
		}
		toBeRemoved.remove(key);
		settingsMap.put(key,  value);
	}
	
	public void removeSetting(String key) {
		if(settingsMap.containsKey(key)) {
			toBeRemoved.add(key);
			settingsMap.remove(key);
			toBeAdded.remove(key);
			toBeUpdated.remove(key);
		}
	}

}
