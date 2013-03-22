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
import daxplorelib.metadata.MetaQuestion.MetaQuestionManager;
import daxplorelib.metadata.TextReference.TextReferenceManager;

public class MetaGroup implements Comparable<MetaGroup> {
	static final DaxploreTable groupTable = new DaxploreTable("CREATE TABLE metagroup (id INTEGER PRIMARY KEY, textref TEXT, idx INTEGER, type INTEGER)", "metagroup");
	static final DaxploreTable groupRelTable = new DaxploreTable("CREATE TABLE metagrouprel (groupid INTEGER, questionid TEXT, idx INTEGER, FOREIGN KEY(questionid) REFERENCES metaquestion(id), FOREIGN KEY(groupid) REFERENCES metagroup(id))", "metagrouprel");
	
	public static class MetaGroupManager {
		
		private Map<Integer, MetaGroup> groupMap = new HashMap<Integer, MetaGroup>();
		private List<MetaGroup> toBeAddedGroup = new LinkedList<MetaGroup>();
		private int addDeltaGroup = 0;
		private List<MetaGroupRel> toBeAddedGroupRel = new LinkedList<MetaGroupRel>();
		private List<MetaGroup> toBeRemoved = new LinkedList<MetaGroup>();
		private Connection connection;
		private TextReferenceManager textsManager;
		private MetaQuestionManager questionManager;
		
		public MetaGroupManager(Connection connection, TextReferenceManager textsManager, MetaQuestionManager questionManager) {
			this.connection = connection;
			this.textsManager = textsManager;
			this.questionManager = questionManager;
		}
		
		public void init() throws SQLException {
			SQLTools.createIfNotExists(groupTable, connection);
			SQLTools.createIfNotExists(groupRelTable, connection);
		}
		
		public MetaGroup get(int id) throws SQLException, DaxploreException {
			if(groupMap.containsKey(id)) {
				return groupMap.get(id);
			}
			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM metagroup WHERE id = ?");
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			rs.next();
			
			TextReference tr = textsManager.get(rs.getString("textref"));
			int index = rs.getInt("idx");
			GroupType type = GroupType.fromInt(rs.getInt("type"));
			rs.close();
			
			List<MetaQuestion> qList = new LinkedList<MetaQuestion>();
			PreparedStatement qstmt = connection.prepareStatement("SELECT questionid FROM metagrouprel WHERE groupid = ? ORDER BY idx ASC");
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
			addDeltaGroup++;
			
			int id = SQLTools.maxId(groupTable.name, "id", connection) + addDeltaGroup;
			MetaGroup mg = new MetaGroup(id, textref, index, type, qList);
			toBeAddedGroup.add(mg);
			groupMap.put(id, mg);
			
			for(int idx = 0; idx < qList.size(); idx++) {
				MetaGroupRel groupRel = new MetaGroupRel(id, qList.get(idx).getId(), idx);
				toBeAddedGroupRel.add(groupRel);
			}
			
			return mg;
		}
		
		public void remove(int id) {
			MetaGroup tbr = groupMap.remove(id);
			toBeAddedGroup.remove(tbr);
			toBeRemoved.add(tbr);
		}
		
		public void saveAll() throws SQLException {
			PreparedStatement updateGroupStmt = connection.prepareStatement("UPDATE metagroup SET textref = ?, idx = ?, type = ? WHERE id = ?");
			PreparedStatement deleteGroupStmt = connection.prepareStatement("DELETE FROM metagroup WHERE id = ?");
			PreparedStatement insertGroupStmt = connection.prepareStatement("INSERT INTO metagroup (id, textref, idx, type) VALUES (?, ?, ?, ?)");
			
			PreparedStatement deleteGroupRelStmt = connection.prepareStatement("DELETE FROM metagrouprel WHERE groupid = ?");
			PreparedStatement insertGroupRelStmt = connection.prepareStatement("INSERT INTO metagrouprel (groupid, questionid, idx) VALUES (?, ?, ?)");
			
			for(MetaGroup mg: groupMap.values()) {
				if(mg.modified) {
					updateGroupStmt.setString(1, mg.textref.getRef());
					updateGroupStmt.setInt(2, mg.index);
					updateGroupStmt.setInt(3, mg.type.asInt());
					updateGroupStmt.setInt(4, mg.id);
					updateGroupStmt.executeUpdate();
					
					deleteGroupRelStmt.setInt(1, mg.id);
					deleteGroupRelStmt.executeUpdate();
					
					for(int idx = 0; idx < mg.qList.size(); idx++) {
						insertGroupRelStmt.setInt(1, mg.id);
						insertGroupRelStmt.setString(2, mg.qList.get(idx).getId());
						insertGroupRelStmt.setInt(3, idx);
						insertGroupRelStmt.addBatch();
					}
					insertGroupRelStmt.executeBatch();
					
					mg.modified = false;
				}
			}
			
			
			for(MetaGroup mg: toBeRemoved) {
				deleteGroupStmt.setInt(1, mg.id);
				deleteGroupStmt.addBatch();
				deleteGroupRelStmt.setInt(1, mg.id);
				deleteGroupRelStmt.addBatch();
			}
			deleteGroupStmt.executeBatch();
			deleteGroupRelStmt.executeBatch();
			toBeRemoved.clear();
			
			
			for(MetaGroup mg: toBeAddedGroup) {
				insertGroupStmt.setInt(1, mg.id);
				insertGroupStmt.setString(2, mg.textref.getRef());
				insertGroupStmt.setInt(3, mg.index);
				insertGroupStmt.setInt(4, mg.type.asInt());
				insertGroupStmt.addBatch();
			}
			insertGroupStmt.executeBatch();
			toBeAddedGroup.clear();
			addDeltaGroup = 0;
			
			for(MetaGroupRel mgr: toBeAddedGroupRel) {
				insertGroupRelStmt.setInt(1, mgr.groupid);
				insertGroupRelStmt.setString(2, mgr.questionid);
				insertGroupRelStmt.setInt(3, mgr.idx);
				insertGroupRelStmt.addBatch();
			}
			insertGroupRelStmt.executeBatch();
			toBeAddedGroupRel.clear();
		}
		
		public List<MetaGroup> getAll() throws SQLException, DaxploreException {
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

		public int getHighestId() throws SQLException {
			return SQLTools.maxId(groupTable.name, "id", connection);
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
	
	private static class MetaGroupRel {
		int groupid, idx;
		String questionid;
		public MetaGroupRel(int groupid, String questionid, int idx){
			this.groupid = groupid;
			this.questionid = questionid;
			this.idx = idx;
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
	
	public MetaQuestion getQuestion(int index) {
		return qList.get(index);
	}
	
	public void addQuestion(MetaQuestion mq, int atIndex) {
		qList.add(atIndex, mq);
		modified = true;
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
	
	public GroupType getType() {
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

	public int getQuestionCount() {
		return qList.size();
	}
}
