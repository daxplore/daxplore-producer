/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.daxplorelib;

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

/**
 * This class mirrors the 'about' table in the project file
 * It's a one row table with information about the entire save file.
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
	
	public About(Connection sqliteDatabase, Settings settings) throws SQLException {
		this(sqliteDatabase, settings, false);
	}
	
	About(Connection sqliteDatabase, Settings settings, boolean createnew) throws SQLException { 
		this.connection = sqliteDatabase;
		this.settings = settings;
		locales = new TreeSet<>(new Comparator<Locale>() {
			@Override
			public int compare(Locale o1, Locale o2) {
				return o1.getDisplayLanguage().compareTo(o2.getDisplayLanguage());
			}
		});
		if(createnew){
			settings.putSetting("filetypeversionmajor", DaxploreProperties.filetypeversionmajor);
			settings.putSetting("filetypeversionminor", DaxploreProperties.filetypeversionminor);
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
		SQLTools.createIfNotExists(localeTable, connection);
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
	}
	
	public void discardChanges() throws SQLException {
		locales.clear();
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery("SELECT locale FROM locales")) {
			while (rs.next()) {
				locales.add(new Locale(rs.getString("locale")));
			}
		}
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
