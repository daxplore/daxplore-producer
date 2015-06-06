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
import java.sql.Types;
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
import org.daxplore.producer.daxplorelib.metadata.MetaScale.MetaScaleManager;
import org.daxplore.producer.daxplorelib.metadata.MetaScale.Option;
import org.daxplore.producer.daxplorelib.metadata.MetaTimepointShort.MetaTimepointShortManager;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReferenceManager;

import com.beust.jcommander.internal.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class MetaQuestion {
	
	private static final DaxploreTable table = new DaxploreTable(
			"CREATE TABLE metaquestion (id TEXT PRIMARY KEY, scaleid INTEGER, fulltextref TEXT NOT NULL, shorttextref TEXT NOT NULL, extratextref TEXT, FOREIGN KEY(scaleid) REFERENCES metascale(id))",
			"metaquestion");
	private static final DaxploreTable timePointTable = new DaxploreTable(
			"CREATE TABLE questtimerel (qid TEXT NOT NULL, timeid INTEGER NOT NULL, FOREIGN KEY(qid) REFERENCES metaquestion(id), FOREIGN KEY(timeid) REFERENCES timepoints(id))", 
			"questtimerel");
	
	public static class MetaQuestionManager {
		
		private Connection connection;
		private MetaScaleManager metascaleManager;
		private TextReferenceManager textsManager;
		private MetaTimepointShortManager timePointManager;
		private Map<String, MetaQuestion> questionMap = new HashMap<>();
		private LinkedList<MetaQuestion> toBeAdded= new LinkedList<>();
		private Map<String, MetaQuestion> toBeRemoved = new HashMap<>();
		
		public MetaQuestionManager(Connection connection, TextReferenceManager textsManager,
				MetaScaleManager metaScaleManager, MetaTimepointShortManager timePointManager) throws SQLException {
			this.connection = connection;
			this.metascaleManager = metaScaleManager;
			this.textsManager = textsManager;
			this.timePointManager = timePointManager;

			if(!SQLTools.tableExists(table.name, connection)) {
				try(Statement stmt = connection.createStatement()) {
					stmt.executeUpdate(table.sql);
				}
			}
			if(!SQLTools.tableExists(timePointTable.name, connection)) {
				try(Statement stmt = connection.createStatement()) {
					stmt.executeUpdate(timePointTable.sql);
				}
			}
		}
		
		public MetaQuestion get(String id) throws SQLException, DaxploreException {
			if(questionMap.containsKey(id)) {
				return questionMap.get(id);
			} else if(toBeRemoved.containsKey(id)) {
				throw new DaxploreException("No question with id '"+id+"'");
			}
			
			TextReference fullTextRef, shortTextRef, extraTextRef;
			MetaScale scale = null;
			try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM metaquestion WHERE id = ?")) {
				stmt.setString(1, id);
				try(ResultSet rs = stmt.executeQuery()) {
					if(!rs.next()) {
						throw new IllegalArgumentException("QuestionID does not exist?");
					}
					fullTextRef = textsManager.get(rs.getString("fulltextref"));
					shortTextRef = textsManager.get(rs.getString("shorttextref"));
					String extraTextRefId = rs.getString("extratextref");
					if(!rs.wasNull()) {
						extraTextRef = textsManager.get(extraTextRefId);
					} else {
						extraTextRef = null;
					}
					
					int scaleid = rs.getInt("scaleid");
					if(!rs.wasNull()) {
						scale = metascaleManager.get(scaleid);
					}
				}
			}
					
			List<MetaTimepointShort> timepoints = new LinkedList<>();
			try(PreparedStatement stmt2 = connection.prepareStatement("SELECT timeid FROM questtimerel WHERE qid = ?")) {
				stmt2.setString(1, id);
				try(ResultSet rs2 = stmt2.executeQuery()) {
					while(rs2.next()) {
						timepoints.add(timePointManager.get(rs2.getInt("timeid")));
					}
					
				}
			}
			MetaQuestion mq = new MetaQuestion(id, shortTextRef, fullTextRef, extraTextRef, scale, timepoints);
			questionMap.put(id, mq);
			return mq;
		}
		
		public MetaQuestion create(String id, TextReference shortTextRef, TextReference fullTextRef, TextReference extraTextRef, MetaScale scale, List<MetaTimepointShort> timepoints) {
			MetaQuestion mq = new MetaQuestion(id, shortTextRef, fullTextRef, extraTextRef, scale, timepoints);
			toBeAdded.add(mq);
			questionMap.put(id, mq);
			return mq;
		}
		
		public void remove(String id) {
			MetaQuestion mq = questionMap.remove(id);
			toBeAdded.remove(mq);
			toBeRemoved.put(id, mq);
		}
		
		public void saveAll() throws SQLException {
			int nNew = 0, nModified = 0, nRemoved = 0, nTimePoint = 0;
			try (
			PreparedStatement updateStmt = connection.prepareStatement("UPDATE metaquestion SET scaleid = ?, fulltextref = ?, shorttextref = ?, extratextref = ? WHERE id = ?");
			PreparedStatement insertStmt = connection.prepareStatement("INSERT INTO metaquestion (id, scaleid, fulltextref, shorttextref, extratextref) VALUES (?, ?, ?, ? , ?)");
			PreparedStatement deleteStmt = connection.prepareStatement("DELETE FROM metaquestion WHERE id = ?");
			PreparedStatement deleteRelStmt = connection.prepareStatement("DELETE FROM questtimerel WHERE qid = ?");
			PreparedStatement insertRelStmt = connection.prepareStatement("INSERT INTO questtimerel (qid, timeid) VALUES (?, ?)");
			) {
			
				for(MetaQuestion mq: questionMap.values()) {
					if(mq.modified) {
						nModified++;
						updateStmt.setInt(1, mq.scale.getId());
						updateStmt.setString(2, mq.fullTextRef.getRef());
						updateStmt.setString(3, mq.shortTextRef.getRef());
						if(mq.extraTextRef != null) {
							updateStmt.setString(4, mq.extraTextRef.getRef());
						} else {
							updateStmt.setNull(4, Types.VARCHAR);
						}
						updateStmt.setString(5, mq.id);
						updateStmt.executeUpdate();
						mq.modified = false;
					}
					if(mq.timemodified) {
						deleteRelStmt.setString(1, mq.id);
						deleteRelStmt.executeUpdate();
						
						for(MetaTimepointShort timepoint: mq.timepoints) {
							nTimePoint++;
							insertRelStmt.setString(1, mq.id);
							insertRelStmt.setInt(2, timepoint.getId());
	//						insertRelStmt.addBatch();
							insertRelStmt.executeUpdate();
						}
	//					insertRelStmt.executeBatch();
						
						mq.timemodified = false;
					}
				}
				//TODO: add new ones before changing the modified ones and set modified flag to false??
				for(MetaQuestion mq: toBeAdded) {
					nNew++;
					insertStmt.setString(1, mq.id);
					if(mq.scale != null) {
						insertStmt.setInt(2, mq.scale.getId());
					} else {
						insertStmt.setNull(2, Types.INTEGER);
					}
					insertStmt.setString(3, mq.fullTextRef.getRef());
					insertStmt.setString(4, mq.shortTextRef.getRef());
					if(mq.extraTextRef != null) {
						insertStmt.setString(5, mq.extraTextRef.getRef());
					} else {
						insertStmt.setNull(5, Types.VARCHAR);
					}
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
				
				
				for(MetaQuestion mq: toBeRemoved.values()) {
					nRemoved++;
					deleteStmt.setString(1, mq.id);
					deleteStmt.addBatch();
					
					deleteRelStmt.setString(1, mq.id);
					deleteRelStmt.addBatch();
				}
				deleteRelStmt.executeBatch();
				deleteStmt.executeBatch();
				toBeRemoved.clear();
			}
			
			if(nModified != 0 || nNew != 0 || nRemoved != 0 || nTimePoint != 0) {
				String logString = String.format("MetaQuestion: Saved %d (%d new), %d removed, %d timepoints changed", nModified+nNew, nNew, nRemoved, nTimePoint);
				Logger.getGlobal().log(Level.INFO, logString);
			}
		}
		
		public int getUnsavedChangesCount() {
			int nModified = 0, nTimePoint = 0;
			
			for(MetaQuestion mq: questionMap.values()) {
				if(mq.modified) {
					nModified++;
				}
				if(mq.timemodified) {
					nTimePoint++;
				}
			}
			
			return toBeAdded.size() + toBeRemoved.size() + nModified + nTimePoint;
		}
		
		public List<MetaQuestion> getAll() throws DaxploreException {
			// make sure all questions are cached before returning the content of the map
			try (Statement stmt = connection.createStatement();
					ResultSet rs = stmt.executeQuery("SELECT id FROM metaquestion")) {
				while(rs.next()) {
					String id = rs.getString("id");
					if(!questionMap.containsKey(id) && !toBeRemoved.containsKey(id)) {
						get(id);
					}
				}
			} catch (SQLException e) {
				throw new DaxploreException("Failed to load questions", e);
			}
			return new LinkedList<>(questionMap.values());
		}
		
		public void discardChanges() {
			questionMap.clear();
			toBeAdded.clear();
			toBeRemoved.clear();
		}
	}
	
	private String id;
	private TextReference shortTextRef, fullTextRef, extraTextRef;
	private MetaScale scale;
	private List<MetaTimepointShort> timepoints;
	
	private boolean modified = false;
	private boolean timemodified = false;
	
	private MetaQuestion(String id, TextReference shortTextRef, TextReference fullTextRef, TextReference extraTextRef, MetaScale scale, List<MetaTimepointShort> timepoints) {
		this.id = id;
		this.shortTextRef = shortTextRef;
		this.fullTextRef = fullTextRef;
		this.extraTextRef = extraTextRef;
		this.scale = scale;
		this.timepoints = timepoints;
	}
	
	public String getId() {
		return id;
	}
	
	public TextReference getShortTextRef() {
		return shortTextRef;
	}
	
	public void setShortTextRef(TextReference shortTextRef) {
		if(!shortTextRef.equals(this.shortTextRef)) {
			this.shortTextRef = shortTextRef;
			modified = true;
		}
	}
	
	public TextReference getFullTextRef() {
		return fullTextRef;
	}
	
	public void setFullTextRef(TextReference fullTextRef) {
		if(!fullTextRef.equals(this.fullTextRef)) {
			this.fullTextRef = fullTextRef;
			modified = true;
		}
	}
	
	public TextReference getExtraTextRef() {
		return extraTextRef;
	}
	
	public void setExtraTextRef(TextReference extraTextRef) {
		if(!extraTextRef.equals(this.extraTextRef)) {
			this.extraTextRef = extraTextRef;
			modified = true;
		}
	}
	
	public MetaScale getScale() {
		return scale;
	}
	
	public void setScale(MetaScale scale) {
		if(!scale.equals(this.scale)) {
			this.scale = scale;
			modified = true;
		}
	}

	public List<MetaTimepointShort> getTimepoints() {
		Collections.sort(timepoints);
		return Lists.newLinkedList(timepoints);
	}
	
	public void setTimepoints(List<MetaTimepointShort> timepoints) {
		if(!timepoints.equals(this.timepoints)) {
			this.timepoints = timepoints;
			timemodified = true;
		}
	}
	
	public JsonElement toJSONObject(Locale locale) {
		
		JsonObject json = new JsonObject();
		json.addProperty("column", id);
		json.addProperty("short", shortTextRef.get(locale));
		json.addProperty("text", fullTextRef.get(locale));
		
		JsonArray options = new JsonArray();
		for(Option option : scale.getOptions()) {
			String text = option.getTextRef().get(locale);
			if(text!=null) {
				options.add(new JsonPrimitive(text));
			} else {
				options.add(new JsonPrimitive(""));
			}
		}
		json.add("options", options);
		
		JsonArray tps = new JsonArray();
		for(MetaTimepointShort tp : getTimepoints()) {
			tps.add(new JsonPrimitive(tp.getTimeindex()));
		}
		json.add("timepoints", tps);

		return json;
	}
}
