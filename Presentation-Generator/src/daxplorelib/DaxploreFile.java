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

import daxplorelib.fileformat.About;
import daxplorelib.fileformat.ImportedData;

public class DaxploreFile {
	public static final int filetypeversionmajor = 1;
	public static final int filetypeversionminor = 1;
	Connection sqliteDatabase;
	About about;
	
	public DaxploreFile(File dbFile, boolean createnew){
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			throw new Error("Sqlite could not be found");
		}
		if(dbFile.exists()){
			try {
				sqliteDatabase =  DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
				about = new About(sqliteDatabase);
			} catch (SQLException e) {
				System.err.println("Not a sqlite file?");
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (createnew) { //create new project
			try {
				sqliteDatabase =  DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
				about = new About(sqliteDatabase, createnew);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			throw new Error("Not new and dosn't exist");
		}
		/*try {
			about = new About(sqliteDatabase);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Not a Daxplore project file?");
			throw new Error("Not a Daxplore project");
		}*/
	}
	
	public boolean importSPSS(File spssFile, Charset charset) throws FileNotFoundException, IOException, DaxploreException{
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
			// TODO Auto-generated catch block
			e2.printStackTrace();
			throw new DaxploreException("SPSSFileException");
		}
		//Savepoint save = null;
		
		try {
			//save = sqliteDatabase.setSavepoint();
			boolean autocommit = sqliteDatabase.getAutoCommit();
			sqliteDatabase.setAutoCommit(false);
			sqliteDatabase.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			
			ImportedData importedData = new ImportedData(sqliteDatabase);
			
			importedData.importSPSS(sf, charset);

			sqliteDatabase.commit();
			sqliteDatabase.setAutoCommit(autocommit);
			return true;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println(e.getMessage());
			/*try {
				if(save != null){
					sqliteDatabase.rollback(save);
				}
				return false;
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return false;
			}*/
			return false;
		}
		
		
		
	}
	
}
