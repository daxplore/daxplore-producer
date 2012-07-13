package daxplorelib;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import tools.MyTools;

public class DaxploreMemoryFile extends DaxploreFile {

	protected static DaxploreMemoryFile createFresh(DaxploreFile daxploreFile) throws DaxploreException {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			throw new DaxploreException("Sqlite could not be found", e);
		}
		try {
			Connection connection =  DriverManager.getConnection("jdbc:sqlite::memory:");
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
			Connection connection =  DriverManager.getConnection("jdbc:sqlite::memory:");
			return new DaxploreMemoryFile(connection, true, daxploreFile);
		} catch (SQLException e) {
			throw new DaxploreException("Not a sqlite file?", e);
		}
	}
	protected DaxploreFile baseDaxploreFile;
	
	protected DaxploreMemoryFile(Connection connection, boolean copy, DaxploreFile daxploreFile) throws DaxploreException {
		super(connection, true, null);
		this.baseDaxploreFile = daxploreFile;
		try {
			Statement stmt = connection.createStatement();
			stmt.executeUpdate("ATTACH DATABASE '"+ daxploreFile.getFile().getAbsolutePath() +"' AS real");
			if(copy) {
				List<DaxploreTable> tablelist = daxploreFile.getTables();
				for(DaxploreTable table: tablelist) {
					if(SQLTools.tableExists(table.name, connection)) {
						stmt.executeUpdate("DROP TABLE " + table.name);
					}
					stmt.executeUpdate("CREATE TABLE "+ table.name + " AS SELECT * FROM real."+ table.name);
				}
			}
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	boolean madeImport = false;
	
	@Override
	public void importSPSS(File spssFile, Charset charset) throws FileNotFoundException, IOException, DaxploreException{
		super.importSPSS(spssFile, charset);
		madeImport = true;
	}
	
	public void saveImportToDisk() throws DaxploreException {
		if(madeImport) {
			boolean autocommit = true;
			try {
				autocommit = connection.getAutoCommit();
				connection.setAutoCommit(false);
				int isolation = connection.getTransactionIsolation();
				connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
				
				Statement stmt = connection.createStatement();
				stmt.executeUpdate("DROP TABLE real.rawdata");
				stmt.executeUpdate("DROP TABLE real.rawmeta");
				
				stmt.executeUpdate("CREATE TABLE real.rawdata AS SELECT * FROM rawdata");
				stmt.executeUpdate("CREATE TABLE real.rawmeta AS SELECT * FROM rawmeta");
				
				baseDaxploreFile.getAbout().setImport(getAbout().getImportFilename());
				
				stmt.close();
				
				connection.commit();
				connection.setTransactionIsolation(isolation);
				connection.setAutoCommit(autocommit);
			} catch (SQLException e) {
				MyTools.printSQLExeption(e);
				try {
					connection.rollback();
				} catch (SQLException e1) {
					throw new DaxploreException("Copy error. Could not rollback.", e);
				}
				throw new DaxploreException("Copy error.", e);
			}
		}
	}
	
	public void saveMetaDataToDisk() throws DaxploreException {
		boolean autocommit = true;
		try {
			autocommit = connection.getAutoCommit();
			connection.setAutoCommit(false);
			int isolation = connection.getTransactionIsolation();
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			
			Statement stmt = connection.createStatement();

			List<DaxploreTable> tables = getMetaData().getTables();
			
			for(DaxploreTable table: tables) {
				stmt.executeUpdate("DROP TABLE real." + table.name);
				stmt.executeUpdate("CREATE TABLE real." + table.name + " AS SELECT * FROM " + table.name);
			}
			
			stmt.close();
			
			connection.commit();
			connection.setTransactionIsolation(isolation);
			connection.setAutoCommit(autocommit);
		} catch (SQLException e) {
			MyTools.printSQLExeption(e);
			try {
				connection.rollback();
			} catch (SQLException e1) {
				throw new DaxploreException("Copy error. Could not rollback.", e);
			}
			throw new DaxploreException("Copy error.", e);
		}
	}
	
	public void copyImportToMemory() throws DaxploreException {
		boolean autocommit = true;
		try {
			autocommit = connection.getAutoCommit();
			connection.setAutoCommit(false);
			int isolation = connection.getTransactionIsolation();
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			
			Statement stmt = connection.createStatement();
			stmt.executeUpdate("DROP TABLE rawdata");
			stmt.executeUpdate("DROP TABLE rawmeta");
			
			stmt.executeUpdate("CREATE TABLE rawdata AS SELECT * FROM real.rawdata");
			stmt.executeUpdate("CREATE TABLE rawmeta AS SELECT * FROM real.rawmeta");
			
			getAbout().setImport(baseDaxploreFile.getAbout().getImportFilename());
			
			stmt.close();
			
			connection.commit();
			connection.setTransactionIsolation(isolation);
			connection.setAutoCommit(autocommit);
		} catch (SQLException e) {
			MyTools.printSQLExeption(e);
			try {
				connection.rollback();
			} catch (SQLException e1) {
				throw new DaxploreException("Copy error. Could not rollback.", e);
			}
			throw new DaxploreException("Copy error.", e);
		}
	}
	
	public void copyMetDataToMemory() throws DaxploreException {
		boolean autocommit = true;
		try {
			autocommit = connection.getAutoCommit();
			connection.setAutoCommit(false);
			int isolation = connection.getTransactionIsolation();
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			
			Statement stmt = connection.createStatement();

			List<DaxploreTable> tables = getMetaData().getTables();
			
			for(DaxploreTable table: tables) {
				stmt.executeUpdate("DROP TABLE " + table.name);
				stmt.executeUpdate("CREATE TABLE " + table.name + " AS SELECT * FROM real." + table.name);
			}
			
			stmt.close();
			
			connection.commit();
			connection.setTransactionIsolation(isolation);
			connection.setAutoCommit(autocommit);
		} catch (SQLException e) {
			MyTools.printSQLExeption(e);
			try {
				connection.rollback();
			} catch (SQLException e1) {
				throw new DaxploreException("Copy error. Could not rollback.", e);
			}
			throw new DaxploreException("Copy error.", e);
		}
	}
}
