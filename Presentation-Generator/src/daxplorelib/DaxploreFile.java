package daxplorelib;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.opendatafoundation.data.FileFormatInfo;
import org.opendatafoundation.data.FileFormatInfo.ASCIIFormat;
import org.opendatafoundation.data.FileFormatInfo.Compatibility;
import org.opendatafoundation.data.spss.SPSSFile;
import org.opendatafoundation.data.spss.SPSSFileException;

import tools.MyTools;
import daxplorelib.metadata.MetaData;
import daxplorelib.raw.RawImport;
import daxplorelib.raw.RawMeta;

public class DaxploreFile {
	public static final int filetypeversionmajor = 0;
	public static final int filetypeversionminor = 1;
	Connection connection;
	About about;
	File file = null;
	MetaData metadata;
	SPSSFile sf = null;
	
	public static DaxploreFile createFromExistingFile(File file) throws DaxploreException {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			throw new DaxploreException("Sqlite could not be found", e);
		}
		
		try {
			Connection connection =  DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
			return new DaxploreFile(connection, false, file);
		} catch (SQLException e) {
			throw new DaxploreException("Not a sqlite file?", e);
		}
	}
	
	public static DaxploreFile createWithNewFile(File file) throws DaxploreException {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			throw new DaxploreException("Sqlite could not be found", e);
		}
		
		try {
			Connection connection =  DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
			return new DaxploreFile(connection, true, file);
		} catch (SQLException e) {
			throw new DaxploreException("Could not create new sqlite database (No write access?)", e);
		}
	}
	
	/*public static DaxploreFile createInMemory() throws DaxploreException {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			throw new DaxploreException("Sqlite could not be found", e);
		}
		
		try {
			Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
			return new DaxploreFile(connection, true);
		} catch (SQLException e) {
			throw new DaxploreException("Could not create in mamory database");
		}
	}*/
	
	protected DaxploreFile(Connection connection, boolean createNew, File file) throws DaxploreException {
		this.connection = connection;
		this.file = file;
		try {
			about = new About(connection, createNew);
		} catch (SQLException e) {
			throw new DaxploreException("Error creating about", e);
		}
	}
	
	public DaxploreMemoryFile getInMemoryFile() throws DaxploreException {
		return DaxploreMemoryFile.createCopy(this);
	}
	
	public void openSPSS(File spssFile, Charset charset) throws FileNotFoundException, IOException, DaxploreException {
		FileFormatInfo ffi = new FileFormatInfo();
		ffi.namesOnFirstLine = false;
		ffi.asciiFormat = ASCIIFormat.CSV;
		ffi.compatibility = Compatibility.GENERIC;
		try {
			sf = new SPSSFile(spssFile,charset);
			sf.logFlag = false;
			sf.loadMetadata();
		} catch (SPSSFileException e2) {
			throw new DaxploreException("SPSSFileException", e2);
		}
	}
	
	public void importDataFromSPSS() throws DaxploreException {
		if(sf == null) {
			throw new DaxploreException("No SPSSfile loaded");
		}
		boolean autocommit = true;
		try {
			autocommit = connection.getAutoCommit();
			connection.setAutoCommit(false);
			int isolation = connection.getTransactionIsolation();
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			
			RawImport rawImport = new RawImport(connection);

			rawImport.importSPSSData(sf);

				
			about.setImport(sf.file.getName());
			
			connection.commit();
			connection.setTransactionIsolation(isolation);
			connection.setAutoCommit(autocommit);
		} catch (SQLException e) {
			MyTools.printSQLExeption(e);
			try {
				connection.rollback();
			} catch (SQLException e1) {
				throw new DaxploreException("Import error. Could not rollback.", e);
			}
			throw new DaxploreException("Import error.", e);
		}
	}
	
	public void importMetaDataFromSPSS(Charset charset) throws DaxploreException {
		if(sf == null) {
			throw new DaxploreException("No SPSSfile loaded");
		}
		boolean autocommit = true;
		try {
			autocommit = connection.getAutoCommit();
			connection.setAutoCommit(false);
			int isolation = connection.getTransactionIsolation();
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			
			RawImport rawImport = new RawImport(connection);

			rawImport.importSPSSMeta(sf, charset);

				
			about.setImport(sf.file.getName());
			
			connection.commit();
			connection.setTransactionIsolation(isolation);
			connection.setAutoCommit(autocommit);
		} catch (SQLException e) {
			MyTools.printSQLExeption(e);
			try {
				connection.rollback();
			} catch (SQLException e1) {
				throw new DaxploreException("Import error. Could not rollback.", e);
			}
			throw new DaxploreException("Import error.", e);
		}
	}
	
	public void importSPSS(File spssFile, Charset charset) throws FileNotFoundException, IOException, DaxploreException{
		SPSSFile sf = null;
		FileFormatInfo ffi = new FileFormatInfo();
		ffi.namesOnFirstLine = false;
		ffi.asciiFormat = ASCIIFormat.CSV;
		ffi.compatibility = Compatibility.GENERIC;
		try {
			sf = new SPSSFile(spssFile,charset);
			sf.logFlag = false;
			sf.loadMetadata();
		} catch (SPSSFileException e2) {
			throw new DaxploreException("SPSSFileException", e2);
		}
		
		boolean autocommit = true;
		try {
			autocommit = connection.getAutoCommit();
			connection.setAutoCommit(false);
			int isolation = connection.getTransactionIsolation();
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			
			RawImport rawImport = new RawImport(connection);

			rawImport.importSPSS(sf, charset);

				
			about.setImport(sf.file.getName());
			
			connection.commit();
			connection.setTransactionIsolation(isolation);
			connection.setAutoCommit(autocommit);
		} catch (SQLException e) {
			MyTools.printSQLExeption(e);
			try {
				connection.rollback();
			} catch (SQLException e1) {
				throw new DaxploreException("Import error. Could not rollback.", e);
			}
			throw new DaxploreException("Import error.", e);
		}
		sf.close();
	}
	
	public About getAbout(){
		return about;
	}
	
	public RawImport getImportedData(){
		try {
			RawImport id = new RawImport(connection);
			return id;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public MetaData getMetaData() throws DaxploreException {
		if(metadata != null) {
			return metadata;
		} else {
			try {
				metadata = new MetaData(connection);
				return metadata;
			} catch (SQLException e) {
				throw new DaxploreException("Couldn't get metadata", e);
			}
		}
	}
	
	public RawMeta getRawMeta() throws DaxploreException {
		try {
			return new RawMeta(connection);
		} catch (SQLException e) {
			throw new DaxploreException("Could't get RawMeta", e);
		}
	}
	
	public void close() throws DaxploreException {
		try {
			connection.close();
		} catch (SQLException e) {
			throw new DaxploreException("Could not close", e);
		}
	}
	
	public File getFile() {
		return file;
	}
	
	protected List<DaxploreTable> getTables() throws DaxploreException {
		List<DaxploreTable> list = new LinkedList<DaxploreTable>();
		list.add(About.table);
		list.addAll(getMetaData().getTables());
		list.addAll(getImportedData().getTables());
		return list;
	}
}
