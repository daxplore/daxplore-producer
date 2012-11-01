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

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import daxplorelib.DaxploreTable;
import daxplorelib.SQLTools;
import daxplorelib.metadata.TextReference.TextReferenceManager;

import tools.MyTools;
import tools.NumberlineCoverage;
import tools.Pair;

public class MetaScale {
	
	protected static final DaxploreTable maintable = new DaxploreTable(
			"CREATE TABLE metascale (id INTEGER PRIMARY KEY, ignore STRING)"
			, "metascale");
	protected static final DaxploreTable optiontable = new DaxploreTable(
			"CREATE TABLE metascaleoption (FOREIGN KEY(scaleid) REFERENCES metascale(id), textref STRING, ord INTEGER, value REAL, transform STRING)"
			, "metascaleoption");
	
	public class MetaScaleManager {
		Map<Integer, MetaScale> scaleMap = new HashMap<Integer, MetaScale>();
		Connection connection;
		protected TextReferenceManager textsManager;
		
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
		
		public MetaScale getMetaScale(int id) throws SQLException {
			if(scaleMap.containsKey(id)) {
				return scaleMap.get(id);
			} else {
				PreparedStatement stmt = connection.prepareStatement("SELECT * FROM metascale WHERE id = ?");
				stmt.setInt(1, id);
				ResultSet rs = stmt.executeQuery();
				rs.next();
				NumberlineCoverage ignore = new NumberlineCoverage(rs.getString("ignore"));
				
				stmt = connection.prepareStatement("SELECT * FROM metascaleoption WHERE id = ? ORDER BY ord");
				stmt.setInt(1, id);
				rs = stmt.executeQuery();
				
				List<Option> options = new LinkedList<Option>();
				
				while(rs.next()) {
					options.add(
							new Option(
									textsManager.get(rs.getString("textref")), 
									rs.getDouble("value"), 
									new NumberlineCoverage(rs.getString("transform"))));
				}
				MetaScale ms = new MetaScale(id, options, ignore);
				scaleMap.put(id, ms);
				return ms;
			}
		}
		
		public MetaScale createMetaScale(List<Option> options, NumberlineCoverage ignore) throws SQLException {
			PreparedStatement createScaleStmt = connection.prepareStatement("INSERT INTO metascale (ignore) VALUES (?)");
			createScaleStmt.setString(1, ignore.toString());
			createScaleStmt.execute();
			
			int id = SQLTools.lastId(maintable.name, connection);
			
			PreparedStatement addOptionStmt = connection.prepareStatement("INSERT INTO metascaleoption (scaleid, textref, ord, value, transform) VALUES (?, ?, ?, ?, ?)");
			
			int ord = 0;
			for(Option opt: options) {
				ord++;
				addOptionStmt.setInt(1, id);
				addOptionStmt.setString(2, opt.textRef.getRef());
				addOptionStmt.setInt(3, ord);
				addOptionStmt.setDouble(4, opt.value);
				addOptionStmt.setString(5, opt.transformation.toString());
				addOptionStmt.addBatch();
			}
			addOptionStmt.executeBatch();
			
			MetaScale ms = new MetaScale(id, options, ignore);
			scaleMap.put(id, ms);
			return ms;
		}
		
		public void remove(int id) {
			//TODO: implement
		}
		
		public void saveAll() throws SQLException {
			PreparedStatement scaleStmt = connection.prepareStatement("UPDATE metascale SET ignore = ? WHERE id = ? ");
			PreparedStatement removeOptionStmt = connection.prepareStatement("DELETE FROM metascaleoption WHERE scaleid = ?");
			PreparedStatement addOptionStmt = connection.prepareStatement("INSERT INTO metascaleoption (scaleid, textref, ord, value, transform) VALUES (?, ?, ?, ?, ?)");
			for(MetaScale ms: scaleMap.values()) {
				if(ms.modified) {
					scaleStmt.setInt(2, ms.id);
					scaleStmt.setString(1, ms.ignore.toString());
					scaleStmt.executeUpdate();
					
					removeOptionStmt.setInt(1, ms.id);
					removeOptionStmt.executeUpdate();
					
					int ord = 0;
					for(Option opt: ms.options) {
						ord++;
						addOptionStmt.setInt(1, ms.id);
						addOptionStmt.setString(2, opt.textRef.getRef());
						addOptionStmt.setInt(3, ord);
						addOptionStmt.setDouble(4, opt.value);
						addOptionStmt.setString(5, opt.transformation.toString());
						addOptionStmt.addBatch();
					}
					addOptionStmt.executeBatch();
					ms.modified = false;
				}
			}
			//save all unsaved MetaScales
		}
		
		public List<MetaScale> getAll() throws SQLException {
			return null; //TODO: implement
		}
	}
	
	public class Option {
		TextReference textRef;
		double value;
		NumberlineCoverage transformation;
		
		public Option(TextReference textRef, double value, NumberlineCoverage transformation) {
			this.textRef = textRef; this.value = value; this.transformation = transformation;
		}
	}
	
	
	/** Each Option's position is defined by the order of this list */
	protected List<Option> options;
	protected NumberlineCoverage ignore;

	protected int id;
	protected boolean modified = false;
	
	public MetaScale(int id, List<Option> options, NumberlineCoverage ignore) {
		this.id = id;
		this.options = options;
		this.ignore = ignore;
	}
	
	public int getId() {
		return id;
	}
	
	public List<Option> getOptions() {
		return options;
	}

	public void setOptions(List<Option> options) {
		this.options = options;
		modified = true;
	}

	public NumberlineCoverage getIgnoreOption() {
		return ignore;
	}
	
	public void setIgnoreOption(NumberlineCoverage ignore) {
		this.ignore = ignore;
		modified = true;
	}
	
	/**
	 * 
	 * @param value
	 * @throws Exception 
	 */
	public double transform(double value) throws Exception {
		for(Option opt: options) {
			if(opt.transformation.contains(value)) {
				return opt.value;
			}
		}
		throw new Exception("Ignore Exception");
	}
	
	public boolean ignored(double value) {
		return ignore.contains(value);
	}
	
}
