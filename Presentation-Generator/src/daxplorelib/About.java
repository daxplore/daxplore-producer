package daxplorelib;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Date;

/**
 * This class mirrors the 'about' table in the project file
 * It's a one row table with the columns: filetypeversion, creation, lastupdate, activerawdata
 */
public class About {
	protected static final DaxploreTable table = new DaxploreTable(
			"CREATE TABLE about (filetypeversionmajor INTEGER, filetypeversionminor INTEGER, creation INTEGER," +
			"lastupdate INTEGER, importdate INTEGER, filename TEXT, timeseriestype TEXT, timeshortcolumn TEXT)", "about");
	
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
	
	private boolean modified = false;
	
	/**
	 * If the timeseries type is SHORT, this is the column that keeps track of time points.
	 * For other timeseries types, this should be set to null.
	 */
	String timeSeriesShortColumn;
	
	Connection connection;
	
	public About(Connection sqliteDatabase) throws SQLException {
		this(sqliteDatabase, false);
	}
	
	public About(Connection sqliteDatabase, boolean createnew) throws SQLException{
		this.connection = sqliteDatabase;
		Statement stmt;
		if(createnew){
			filetypeversionmajor = DaxploreFile.filetypeversionmajor;
			filetypeversionminor = DaxploreFile.filetypeversionminor;
			creation = new Date();
			lastupdate = (Date) creation.clone();
			timeSeriesType = TimeSeriesType.SHORT;
			stmt = connection.createStatement();
			stmt.executeUpdate(table.sql);
			stmt.close();
			PreparedStatement prepared = connection.prepareStatement("INSERT INTO about VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
			prepared.setInt(1, filetypeversionmajor);
			prepared.setInt(2, filetypeversionminor);
			prepared.setLong(3, creation.getTime());
			prepared.setLong(4, lastupdate.getTime());
			prepared.setLong(5, 0);
			prepared.setString(6, "");
			prepared.setString(7, TimeSeriesType.NONE.name());
			prepared.setNull(8, Types.VARCHAR);
			prepared.execute();
			prepared.close();
			
		}else{
			stmt = connection.createStatement();
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
			timeSeriesShortColumn = rs.getString("timeshortcolumn");
			stmt.close();
		}
	}
	
	public void saveAll() throws SQLException {
		if(modified) {
			PreparedStatement updateStmt = connection.prepareStatement(
					"UPDATE about SET filetypeversionmajor = ?, filetypeversionminor = ?, creation = ?," +
					"lastupdate = ?, importdate = ?, filename = ?, timeseriestype = ?, timeshortcolumn = ?");
			Date now = new Date();
			updateStmt.setInt(1, filetypeversionmajor);
			updateStmt.setInt(2, filetypeversionminor);
			updateStmt.setLong(3, creation.getTime());
			updateStmt.setLong(4, now.getTime());
			updateStmt.setLong(4, importdate.getTime());
			updateStmt.setString(6, filename);
			updateStmt.setString(7, timeSeriesType.name());
			updateStmt.setString(8, timeSeriesShortColumn);
			updateStmt.executeUpdate();
			updateStmt.close();
			modified = false;
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
	
	public void setImport(String filename) throws SQLException {
		Date now = new Date();
		this.filename = filename;
		this.importdate = now;
		modified = true;
	}
	
	public String getImportFilename(){
		return filename;
	}
	
	public void setTimeSeriesType(TimeSeriesType timeSeriesType) throws SQLException {
		this.timeSeriesType = timeSeriesType;
		modified = true;
	}
	
	public TimeSeriesType getTimeSeriesType() {
		return timeSeriesType;
	}
	
	public void setTimeSeriesShortColumn(String column) throws SQLException {
		timeSeriesShortColumn = column;
		modified = true;
	}
	
	public String getTimeSeriesShortColumn() {
		return timeSeriesShortColumn;
	}
}
