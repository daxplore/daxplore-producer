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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.DaxploreTable;
import org.daxplore.producer.daxplorelib.SQLTools;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReferenceManager;
import org.daxplore.producer.daxplorelib.raw.VariableType;

import com.google.common.collect.Lists;
import com.google.gson.Gson;

public class MetaScale<T> {
	
	private static final DaxploreTable scaletable = new DaxploreTable(
			"CREATE TABLE metascale ("
			+ "questionid INTEGER NOT NULL, "
			+ "textref STRING NOT NULL, "
			+ "pos INTEGER NOT NULL, "
			+ "mappedvals TEXT NOT NULL, "
			+ "dichselected BOOLEAN NOT NULL, "
			+ "FOREIGN KEY(questionid) REFERENCES metaquestion(id), "
			+ "UNIQUE(questionid, textref) ON CONFLICT REPLACE)"
			, "metascale");
	
	public static class MetaScaleManager {
		private Connection connection;
		private TextReferenceManager textsManager;
		
		private Map<Integer, MetaScale<?>> scaleMap = new HashMap<>();
		private List<MetaScale<?>> toBeAdded = new LinkedList<>();
		private Map<Integer, MetaScale<?>> toBeRemoved = new HashMap<>();
		
		public MetaScaleManager(Connection connection, TextReferenceManager textsManager) throws SQLException {
			this.connection = connection;
			this.textsManager = textsManager;

			if(!SQLTools.tableExists(scaletable.name, connection)) {
				try (Statement stmt = connection.createStatement()) {
					stmt.executeUpdate(scaletable.sql);
				}
			}
		}
		
		public MetaScale<?> get(int questionid, VariableType type) throws SQLException, DaxploreException {
			if(scaleMap.containsKey(questionid)) {
				return scaleMap.get(questionid);
			} else if(toBeRemoved.containsKey(questionid)) {
				return null; // TODO: handle non-scales in a more structured way?
			} else {
				Gson gson = new Gson();
				try(PreparedStatement stmt = connection.prepareStatement("SELECT * FROM metascale WHERE questionid = ? ORDER BY pos")) {
					stmt.setInt(1, questionid);
					try(ResultSet rs  = stmt.executeQuery()) {
						switch (type) {
						case NUMERIC:
							List<Option<Double>> optionsDouble = new LinkedList<>();
							while(rs.next()) {
								Double[] dArray = gson.fromJson(rs.getString("mappedvals"), Double[].class);
								optionsDouble.add(
								new Option<Double>(
									textsManager.get(rs.getString("textref")), 
									Arrays.asList(dArray),
									rs.getBoolean("dichselected"),
									false));
							}
							MetaScale<Double> msDouble = new MetaScale<Double>(questionid, optionsDouble, false, type);
							scaleMap.put(questionid, msDouble);
							return msDouble;
						case TEXT:
							List<Option<String>> optionsString = new LinkedList<>();
							
							while(rs.next()) {
								String[] sArray = gson.fromJson(rs.getString("mappedvals"), String[].class);
								optionsString.add(
								new Option<String>(
									textsManager.get(rs.getString("textref")), 
									Arrays.asList(sArray),
									rs.getBoolean("dichselected"),
									false));
							}
							MetaScale<String> msString = new MetaScale<String>(questionid, optionsString, false, type);
							scaleMap.put(questionid, msString);
							return msString;
						}
					}
				}
			}
			throw new DaxploreException("Could not get metascale with id " + questionid);
		}
		
		public MetaScale<String> createString(int questionid, List<Option<String>> options) {
			MetaScale<String> scaleString = new MetaScale<String>(questionid, options, true, VariableType.TEXT);
			toBeAdded.add(scaleString);
			scaleMap.put(questionid, scaleString);
			return scaleString;
		}
		
		public MetaScale<Double> createDouble(int questionid, List<Option<Double>> options) {
			MetaScale<Double> scaleDouble = new MetaScale<Double>(questionid, options, true, VariableType.NUMERIC);
			toBeAdded.add(scaleDouble);
			scaleMap.put(questionid, scaleDouble);
			return scaleDouble;
		}
		
		public void remove(int id) {
			MetaScale<?> scale = scaleMap.remove(id);
			toBeAdded.remove(scale);
			toBeRemoved.put(id, scale);
		}
		
