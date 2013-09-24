package org.daxplore.producer.daxplorelib;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This class mirrors the 'about' table in the project file
 * It's a one row table with information about the entire save file.
 */
public class About {
	private static final DaxploreTable table = new DaxploreTable(
			"CREATE TABLE about (filetypeversionmajor INTEGER, filetypeversionminor INTEGER, creation INTEGER," +
			"lastupdate INTEGER, importdate INTEGER, filename TEXT, timeseriestype TEXT, timeshortcolumn TEXT)", "about");
	
	private static final DaxploreTable localeTable = new DaxploreTable("CREATE TABLE locales (locale TEXT PRIMARY KEY)", "locales");
	
	public enum TimeSeriesType {
		NONE, SHORT, LONG
	}
	
	final int filetypeversionmajor;
	final int filetypeversionminor;
	private Date creation;
	private Date lastupdate;
	private Date importdate;
	private String filename;
	private TimeSeriesType timeSeriesType;
	
	private boolean firstSave = false, modified = false;
	
	private SortedSet<Locale> locales;
	private boolean localesModified = false;
	
	/**
	 * If the timeseries type is SHORT, this is the column that keeps track of time points.
	 * For other timeseries types, this should be set to null.
	 */
	private String timeSeriesShortColumn;
	
	private Connection connection;
	
	public About(Connection sqliteDatabase) throws SQLException {
		this(sqliteDatabase, false);
	}
	
	About(Connection sqliteDatabase, boolean createnew) throws SQLException { 
		this.connection = sqliteDatabase;
		locales = new TreeSet<>(new Comparator<Locale>() {
			@Override
			public int compare(Locale o1, Locale o2) {
				return o1.getDisplayLanguage().compareTo(o2.getDisplayLanguage());
			}
		});
		if(createnew){
			filetypeversionmajor = DaxploreProperties.filetypeversionmajor;
			filetypeversionminor = DaxploreProperties.filetypeversionminor;
			creation = new Date();
			lastupdate = (Date) creation.clone();
			importdate = null;
			filename = null;
			timeSeriesType = TimeSeriesType.SHORT;
			timeSeriesShortColumn = null;
			modified = true;
			firstSave = true;
		}else{
			try (Statement stmt = connection.createStatement()) {
				stmt.execute("SELECT * FROM about");
				try(ResultSet rs = stmt.getResultSet()) {
					rs.next();
					filetypeversionmajor = rs.getInt("filetypeversionmajor");
					filetypeversionminor = rs.getInt("filetypeversionminor");
					creation = rs.getDate("creation");
					lastupdate = rs.getDate("lastupdate");
					importdate = rs.getDate("importdate");
					filename = rs.getString("filename");
					timeSeriesType = TimeSeriesType.valueOf(rs.getString("timeseriestype"));
					timeSeriesShortColumn = rs.getString("timeshortcolumn");
				}
				try(ResultSet rs = stmt.executeQuery("SELECT locale FROM locales")) {
					while(rs.next()) {
						locales.add(new Locale(rs.getString("locale")));
					}
				}
			}
		}
	}
	
	void init() throws SQLException {
		SQLTools.createIfNotExists(table, connection);
		SQLTools.createIfNotExists(localeTable, connection);
	}
	
	void save() throws SQLException {
		if(modified) {
			String stmtString;
			if(firstSave) {
				stmtString = 
						"INSERT INTO about (filetypeversionmajor, filetypeversionminor, creation," +
						"lastupdate, importdate, filename, timeseriestype, timeshortcolumn) " +
						"VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
				firstSave = false;
			} else {
				stmtString =
						"UPDATE about SET filetypeversionmajor = ?, filetypeversionminor = ?, creation = ?," +
						"lastupdate = ?, importdate = ?, filename = ?, timeseriestype = ?, timeshortcolumn = ?";
			}
			try(PreparedStatement updateStmt = connection.prepareStatement(stmtString)) {
				Date now = new Date();
				updateStmt.setInt(1, filetypeversionmajor);
				updateStmt.setInt(2, filetypeversionminor);
				updateStmt.setLong(3, creation.getTime());
				updateStmt.setLong(4, now.getTime());
				
				if(importdate!=null) {
					updateStmt.setLong(5, importdate.getTime());
				} else {
					updateStmt.setNull(5, Types.INTEGER);
				}
				
				if(filename!=null) {
					updateStmt.setString(6, filename);
				} else {
					updateStmt.setNull(6, Types.VARCHAR);
				}
				
				updateStmt.setString(7, timeSeriesType.name());
				
				if(timeSeriesShortColumn!=null) {
					updateStmt.setString(8, timeSeriesShortColumn);
				} else {
					updateStmt.setNull(8, Types.VARCHAR);
				}
				
				updateStmt.executeUpdate();
				modified = false;
			}
		}
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
			localesModified = false;
		}
	}
	
	public Date getCreationDate() {
		return creation;
	}
	
	public Date getLastUpdate() {
		return lastupdate;
	}
	
	public Date getImportDate() {
		return importdate;
	}
	
	public void setImport(String filename) {
		Date now = new Date();
		this.filename = filename;
		this.importdate = now;
		modified = true;
	}
	
	public String getImportFilename(){
		return filename;
	}
	
	public void setTimeSeriesType(TimeSeriesType timeSeriesType) {
		this.timeSeriesType = timeSeriesType;
		modified = true;
	}
	
	public TimeSeriesType getTimeSeriesType() {
		return timeSeriesType;
	}
	
	public void setTimeSeriesShortColumn(String column) {
		timeSeriesShortColumn = column;
		modified = true;
	}
	
	public String getTimeSeriesShortColumn() {
		return timeSeriesShortColumn;
	}
	
	public void addLocale(Locale locale) {
		localesModified |= locales.add(locale);
	}
	
	public void removeLocale(Locale locale) {
		localesModified |= locales.remove(locale);
	}
	
	public List<Locale> getLocales() {
		return new LinkedList<>(locales);
	}
}
