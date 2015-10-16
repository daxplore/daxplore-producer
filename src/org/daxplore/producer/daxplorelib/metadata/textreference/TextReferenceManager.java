/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.daxplorelib.metadata.textreference;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.DaxploreFile;
import org.daxplore.producer.daxplorelib.DaxploreTable;
import org.daxplore.producer.daxplorelib.SQLTools;
import org.daxplore.producer.daxplorelib.resources.DaxploreProperties;
import org.daxplore.producer.daxplorelib.resources.DefaultPresenterTexts;
import org.daxplore.producer.tools.SmallMap;

public class TextReferenceManager {
	private static final DaxploreTable table = new DaxploreTable("CREATE TABLE texts (ref TEXT NOT NULL, locale TEXT, text TEXT NOT NULL, UNIQUE ( ref, locale) )", "texts");

	private Connection connection;
	private Map<String, TextReference> toBeRemoved = new HashMap<>();
	
	private TextTree textTree = new TextTree();
	
	private int nNew = 0;
	
	public TextReferenceManager(Connection connection) throws DaxploreException {
		this.connection = connection;
		
		try {
			SQLTools.createIfNotExists(TextReferenceManager.table, connection);
		} catch (SQLException e) {
			throw new DaxploreException("Failed to create the TextReferenceManager SQL table", e);
		}
		
		// set default values
		for (Locale locale : DaxploreProperties.predefinedLocales) {
			DefaultPresenterTexts defaultTexts = new DefaultPresenterTexts(locale);
			for (String property : DaxploreProperties.presenterUITexts) {
				TextReference ref = get(property);
				if (!ref.hasText(locale)) {
					ref.put(defaultTexts.get(property), locale);
				}
			}
		}
	}
	
