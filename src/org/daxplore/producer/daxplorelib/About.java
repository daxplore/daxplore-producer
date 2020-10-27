/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.daxplorelib;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;
import org.daxplore.producer.daxplorelib.resources.DaxploreProperties;
import org.daxplore.producer.tools.MyTools;

/**
 * General information about the entire save file.
 * 
 * This class contains the 'locale' table in the project file
 * and relies on the settings table for everything else. 
 * 
 */
public class About {
	private static final DaxploreTable localeTable = new DaxploreTable("CREATE TABLE locales (locale TEXT PRIMARY KEY)", "locales");
	
	public enum TimeSeriesType {
		NONE, SHORT
	}
	
	private Settings settings;
	
	private SortedSet<Locale> locales;
	private boolean localesModified = false;
	
	private Connection connection;
	
	About(Connection connection, Settings settings) throws SQLException { 
		this.connection = connection;
		this.settings = settings;
		locales = new TreeSet<>(new Comparator<Locale>() {
			@Override
			public int compare(Locale o1, Locale o2) {
				return o1.getDisplayLanguage().compareTo(o2.getDisplayLanguage());
			}
		});
		// Create locale table if it doesn't exist, and if needed also initialize other About-related settings
		boolean createnew = SQLTools.createIfNotExists(localeTable, connection);
		if(createnew){
			ZonedDateTime create = ZonedDateTime.now();
			settings.putSetting("creation", create);
			settings.putSetting("lastupdate", create);
			
			settings.putSetting("timeSeriesType", TimeSeriesType.NONE.name());
			
			// If the timeseries type is SHORT, timeSeriesShortColumn is the column that keeps track of time points.
			// For other timeseries types, this setting is ignored.
		}else{
			try (Statement stmt = connection.createStatement();
				 ResultSet rs = stmt.executeQuery("SELECT locale FROM locales")) {
				while (rs.next()) {
					locales.add(new Locale(rs.getString("locale")));
				}
			}
		}
		TextReference.setActiveLocales(new ArrayList<Locale>(locales));
	}
	
	void save() throws SQLException {
		if(localesModified) {
			try(Statement stmt = connection.createStatement()) {
				stmt.executeUpdate("DELETE FROM locales");
			}
			try(PreparedStatement insertLocaleStmt = connection.prepareStatement("INSERT INTO locales (locale) VALUES (?)")) {
				for(Locale l : locales) {
					insertLocaleStmt.setString(1, l.toLanguageTag());
					insertLocaleStmt.addBatch();
				}
				insertLocaleStmt.executeBatch();
			}
			
			String logString = String.format("About: Saved %d active locale(s)", locales.size());
			Logger.getGlobal().log(Level.INFO, logString);
			
			localesModified = false;
		}
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate("PRAGMA user_version = " + DaxploreProperties.daxploreFileVersion + ";");
			stmt.executeUpdate("PRAGMA application_id = " + DaxploreProperties.daxploreFileApplicationID + ";");
		}
	}
	
	public void discardChanges() throws SQLException {
		locales.clear();
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery("SELECT locale FROM locales")) {
			while (rs.next()) {
				locales.add(new Locale(rs.getString("locale")));
			}
		}
		localesModified = false;
	}
	
	public int getUnsavedChangesCount() {
		return (localesModified ? 1 : 0);
	}
	
	public ZonedDateTime getCreationDate() {
		return settings.getDate("creation");
	}
	
	public ZonedDateTime getLastUpdate() {
		return settings.getDate("lastupdate");
	}
	
	public ZonedDateTime getImportDate() {
		return settings.getDate("importdate");
	}
	
	public void setImport(String filename) {
		ZonedDateTime now = ZonedDateTime.now();
		settings.putSetting("filename", filename);
		settings.putSetting("importdate", now);
	}
	
	public String getImportFilename() {
		return settings.getString("filename");
	}
	
	/**
	 * Get a hash based ID based on the import filename
	 * 
	 * Only intended for internal use, to keep track of exported files without uploading actual filenames.
	 * Not intended to be robust on the level of a UUID.
	 * 
	 * @return A short ID
	 * @throws DaxploreException 
	 */
	public String getImportFileNameID () throws DaxploreException {
		try {
			// Add short, randomized IDs based on the used daxplore filename and spss filename
			// Only a few characters are needed, as this is only used for internal reference to keep track of exported versions.
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update((byte) 131);
			return MyTools.bytesToHex(md.digest(getImportFilename().getBytes(StandardCharsets.UTF_8))).substring(0, 8);
		} catch (NoSuchAlgorithmException e) {
			throw new DaxploreException("Failed to create hash", e);
		}
	}
	
	public void setTimeSeriesType(TimeSeriesType timeSeriesType) {
		settings.putSetting("timeSeriesType", timeSeriesType.name());
	}
	
	public TimeSeriesType getTimeSeriesType() {
		return TimeSeriesType.valueOf(settings.getString("timeSeriesType"));
	}
	
	public void setTimeSeriesShortColumn(String column) {
		settings.putSetting("timeSeriesShortColumn", column);
	}
	
	public String getTimeSeriesShortColumn() {
		return settings.getString("timeSeriesShortColumn");
	}
	
	public void addLocale(Locale locale) {
		localesModified |= locales.add(locale);
		TextReference.setActiveLocales(new ArrayList<Locale>(locales));
	}
	
	public void removeLocale(Locale locale) {
		localesModified |= locales.remove(locale);
		TextReference.setActiveLocales(new ArrayList<Locale>(locales));
	}
	
	public List<Locale> getLocales() {
		return new LinkedList<>(locales);
	}
}
