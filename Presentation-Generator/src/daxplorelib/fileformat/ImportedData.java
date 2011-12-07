package daxplorelib.fileformat;

import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import org.opendatafoundation.data.spss.SPSSFile;

import daxplorelib.DaxploreException;
import daxplorelib.SQLTools;

public class ImportedData {

	private Connection database;
	protected RawMeta rawmeta;
	protected RawData rawdata;
	protected int version;
	
	public ImportedData(Connection sqliteDatabase) throws SQLException{
		this.database = sqliteDatabase;
		Date today = new Date();
		
		if(SQLTools.tableExists("sqlite_sequence", sqliteDatabase)){
			Statement verstmt = sqliteDatabase.createStatement();
			ResultSet rs = verstmt.executeQuery("SELECT seq FROM sqlite_sequence WHERE name='rawversions'");
			if(rs.next()){
				version = rs.getInt("seq") +1;
			} else version = 1;
		} else {
			version = 1;
		}
		PreparedStatement stmt = database.prepareStatement("INSERT INTO rawversions (rawmeta, rawdata, importdate, filename) values (?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
		
		/*ResultSet res = stmt.getGeneratedKeys();
		int key = 0;
		while (res.next()) {
		    key = res.getInt(1);
		}
		this.version = key;*/
		System.out.println("Creating version " + version);
		stmt.setString(1, "rawmeta" + version);
		stmt.setString(2, "rawdata" + version);
		stmt.setLong(3, today.getTime());
		stmt.execute();

		this.rawmeta = new RawMeta("rawmeta" + version, database);
		this.rawdata = new RawData("rawdata" + version, this.rawmeta, database);
	}
	
	public ImportedData(int version, Connection sqliteDatabase) throws SQLException{
		this.database = sqliteDatabase;
		this.version = version;
		PreparedStatement stmt = database.prepareStatement("SELECT * FROM rawversions WHERE version = ?");
		stmt.setInt(1, version);
		ResultSet res = stmt.executeQuery();
		res.next();
		this.rawmeta = new RawMeta(res.getString("rawmeta"), database);
		this.rawdata = new RawData(res.getString("rawdata"), rawmeta, database);
	}
	
	public void importSPSS(SPSSFile spssFile, Charset charset) throws SQLException, DaxploreException{
		String filename = spssFile.file.getName();
		
		PreparedStatement stmt = database.prepareStatement("UPDATE rawversions SET filename = ? WHERE version = ?");
		stmt.setString(1, filename);
		stmt.setInt(2, version);
		stmt.execute();
		
		rawmeta.importSPSS(spssFile, charset);
		rawdata.importSPSS(spssFile, charset);
	}
	
	public RawMeta getRawMeta(){
		return rawmeta;
	}
	
	public RawData getRawData(){
		return rawdata;
	}
}
