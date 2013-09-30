package org.daxplore.producer.daxplorelib.metadata;

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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.DaxploreTable;
import org.daxplore.producer.daxplorelib.SQLTools;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReferenceManager;

public class MetaTimepointShort implements Comparable<MetaTimepointShort> {
	/**
	 * name: the name of the timepoint, for example 1992 and 2010
	 * timeindex: the order of the timepoints
	 * value: the timepoint's value as it is stored in the column (see above) //TODO support TEXT/REAL/INTEGER for value
	 */
	private static final DaxploreTable pointTable = new DaxploreTable(
			"CREATE TABLE timepoints (id INTEGER PRIMARY KEY, textref TEXT NOT NULL, timeindex INTEGER UNIQUE NOT NULL, value REAL NOT NULL)", "timepoints"); 
	
	//TODO handle timeindexes (unique, swappable, etc.)
	public static class MetaTimepointShortManager {
		private Map<Integer, MetaTimepointShort> pointMap = new HashMap<>();
		private List<MetaTimepointShort> toBeAdded = new LinkedList<>();
		private int addDelta = 0;
		private Map<Integer, MetaTimepointShort> toBeRemoved = new HashMap<>();
		
		private Connection connection;
		private TextReferenceManager textReferenceManager;
		
		public MetaTimepointShortManager(Connection connection, TextReferenceManager textReferenceManager) throws SQLException {
			this.connection = connection;
			this.textReferenceManager = textReferenceManager;

			SQLTools.createIfNotExists(pointTable, connection);
		}
		
		public MetaTimepointShort get(int id) throws SQLException, DaxploreException {
			if(pointMap.containsKey(id)) {
				return pointMap.get(id);
			}
			if(toBeRemoved.containsKey(id)) {
				throw new DaxploreException("No timepoint with id '"+id+"'");
			}
			
			try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM timepoints WHERE id = ?")) {
				stmt.setInt(1, id);
				try (ResultSet rs = stmt.executeQuery()) {
					if(!rs.next()) {
						throw new DaxploreException("No timepoint with id '"+id+"'");
					}
					TextReference textref = textReferenceManager.get(rs.getString("textref"));
					int timeindex = rs.getInt("timeindex"); 
					double value = rs.getDouble("value");
					rs.close();			
					MetaTimepointShort timepoint = new MetaTimepointShort(id, textref, timeindex, value);
					pointMap.put(id, timepoint);
					return timepoint;
				}
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
			int nNew = 0, nModified = 0, nRemoved = 0;
			try (
				PreparedStatement deleteStmt = connection.prepareStatement("DELETE FROM timepoints WHERE id = ?");
				PreparedStatement insertStmt = connection.prepareStatement("INSERT INTO timepoints (id, textref, timeindex, value) VALUES (?, ?, ?, ?)");
			) {
			
				for(MetaTimepointShort timepoint : pointMap.values()) {
					if(timepoint.modified) {
						nModified++;
						deleteStmt.setInt(1, timepoint.id);
						deleteStmt.addBatch();
						insertStmt.setInt(1, timepoint.id);
						insertStmt.setString(2, timepoint.textref.getRef());
						insertStmt.setInt(3, timepoint.timeindex);
						insertStmt.setDouble(4, timepoint.value);
						insertStmt.addBatch();
						timepoint.modified = false;
					}
				}
				deleteStmt.executeBatch();
				insertStmt.executeBatch();
	
				for(MetaTimepointShort timepoint : toBeRemoved.values()) {
					nRemoved++;
					deleteStmt.setInt(1, timepoint.id);
					deleteStmt.addBatch();
				}
				deleteStmt.executeBatch();
				toBeRemoved.clear();
				
				for(MetaTimepointShort timepoint : toBeAdded) {
					nNew++;
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
			
			if(nModified != 0 || nNew != 0 || nRemoved != 0) {
				String logString = String.format("MetaTimePoint: Saved %d (%d new), %d removed", nModified+nNew, nNew, nRemoved);
				Logger.getGlobal().log(Level.INFO, logString);
			}
		}
		
		public List<MetaTimepointShort> getAll() throws DaxploreException {
			// make sure all timepoints are cached before returning the content of the map
			try (Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT id FROM timepoints ORDER BY timeindex ASC")) {
				while(rs.next()) {
					int id = rs.getInt("id");
					if(!pointMap.containsKey(id) && !toBeRemoved.containsKey(id)) {
						get(id);
					}
				}
			} catch (SQLException e) {
				throw new DaxploreException("Failed to load short timepoints", e);
			}
			List<MetaTimepointShort> pointList = new LinkedList<>(pointMap.values());
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
		if (timeindex == o.timeindex) {
			return 0;
		}
		return timeindex > o.timeindex ? 1 : -1;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + timeindex;
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj || obj == null || getClass() != obj.getClass()) {
			return false;
		}
		return timeindex == ((MetaTimepointShort)obj).timeindex;
	}
}
