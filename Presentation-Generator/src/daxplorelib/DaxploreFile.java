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

import daxplorelib.fileformat.ImportedData;

public class DaxploreFile {
	public static final int filetypeversionmajor = 0;
	public static final int filetypeversionminor = 1;
	Connection sqliteDatabase;
	About about;
	
	public DaxploreFile(File dbFile, boolean createnew) throws DaxploreException{
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			throw new DaxploreException("Sqlite could not be found", e);
		}
		if(dbFile.exists()){
			try {
				sqliteDatabase =  DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
				about = new About(sqliteDatabase);
			} catch (SQLException e) {
				throw new DaxploreException("Not a sqlite file?", e);
			}
		} else if (createnew) { //create new project
			try {
				sqliteDatabase =  DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
				about = new About(sqliteDatabase, createnew);
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
		//Savepoint save = null;
		
		boolean autocommit = true;
		try {
			//save = sqliteDatabase.setSavepoint();
			autocommit = sqliteDatabase.getAutoCommit();
			sqliteDatabase.setAutoCommit(false);
			sqliteDatabase.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			
			ImportedData importedData = new ImportedData(sqliteDatabase);
			
			importedData.importSPSS(sf, charset);
			
			about.setImport(sf.file.getName());
			
			sqliteDatabase.commit();
		} catch (SQLException e) {
			try {
				sqliteDatabase.rollback();
			} catch (SQLException e1) {
				throw new DaxploreException("Import error. Could not rollback.", e);
			}
			throw new DaxploreException("Import error.", e);
		} finally {
			try {
				sqliteDatabase.setAutoCommit(autocommit);
			} catch (SQLException e) {
				throw new DaxploreException("Import error. Could not set autocommit. General wtf.", e);
			}
		}
	}
	
	public About getAbout(){
		return about;
	}
}
