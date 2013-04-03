package daxplorelib.metadata;

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

import tools.NumberlineCoverage;
import tools.NumberlineCoverage.NumberlineCoverageException;
import daxplorelib.DaxploreTable;
import daxplorelib.SQLTools;
import daxplorelib.metadata.textreference.TextReference;
import daxplorelib.metadata.textreference.TextReferenceManager;

public class MetaScale {
	
	protected static final DaxploreTable maintable = new DaxploreTable(
			"CREATE TABLE metascale (id INTEGER PRIMARY KEY, ignore STRING)"
			, "metascale");
	protected static final DaxploreTable optiontable = new DaxploreTable(
			"CREATE TABLE metascaleoption (scaleid INTEGER, textref STRING, ord INTEGER, value REAL, transform STRING, FOREIGN KEY(scaleid) REFERENCES metascale(id))"
			, "metascaleoption");
	
	public static class MetaScaleManager {
		private Map<Integer, MetaScale> scaleMap = new HashMap<Integer, MetaScale>();
		private Connection connection;
		private TextReferenceManager textsManager;
		
		private List<MetaScale> toBeAdded = new LinkedList<MetaScale>();
		private int addDelta = 0;
		private List<MetaScale> toBeRemoved = new LinkedList<MetaScale>();
		
		public MetaScaleManager(Connection connection, TextReferenceManager textsManager) {
			this.connection = connection;
			this.textsManager = textsManager;
		}
		
		protected void init() throws SQLException {
			if(!SQLTools.tableExists(maintable.name, connection)) {
				Statement stmt = connection.createStatement();
				stmt.executeUpdate(maintable.sql);
			}
			if(!SQLTools.tableExists(optiontable.name, connection)) {
				Statement stmt = connection.createStatement();
				stmt.executeUpdate(optiontable.sql);
			}
		}
		
		public MetaScale get(int id) throws SQLException {
			if(scaleMap.containsKey(id)) {
				return scaleMap.get(id);
			} else {
				PreparedStatement stmt = connection.prepareStatement("SELECT * FROM metascale WHERE id = ?");
				stmt.setInt(1, id);
				ResultSet rs = stmt.executeQuery();
				NumberlineCoverage ignore;
				if(rs.next()) {
					try {
						ignore = new NumberlineCoverage(rs.getString("ignore"));
					} catch (NumberlineCoverageException e) {
						ignore = new NumberlineCoverage(); //TODO: do something if corrupt data is stored in database?
					}
				} else {
					ignore = new NumberlineCoverage();
				}
				
				stmt = connection.prepareStatement("SELECT * FROM metascaleoption WHERE scaleid = ? ORDER BY ord");
				stmt.setInt(1, id);
				rs = stmt.executeQuery();
				
				List<Option> options = new LinkedList<Option>();
				
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
			toBeRemoved.add(scale);
		}
		
		public void saveAll() throws SQLException {
			PreparedStatement updateScaleStmt = connection.prepareStatement("UPDATE metascale SET ignore = ? WHERE id = ? ");
			PreparedStatement insertScaleStmt = connection.prepareStatement("INSERT INTO metascale (id, ignore) VALUES (?, ?)");
			PreparedStatement deleteStmt = connection.prepareStatement("DELETE FROM metascale WHERE id = ?");
			
			PreparedStatement addOptionStmt = connection.prepareStatement("INSERT INTO metascaleoption (scaleid, textref, ord, value, transform) VALUES (?, ?, ?, ?, ?)");
			PreparedStatement deleteOptionStmt = connection.prepareStatement("DELETE FROM metascaleoption WHERE scaleid = ?");
			PreparedStatement updateOptionStmt = connection.prepareStatement("UPDATE metascaleoption SET textref = ?, value = ?, transform = ? WHERE scaleid = ? AND ord = ?");
			
			int nNew = 0, nModified = 0, nRemoved = 0, nOptionModified = 0;
			
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
						addOptionStmt.executeBatch();
					}
					ms.modified = false;
				}
				if(!ms.structureChanged) {
					for(int ord = 0; ord < ms.options.size(); ord++) {
						Option opt = ms.options.get(ord);
						if(opt.modified) {
							nOptionModified++;
							updateOptionStmt.setString(1, opt.textRef.getRef());
							updateOptionStmt.setDouble(2, opt.value);
							updateOptionStmt.setString(3, opt.transformation.toString());
							updateOptionStmt.setInt(4, ms.id);
							updateOptionStmt.setInt(5, ord);
							updateOptionStmt.addBatch();
							opt.modified = false;
						}
						updateOptionStmt.executeBatch();
					}
				}
				ms.structureChanged = false;
			}
			updateScaleStmt.executeBatch();
			deleteOptionStmt.executeBatch();
			
