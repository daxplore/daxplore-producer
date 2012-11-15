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

import daxplorelib.DaxploreTable;
import daxplorelib.SQLTools;
import daxplorelib.metadata.MetaQuestion.MetaQuestionManager;
import daxplorelib.metadata.TextReference.TextReferenceManager;

public class MetaGroup implements Comparable<MetaGroup> {
	protected static final DaxploreTable table = new DaxploreTable("CREATE TABLE metagroup (id INTEGER PRIMARY KEY, textref TEXT, ind INTEGER, type INTEGER)", "metagroup");
	protected static final DaxploreTable table2 = new DaxploreTable("CREATE TABLE metagrouprel (groupid INTEGER, questionid TEXT, FOREIGN KEY(questionid) REFERENCES metaquestion(id), FOREIGN KEY(groupid) REFERENCES metagroup(id))", "metagrouprel");
	
	public static class MetaGroupManager {
		
		protected Map<Integer, MetaGroup> groupMap = new HashMap<Integer, MetaGroup>();
		protected List<MetaGroup> toBeRemoved = new LinkedList<MetaGroup>();
		Connection connection;
		TextReferenceManager textsManager;
		MetaQuestionManager questionManager;
		
		public MetaGroupManager(Connection connection, TextReferenceManager textsManager, MetaQuestionManager questionManager) {
			this.connection = connection;
			this.textsManager = textsManager;
			this.questionManager = questionManager;
		}
		
		public void init() throws SQLException {
			SQLTools.createIfNotExists(table, connection);
			SQLTools.createIfNotExists(table2, connection);
		}
		
		public MetaGroup get(int id) throws SQLException {
			if(groupMap.containsKey(id)) {
				return groupMap.get(id);
			}
			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM metagroup WHERE id = ?");
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			rs.next();
			
			TextReference tr = textsManager.get(rs.getString("textref"));
			int index = rs.getInt("ind");
			GroupType type = GroupType.fromInt(rs.getInt("type"));
			rs.close();
			
			List<MetaQuestion> qList = new LinkedList<MetaQuestion>();
			PreparedStatement qstmt = connection.prepareStatement("SELECT questionid FROM metagrouprel WHERE groupid = ?");
			qstmt.setInt(1, id);
			rs = qstmt.executeQuery();
			while(rs.next()) {
				qList.add(questionManager.get(rs.getString("questionid")));
			}
			
			MetaGroup mg = new MetaGroup(id, tr, index, type, qList);
			groupMap.put(id, mg);
			return mg;
		}
		
		public MetaGroup create(TextReference textref, int index, GroupType type, List<MetaQuestion> qList) throws SQLException {
			PreparedStatement stmt = connection.prepareStatement("INSERT INTO metagroup (textref, ind, type) VALUES (?, ?, ?)");
			stmt.setString(1, textref.getRef());
			stmt.setInt(2, index);
			stmt.setInt(3, type.asInt());
			stmt.executeUpdate();
			
			int id = SQLTools.lastId(table.name, connection);
			
			stmt = connection.prepareStatement("INSERT INTO metagrouprel (groupid, questionid) VALUES (?, ?)");
			for(MetaQuestion mq: qList) {
				stmt.setInt(1, id);
				stmt.setString(2, mq.getId());
				stmt.addBatch();
			}
			stmt.executeBatch();
			
			MetaGroup mg = new MetaGroup(id, textref, index, type, qList);
			
			groupMap.put(id, mg);
			return mg;
		}
		
		public void remove(int id) {
			MetaGroup tbr = groupMap.remove(id);
			toBeRemoved.add(tbr);
		}
		
		public void saveAll() throws SQLException {
			PreparedStatement updateStmt = connection.prepareStatement("UPDATE metagroup SET textref = ?, ind = ?, type = ? WHERE id = ?");
			PreparedStatement deleteStmt = connection.prepareStatement("DELETE FROM metagrouprel WHERE groupid = ?");
			PreparedStatement insertStmt = connection.prepareStatement("INSERT INTO metagrouprel (groupid, questionid) VALUES (?, ?)");
			for(MetaGroup mg: groupMap.values()) {
				if(mg.modified) {
					updateStmt.setString(1, mg.textref.getRef());
					updateStmt.setInt(2, mg.index);
					updateStmt.setInt(3, mg.type.asInt());
					updateStmt.setInt(4, mg.id);
					updateStmt.executeUpdate();
					
					deleteStmt.setInt(1, mg.id);
					deleteStmt.executeUpdate();
					
					for(MetaQuestion mq: mg.qList) {
						insertStmt.setInt(1, mg.id);
						insertStmt.setString(2, mq.getId());
						insertStmt.addBatch();
					}
					insertStmt.executeBatch();
					
					mg.modified = false;
				}
			}
			
			PreparedStatement delete2Stmt = connection.prepareStatement("DELETE FROM metagroup WHERE id = ?");
			for(MetaGroup mg: toBeRemoved) {
				delete2Stmt.setInt(1, mg.id);
				delete2Stmt.addBatch();
				deleteStmt.setInt(1, mg.id);
				deleteStmt.addBatch();
			}
			deleteStmt.executeBatch();
			delete2Stmt.executeBatch();
			toBeRemoved = new LinkedList<MetaGroup>();
		}
		
		public List<MetaGroup> getAll() throws SQLException {
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT id FROM metagroup");
			while(rs.next()) {
				int id = rs.getInt("id");
				if(!groupMap.containsKey(id)) {
					get(id); //can be improved
				}
			}
			List<MetaGroup> groupList = new LinkedList<MetaGroup>(groupMap.values());
			Collections.sort(groupList);
			return groupList;
		}
		
	}
	
	
	public enum GroupType {
		QUESTIONS(0), PERSPECTIVE(1);
		
		protected final int type;
		GroupType(int type) {
			this.type = type;
		}
		
		public int asInt() {
			return type;
		}
		
		public static GroupType fromInt(int i) {
			switch(i) {
			case 0:
				return QUESTIONS;
			case 1:
				return PERSPECTIVE;
			}
			return null;
		}
	}
	
	protected int id, index;
	protected List<MetaQuestion> qList;
	protected GroupType type;
	protected TextReference textref;
	
	protected boolean modified = false;
	
	protected MetaGroup(int id, TextReference textref, int index, GroupType type, List<MetaQuestion> qList) {
		this.id = id;
		this.index = index;
		this.qList = qList;
		this.type = type;
		this.textref = textref;
	}
	
	public void addQuestion(MetaQuestion mq) {
		qList.add(mq);
		modified = true;
	}
	
	public void removeQuestion(MetaQuestion mq) {
		qList.remove(mq);
		modified = true;
	}
	
	public TextReference getTextRef() {
		return textref;
	}
	
	public void setTextRef(TextReference textref) {
		this.textref = textref;
		this.modified = true;
	}
	
	public GroupType getType() throws SQLException {
		return type;
	}
	
	public void resetQuestions() {
		qList = new LinkedList<MetaQuestion>();
		modified = true;
	}

	public List<MetaQuestion> getQuestions() {
		return qList;
	}
	
	public void setQuestions(List<MetaQuestion> qlist) {
		this.qList = qlist;
		this.modified = true;
	}
	
	public int getIndex() {
		return index;
	}
	
	public void setIndex(int index) {
		this.index = index;
		this.modified = true;
	}

	@Override
	public int compareTo(MetaGroup o) {
		return index < o.index ? 1: -1;
	}
}