		public void saveAll() throws SQLException {
			Gson gson = new Gson();
			try (
				PreparedStatement addOptionStmt = connection.prepareStatement("INSERT INTO metascale (questionid, textref, pos, mappedvals, dichselected) VALUES (?, ?, ?, ?, ?)");
				PreparedStatement deleteOptionStmt = connection.prepareStatement("DELETE FROM metascale WHERE questionid = ?");
				//PreparedStatement updateOptionStmt = connection.prepareStatement("UPDATE metascaleoption SET textref = ?, value = ?, transform = ? WHERE scaleid = ? AND ord = ?");
			) {
				int nNew = 0, nModified = 0, nRemoved = 0;
				
				for(MetaScale<?> ms: toBeAdded) {
					nNew++;
					int ord = 0;
					for(Option<?> opt: ms.options) {
						addOptionStmt.setInt(1, ms.id);
						addOptionStmt.setString(2, opt.textRef.getRef());
						addOptionStmt.setInt(3, ord);
						addOptionStmt.setString(4, gson.toJson(opt.getValues()));
						addOptionStmt.setBoolean(5, opt.isSelectedInDichotomized());
						addOptionStmt.addBatch();
						ord++;
					}
					ms.setSaved();
				}
				toBeAdded.clear();
				addOptionStmt.executeBatch();
				
				for(MetaScale<?> ms: scaleMap.values()) {
					if(ms.isModified()) {
						nModified++;
						
						deleteOptionStmt.setInt(1, ms.id);
						deleteOptionStmt.addBatch();
						
						int ord = 0;
						for(Option<?> opt: ms.options) {
							addOptionStmt.setInt(1, ms.id);
							addOptionStmt.setString(2, opt.textRef.getRef());
							addOptionStmt.setInt(3, ord);
							addOptionStmt.setString(4, gson.toJson(opt.getValues()));
							addOptionStmt.setBoolean(5,opt.isSelectedInDichotomized());
							addOptionStmt.addBatch();
							ord++;
						}
						
						ms.setSaved();
					}
				}
				deleteOptionStmt.executeBatch();
				addOptionStmt.executeBatch();
				
				for(MetaScale<?> ms: toBeRemoved.values()) {
					nRemoved++;
					deleteOptionStmt.setInt(1, ms.id);
					deleteOptionStmt.addBatch();
				}
				deleteOptionStmt.executeBatch();
				toBeRemoved.clear();
				
				if(nModified != 0 || nNew != 0 || nRemoved != 0) {
					String logString = String.format("MetaScale: Saved %d (%d new), %d removed", nModified, nNew, nRemoved);
					Logger.getGlobal().log(Level.INFO, logString);
				}
			}
		}
		
		public int getUnsavedChangesCount() {
			int nModified = 0;
			for(MetaScale<?> ms: scaleMap.values()) {
				if(ms.isModified()) {
					nModified++;
				}
			}			
			return toBeRemoved.size() + nModified;
		}

		public void discardChanges() {
			scaleMap.clear();
			toBeAdded.clear();
			toBeRemoved.clear();
		}
	}
	
	public static class Option<T> {
		private TextReference textRef;
		private Set<T> values = new HashSet<T>();
		private boolean selectedInDichotomized;
		private boolean modified = false;
		
		public Option(TextReference textRef, Collection<T> values, boolean selectedInDichotomized, boolean setNew) {
			this.textRef = textRef;
			this.values.addAll(values);
			this.selectedInDichotomized = selectedInDichotomized;
			this.modified = setNew;
		}

		public TextReference getTextRef() {
			return textRef;
		}

		public void setTextRef(TextReference textRef) {
			if(!textRef.equals(this.textRef)) {
				this.textRef = textRef;
				modified = true;
			}
		}

		public Set<T> getValues() {
			return values;
		}

		public void addValue(T value) {
			values.add(value);
			modified = true;
		}
		
		public void removeValue(T value) {
			values.remove(value);
			modified = true;
		}
		
		public boolean containsValue(T value) {
			return values.contains(value);
		}
		
		public void setValues(Collection<T> values) {
			values.clear();
			values.addAll(values);
			modified = true;
		}
		
		public void setSelectedInDichotomized(boolean selected) {
			if (selectedInDichotomized != selected) {
				selectedInDichotomized = selected;
				modified = true;
			}
		}
		
		public boolean isSelectedInDichotomized() {
			return selectedInDichotomized;
		}
	}
	
	
	/** Each Option's position is defined by the order of this list */ //TODO change to explicit ordering?
	private int id;
	private List<Option<T>> options;
	private VariableType type;
	private boolean modified = false;
	
	private MetaScale(int id, List<Option<T>> options, boolean newScale, VariableType type) {
		this.id = id;
		this.options = options;
		this.type = type;
		this.modified = newScale;
	}
	
	public int getId() {
		return id;
	}
	
	public int getOptionCount() {
		return options.size();
	}
	
	public List<Option<T>> getOptions() {
		return Lists.newLinkedList(options);
	}
	
	public VariableType getType() {
		return type;
	}

	public void addOption(Option<T> option) {
		modified = options.add(option);
	}
	
	public void setOptions(List<Option<T>> options) {
		if(!options.equals(this.options)) {
			this.options = options;
			modified = true;
		}
	}
	
	/**
	 * Finds the index of the option that maps to this value
	 * @param value
	 * @return index of value, -1 if not found
	 */
	public int getOptionIndex(T value) {
		//TODO: optimize with better data structure
		for(int i = 0; i < options.size(); i++ ) {
			if(options.get(i).getValues().contains(value)) {
				return i;
			}
		}
		return -1;
	}
	
	public boolean isModified() {
		if(modified) {
			return true; 
		}
		for(Option<T> opt: options) {
			if(opt.modified) {
				return true;
			}
		}
		return false;
	}
	
	public void setSaved() {
		modified = false;
		for(Option<T> opt: options) {
			opt.modified = false;
		}
	}
	
	public int getLowestUnusedTextrefIndex() {
		Set<Integer> usedRefIndex = new TreeSet<>(); 
		for(Option<T> o : options) {
			String[] textRefElements = o.textRef.getRef().split("_");
			usedRefIndex.add(Integer.parseInt(textRefElements[textRefElements.length-1]));
		}
		for(int i = 1;; i++) {
			if(!usedRefIndex.contains(i)) {
				return i;
			}
		}
	}
	
	/* 
	 * Change Scale to a new type.
	 * 
	 * Overrides generics safety at runtime and throws away all previous options.
	 */
	public void setScaleVariableType(VariableType type) {
		if (this.type != type) {
			this.type = type;
			options = new LinkedList();	
			modified = true;
		}
	}
}
