package org.daxplore.producer.daxplorelib.metadata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.DaxploreTable;
import org.daxplore.producer.daxplorelib.SQLTools;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReferenceManager;
import org.daxplore.producer.tools.NumberlineCoverage;
import org.daxplore.producer.tools.NumberlineCoverage.NumberlineCoverageException;

import com.beust.jcommander.internal.Lists;

public class MetaScale {
	
	private static final DaxploreTable maintable = new DaxploreTable(
			"CREATE TABLE metascale (id INTEGER PRIMARY KEY, ignore STRING NOT NULL)"
			, "metascale");
	private static final DaxploreTable optiontable = new DaxploreTable(
			"CREATE TABLE metascaleoption (scaleid INTEGER NOT NULL, textref STRING NOT NULL, ord INTEGER NOT NULL, value REAL NOT NULL, transform STRING NOT NULL, FOREIGN KEY(scaleid) REFERENCES metascale(id))" /*, UNIQUE(scaleid, textref) ON CONFLICT REPLACE)" */
			, "metascaleoption");
	
	public static class MetaScaleManager {
		private Map<Integer, MetaScale> scaleMap = new HashMap<>();
		private Connection connection;
		private TextReferenceManager textsManager;
		
		private List<MetaScale> toBeAdded = new LinkedList<>();
		private int addDelta = 0;
		private Map<Integer, MetaScale> toBeRemoved = new HashMap<>();
//		private List<MetaScale> toBeRemoved = new LinkedList<MetaScale>();
		
		public MetaScaleManager(Connection connection, TextReferenceManager textsManager) throws SQLException {
			this.connection = connection;
			this.textsManager = textsManager;

			if(!SQLTools.tableExists(maintable.name, connection)) {
				try (Statement stmt = connection.createStatement()) {
					stmt.executeUpdate(maintable.sql);
				}
			}
			if(!SQLTools.tableExists(optiontable.name, connection)) {
				try (Statement stmt = connection.createStatement()) {
					stmt.executeUpdate(optiontable.sql);
				}
			}
		}
		
		public MetaScale get(int id) throws SQLException, DaxploreException {
			if(scaleMap.containsKey(id)) {
				return scaleMap.get(id);
			} else if(toBeRemoved.containsKey(id)) {
				return null; // TODO: handle non-scales in a more structured way?
			} else {
				NumberlineCoverage ignore;
				try(PreparedStatement stmt = connection.prepareStatement("SELECT * FROM metascale WHERE id = ?")) {
					stmt.setInt(1, id);
					try(ResultSet rs = stmt.executeQuery()) {
						if(rs.next()) {
							try {
								ignore = new NumberlineCoverage(rs.getString("ignore"));
							} catch (NumberlineCoverageException e) {
								throw new DaxploreException("Corrupt numberline coverage in metascale", e);
							}
						} else {
							return null; // TODO: handle non-scales in a more structured way?
						}
					}
				}
						
				try(PreparedStatement stmt = connection.prepareStatement("SELECT * FROM metascaleoption WHERE scaleid = ? ORDER BY ord")) {
					stmt.setInt(1, id);
					try(ResultSet rs  = stmt.executeQuery()) {
						List<Option> options = new LinkedList<>();
						
						while(rs.next()) {
							NumberlineCoverage numberlineCoverage;
							try {
								numberlineCoverage = new NumberlineCoverage(rs.getString("transform"));
							} catch (NumberlineCoverageException e) {
								numberlineCoverage = new NumberlineCoverage(); //TODO: do something if corrupt data is stored in database?
							}
							
							options.add(
									new Option(
											textsManager.get(rs.getString("textref")), 
											rs.getDouble("value"), 
											numberlineCoverage,
											false));
						}
						MetaScale ms = new MetaScale(id, options, ignore, false);
						scaleMap.put(id, ms);
						return ms;
					}
				}
			}
		}
		
		public MetaScale create(List<Option> options, NumberlineCoverage ignore) throws SQLException {
			addDelta++;
			int id = SQLTools.maxId(maintable.name, "id", connection) + addDelta;
			MetaScale scale = new MetaScale(id, options, ignore, true);
			toBeAdded.add(scale);
			scaleMap.put(id, scale);
			return scale;
		}
		
		public void remove(int id) {
			MetaScale scale = scaleMap.remove(id);
			toBeAdded.remove(scale);
			toBeRemoved.put(id, scale);
		}
		
