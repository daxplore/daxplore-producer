/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.daxplorelib.metadata;

import java.math.BigDecimal;
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
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.DaxploreTable;
import org.daxplore.producer.daxplorelib.SQLTools;
import org.daxplore.producer.daxplorelib.metadata.MetaMean.Direction;
import org.daxplore.producer.daxplorelib.metadata.MetaMean.MetaMeanManager;
import org.daxplore.producer.daxplorelib.metadata.MetaScale.MetaScaleManager;
import org.daxplore.producer.daxplorelib.metadata.MetaScale.Option;
import org.daxplore.producer.daxplorelib.metadata.MetaTimepointShort.MetaTimepointShortManager;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReferenceManager;
import org.daxplore.producer.daxplorelib.raw.VariableType;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class MetaQuestion {
	
	private static final DaxploreTable table = new DaxploreTable(
			"CREATE TABLE metaquestion ("
			+ "id INTEGER PRIMARY KEY, "
			+ "col TEXT NOT NULL, "
			+ "type STRING NOT NULL, "
			+ "displaytypes STRING NOT NULL)",
			"metaquestion");
	
	private static final DaxploreTable timePointTable = new DaxploreTable(
			"CREATE TABLE questtimerel ("
			+ "qid INTEGER NOT NULL, "
			+ "timeid INTEGER NOT NULL, "
			+ "FOREIGN KEY(qid) REFERENCES metaquestion(id), "
			+ "FOREIGN KEY(timeid) REFERENCES timepoints(id))", 
			"questtimerel");
	
	private enum DisplayTypes {
		FREQ, DICH, MEAN
	}
	
	static final String textrefSuffixFullText= "_fulltext";
	static final String textrefSuffixShortText= "_shorttext";
	static final String textrefSuffixDescriptionText = "_descriptiontext";
	static final String textrefSuffixTitleMatchRegex = "_titlematchregex";
	
	public static class MetaQuestionManager {
		
		private Connection connection;
		private MetaScaleManager metascaleManager;
		private TextReferenceManager textsManager;
		private MetaMeanManager metaMeanManager;
		private MetaTimepointShortManager timePointManager;
		
		private Map<Integer, MetaQuestion> questionMap = new HashMap<>();
		private LinkedList<MetaQuestion> toBeAdded= new LinkedList<>();
		private Map<Integer, MetaQuestion> toBeRemoved = new HashMap<>();
		private int addDelta = 0;
		
		public MetaQuestionManager(Connection connection, TextReferenceManager textsManager,
				MetaScaleManager metaScaleManager, MetaMeanManager metaMeanManager, MetaTimepointShortManager timePointManager) throws SQLException {
			this.connection = connection;
			this.metascaleManager = metaScaleManager;
			this.textsManager = textsManager;
			this.metaMeanManager = metaMeanManager;
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
		
		public MetaQuestion get(int id) throws SQLException, DaxploreException {
			if(questionMap.containsKey(id)) {
				return questionMap.get(id);
			} else if(toBeRemoved.containsKey(id)) {
				throw new DaxploreException("No metaquestion with id '"+id+"'");
			}
			
			String column;
			VariableType type;
			TextReference fullTextRef, shortTextRef, descriptionTextRef, titleMatchRegexTextRef;
			MetaScale<?> scale = null;
			boolean useFrequencies, useDich, useMean;
			try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM metaquestion WHERE id = ?")) {
				stmt.setInt(1, id);
				try(ResultSet rs = stmt.executeQuery()) {
					if(!rs.next()) {
						throw new DaxploreException("MetaQuestion '" + id + "' does not exist?");
					}
					column = rs.getString("col");
					type = VariableType.valueOf(rs.getString("type")); //TODO check exception
										
					String displayTypesJson = rs.getString("displaytypes");
					Gson gson = new Gson();
					Set<String> displayTypes = Sets.newHashSet(gson.fromJson(displayTypesJson, String[].class));
					useFrequencies = displayTypes.contains(DisplayTypes.FREQ.name());
					useDich = displayTypes.contains(DisplayTypes.DICH.name());
					useMean = displayTypes.contains(DisplayTypes.MEAN.name());
					
					scale = metascaleManager.get(id, type);
				}
			}
			
			fullTextRef = textsManager.get(column + textrefSuffixFullText);
			shortTextRef = textsManager.get(column + textrefSuffixShortText);
			descriptionTextRef = textsManager.get(column + textrefSuffixDescriptionText);
			titleMatchRegexTextRef = textsManager.get(column + textrefSuffixTitleMatchRegex);
						
			MetaMean metaMean = metaMeanManager.get(id);
			List<MetaTimepointShort> timepoints = new LinkedList<>();
			try(PreparedStatement stmt2 = connection.prepareStatement("SELECT timeid FROM questtimerel WHERE qid = ?")) {
				stmt2.setInt(1, id);
				try(ResultSet rs2 = stmt2.executeQuery()) {
					while(rs2.next()) {
						timepoints.add(timePointManager.get(rs2.getInt("timeid")));
					}
					
				}
			}
			MetaQuestion mq = new MetaQuestion(id, column, type, shortTextRef, fullTextRef, descriptionTextRef,
					titleMatchRegexTextRef, scale, useFrequencies, useDich, metaMean, useMean, timepoints);
			questionMap.put(id, mq);
			return mq;
		}
		
		@SuppressWarnings("unchecked")
		public MetaQuestion create(String column, VariableType type, TextReference shortTextRef, TextReference fullTextRef, TextReference descriptionTextRef, TextReference titleMatchRegexTextRef,
				List<MetaScale.Option<?>> scaleOptions, Set<Double> meanExcludedValues, double globalMean, List<MetaTimepointShort> timepoints) throws DaxploreException {
			boolean useMean = (meanExcludedValues != null);
			
			addDelta++;

			int id;
			try {
				id = SQLTools.maxId(table.name, "id", connection) + addDelta;
			} catch (SQLException e) {
				throw new DaxploreException("Failed to get max id from metaquestion");
			}
			
			MetaScale<?> scale = null;
			switch(type) {
			case NUMERIC:
				List<Option<Double>> scaleOptionsDouble = new LinkedList<MetaScale.Option<Double>>();
				for(Option<?> o : scaleOptions) {
					scaleOptionsDouble.add((Option<Double>)o);
				}
				scale = metascaleManager.createDouble(id, scaleOptionsDouble);
				break;
			case TEXT:
				List<Option<String>> scaleOptionsString = new LinkedList<MetaScale.Option<String>>();
				for(Option<?> o : scaleOptions) {
					scaleOptionsString.add((Option<String>)o);
				}
				scale = metascaleManager.createString(id, scaleOptionsString);
				break;
			}
			
			MetaMean metaMean = metaMeanManager.create(id, meanExcludedValues, false, globalMean, Direction.UNDEFINED);
			
			MetaQuestion mq = new MetaQuestion(id, column, type, shortTextRef, fullTextRef, descriptionTextRef, titleMatchRegexTextRef, scale, true, false, metaMean, useMean, timepoints);
			toBeAdded.add(mq);
			questionMap.put(id, mq);
			return mq;
		}
		
		public void remove(int id) {
			MetaQuestion mq = questionMap.remove(id);
			toBeAdded.remove(mq);
			toBeRemoved.put(id, mq);
		}
		
		public void saveAll() throws SQLException {
			int nNew = 0, nModified = 0, nRemoved = 0, nTimePoint = 0;
			try (
			PreparedStatement updateStmt = connection.prepareStatement("UPDATE metaquestion SET col = ?, type = ?, displaytypes = ? WHERE id = ?");
			PreparedStatement insertStmt = connection.prepareStatement("INSERT INTO metaquestion (id, col, type, displaytypes) VALUES (?, ?, ?, ?)");
			PreparedStatement deleteStmt = connection.prepareStatement("DELETE FROM metaquestion WHERE id = ?");
			PreparedStatement deleteRelStmt = connection.prepareStatement("DELETE FROM questtimerel WHERE qid = ?");
			PreparedStatement insertRelStmt = connection.prepareStatement("INSERT INTO questtimerel (qid, timeid) VALUES (?, ?)");
			) {
				for(MetaQuestion mq: questionMap.values()) {
					if(mq.modified) {
						nModified++;
						updateStmt.setString(1, mq.column);
						updateStmt.setString(2, mq.type.name());
						String displayTypes = mq.displayTypesAsJSON().toString();
						if (displayTypes != null) {
							updateStmt.setString(3, displayTypes);
						} else {
							updateStmt.setString(3, "");
						}
						updateStmt.setInt(4, mq.id);
						updateStmt.executeUpdate();
						mq.modified = false;
					}
					
					if(mq.timemodified) {
						deleteRelStmt.setInt(1, mq.id);
						deleteRelStmt.executeUpdate();
						
						for(MetaTimepointShort timepoint: mq.timepoints) {
							nTimePoint++;
							insertRelStmt.setInt(1, mq.id);
							insertRelStmt.setInt(2, timepoint.getId());
	//						insertRelStmt.addBatch();
							insertRelStmt.executeUpdate();
						}
	//					insertRelStmt.executeBatch();
						
						mq.timemodified = false;
					}
				}
				//TODO: add new ones before changing the modified ones and set modified flag to false??
				for(MetaQuestion mq : toBeAdded) {
					nNew++;
					insertStmt.setInt(1, mq.id);
					insertStmt.setString(2, mq.column);
					insertStmt.setString(3, mq.type.name());
					
					String displayTypes = mq.displayTypesAsJSON().toString();
					if (displayTypes != null) {
						insertStmt.setString(4, displayTypes);
					} else {
						insertStmt.setString(4, "");
					}

					insertStmt.addBatch();
					
					for(MetaTimepointShort timepoint: mq.timepoints) {
						insertRelStmt.setInt(1, mq.id);
						insertRelStmt.setInt(2, timepoint.getId());
						insertRelStmt.addBatch();
					}
					insertRelStmt.executeBatch();
				}
				insertStmt.executeBatch();
				toBeAdded.clear();
				addDelta = 0;
				
				for(MetaQuestion mq: toBeRemoved.values()) {
					nRemoved++;
					deleteStmt.setInt(1, mq.id);
					deleteStmt.addBatch();
					
					deleteRelStmt.setInt(1, mq.id);
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
					int id = rs.getInt("id");
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
			addDelta = 0;
		}
	}
	
	private int id;
	private String column;
	private VariableType type;
	private TextReference shortTextRef, fullTextRef, descriptionTextRef, titleMatchRegexTextRef;
	private boolean useFrequencies, useDichotomizedLine, useMean;
	private MetaScale<?> scale;
	private MetaMean metaMean;
	private List<MetaTimepointShort> timepoints;
	
	private boolean modified = false;
	private boolean timemodified = false;
	
	private MetaQuestion(int id, String column, VariableType type, TextReference shortTextRef, TextReference fullTextRef,
			TextReference descriptionTextRef, TextReference titleMatchRegexTextRef, MetaScale<?> scale, boolean useFrequencies, boolean useDichotomizedLined,
			MetaMean metaMean, boolean useMean, List<MetaTimepointShort> timepoints) throws DaxploreException {
		this.id = id;
		this.column = column;
		this.type = type;
		this.shortTextRef = shortTextRef;
		this.fullTextRef = fullTextRef;
		this.descriptionTextRef = descriptionTextRef;
		this.titleMatchRegexTextRef = titleMatchRegexTextRef;
		this.useFrequencies = useFrequencies;
		this.useDichotomizedLine = useDichotomizedLined;
		this.metaMean = metaMean;
		this.useMean = useMean;
		this.timepoints = timepoints;

		if(scale.getType() == type) {
			this.scale = scale;
		} else {
			throw new DaxploreException("Type mismatch: this MetaQuestion can't take a MetaScale of type " + scale.getType());
		}
	}
	
	public int getId() {
		return id;
	}
	
	public String getColumn() {
		return column;
	}
	
	public VariableType getType() {
		return type;
	}
	
	public MetaScale<?> getScale() {
		return scale;
	}
	
	public void setScale(MetaScale<?> scale) throws DaxploreException {
		if(!scale.equals(this.scale)) {
			if(scale.getType() == type) {
				this.scale = scale;
				modified = true;
			} else {
				throw new DaxploreException("Type mismatch: this MetaQuestion can't take a MetaScale of type " + scale.getType());
			}
		}
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
	
	public TextReference getTitleMatchRegexTextRef() {
		return titleMatchRegexTextRef;
	}
	
	public void setTitleMatchRegexTextRef(TextReference titleMatchRegexTextRef) {
		if(!titleMatchRegexTextRef.equals(this.titleMatchRegexTextRef)) {
			this.titleMatchRegexTextRef = titleMatchRegexTextRef;
			modified = true;
		}
	}
	
	public TextReference getDescriptionTextRef() {
		return descriptionTextRef;
	}
	
	public void setDescriptionTextRef(TextReference descriptionTextRef) {
		if(!descriptionTextRef.equals(this.descriptionTextRef)) {
			this.descriptionTextRef = descriptionTextRef;
			modified = true;
		}
	}
	
	public boolean useFrequencies() {
		return useFrequencies;
	}

	public void setUseFrequencies(boolean useFrequencies) {
		if (this.useFrequencies != useFrequencies) {
			this.useFrequencies = useFrequencies;
			modified = true;
		}
	}
	
	public boolean useDichotomizedLine() {
		return useDichotomizedLine;
	}

	public void setUseDichotomizedLine(boolean useDichotomizedLine) {
		if (this.useDichotomizedLine != useDichotomizedLine) {
			this.useDichotomizedLine = useDichotomizedLine;
			modified = true;
		}
	}
	
	public boolean useMean() {
		return useMean;
	}

	public void setUseMean(boolean useMean) {
		if(metaMean != null) {
			this.useMean = useMean;
			modified = true;
		}
	}
	
	public MetaMean getMetaMean() {
		return metaMean;
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
	
	@SuppressWarnings("rawtypes")
	public JsonElement toJSONObject(Locale locale) {
		int decimalPlaces = 2; //TODO turn into producer setting, also used in BarStats
		
		JsonObject json = new JsonObject();
		json.addProperty("column", column);
		json.addProperty("short", shortTextRef.getWithPlaceholder(locale));
		if(Strings.isNullOrEmpty(fullTextRef.getText(locale))) {
			json.addProperty("text", shortTextRef.getWithPlaceholder(locale));
		} else {
			json.addProperty("text", fullTextRef.getText(locale));
		}
		
		json.addProperty("description", descriptionTextRef.getText(locale));
		
		if(!Strings.isNullOrEmpty(titleMatchRegexTextRef.getText(locale))) {
			json.addProperty("titlematchregex", titleMatchRegexTextRef.getText(locale));
		}
		
		//TODO descriptiontextref
		JsonArray options = new JsonArray();
		for(Option option : scale.getOptions()) {
			String text = option.getTextRef().getWithPlaceholder(locale);
			if(text!=null) {
				options.add(new JsonPrimitive(text));
			} else {
				options.add(new JsonPrimitive(""));
			}
		}
		json.add("options", options);
		
		JsonArray tps = new JsonArray();
		if(!getTimepoints().isEmpty()) {
			for(MetaTimepointShort tp : getTimepoints()) {
				tps.add(new JsonPrimitive(tp.getTimeindex()));
			}
		} else {
			tps.add(new JsonPrimitive(0));
		}
		json.add("timepoints", tps);
		
		json.add("displaytypes", displayTypesAsJSON());

		if (useDichotomizedLine) {
			json.add("dichselected", dichotomizedSelectedAsJSON());
		}
		
		json.add("use_mean_reference", new JsonPrimitive(metaMean.useMeanReferenceValue()));

		double globalMean = metaMean.useMeanReferenceValue() ? getMetaMean().getGlobalMean() : Double.NaN;
		if(!Double.isNaN(globalMean)) {
			json.add("mean_reference", new JsonPrimitive((new BigDecimal(globalMean)).setScale(decimalPlaces, BigDecimal.ROUND_HALF_UP)));
		}
		
		Direction goodDirection = metaMean.getGoodDirection();
		if(goodDirection != Direction.UNDEFINED) {
			json.add("gooddirection", new JsonPrimitive(goodDirection.name()));
		}
		
		return json;
	}
	
	private JsonElement displayTypesAsJSON() {
		JsonArray displayTypesJson = new JsonArray();
		if (useFrequencies) {
			displayTypesJson.add(new JsonPrimitive(DisplayTypes.FREQ.name()));
		}
		if (useDichotomizedLine) {
			displayTypesJson.add(new JsonPrimitive(DisplayTypes.DICH.name()));
		}
		if (useMean) {
			displayTypesJson.add(new JsonPrimitive(DisplayTypes.MEAN.name()));
		}
		return displayTypesJson;
	}
	
	private JsonArray dichotomizedSelectedAsJSON() {
		JsonArray displayTypesJson = new JsonArray();
		
		int index = 0;
		for (Option o : scale.getOptions()) {
			if(o.isSelectedInDichotomized()) {
				displayTypesJson.add(new JsonPrimitive(index));
			}
			index++;
		}
		
		return displayTypesJson;
	}
	
	public List<TextReference> getEmptyTextRefs(Locale locale) {
		List<TextReference> emptyRefs = new LinkedList<TextReference>();
		if(Strings.isNullOrEmpty(shortTextRef.getText(locale))) {
			emptyRefs.add(shortTextRef);
		}
		//TODO descriptiontextref
		for(Option option : scale.getOptions()) {
			if(Strings.isNullOrEmpty(option.getTextRef().getText(locale))) {
				emptyRefs.add(option.getTextRef());
			}
		}
		return emptyRefs;
	}
}
