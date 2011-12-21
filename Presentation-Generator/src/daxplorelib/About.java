package daxplorelib;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import daxplorelib.fileformat.ImportedData;

/**
 * This class mirrors the 'about' table in the project file
 * It's a one row table with the columns: filetypeversion, creation, lastupdate, activerawdata
 */
public class About {
	final int filetypeversionmajor;
	final int filetypeversionminor;
	Date creation;
	Date lastupdate;
	int activeRawData;
	Connection database;
	
	public About(Connection sqliteDatabase) throws SQLException{
		this(sqliteDatabase, false);
	}
	
	public About(Connection sqliteDatabase, boolean createnew) throws SQLException{
		this.database = sqliteDatabase;
		Statement stmt;
		if(createnew){
			filetypeversionmajor = DaxploreFile.filetypeversionmajor;
			filetypeversionminor = DaxploreFile.filetypeversionminor;
			activeRawData = 0;
			creation = new Date();
			lastupdate = (Date) creation.clone();
			stmt = database.createStatement();
			stmt.executeUpdate("CREATE TABLE about (filetypeversionmajor INTEGER, filetypeversionminor INTEGER, creation INTEGER, lastupdate INTEGER, activerawdata INTEGER)");
			PreparedStatement prepared = database.prepareStatement("INSERT INTO about VALUES (?, ?, ?, ?, ?)");
			prepared.setInt(1, filetypeversionmajor);
			prepared.setInt(2, filetypeversionminor);
			prepared.setLong(3, creation.getTime());
			prepared.setLong(4, lastupdate.getTime());
			prepared.setInt(5, activeRawData);
			prepared.execute();
			stmt = database.createStatement();
			stmt.executeUpdate("CREATE TABLE rawversions (version INTEGER PRIMARY KEY AUTOINCREMENT, rawmeta TEXT, rawdata TEXT, importdate INTEGER, filename TEXT)");
		}else{
			stmt = database.createStatement();
			stmt.execute("SELECT * FROM about");
			ResultSet rs = stmt.getResultSet();
			rs.next();
			filetypeversionmajor = rs.getInt("filetypeversionmajor");
			filetypeversionminor = rs.getInt("filetypeversionminor");
			creation = rs.getDate("creation");
			lastupdate = rs.getDate("lastupdate");
			activeRawData = rs.getInt("activerawdata");
		}
	}
	
	public int getActiveRawData(){
		return activeRawData;
	}
	
	void setActiveRawData(int newactive) throws SQLException{
		PreparedStatement prepared = database.prepareStatement("UPDATE about SET activerawdata = ?");
		prepared.setInt(1, newactive);
		int rowsaffected = prepared.executeUpdate();
		if(rowsaffected == 1){
			activeRawData = newactive;
			return;
		} else if(rowsaffected > 1){
			activeRawData = newactive;
			System.err.println("WTF! Do we have more than 1 row in about");
			return;
		} else if(rowsaffected == 0){
			System.err.println("No rows in about?");
			return;
		}
	}
	
	public ImportedData getImportedData(int version){
		try {
			ImportedData id = new ImportedData(version, database);
			return id;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public List<ImportedData> getImportedDataVersions() {
		List<ImportedData> importlist = new LinkedList<ImportedData>();
		try {
			boolean autocommit = database.getAutoCommit();
			database.setAutoCommit(true);
			Statement stmt = database.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT version FROM rawversions ORDER BY version ASC");
			while(rs.next()){
				int version = rs.getInt("version");
				try {
					ImportedData ip = new ImportedData(version, database);
					importlist.add(ip);
				} catch (SQLException e ) {
					System.err.println("Error opening version " + version);
				}
			}
			database.setAutoCommit(autocommit);
			return importlist;
		} catch (SQLException e){
			System.err.println("Error getting imported versions");
			return null;
		}
	}
	
	public Date getCreationDate() {
		return creation;
	}
	
	public Date getLastUpdate() {
		return lastupdate;
	}
}