		public void saveAll() throws SQLException {
			try (
				PreparedStatement updateScaleStmt = connection.prepareStatement("UPDATE metascale SET ignore = ? WHERE id = ? ");
				PreparedStatement insertScaleStmt = connection.prepareStatement("INSERT INTO metascale (id, ignore) VALUES (?, ?)");
				PreparedStatement deleteStmt = connection.prepareStatement("DELETE FROM metascale WHERE id = ?");
				
				PreparedStatement addOptionStmt = connection.prepareStatement("INSERT INTO metascaleoption (scaleid, textref, ord, value, transform) VALUES (?, ?, ?, ?, ?)");
				PreparedStatement deleteOptionStmt = connection.prepareStatement("DELETE FROM metascaleoption WHERE scaleid = ?");
				//PreparedStatement updateOptionStmt = connection.prepareStatement("UPDATE metascaleoption SET textref = ?, value = ?, transform = ? WHERE scaleid = ? AND ord = ?");
			) {
				int nNew = 0, nModified = 0, nRemoved = 0, nOptionModified = 0;
				
				for(MetaScale ms: toBeAdded) {
					nNew++;
					insertScaleStmt.setInt(1, ms.id);
					insertScaleStmt.setString(2, ms.ignore.toString());
					insertScaleStmt.addBatch();
					
					int ord = 0;
					for(Option opt: ms.options) {
						addOptionStmt.setInt(1, ms.id);
						addOptionStmt.setString(2, opt.textRef.getRef());
						addOptionStmt.setInt(3, ord);
						addOptionStmt.setDouble(4, opt.value);
						addOptionStmt.setString(5, opt.transformation.toString());
						addOptionStmt.addBatch();
						ord++;
					}
					ms.modified = false;
					ms.structureChanged = false;
				}
				toBeAdded.clear();
				insertScaleStmt.executeBatch();
				addOptionStmt.executeBatch();
				addDelta = 0;
				
				for(MetaScale ms: scaleMap.values()) {
					if(ms.modified) {
						nModified++;
						updateScaleStmt.setInt(2, ms.id);
						updateScaleStmt.setString(1, ms.ignore.toString());
						updateScaleStmt.addBatch();
						
						if(ms.structureChanged) {
							deleteOptionStmt.setInt(1, ms.id);
							deleteOptionStmt.addBatch();
							
							int ord = 0;
							for(Option opt: ms.options) {
								nOptionModified++;
								addOptionStmt.setInt(1, ms.id);
								addOptionStmt.setString(2, opt.textRef.getRef());
								addOptionStmt.setInt(3, ord);
								addOptionStmt.setDouble(4, opt.value);
								addOptionStmt.setString(5, opt.transformation.toString());
								addOptionStmt.addBatch();
								ord++;
							}
							ms.structureChanged = false;
						}
						ms.modified = false;
					}
					if(ms.structureChanged) {
						deleteOptionStmt.setInt(1, ms.id);
						deleteOptionStmt.addBatch();
						
						int ord = 0;
						for(Option opt: ms.options) {
							nOptionModified++;
							addOptionStmt.setInt(1, ms.id);
							addOptionStmt.setString(2, opt.textRef.getRef());
							addOptionStmt.setInt(3, ord);
							addOptionStmt.setDouble(4, opt.value);
							addOptionStmt.setString(5, opt.transformation.toString());
							addOptionStmt.addBatch();
							ord++;
						}
						ms.structureChanged = false;
					}
				}
				updateScaleStmt.executeBatch();
				deleteOptionStmt.executeBatch();
				addOptionStmt.executeBatch();
				
				for(MetaScale ms: toBeRemoved.values()) {
					nRemoved++;
					deleteStmt.setInt(1, ms.id);
					deleteStmt.addBatch();
					deleteOptionStmt.setInt(1, ms.id);
					deleteOptionStmt.addBatch();
				}
				deleteStmt.executeBatch();
				deleteOptionStmt.executeBatch();
				toBeRemoved.clear();
				
				if(nModified != 0 || nNew != 0 || nRemoved != 0 || nOptionModified != 0) {
					String logString = String.format("MetaScale: Saved %d (%d new), %d removed, %d options changed", nModified, nNew, nRemoved, nOptionModified);
					Logger.getGlobal().log(Level.INFO, logString);
				}
			}
		}
		