			for(MetaScale ms: toBeRemoved) {
				nRemoved++;
				deleteStmt.setInt(1, ms.id);
				deleteStmt.addBatch();
				deleteOptionStmt.setInt(1, ms.id);
				deleteOptionStmt.addBatch();
			}
			deleteStmt.executeBatch();
			deleteOptionStmt.executeBatch();
			toBeRemoved.clear();
			
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
			}
			toBeAdded.clear();
			insertScaleStmt.executeBatch();
			addOptionStmt.executeBatch();
			addDelta = 0;
			
			if(nModified != 0 || nNew != 0 || nRemoved != 0 || nOptionModified != 0) {
				String logString = String.format("MetaScale: Saved %d (%d new), %d removed, %d options changed", nModified, nNew, nRemoved, nOptionModified);
				Logger.getGlobal().log(Level.INFO, logString);
			}
		}
		
		public List<MetaScale> getAll() throws SQLException {
			ResultSet rs = connection.createStatement().executeQuery("SELECT id FROM metascale");
			while(rs.next()) {
				get(rs.getInt("id"));
			}
			return new LinkedList<MetaScale>(scaleMap.values());
		}
	}
	
	public static class Option {
		TextReference textRef;
		double value;
		NumberlineCoverage transformation;
		boolean modified = false;
		
		public Option(TextReference textRef, double value, NumberlineCoverage transformation, boolean setNew) {
			this.textRef = textRef; this.value = value; this.transformation = transformation; this.modified = setNew;
		}

		public TextReference getTextRef() {
			return textRef;
		}

		public void setTextRef(TextReference textRef) {
			this.textRef = textRef;
			modified = true;
		}

		public double getValue() {
			return value;
		}

		public void setValue(double value) {
			this.value = value;
			modified = true;
		}

		public NumberlineCoverage getTransformation() {
			return transformation;
		}

		public void setTransformation(NumberlineCoverage transformation) {
			this.transformation = transformation;
			modified = true;
		}
	}
	
	
	/** Each Option's position is defined by the order of this list */
	protected List<Option> options;
	protected NumberlineCoverage ignore;

	protected int id;
	protected boolean modified = false;
	protected boolean structureChanged = false;
	
	public MetaScale(int id, List<Option> options, NumberlineCoverage ignore, boolean newScale) {
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
		return options;
	}

	public void setOptions(List<Option> options) {
		this.options = options;
		structureChanged = true;
		modified = true;
	}

	public NumberlineCoverage getIgnoreOption() {
		return ignore;
	}
	
	public void setIgnoreOption(NumberlineCoverage ignore) {
		this.ignore = ignore;
		modified = true;
	}
	
	public double transform(double value) throws Exception {
		for(Option opt: options) {
			if(opt.transformation.contains(value)) {
				return opt.value;
			}
		}
		throw new Exception("Ignore Exception");
	}
	
	public int matchIndex(double value) throws Exception {
		for(int i = 0; i < options.size(); i++ ) {
			if(options.get(i).transformation.contains(value)) {
				return i;
			}
		}
		throw new Exception("Ignore Exception");
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