	/**
	 * Get a TextReference from refstring. Creates one if it doesn't exist.
	 * @param refstring
	 * @return
	 * @throws SQLException
	 * @throws DaxploreException 
	 */
	public TextReference get(String refstring) throws DaxploreException {
		if(!DaxploreFile.isValidColumnName(refstring)) {
			throw new DaxploreException("Invalid refstring: '" + refstring + "'");
		}
		
		TextReference tr = textTree.get(refstring);
		if(tr == null) {
			boolean newTextReference = true;
			Map<Locale, String> localeMap = new SmallMap<>();
			try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM texts where ref = ?")) {
				stmt.setString(1, refstring);
				try(ResultSet rs = stmt.executeQuery()) {
					while(rs.next()) {
						String loc = rs.getString("locale");
						if(loc != null && !loc.isEmpty()) { //TODO verify that we want to create it?
							localeMap.put(new Locale(rs.getString("locale")), rs.getString("text"));						
						}
						newTextReference = false;
					}
				}
			} catch (SQLException e) {
				throw new DaxploreException("Failed to create a new TextReference for: '" + refstring + "'", e);
			}
			if(newTextReference) {
				nNew++;
			}
			tr = new TextReference(refstring, localeMap);
			tr.modified = newTextReference;
			//System.out.print(newTextReference ? "n": "o");
			textTree.add(tr);
		}
		return tr;
	}
	
	public void remove(String refstring) {
		toBeRemoved.put(refstring, textTree.remove(refstring));
	}
	
	public void remove(TextReference textReference) {
		remove(textReference.getRef());
	}
	
	public void saveAll() throws SQLException {
		int nRemoved = 0, nModified = 0;
		try (
			PreparedStatement insertTextrefStmt = connection.prepareStatement("INSERT OR REPLACE INTO texts (ref, locale, text) VALUES (?, ? , ?)");
			PreparedStatement selectLocalesStmt = connection.prepareStatement("SELECT locale FROM texts WHERE ref = ?");
			PreparedStatement deleteTextrefLocaleStmt = connection.prepareStatement("DELETE FROM texts WHERE ref = ? AND locale = ?");
			PreparedStatement deleteTextrefStmt = connection.prepareStatement("DELETE FROM texts WHERE ref = ?");
		) {
			//Delete those marked to be removed
			for(TextReference tr: toBeRemoved.values()) {
				nRemoved++;
				deleteTextrefStmt.setString(1, tr.reference);
				deleteTextrefStmt.addBatch();
			}
			deleteTextrefStmt.executeBatch();
			toBeRemoved.clear();
			
			for(TextReferenceReference trr: textTree) {
				TextReference tr = (TextReference)trr;
				if(tr.modified) {
					nModified++;
					//first get existing locales
					Set<Locale> oldLocs = new HashSet<>();
					selectLocalesStmt.setString(1, tr.reference);
					try (ResultSet rs = selectLocalesStmt.executeQuery()) {
						while(rs.next()) {
							String loc = rs.getString("locale");
							if(loc != null && !"".equals(loc)) {
								oldLocs.add(new Locale(loc));
							}
						}
					}
					if(tr.localeMap.size() > 0) {
						for(Locale l: tr.localeMap.keySet()) {
							oldLocs.remove(l);
							insertTextrefStmt.setString(1, tr.reference);
							insertTextrefStmt.setString(2, l.toLanguageTag());
							String trtext = tr.getText(l);
							if (trtext == null) {
								trtext = "";
							}
							insertTextrefStmt.setString(3, trtext);
							insertTextrefStmt.executeUpdate(); //TODO: figure out why batch dosn't work here.
						}
						
						deleteTextrefLocaleStmt.setString(1, tr.reference);
						deleteTextrefLocaleStmt.setNull(2, Types.VARCHAR);
						deleteTextrefLocaleStmt.executeUpdate();
					} else {
						insertTextrefStmt.setString(1, tr.reference);
						insertTextrefStmt.setNull(2, Types.VARCHAR);
						insertTextrefStmt.setString(3, "");
						insertTextrefStmt.executeUpdate();
					}
						
					for(Locale l: oldLocs) {
						deleteTextrefLocaleStmt.setString(1, tr.reference);
						deleteTextrefLocaleStmt.setString(2, l.toLanguageTag());
						deleteTextrefLocaleStmt.addBatch();
					}
					deleteTextrefLocaleStmt.executeBatch();
					
					tr.modified = false;
				}
			}
		}
		
		
		if(nModified != 0 || nNew != 0 || nRemoved != 0) {
			String logString = String.format("TextReferences: Saved %d (%d new), %d removed", nModified, nNew, nRemoved);
			Logger.getGlobal().log(Level.INFO, logString);
			nNew = 0;
		}
	}
	
	public int getUnsavedChangesCount() {
		int nModified = 0;
		for(TextReferenceReference trr: textTree) {
			TextReference tr = (TextReference)trr;
			if(tr.modified) {
				nModified++;
			}
		}
		
		return nModified + toBeRemoved.size();
	}

	public List<Locale> getAllLocales() throws DaxploreException { //TODO read from local data, load from databse
		List<Locale> list = new LinkedList<>();
		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT DISTINCT locale FROM texts ORDER BY locale");
				) {
			while(rs.next()) {
				String loc = rs.getString("locale");
				if(!rs.wasNull() && !"".equals(loc)) {
					list.add(new Locale(loc));
				}
			}
		} catch (SQLException e) {
			throw new DaxploreException("Failed to load locales", e);
		}
		return list;
	}

	public TextTree getAll() throws DaxploreException {
		// make sure all references are cached before returning the content of the tree
		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT ref FROM texts")) {
			while(rs.next()) {
				String id = rs.getString("ref");
				if(!textTree.contains(new TextReferenceReference(id)) && !toBeRemoved.containsKey(id)) {
					get(rs.getString("ref"));
				}
			}
		} catch (SQLException e) {
			throw new DaxploreException("Failed to load text references", e);
		}
		return textTree;
	}
	
	public void discardChanges() {
		textTree.clear();
		toBeRemoved.clear();
		nNew = 0;
	}
}
