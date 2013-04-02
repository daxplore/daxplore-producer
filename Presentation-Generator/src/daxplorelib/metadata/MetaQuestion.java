package daxplorelib.metadata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import daxplorelib.DaxploreException;
import daxplorelib.DaxploreTable;
import daxplorelib.SQLTools;
import daxplorelib.metadata.MetaScale.MetaScaleManager;
import daxplorelib.metadata.MetaTimepointShort.MetaTimepointShortManager;
import daxplorelib.metadata.textreference.TextReference;
import daxplorelib.metadata.textreference.TextReferenceManager;

public class MetaQuestion {
	
	protected static final DaxploreTable table = new DaxploreTable(
			"CREATE TABLE metaquestion (id TEXT PRIMARY KEY, scaleid INTEGER, fulltextref TEXT, shorttextref TEXT, calculation INTEGER, FOREIGN KEY(scaleid) REFERENCES metascale(id))",
			"metaquestion");
	protected static final DaxploreTable timePointTable = new DaxploreTable(
			"CREATE TABLE questtimerel (qid TEXT, timeid INTEGER, FOREIGN KEY(qid) REFERENCES metaquestion(id), FOREIGN KEY(timeid) REFERENCES timepoints(id))", 
			"questtimerel");
	
	public static class MetaQuestionManager {
		
		private Connection connection;
		private MetaScaleManager metascaleManager;
		private TextReferenceManager textsManager;
		private MetaTimepointShortManager timePointManager;
		private Map<String, MetaQuestion> questionMap = new HashMap<String, MetaQuestion>();
		private LinkedList<MetaQuestion> toBeAdded= new LinkedList<MetaQuestion>();
		
		protected LinkedList<MetaQuestion> toBeRemoved = new LinkedList<MetaQuestion>();
		
		public MetaQuestionManager(Connection connection, TextReferenceManager textsManager, MetaScaleManager metaScaleManager, MetaTimepointShortManager timePointManager) {
			this.connection = connection;
			this.metascaleManager = metaScaleManager;
			this.textsManager = textsManager;
			this.timePointManager = timePointManager;
		}
		
		public void init() throws SQLException {
			if(!SQLTools.tableExists(table.name, connection)) {
				Statement stmt = connection.createStatement();
				stmt.executeUpdate(table.sql);
			}
			if(!SQLTools.tableExists(timePointTable.name, connection)) {
				Statement stmt = connection.createStatement();
				stmt.executeUpdate(timePointTable.sql);
			}
		}
		
		public MetaQuestion get(String id) throws SQLException, DaxploreException {
			if(questionMap.containsKey(id)) {
				return questionMap.get(id);
			} else {
				PreparedStatement stmt = connection.prepareStatement("SELECT * FROM metaquestion WHERE id = ?");
				stmt.setString(1, id);
				ResultSet rs = stmt.executeQuery();
				if(rs.next()) {
					TextReference fullTextRef = textsManager.get(rs.getString("fulltextref"));
					TextReference shortTextRef = textsManager.get(rs.getString("shorttextref"));
					MetaScale scale = metascaleManager.get(rs.getInt("scaleid"));
					MetaCalculation calculation = new MetaCalculation(rs.getInt("calculation"), connection);
					
					List<MetaTimepointShort> timepoints = new LinkedList<MetaTimepointShort>();
					PreparedStatement stmt2 = connection.prepareStatement("SELECT timeid FROM questtimerel WHERE qid = ?");
					stmt2.setString(1, id);
					ResultSet rs2 = stmt2.executeQuery();
					while(rs2.next()) {
						timepoints.add(timePointManager.get(rs2.getInt("timeid")));
					}
					
					MetaQuestion mq = new MetaQuestion(id, shortTextRef, fullTextRef, scale, calculation, timepoints);
					questionMap.put(id, mq);
					return mq;				
				} else {
					throw new AssertionError("QuestionID does not exist?");
				}
			}
		}
		
		public MetaQuestion create(String id, TextReference shortTextRef, TextReference fullTextRef, MetaScale scale, MetaCalculation calculation, List<MetaTimepointShort> timepoints) throws SQLException {
			MetaQuestion mq = new MetaQuestion(id, shortTextRef, fullTextRef, scale, calculation, timepoints);
			toBeAdded.add(mq);
			questionMap.put(id, mq);
			return mq;
		}
		
		public void remove(String id) {
			MetaQuestion mq = questionMap.remove(id);
			toBeAdded.remove(mq);
			toBeRemoved.add(mq);
		}
		
