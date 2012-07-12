package daxplorelib;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.opendatafoundation.data.FileFormatInfo;
import org.opendatafoundation.data.FileFormatInfo.ASCIIFormat;
import org.opendatafoundation.data.FileFormatInfo.Compatibility;
import org.opendatafoundation.data.spss.SPSSFile;
import org.opendatafoundation.data.spss.SPSSFileException;

import tools.MyTools;

import daxplorelib.fileformat.ImportedData;
import daxplorelib.fileformat.RawMeta;
import daxplorelib.metadata.MetaData;

public class DaxploreFile {
	public static final int filetypeversionmajor = 0;
	public static final int filetypeversionminor = 1;
	Connection connection;
	About about;
	File file;
	
	public DaxploreFile(File dbFile, boolean createnew) throws DaxploreException{
		this.file = dbFile;
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			throw new DaxploreException("Sqlite could not be found", e);
		}
		if(dbFile.exists()){
			try {
				connection =  DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
				about = new About(connection);
			} catch (SQLException e) {
				throw new DaxploreException("Not a sqlite file?", e);
			}
		} else if (createnew) { //create new project
			try {
				connection =  DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
				about = new About(connection, createnew);
			} catch (SQLException e) {
				throw new DaxploreException("Could not create new sqlite database (No write access?)", e);
			}
		} else {
			throw new DaxploreException("Not new and doesn't exist");
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
			
			ImportedData importedData = new ImportedData(connection);

			importedData.importSPSS(sf, charset);

				
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
	
	public ImportedData getImportedData(){
		try {
			ImportedData id = new ImportedData(connection);
			return id;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public MetaData getMetaData() throws DaxploreException {
		try {
			return new MetaData(connection);
		} catch (SQLException e) {
			throw new DaxploreException("Couldn't get metadata", e);
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
}
