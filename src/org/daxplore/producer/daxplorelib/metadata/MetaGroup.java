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
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.DaxploreTable;
import org.daxplore.producer.daxplorelib.SQLTools;
import org.daxplore.producer.daxplorelib.metadata.MetaQuestion.MetaQuestionManager;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReferenceManager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class MetaGroup implements Comparable<MetaGroup> {
	static final DaxploreTable groupTable = new DaxploreTable(
			"CREATE TABLE metagroup (id INTEGER PRIMARY KEY, textref TEXT NOT NULL, ord INTEGER NOT NULL, type INTEGER NOT NULL)",
			"metagroup");
	static final DaxploreTable groupRelTable = new DaxploreTable(
			"CREATE TABLE metagrouprel (groupid INTEGER NOT NULL, questionid TEXT NOT NULL, ord INTEGER NOT NULL, FOREIGN KEY(questionid) REFERENCES metaquestion(id), FOREIGN KEY(groupid) REFERENCES metagroup(id))",
			"metagrouprel");

	public static class MetaGroupManager {

		private Map<Integer, MetaGroup> groupMap = new HashMap<Integer, MetaGroup>();
		private List<MetaGroup> toBeAddedGroup = new LinkedList<MetaGroup>();
		private int addDelta = 0;
		private List<MetaGroupRel> toBeAddedGroupRel = new LinkedList<MetaGroupRel>();
		private Map<Integer, MetaGroup> toBeRemoved = new HashMap<Integer, MetaGroup>();
		private Connection connection;
		private TextReferenceManager textsManager;
		private MetaQuestionManager questionManager;

		public MetaGroupManager(Connection connection,
				TextReferenceManager textsManager,
				MetaQuestionManager questionManager) {
			this.connection = connection;
			this.textsManager = textsManager;
			this.questionManager = questionManager;
		}

		public void init() throws SQLException {
			SQLTools.createIfNotExists(groupTable, connection);
			SQLTools.createIfNotExists(groupRelTable, connection);
		}

		public MetaGroup get(int id) throws SQLException, DaxploreException {
			if (groupMap.containsKey(id)) {
				return groupMap.get(id);
			} else if (toBeRemoved.containsKey(id)) {
				throw new DaxploreException("No group with id '" + id + "'");
			}

			PreparedStatement stmt = connection
					.prepareStatement("SELECT * FROM metagroup WHERE id = ?");
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			TextReference tr;
			int index;
			GroupType type;
			if (rs.next()) {
				tr = textsManager.get(rs.getString("textref"));
				index = rs.getInt("ord");
				type = GroupType.fromInt(rs.getInt("type"));
				rs.close();
			} else {
				throw new DaxploreException("No group with id '" + id + "'");
			}

			List<MetaQuestion> qList = new LinkedList<MetaQuestion>();
			PreparedStatement qstmt = connection
					.prepareStatement("SELECT questionid FROM metagrouprel WHERE groupid = ? ORDER BY ord ASC");
			qstmt.setInt(1, id);
			rs = qstmt.executeQuery();
			while (rs.next()) {
				qList.add(questionManager.get(rs.getString("questionid")));
			}

			MetaGroup mg = new MetaGroup(id, tr, index, type, qList);
			groupMap.put(id, mg);
			return mg;
		}

		public MetaGroup create(TextReference textref, int index,
				GroupType type, List<MetaQuestion> qList) throws SQLException {
			addDelta++;

			int id = SQLTools.maxId(groupTable.name, "id", connection)
					+ addDelta;
			MetaGroup mg = new MetaGroup(id, textref, index, type, qList);
			toBeAddedGroup.add(mg);
			groupMap.put(id, mg);

			for (int ord = 0; ord < qList.size(); ord++) {
				MetaGroupRel groupRel = new MetaGroupRel(id, qList.get(ord)
						.getId(), ord);
				toBeAddedGroupRel.add(groupRel);
			}

			return mg;
		}

		public void remove(int id) {
			MetaGroup tbr = groupMap.remove(id);
			toBeAddedGroup.remove(tbr);
			toBeRemoved.put(id, tbr);
		}

		public void saveAll() throws SQLException {
			PreparedStatement updateGroupStmt = connection.prepareStatement("UPDATE metagroup SET textref = ?, ord = ?, type = ? WHERE id = ?");
			PreparedStatement deleteGroupStmt = connection.prepareStatement("DELETE FROM metagroup WHERE id = ?");
			PreparedStatement insertGroupStmt = connection.prepareStatement("INSERT INTO metagroup (id, textref, ord, type) VALUES (?, ?, ?, ?)");

			PreparedStatement deleteGroupRelStmt = connection.prepareStatement("DELETE FROM metagrouprel WHERE groupid = ?");
			PreparedStatement insertGroupRelStmt = connection.prepareStatement("INSERT INTO metagrouprel (groupid, questionid, ord) VALUES (?, ?, ?)");

			int nNew = 0, nModified = 0, nRemoved = 0, nQuestion = 0;

			for (MetaGroup mg : groupMap.values()) {
				if (mg.modified) {
					nModified++;
					updateGroupStmt.setString(1, mg.textref.getRef());
					updateGroupStmt.setInt(2, mg.index);
					updateGroupStmt.setInt(3, mg.type.asInt());
					updateGroupStmt.setInt(4, mg.id);
					updateGroupStmt.executeUpdate();

					deleteGroupRelStmt.setInt(1, mg.id);
					deleteGroupRelStmt.executeUpdate();

					for (int ord = 0; ord < mg.qList.size(); ord++) {
						nQuestion++;
						insertGroupRelStmt.setInt(1, mg.id);
						insertGroupRelStmt.setString(2, mg.qList.get(ord)
								.getId());
						insertGroupRelStmt.setInt(3, ord);
						insertGroupRelStmt.addBatch();
					}
					insertGroupRelStmt.executeBatch();

					mg.modified = false;
				}
			}

			for (MetaGroup mg : toBeRemoved.values()) {
				nRemoved++;
				deleteGroupStmt.setInt(1, mg.id);
				deleteGroupStmt.addBatch();
				deleteGroupRelStmt.setInt(1, mg.id);
				deleteGroupRelStmt.addBatch();
			}
			deleteGroupStmt.executeBatch();
			deleteGroupRelStmt.executeBatch();
			toBeRemoved.clear();

			for (MetaGroup mg : toBeAddedGroup) {
				nNew++;
				insertGroupStmt.setInt(1, mg.id);
				insertGroupStmt.setString(2, mg.textref.getRef());
				insertGroupStmt.setInt(3, mg.index);
				insertGroupStmt.setInt(4, mg.type.asInt());
				insertGroupStmt.addBatch();
			}
			insertGroupStmt.executeBatch();
			toBeAddedGroup.clear();
			addDelta = 0;

			for (MetaGroupRel mgr : toBeAddedGroupRel) {
				nQuestion++;
				insertGroupRelStmt.setInt(1, mgr.groupid);
				insertGroupRelStmt.setString(2, mgr.questionid);
				insertGroupRelStmt.setInt(3, mgr.ord);
				insertGroupRelStmt.addBatch();
			}
			insertGroupRelStmt.executeBatch();
			toBeAddedGroupRel.clear();

			if (nModified != 0 || nNew != 0 || nRemoved != 0 || nQuestion != 0) {
				String logString = String
						.format("MetaGroup: Saved %d (%d new), %d removed, %d questions position changed",
								nModified+nNew, nNew, nRemoved, nQuestion);
				Logger.getGlobal().log(Level.INFO, logString);
			}
		}

		public List<MetaGroup> getAll() throws SQLException, DaxploreException {
			// make sure all groups are cached before returning the content of
			// the map
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT id FROM metagroup");
			while (rs.next()) {
				int id = rs.getInt("id");
				if (!groupMap.containsKey(id) && !toBeRemoved.containsKey(id)) {
					get(id);
				}
			}
			List<MetaGroup> groupList = new LinkedList<MetaGroup>(
					groupMap.values());
			Collections.sort(groupList);
			return groupList;
		}
		
		public List<MetaGroup> getQuestionGroups() throws SQLException, DaxploreException {
			// make sure all groups are cached before returning the content of
			// the map
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT id FROM metagroup WHERE type = " + GroupType.QUESTIONS.type);
			List<MetaGroup> groupList = new LinkedList<MetaGroup>();
			while (rs.next()) {
				int id = rs.getInt("id");
				if (!toBeRemoved.containsKey(id)) {
					groupList.add(get(id));
				}
			}
			Collections.sort(groupList);
			return groupList;
		}
		
		public MetaGroup getPerspectiveGroup() throws SQLException, DaxploreException {
			ResultSet rs = connection.createStatement().executeQuery("SELECT id FROM metagroup WHERE type = " + GroupType.PERSPECTIVE.type);
			if(rs.next()) {
				return get(rs.getInt("id"));
			} else {
				return create(textsManager.get("PERSPECTIVE"), 999, GroupType.PERSPECTIVE, new LinkedList<MetaQuestion>()); //TODO 999 //TODO use in GUI
			}
		}
		
		public int getHighestId() throws SQLException {
			return SQLTools.maxId(groupTable.name, "id", connection) + addDelta;
		}
		
		public JsonElement getQuestionGroupsJSON(Locale locale) throws SQLException, DaxploreException {
			JsonArray json = new JsonArray(); 
			for(MetaGroup group : getQuestionGroups()) {
				json.add(group.toJSONObject(locale));
			}
			return json;
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
			switch (i) {
			case 0:
				return QUESTIONS;
			case 1:
				return PERSPECTIVE;
			}
			return null;
		}
	}

	private static class MetaGroupRel {
		int groupid, ord;
		String questionid;

		public MetaGroupRel(int groupid, String questionid, int ord) {
			this.groupid = groupid;
			this.questionid = questionid;
			this.ord = ord;
		}
	}

	protected int id, index;
	protected List<MetaQuestion> qList;
	protected GroupType type;
	protected TextReference textref;

	protected boolean modified = false;

	protected MetaGroup(int id, TextReference textref, int index,
			GroupType type, List<MetaQuestion> qList) {
		this.id = id;
		this.index = index;
		this.qList = qList;
		this.type = type;
		this.textref = textref;
	}

	public int getId() {
		return id;
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
		return index > o.index ? 1 : -1;
	}

	public int getQuestionCount() {
		return qList.size();
	}
	
	public JsonElement toJSONObject(Locale locale) {
		JsonArray questions = new JsonArray();
		for(MetaQuestion q : qList) {
			questions.add(new JsonPrimitive(q.getId()));
		}
		switch(type) {
		case PERSPECTIVE:
			return questions;
		case QUESTIONS:
			JsonObject json = new JsonObject();
			json.add("name", new JsonPrimitive(textref.get(locale)));
			json.add("questions", questions);
			return json;
		default:
			throw new AssertionError("Non-existant group type");	
		}
			
	}
}
