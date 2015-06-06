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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.DaxploreTable;
import org.daxplore.producer.daxplorelib.SQLTools;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReferenceManager;

import com.beust.jcommander.internal.Lists;
import com.google.gson.Gson;

public class MetaScale {
	
	/*private static final DaxploreTable maintable = new DaxploreTable(
			"CREATE TABLE metascale (id INTEGER PRIMARY KEY, valmap STRING NOT NULL)"
			, "metascale");*/
	private static final DaxploreTable scaletable = new DaxploreTable(
			"CREATE TABLE metascale (scaleid INTEGER NOT NULL, textref STRING NOT NULL, pos INTEGER NOT NULL, mappedvals TEXT NOT NULL, UNIQUE(scaleid, textref) ON CONFLICT REPLACE)"
			, "metascale");
	
	public static class MetaScaleManager {
		private Map<Integer, MetaScale> scaleMap = new HashMap<>();
		private Connection connection;
		private TextReferenceManager textsManager;
		
		private List<MetaScale> toBeAdded = new LinkedList<>();
		private int addDelta = 0;
		private Map<Integer, MetaScale> toBeRemoved = new HashMap<>();
//		private List<MetaScale> toBeRemoved = new LinkedList<MetaScale>();
		
		public MetaScaleManager(Connection connection, TextReferenceManager textsManager) throws SQLException {
			this.connection = connection;
			this.textsManager = textsManager;

			if(!SQLTools.tableExists(scaletable.name, connection)) {
				try (Statement stmt = connection.createStatement()) {
					stmt.executeUpdate(scaletable.sql);
				}
			}
		}
		
		public MetaScale get(int id) throws SQLException, DaxploreException {
			if(scaleMap.containsKey(id)) {
				return scaleMap.get(id);
			} else if(toBeRemoved.containsKey(id)) {
				return null; // TODO: handle non-scales in a more structured way?
			} else {
				Gson gson = new Gson();
				try(PreparedStatement stmt = connection.prepareStatement("SELECT * FROM metascale WHERE scaleid = ? ORDER BY pos")) {
					stmt.setInt(1, id);
					try(ResultSet rs  = stmt.executeQuery()) {
						List<Option> options = new LinkedList<>();
						
						while(rs.next()) {
							Double[] dArray = gson.fromJson(rs.getString("mappedvals"), Double[].class);
							options.add(
									new Option(
											textsManager.get(rs.getString("textref")), 
											Arrays.asList(dArray),
											false));
						}
						MetaScale ms = new MetaScale(id, options, false);
						scaleMap.put(id, ms);
						return ms;
					}
				}
			}
		}
		
		public MetaScale create(List<Option> options) throws SQLException {
			addDelta++;
			int id = SQLTools.maxId(scaletable.name, "scaleid", connection) + addDelta;
			MetaScale scale = new MetaScale(id, options, true);
			toBeAdded.add(scale);
			scaleMap.put(id, scale);
			return scale;
		}
		
		public void remove(int id) {
			MetaScale scale = scaleMap.remove(id);
			toBeAdded.remove(scale);
			toBeRemoved.put(id, scale);
		}
		
