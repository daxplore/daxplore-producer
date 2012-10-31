package daxplorelib.metadata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import daxplorelib.DaxploreTable;
import daxplorelib.SQLTools;
import daxplorelib.metadata.MetaScale.MetaScaleManager;

public class MetaQuestion {
	
	protected static final DaxploreTable table = new DaxploreTable(
			"CREATE TABLE metaquestion (id INTEGER PRIMARY KEY, FOREIGN KEY(scaleid) REFERENCE metascale(id), fulltextref TEXT, shorttextref TEXT, calculation INTEGER)",
			"metaquestion");
	
	public class MetaQuestionManager {
		
		private Connection connection;
		protected MetaScaleManager metascaleManager;
		protected Map<Integer, MetaQuestion> questionMap = new HashMap<Integer, MetaQuestion>();
		
		public MetaQuestionManager(Connection connection, MetaScaleManager metascaleManager) {
			this.connection = connection;
			this.metascaleManager = metascaleManager;
			
		}
		
		public void init() throws SQLException {
			if(!SQLTools.tableExists("metaquestion", connection)) {
				Statement stmt = connection.createStatement();
				stmt.executeUpdate(table.sql);
			}
		}
		
		public MetaQuestion getMetaQuestion(int id) throws SQLException {
			if(questionMap.containsKey(id)) {
				return questionMap.get(id);
			} else {
				PreparedStatement stmt = connection.prepareStatement("SELECT * FROM metaquestion WHERE id = ?");
				stmt.setInt(1, id);
				ResultSet rs = stmt.executeQuery();
				rs.next();
				TextReference fullTextRef = new TextReference(rs.getString("fulltextref"), connection);
				TextReference shortTextRef = new TextReference(rs.getString("shorttextref"), connection);
				MetaScale scale = metascaleManager.getMetaScale(rs.getInt("scaleid"));
				MetaCalculation calculation = new MetaCalculation(rs.getInt("calculation"), connection);
				
				MetaQuestion mq = new MetaQuestion(id, shortTextRef, fullTextRef, scale, calculation);
				questionMap.put(id, mq);
				return mq;				
			}
		}
		
		public MetaQuestion createMetaQuestion(TextReference shortTextRef, TextReference fullTextRef, MetaScale scale, MetaCalculation calculation) throws SQLException {
			PreparedStatement stmt = connection.prepareStatement("INSERT INTO metaquestion (scaleid, fulltextref, shorttextref, calculation) VALUES (?, ?, ? .?)");
			stmt.setInt(1, scale.getId());
			stmt.setString(2, fullTextRef.getRef());
			stmt.setString(3, shortTextRef.getRef());
			stmt.setInt(4, calculation.getID());
			stmt.executeUpdate();
			
			int id = SQLTools.lastId("metaquestion", connection);
			
			MetaQuestion mq = new MetaQuestion(id, shortTextRef, fullTextRef, scale, calculation);
			questionMap.put(id, mq);
			
			return mq;
		}
		
		public void remove(int id) {
			//TODO: implement
		}
		
		public void saveAll() throws SQLException {
			PreparedStatement stmt = connection.prepareStatement("UPDATE metaquestion SET scaleid = ?, fulltextref = ?, shorttextref = ?, calculation = ? WHERE id = ?");
			for(MetaQuestion mq: questionMap.values()) {
				if(mq.modified) {
					stmt.setInt(1, mq.scale.getId());
					stmt.setString(2, mq.fullTextRef.getRef());
					stmt.setString(3, mq.shortTextRef.getRef());
					stmt.setInt(4, mq.calculation.getID());
					stmt.setInt(5, mq.id);
					stmt.executeUpdate();
					mq.modified = false;
				}
			}
		}
		
		public List<MetaQuestion> getAll() {
			return null; //TODO: implement
		}
	}
	
	protected int id;
	protected TextReference shortTextRef, fullTextRef;
	protected MetaScale scale;
	protected MetaCalculation calculation;
	
	protected boolean modified = false;
	
	protected MetaQuestion(int id, TextReference shortTextRef, TextReference fullTextRef, MetaScale scale, MetaCalculation calculation) {
		this.id = id;
		this.shortTextRef = shortTextRef;
		this.fullTextRef = fullTextRef;
		this.scale = scale;
		this.calculation = calculation;
	}
	
	public int getId() {
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
}
