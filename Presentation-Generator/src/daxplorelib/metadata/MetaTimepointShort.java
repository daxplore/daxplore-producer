package daxplorelib.metadata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import daxplorelib.DaxploreException;
import daxplorelib.DaxploreTable;
import daxplorelib.SQLTools;
import daxplorelib.metadata.textreference.TextReference;
import daxplorelib.metadata.textreference.TextReferenceManager;

public class MetaTimepointShort implements Comparable<MetaTimepointShort> {
	/**
	 * name: the name of the timepoint, for example 1992 and 2010
	 * timeindex: the order of the timepoints
	 * value: the timepoint's value as it is stored in the column (see above) //TODO support TEXT/REAL/INTEGER for value
	 */
	protected static final DaxploreTable pointTable = new DaxploreTable(
			"CREATE TABLE timepoints (id INTEGER PRIMARY KEY, textref TEXT, timeindex INTEGER UNIQUE, value REAL)", "timepoints"); 
	
	//TODO handle timeindexes (unique, swappable, etc.)
	public static class MetaTimepointShortManager {
		private Map<Integer, MetaTimepointShort> pointMap = new HashMap<Integer, MetaTimepointShort>();
		private List<MetaTimepointShort> toBeAdded = new LinkedList<MetaTimepointShort>();
		private int addDelta = 0;
		private Map<Integer, MetaTimepointShort> toBeRemoved = new HashMap<Integer, MetaTimepointShort>();
		
		private Connection connection;
		private TextReferenceManager textReferenceManager;
		
		public MetaTimepointShortManager(Connection connection, TextReferenceManager textReferenceManager) {
			this.connection = connection;
			this.textReferenceManager = textReferenceManager;
		}
		
		public void init() throws SQLException {
			SQLTools.createIfNotExists(pointTable, connection);
		}
		
		public MetaTimepointShort get(int id) throws SQLException, DaxploreException {
			if(pointMap.containsKey(id)) {
				return pointMap.get(id);
			}
			if(toBeRemoved.containsKey(id)) {
				throw new DaxploreException("No timepoint with id '"+id+"'");
			}
			
			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM timepoints WHERE id = ?");
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()) {
				TextReference textref = textReferenceManager.get(rs.getString("textref"));
				int timeindex = rs.getInt("timeindex"); 
				double value = rs.getDouble("value");
				rs.close();			
				MetaTimepointShort timepoint = new MetaTimepointShort(id, textref, timeindex, value);
				pointMap.put(id, timepoint);
				return timepoint;
			} else {
				throw new DaxploreException("No timepoint with id '"+id+"'");
			}
		}
		
		public MetaTimepointShort create(TextReference textref, int timeindex, double value) throws SQLException {
			addDelta++;
			int id = SQLTools.maxId(pointTable.name, "id", connection) + addDelta;
			
			MetaTimepointShort timepoint = new MetaTimepointShort(id, textref, timeindex, value);
			toBeAdded.add(timepoint);
			
			pointMap.put(id, timepoint);
			return timepoint;
		}
		
		public void remove(int id) {
			MetaTimepointShort timepoint = pointMap.remove(id);
			toBeAdded.remove(timepoint);
			toBeRemoved.put(id, timepoint);
		}
		
		public void saveAll() throws SQLException {
			PreparedStatement updateStmt = connection.prepareStatement("UPDATE timepoints SET textref = ?, timeindex = ?, value = ? WHERE id = ?");
			PreparedStatement deleteStmt = connection.prepareStatement("DELETE FROM timepoints WHERE id = ?");
			PreparedStatement insertStmt = connection.prepareStatement("INSERT INTO timepoints (id, textref, timeindex, value) VALUES (?, ?, ?, ?)");
			
			for(MetaTimepointShort timepoint : pointMap.values()) {
				if(timepoint.modified) {
					updateStmt.setString(1, timepoint.textref.getRef());
					updateStmt.setInt(2, timepoint.timeindex);
					updateStmt.setDouble(3, timepoint.value);
					updateStmt.setInt(4, timepoint.id);
					updateStmt.addBatch();
					timepoint.modified = false;
				}
			}
			updateStmt.executeBatch();
			
			for(MetaTimepointShort timepoint : toBeRemoved.values()) {
				deleteStmt.setInt(1, timepoint.id);
				deleteStmt.addBatch();
			}
			deleteStmt.executeBatch();
			toBeRemoved.clear();
			
			for(MetaTimepointShort timepoint : toBeAdded) {
				insertStmt.setInt(1, timepoint.id);
				insertStmt.setString(2, timepoint.textref.getRef());
				insertStmt.setInt(3, timepoint.timeindex);
				insertStmt.setDouble(4, timepoint.value);
				insertStmt.addBatch();
			}
			insertStmt.executeBatch();
			toBeAdded.clear();
			addDelta = 0;
		}
		
		public List<MetaTimepointShort> getAll() throws SQLException {
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT id FROM timepoints ORDER BY timeindex ASC");
			while(rs.next()) {
				int id = rs.getInt("id");
				if(!pointMap.containsKey(id) && !toBeRemoved.containsKey(id)) {
					try {
						get(id); //can be improved
					} catch (DaxploreException e) {
						throw new AssertionError(e);
					}
				}
			}
			List<MetaTimepointShort> pointList = new LinkedList<MetaTimepointShort>(pointMap.values());
			Collections.sort(pointList);
			return pointList;
		}
		
		public int getHighestId() throws SQLException {
			return SQLTools.maxId(pointTable.name, "id", connection) + addDelta;
		}
	}
	
	private TextReference textref;
	private int id, timeindex;
	private double value;
	private boolean modified = false;
	
	private MetaTimepointShort(int id, TextReference textref, int timeindex, double value) {
		this.id = id;
		this.textref = textref;
		this.timeindex = timeindex; 
		this.value = value;
	}
	
	public int getId() {
		return id;
	}
	
	public TextReference getTextRef() {
		return textref;
	}

	public void setName(TextReference textref) {
		this.textref = textref;
		modified = true;
	}

	public int getTimeindex() {
		return timeindex;
	}

	public void setTimeindex(int timeindex) {
		this.timeindex = timeindex;
		modified = true;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
		modified = true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(MetaTimepointShort o) {
		return timeindex > o.timeindex ? 1: -1;
	}

}
