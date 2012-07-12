package daxplorelib;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DaxploreMemoryFile extends DaxploreFile {

	protected static DaxploreMemoryFile createFresh(DaxploreFile daxploreFile) throws DaxploreException {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			throw new DaxploreException("Sqlite could not be found", e);
		}
		try {
			Connection connection =  DriverManager.getConnection("jdbc:sqlite::memory");
			return new DaxploreMemoryFile(connection, false, daxploreFile);
		} catch (SQLException e) {
			throw new DaxploreException("Not a sqlite file?", e);
		}
	}
	
	protected static DaxploreMemoryFile createCopy(DaxploreFile daxploreFile) throws DaxploreException {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			throw new DaxploreException("Sqlite could not be found", e);
		}
		try {
			Connection connection =  DriverManager.getConnection("jdbc:sqlite::memory");
			return new DaxploreMemoryFile(connection, true, daxploreFile);
		} catch (SQLException e) {
			throw new DaxploreException("Not a sqlite file?", e);
		}
	}
	
	protected DaxploreMemoryFile(Connection connection, boolean copy, DaxploreFile daxploreFile) throws DaxploreException {
		super(connection, !copy, null);
		try {
			Statement stmt = connection.createStatement();
			stmt.executeUpdate("ATTACH DATABASE 'file:"+ daxploreFile.getFile().getAbsolutePath() +"' AS real");
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
