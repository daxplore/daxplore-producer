/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
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
import org.daxplore.producer.daxplorelib.metadata.MetaScale.Option;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReferenceManager;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class MetaGroup implements Comparable<MetaGroup> {
	private static final DaxploreTable groupTable = new DaxploreTable(
			"CREATE TABLE metagroup ("
			+ "id INTEGER PRIMARY KEY, "
			+ "textref TEXT NOT NULL, "
			+ "ord INTEGER NOT NULL, "
			+ "type INTEGER NOT NULL)",
			"metagroup");
	private static final DaxploreTable groupRelTable = new DaxploreTable(
			"CREATE TABLE metagrouprel ("
			+ "questionid INTEGER NOT NULL, "
			+ "groupid INTEGER NOT NULL, "
			+ "ord INTEGER NOT NULL, "
			+ "FOREIGN KEY(questionid) REFERENCES metaquestion(id), "
			+ "FOREIGN KEY(groupid) REFERENCES metagroup(id))",
			"metagrouprel");

	public static class MetaGroupManager {

		private Map<Integer, MetaGroup> groupMap = new HashMap<>();
		private List<MetaGroup> toBeAddedGroup = new LinkedList<>();
		private int addDelta = 0;
		private List<MetaGroupRel> toBeAddedGroupRel = new LinkedList<>();
		private Map<Integer, MetaGroup> toBeRemoved = new HashMap<>();
		private Connection connection;
		private TextReferenceManager textsManager;
		private MetaQuestionManager questionManager;

		public MetaGroupManager(Connection connection, TextReferenceManager textsManager,
				MetaQuestionManager questionManager) throws SQLException {
			this.connection = connection;
			this.textsManager = textsManager;
			this.questionManager = questionManager;

			SQLTools.createIfNotExists(groupTable, connection);
			SQLTools.createIfNotExists(groupRelTable, connection);
		}

		public MetaGroup get(int id) throws SQLException, DaxploreException {
			if (groupMap.containsKey(id)) {
				return groupMap.get(id);
			} else if (toBeRemoved.containsKey(id)) {
				throw new DaxploreException("No group with id '" + id + "'");
			}

			TextReference tr;
			int index;
			GroupType type;
			try (PreparedStatement stmt = connection
					.prepareStatement("SELECT * FROM metagroup WHERE id = ?")) {
				stmt.setInt(1, id);
				try (ResultSet rs = stmt.executeQuery()) {
					if (rs.next()) {
						tr = textsManager.get(rs.getString("textref"));
						index = rs.getInt("ord");
						type = GroupType.fromInt(rs.getInt("type"));
					} else {
						throw new DaxploreException("No group with id '" + id + "'");
					}
		
				}
			}

			List<MetaQuestion> qList = new LinkedList<>();
			try (PreparedStatement qstmt = connection
					.prepareStatement("SELECT questionid FROM metagrouprel WHERE groupid = ? ORDER BY ord ASC")) {
				qstmt.setInt(1, id);
				try (ResultSet rs = qstmt.executeQuery()) {
					while (rs.next()) {
						qList.add(questionManager.get(rs.getInt("questionid")));
					}
				}
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
			int nNew = 0, nModified = 0, nRemoved = 0, nQuestion = 0;
			try (
				PreparedStatement updateGroupStmt = connection.prepareStatement("UPDATE metagroup SET textref = ?, ord = ?, type = ? WHERE id = ?");
				PreparedStatement deleteGroupStmt = connection.prepareStatement("DELETE FROM metagroup WHERE id = ?");
				PreparedStatement insertGroupStmt = connection.prepareStatement("INSERT INTO metagroup (id, textref, ord, type) VALUES (?, ?, ?, ?)");

				PreparedStatement deleteGroupRelStmt = connection.prepareStatement("DELETE FROM metagrouprel WHERE groupid = ?");
				PreparedStatement insertGroupRelStmt = connection.prepareStatement("INSERT INTO metagrouprel (groupid, questionid, ord) VALUES (?, ?, ?)");
			) {

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
							insertGroupRelStmt.setInt(2, mg.qList.get(ord)
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
					insertGroupRelStmt.setInt(2, mgr.questionid);
					insertGroupRelStmt.setInt(3, mgr.ord);
					insertGroupRelStmt.addBatch();
				}
				insertGroupRelStmt.executeBatch();
				toBeAddedGroupRel.clear();
			}

			if (nModified != 0 || nNew != 0 || nRemoved != 0 || nQuestion != 0) {
				String logString = String
						.format("MetaGroup: Saved %d (%d new), %d removed, %d questions position changed",
								nModified+nNew, nNew, nRemoved, nQuestion);
				Logger.getGlobal().log(Level.INFO, logString);
			}
		}
		
		public int getUnsavedChangesCount() {
			int nModified = 0;
			int perchanged = 0;
			for (MetaGroup mg : groupMap.values()) {
				if (mg.modified) {
					nModified++;
				}
			}
			try {
				perchanged = getPerspectiveGroup().modified? 1: 0;
			} catch (DaxploreException e) {
				//TODO: autogenerated catch clause
				e.printStackTrace();
			}
			return nModified + toBeAddedGroup.size() + toBeAddedGroupRel.size() + toBeRemoved.size() + perchanged;
		}

		public List<MetaGroup> getAll() throws DaxploreException {
			// make sure all groups are cached before returning the content of
			// the map
			try (Statement stmt = connection.createStatement();
					ResultSet rs = stmt.executeQuery("SELECT id FROM metagroup")) {
				while (rs.next()) {
					int id = rs.getInt("id");
					if (!groupMap.containsKey(id) && !toBeRemoved.containsKey(id)) {
						get(id);
					}
				}
			} catch (SQLException e) {
				throw new DaxploreException("Failed to load the groups", e);
			}
			List<MetaGroup> groupList = new LinkedList<>(groupMap.values());
			Collections.sort(groupList);
			return groupList;
		}
		
		public List<MetaGroup> getQuestionGroups() throws DaxploreException {
			// make sure all groups are cached before returning the content of
			// the map
			List<MetaGroup> groupList = new LinkedList<>();
			try (Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT id FROM metagroup WHERE type = " + GroupType.QUESTIONS.type)) {
				while (rs.next()) {
					int id = rs.getInt("id");
					if (!toBeRemoved.containsKey(id)) {
						groupList.add(get(id));
					}
				}
			} catch (SQLException e) {
				throw new DaxploreException("Exception while loading questions", e);
			}
			Collections.sort(groupList);
			return groupList;
		}
		
		public MetaGroup getPerspectiveGroup() throws DaxploreException {
			try (Statement stmt =connection.createStatement();
					ResultSet rs = stmt.executeQuery("SELECT id FROM metagroup WHERE type = " + GroupType.PERSPECTIVE.type)) {
				if(rs.next()) {
					return get(rs.getInt("id"));
				}
				return create(textsManager.get("PERSPECTIVE"), 999, GroupType.PERSPECTIVE, new LinkedList<MetaQuestion>()); //TODO 999
			} catch (SQLException e) {
				throw new DaxploreException("Failed to load perspectives", e);
			}
		}
		
		public boolean inPerspectives(MetaQuestion metaQuestion) throws DaxploreException {
			return getPerspectiveGroup().contains(metaQuestion);
		}
		
		public int getHighestId() throws SQLException {
			return SQLTools.maxId(groupTable.name, "id", connection) + addDelta;
		}
		
		public JsonElement getQuestionGroupsJSON(Locale locale) throws DaxploreException {
			JsonArray json = new JsonArray(); 
			for(MetaGroup group : getQuestionGroups()) {
				json.add(group.toJSONObject(locale));
			}
			return json;
		}
		
		public List<TextReference> getEmptyTextRefs(Locale locale) throws DaxploreException {
			List<TextReference> emptyRefs = new LinkedList<TextReference>();
			for(MetaGroup group : getQuestionGroups()) {
				if(group.type == GroupType.QUESTIONS && Strings.isNullOrEmpty(group.textref.get(locale))) {
					emptyRefs.add(group.textref);
				}
			}
			return emptyRefs;
		}
		
		public void discardChanges() {
			groupMap.clear();
			toBeAddedGroup.clear();
			toBeAddedGroupRel.clear();
			toBeRemoved.clear();
			addDelta = 0;
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
			default:
				throw new IllegalArgumentException("No group type with that type id: '" + i + "'");
			}
		}
	}

	private static class MetaGroupRel {
		int groupid, ord, questionid;

		public MetaGroupRel(int groupid, int questionid, int ord) {
			this.groupid = groupid;
			this.questionid = questionid;
			this.ord = ord;
		}
	}

	private int id, index;
	private List<MetaQuestion> qList;
	private GroupType type;
	private TextReference textref;

	private boolean modified = false;

	private MetaGroup(int id, TextReference textref, int index,
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
	
	public MetaQuestion getQuestion(int atIndex) {
		return qList.get(atIndex);
	}
	
	public void addQuestion(MetaQuestion mq, int atIndex) {
		if(qList.contains(mq)) {
			throw new IllegalArgumentException("Question already added to Group");
		}
		qList.add(atIndex, mq);
		modified = true;
	}

	public void addQuestion(MetaQuestion mq) {
		if(qList.contains(mq)) {
			throw new IllegalArgumentException("Question already added to Group");
		}
		qList.add(mq);
		modified = true;
	}

	public void removeQuestion(MetaQuestion mq) {
		if(qList.remove(mq)) {
			modified = true;
		}
	}
	
	public boolean contains(MetaQuestion mq) {
		return qList.contains(mq);
	}

	public TextReference getTextRef() {
		return textref;
	}

	public void setTextRef(TextReference textref) {
		if(!textref.equals(this.textref)) {
			this.textref = textref;
			modified = true;
		}
	}

	public GroupType getType() {
		return type;
	}

	public void resetQuestions() {
		if(!qList.isEmpty()) {
			qList = new LinkedList<>();
			modified = true;
		}
	}

	public List<MetaQuestion> getQuestions() {
		return Lists.newLinkedList(qList);
	}

	public void setQuestions(List<MetaQuestion> questionList) {
		if(!questionList.equals(qList)) {
			qList = questionList;
			modified = true;
		}
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		if(this.index != index) {
			this.index = index;
			modified = true;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(MetaGroup o) {
		if(index == o.index) {
			return 0;
		}
		return index > o.index ? 1 : -1;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass() || id != ((MetaGroup)obj).id) {
			return false;
		}
		return true;
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
			json.add("name", new JsonPrimitive(textref.getWithPlaceholder(locale)));
			json.add("questions", questions);
			return json;
		default:
			throw new AssertionError("Non-existant group type");	
		}
			
	}
}
