package org.daxplore.producer.daxplorelib;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Settings {
	
	public enum SettingsType {
		INT, DOUBLE, STRING, DATE, BOOL
	}
	
	private HashMap<String, Object> settingsMap = new HashMap<>();
	private Set<String> toBeRemoved = new HashSet<>();
	private Set<String> toBeAdded = new HashSet<>();
	private Set<String> toBeUpdated = new HashSet<>();

	private static final DaxploreTable table = new DaxploreTable("CREATE TABLE settings (key TEXT PRIMARY KEY, datatype TEXT, val TEXT)", "settings");
	
	private Connection connection;
	
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
					ZonedDateTime date = (ZonedDateTime)(settingsMap.get(key));
					addStmt.setString(3, date.format(DateTimeFormatter.ISO_INSTANT));
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
					ZonedDateTime date = (ZonedDateTime)(settingsMap.get(key));
					addStmt.setString(2, date.format(DateTimeFormatter.ISO_INSTANT));
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
		} else if(object instanceof ZonedDateTime) {
			return SettingsType.DATE;
		} else if(object instanceof Boolean) {
			return SettingsType.BOOL;
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
					ZonedDateTime date = ZonedDateTime.parse(rs.getString("val"));
					settingsMap.put(key, date);
					return date;
				case BOOL:
					boolean bool = Boolean.parseBoolean(rs.getString("val"));
					settingsMap.put(key, bool);
					return bool;
				}
			} catch (SQLException|DateTimeParseException e) {
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
	
	public ZonedDateTime getDate(String key) {
		return (ZonedDateTime)getAndLoad(key);
	}
	
	public boolean getBool(String key) {
		return (Boolean)getAndLoad(key);
	}
	
	public boolean has(String key) {
		return getAndLoad(key) != null;
	}
	
	public void putSetting(String key, Object value) {
		boolean isindb = (settingsMap.containsKey(key) && !toBeAdded.contains(key)) || toBeRemoved.contains(key); 
		if(isindb) {
			toBeUpdated.add(key);
		} else {
			toBeAdded.add(key);
		}
		toBeRemoved.remove(key);
		settingsMap.put(key,  value);
	}
	
	public void removeSetting(String key) {
		boolean isindb = (settingsMap.containsKey(key) && !toBeAdded.contains(key));
		if(isindb) {
			toBeRemoved.add(key);
			settingsMap.remove(key);
			toBeAdded.remove(key);
			toBeUpdated.remove(key);
		}
	}

}