		public void saveAll() throws SQLException {
			PreparedStatement updateStmt = connection.prepareStatement("UPDATE metaquestion SET scaleid = ?, fulltextref = ?, shorttextref = ?, calculation = ? WHERE id = ?");
			PreparedStatement insertStmt = connection.prepareStatement("INSERT INTO metaquestion (id, scaleid, fulltextref, shorttextref, calculation) VALUES (?, ?, ?, ? ,?)");
			PreparedStatement deleteStmt = connection.prepareStatement("DELETE FROM metaquestion WHERE id = ?");
			PreparedStatement deleteRelStmt = connection.prepareStatement("DELETE FROM questtimerel WHERE qid = ?");
			PreparedStatement insertRelStmt = connection.prepareStatement("INSERT INTO questtimerel (qid, timeid) VALUES (?, ?)");
			
			for(MetaQuestion mq: questionMap.values()) {
				if(mq.modified) {
					updateStmt.setInt(1, mq.scale.getId());
					updateStmt.setString(2, mq.fullTextRef.getRef());
					updateStmt.setString(3, mq.shortTextRef.getRef());
					updateStmt.setInt(4, mq.calculation.getID());
					updateStmt.setString(5, mq.id);
					updateStmt.executeUpdate();
					mq.modified = false;
				}
				if(mq.timemodified) {
					deleteRelStmt.setString(1, mq.id);
					deleteRelStmt.executeUpdate();
					
					for(MetaTimepointShort timepoint: mq.timepoints) {
						insertRelStmt.setString(1, mq.id);
						insertRelStmt.setInt(2, timepoint.getId());
						insertRelStmt.addBatch();
					}
					insertRelStmt.executeBatch();
					
					mq.timemodified = false;
				}
			}
			
			for(MetaQuestion mq: toBeAdded) {
				insertStmt.setString(1, mq.id);
				if(mq.scale != null) {
					insertStmt.setInt(2, mq.scale.getId());
				} else {
					insertStmt.setNull(2, Types.INTEGER);
				}
				insertStmt.setString(3, mq.fullTextRef.getRef());
				insertStmt.setString(4, mq.shortTextRef.getRef());
				insertStmt.setInt(5, mq.calculation.getID());
				insertStmt.addBatch();
				
				for(MetaTimepointShort timepoint: mq.timepoints) {
					insertRelStmt.setString(1, mq.id);
					insertRelStmt.setInt(2, timepoint.getId());
					insertRelStmt.addBatch();
				}
				insertRelStmt.executeBatch();
			}
			insertStmt.executeBatch();
			toBeAdded.clear();
			
			
			for(MetaQuestion mq: toBeRemoved) {
				deleteStmt.setString(1, mq.id);
				deleteStmt.addBatch();
				
				deleteRelStmt.setString(1, mq.id);
				deleteRelStmt.addBatch();
			}
			deleteRelStmt.executeBatch();
			deleteStmt.executeBatch();
			toBeRemoved.clear();
		}
		
		public List<MetaQuestion> getAll() throws SQLException, DaxploreException{
			ResultSet rs = connection.createStatement().executeQuery("SELECT id FROM metaquestion");
			while(rs.next()) {
				get(rs.getString("id"));
			}
			return new LinkedList<MetaQuestion>(questionMap.values());
		}
	}
	
	protected String id;
	protected TextReference shortTextRef, fullTextRef;
	protected MetaScale scale;
	protected MetaCalculation calculation;
	protected List<MetaTimepointShort> timepoints;
	
	protected boolean modified = false;
	protected boolean timemodified = false;
	
	
	protected MetaQuestion(String id, TextReference shortTextRef, TextReference fullTextRef, MetaScale scale, MetaCalculation calculation, List<MetaTimepointShort> timepoints) {
		this.id = id;
		this.shortTextRef = shortTextRef;
		this.fullTextRef = fullTextRef;
		this.scale = scale;
		this.calculation = calculation;
		this.timepoints = timepoints;
	}
	
	public String getId() {
		return id;
	}
	
	public TextReference getShortTextRef() {
		return shortTextRef;
	}
	
	public void setShortTextRef(TextReference shortTextRef) {
		this.shortTextRef = shortTextRef;
		this.modified = true;
	}
	
	public TextReference getFullTextRef() {
		return fullTextRef;
	}
	
	public void setFullTextRef(TextReference fullTextRef) {
		this.fullTextRef = fullTextRef;
		this.modified = true;
	}
	
	public MetaScale getScale() {
		return scale;
	}
	
	public void setScale(MetaScale scale) {
		this.scale = scale;
		this.modified = true;
	}
	
	public MetaCalculation getCalculation() {
		return calculation;
	}
	
	public void setCalculation(MetaCalculation calculation) {
		this.calculation = calculation;
		this.modified = true;
	}

	public List<MetaTimepointShort> getTimepoints() {
		return timepoints;
	}
	
	public void setTimepoints(List<MetaTimepointShort> timepoints) {
		this.timepoints = timepoints;
		this.timemodified = true;
	}
}
