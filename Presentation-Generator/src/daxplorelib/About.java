package daxplorelib;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

/**
 * This class mirrors the 'about' table in the project file
 * It's a one row table with the columns: filetypeversion, creation, lastupdate, activerawdata
 */
public class About {
	protected static final DaxploreTable table = new DaxploreTable(
			"CREATE TABLE about (filetypeversionmajor INTEGER, filetypeversionminor INTEGER, creation INTEGER," +
			"lastupdate INTEGER, importdate INTEGER, filename TEXT, timeseriestype TEXT)", "about");
	
	public enum TimeSeriesType {
		NONE, SHORT, LONG
	}
	
	final int filetypeversionmajor;
	final int filetypeversionminor;
	Date creation;
	Date lastupdate;
	Date importdate;
	String filename;
	TimeSeriesType timeSeriesType;
	
	Connection database;
	
	public About(Connection sqliteDatabase) throws SQLException {
		this(sqliteDatabase, false);
	}
	
	public About(Connection sqliteDatabase, boolean createnew) throws SQLException{
		this.database = sqliteDatabase;
		Statement stmt;
		if(createnew){
			filetypeversionmajor = DaxploreFile.filetypeversionmajor;
			filetypeversionminor = DaxploreFile.filetypeversionminor;
			creation = new Date();
			lastupdate = (Date) creation.clone();
			timeSeriesType = TimeSeriesType.SHORT;
			stmt = database.createStatement();
			stmt.executeUpdate(table.sql);
			stmt.close();
			PreparedStatement prepared = database.prepareStatement("INSERT INTO about VALUES (?, ?, ?, ?, ?, ?, ?)");
			prepared.setInt(1, filetypeversionmajor);
			prepared.setInt(2, filetypeversionminor);
			prepared.setLong(3, creation.getTime());
			prepared.setLong(4, lastupdate.getTime());
			prepared.setLong(5, 0);
			prepared.setString(6, "");
			prepared.setString(7, TimeSeriesType.NONE.name());
			prepared.execute();
			prepared.close();
			
		}else{
			stmt = database.createStatement();
			stmt.execute("SELECT * FROM about");
			ResultSet rs = stmt.getResultSet();
			rs.next();
			filetypeversionmajor = rs.getInt("filetypeversionmajor");
			filetypeversionminor = rs.getInt("filetypeversionminor");
			creation = rs.getDate("creation");
			lastupdate = rs.getDate("lastupdate");
			importdate = rs.getDate("importdate");
			filename = rs.getString("filename");
			timeSeriesType = TimeSeriesType.valueOf(rs.getString("timeseriestype"));
			stmt.close();
		}
	}
	
	public Date getCreationDate() {
		return creation;
	}
	
	public Date getLastUpdate() {
		return lastupdate;
	}
	
	public void setUpdate() throws SQLException {
		Date now = new Date();
		PreparedStatement stmt = database.prepareStatement("UPDATE about SET lastupdate = ?");
		stmt.setLong(1, now.getTime());
		lastupdate = now;
		stmt.executeUpdate();
		stmt.close();
	}
	
	public Date getImportDate() {
		return importdate;
	}
	
	public void setImport(String filename) throws SQLException {
		Date now = new Date();
		PreparedStatement stmt = database.prepareStatement("UPDATE about SET importdate = ?, filename = ?");
		stmt.setLong(1, now.getTime());
		stmt.setString(2, filename);
		this.filename = filename;
		this.importdate = now;
		stmt.executeUpdate();
		stmt.close();
		setUpdate();
	}
	
	public String getImportFilename(){
		return filename;
	}
	
	public void setTimeSeriesType(TimeSeriesType timeSeriesType) throws SQLException {
		PreparedStatement stmt = database.prepareStatement("UPDATE about SET timeseriestype = ?");
		stmt.setString(1, timeSeriesType.name());
		stmt.executeUpdate();
		stmt.close();
		this.timeSeriesType = timeSeriesType; 
	}
	
	public TimeSeriesType getTimeSeriesType() {
		return timeSeriesType;
	}
}