		public int getUnsavedChangesCount() {
			int nModified = 0, nOptionModified = 0;
			for(MetaScale ms: scaleMap.values()) {
				if(ms.modified) {
					nModified++;
				}
				if(ms.structureChanged) {
					nOptionModified++;
				}
			}			
			return toBeRemoved.size() + nModified + nOptionModified;
		}
		
		public List<MetaScale> getAll() throws SQLException, DaxploreException {
			// make sure all scales are cached before returning the content of the map
			try(Statement stmt = connection.createStatement();
					ResultSet rs = stmt.executeQuery("SELECT id FROM metascale")) {
				while(rs.next()) {
					int id = rs.getInt("id");
					if(!scaleMap.containsKey(id) && !toBeRemoved.containsKey(id)) {
						get(rs.getInt("id"));
					}
				}
			}
			return new LinkedList<>(scaleMap.values());
		}
		
		public void discardChanges() {
			scaleMap.clear();
			toBeAdded.clear();
			toBeRemoved.clear();
			addDelta = 0;
		}
	}
	
	public static class Option {
		private TextReference textRef;
		private double value;
		private NumberlineCoverage transformation;
		private boolean modified = false; //TODO remove modified tag as it's never read and Options are overwritten in saveall anyway?
		//TODO remove unused setters
		
		public Option(TextReference textRef, double value, NumberlineCoverage transformation, boolean setNew) {
			this.textRef = textRef; this.value = value; this.transformation = transformation; this.modified = setNew;
		}

		public TextReference getTextRef() {
			return textRef;
		}

		public void setTextRef(TextReference textRef) {
			if(!textRef.equals(this.textRef)) {
				this.textRef = textRef;
				modified = true;
			}
		}

		public double getValue() {
			return value;
		}

		public void setValue(double value) {
			if(value != this.value) {
				this.value = value;
				modified = true;
			}
		}

		public NumberlineCoverage getTransformation() {
			return transformation;
		}

		public void setTransformation(NumberlineCoverage transformation) {
			if(!transformation.equals(this.transformation)) {
				this.transformation = transformation;
				modified = true;
			}
		}
	}
	
	
	/** Each Option's position is defined by the order of this list */
	private List<Option> options;
	private NumberlineCoverage ignore;

	private int id;
	private boolean modified = false;
	private boolean structureChanged = false;
	
	private MetaScale(int id, List<Option> options, NumberlineCoverage ignore, boolean newScale) {
		this.id = id;
		this.options = options;
		this.ignore = ignore;
		this.modified = newScale;
		this.structureChanged = newScale;
	}
	
	public int getId() {
		return id;
	}
	
	public int getOptionCount() {
		return options.size();
	}
	
	public List<Option> getOptions() {
		return Lists.newLinkedList(options);
	}

	public void setOptions(List<Option> options) {
		if(!options.equals(this.options)) {
			this.options = options;
			structureChanged = true;
			modified = true;
		}
	}

	public NumberlineCoverage getIgnoreOption() {
		return ignore;
	}
	
	public void setIgnoreOption(NumberlineCoverage ignore) {
		if(!ignore.equals(this.ignore)) {
			this.ignore = ignore;
			modified = true;
		}
	}
	
	/**
	 * 
	 * @param value
	 * @return Output value Double.NaN if not found
	 * @throws Exception
	 */
	public double transform(double value) {
		for(Option opt: options) {
			if(opt.transformation.contains(value)) {
				return opt.value;
			}
		}
		return Double.NaN;
	}
	
	/**
	 * 
	 * @param value
	 * @return index of value, -1 if not found
	 */
	public int matchIndex(double value) {
		for(int i = 0; i < options.size(); i++ ) {
			if(options.get(i).transformation.contains(value)) {
				return i;
			}
		}
		return -1;
	}
	
	public boolean ignored(double value) {
		return ignore.contains(value);
	}
	
	public boolean equalsLocale(MetaScale other, Locale byLocale) {
		if(options.size() != other.options.size()) { return false; }
		for(int i = 0; i < options.size(); i++) {
			if(!options.get(i).textRef.get(byLocale).trim().equals(other.options.get(i).textRef.get(byLocale).trim())) { return false; }
			if(options.get(i).value != other.options.get(i).value) { return false; }
			if(!options.get(i).transformation.toString().equals(other.options.get(i).transformation.toString())) { return false; }
		}
		if(!ignore.toString().equals(other.ignore.toString())) { return false; }
		return true;
	}
}
