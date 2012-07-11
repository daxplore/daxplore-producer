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
	Date importdate;
	String filename;
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
			stmt = database.createStatement();
			stmt.executeUpdate("CREATE TABLE about (filetypeversionmajor INTEGER, filetypeversionminor INTEGER, creation INTEGER, lastupdate INTEGER, importdate INTEGER, filename TEXT)");
			PreparedStatement prepared = database.prepareStatement("INSERT INTO about VALUES (?, ?, ?, ?, ?, ?)");
			prepared.setInt(1, filetypeversionmajor);
			prepared.setInt(2, filetypeversionminor);
			prepared.setLong(3, creation.getTime());
			prepared.setLong(4, lastupdate.getTime());
			prepared.setLong(5, 0);
			prepared.setString(6, "");
			prepared.execute();
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
		}
	}
	
	public ImportedData getImportedData(){
		try {
			ImportedData id = new ImportedData(database);
			return id;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
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
		PreparedStatement prep = database.prepareStatement("UPDATE about SET lastupdate = ?");
		prep.setLong(1, now.getTime());
		lastupdate = now;
		prep.executeUpdate();
	}
	
	public Date getImportDate() {
		return importdate;
	}
	
	public void setImport(String filename) throws SQLException {
		Date now = new Date();
		PreparedStatement prep = database.prepareStatement("UPDATE about SET importdate = ?, filename = ?");
		prep.setLong(1, now.getTime());
		lastupdate = now;
		prep.setString(2, filename);
		this.filename = filename;
		prep.executeUpdate();
	}
	
	public String getImportFilename(){
		return filename;
	}
}