		public void saveAll() throws SQLException {
			Gson gson = new Gson();
			try (
				PreparedStatement addOptionStmt = connection.prepareStatement("INSERT INTO metascale (scaleid, textref, pos, mappedvals) VALUES (?, ?, ?, ?)");
				PreparedStatement deleteOptionStmt = connection.prepareStatement("DELETE FROM metascale WHERE scaleid = ?");
				//PreparedStatement updateOptionStmt = connection.prepareStatement("UPDATE metascaleoption SET textref = ?, value = ?, transform = ? WHERE scaleid = ? AND ord = ?");
			) {
				int nNew = 0, nModified = 0, nRemoved = 0;
				
				for(MetaScale ms: toBeAdded) {
					nNew++;
					int ord = 0;
					for(Option opt: ms.options) {
						addOptionStmt.setInt(1, ms.id);
						addOptionStmt.setString(2, opt.textRef.getRef());
						addOptionStmt.setInt(3, ord);
						addOptionStmt.setString(4, gson.toJson(opt.getValues()));
						addOptionStmt.addBatch();
						ord++;
					}
					ms.setSaved();
				}
				toBeAdded.clear();
				addOptionStmt.executeBatch();
				addDelta = 0;
				
				for(MetaScale ms: scaleMap.values()) {
					if(ms.isModified()) {
						nModified++;
						
						deleteOptionStmt.setInt(1, ms.id);
						deleteOptionStmt.addBatch();
						
						int ord = 0;
						for(Option opt: ms.options) {
							addOptionStmt.setInt(1, ms.id);
							addOptionStmt.setString(2, opt.textRef.getRef());
							addOptionStmt.setInt(3, ord);
							addOptionStmt.setString(4, gson.toJson(opt.getValues()));
							addOptionStmt.addBatch();
							ord++;
						}
						
						ms.setSaved();
					}
				}
				deleteOptionStmt.executeBatch();
				addOptionStmt.executeBatch();
				
				for(MetaScale ms: toBeRemoved.values()) {
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
			for(MetaScale ms: scaleMap.values()) {
				if(ms.isModified()) {
					nModified++;
				}
			}			
			return toBeRemoved.size() + nModified;
		}
		
		public List<MetaScale> getAll() throws SQLException, DaxploreException {
			// make sure all scales are cached before returning the content of the map
			try(Statement stmt = connection.createStatement();
					ResultSet rs = stmt.executeQuery("SELECT id FROM metascale")) {
				while(rs.next()) {
					int id = rs.getInt("id");
					if(!scaleMap.containsKey(id) && !toBeRemoved.containsKey(id)) {
						get(rs.getInt("id"));
					}
				}
			}
			return new LinkedList<>(scaleMap.values());
		}
		
		public void discardChanges() {
			scaleMap.clear();
			toBeAdded.clear();
			toBeRemoved.clear();
			addDelta = 0;
		}
	}
	
	public static class Option {
		private TextReference textRef;
		private Set<Double> values = new HashSet<Double>();
		private boolean modified = false;
		
		public Option(TextReference textRef, Collection<Double> values, boolean setNew) {
			this.textRef = textRef;
			this.values.addAll(values);
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

		public Set<Double> getValues() {
			return values;
		}

		public void addValue(Double value) {
			values.add(value);
		}
		
		public void removeValue(Double value) {
			values.remove(value);
		}
		
		public boolean containsValue(Double value) {
			return values.contains(value);
		}
		
		public void setValues(Collection<Double> values) {
			values.clear();
			values.addAll(values);
		}
	}
	
	
	/** Each Option's position is defined by the order of this list */ //TODO change to explicit ordering
	private List<Option> options;

	private int id;
	private boolean modified = false;
	
	private MetaScale(int id, List<Option> options, boolean newScale) {
		this.id = id;
		this.options = options;
		this.modified = newScale;
	}
	
	public int getId() {
		return id;
	}
	
	public int getOptionCount() {
		return options.size();
	}
	
	public List<Option> getOptions() {
		return Lists.newLinkedList(options);
	}

	public void setOptions(List<Option> options) {
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
	public int getOptionIndex(Double value) {
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
		for(Option opt: options) {
			if(opt.modified) {
				return true;
			}
		}
		return false;
	}
	
	public void setSaved() {
		modified = false;
		for(Option opt: options) {
			opt.modified = false;
		}
	}
	
	/*public boolean equalsLocale(MetaScale other, Locale byLocale) {
		if(options.size() != other.options.size()) { return false; }
		for(int i = 0; i < options.size(); i++) {
			if(!options.get(i).textRef.get(byLocale).trim().equals(other.options.get(i).textRef.get(byLocale).trim())) { return false; }
			if(options.get(i).value != other.options.get(i).value) { return false; }
		}
		return true;
	}*/
}
