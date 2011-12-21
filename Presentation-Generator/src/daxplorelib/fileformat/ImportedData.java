package daxplorelib.fileformat;

import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendatafoundation.data.spss.SPSSFile;

import daxplorelib.DaxploreException;
import daxplorelib.SQLTools;

public class ImportedData {

	private Connection database;
	protected RawMeta rawmeta;
	protected RawData rawdata;
	protected int version;
	protected String filename;
	protected Date importdate = null;
	
	public ImportedData(Connection sqliteDatabase) throws SQLException{
		this.database = sqliteDatabase;
		//importdate = new Date();
		
		if(SQLTools.tableExists("sqlite_sequence", sqliteDatabase)){
			Statement verstmt = sqliteDatabase.createStatement();
			ResultSet rs = verstmt.executeQuery("SELECT seq FROM sqlite_sequence WHERE name='rawversions'");
			if(rs.next()){
				version = rs.getInt("seq") +1;
			} else version = 1;
		} else {
			version = 1;
		}
		//PreparedStatement stmt = database.prepareStatement("INSERT INTO rawversions (rawmeta, rawdata, importdate, filename) values (?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
		PreparedStatement stmt = database.prepareStatement("INSERT INTO rawversions (rawmeta, rawdata) values (?,?)", Statement.RETURN_GENERATED_KEYS);

		/*ResultSet res = stmt.getGeneratedKeys();
		int key = 0;
		while (res.next()) {
		    key = res.getInt(1);
		}
		this.version = key;*/
		System.out.println("Creating version " + version);
		stmt.setString(1, "rawmeta" + version);
		stmt.setString(2, "rawdata" + version);
		//stmt.setLong(3, importdate.getTime());
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
		this.filename = res.getString("filename");
		this.importdate = res.getDate("importdate");
		this.rawmeta = new RawMeta(res.getString("rawmeta"), database);
		this.rawdata = new RawData(res.getString("rawdata"), rawmeta, database);
	}
	
	public void importSPSS(SPSSFile spssFile, Charset charset) throws SQLException, DaxploreException{
		String filename = spssFile.file.getName();
		importdate = new Date();
		PreparedStatement stmt = database.prepareStatement("UPDATE rawversions SET importdate = ? , filename = ? WHERE version = ?");
		stmt.setLong(1, importdate.getTime());
		stmt.setString(2, filename);
		stmt.setInt(3, version);
		stmt.execute();
		
		rawmeta.importSPSS(spssFile, charset);
		rawdata.importSPSS(spssFile, charset);
	}
	
	/**
	 * Compare the columns of two different versions.
	 * 
	 * @param other ImportedData to compare to.
	 * @return Map of all columns with the values 0 if they exist in both, -1 if it only exists in other and 1 if it only exists in this
	 */
	public Map<String, Integer> compareColumns(ImportedData other){
		try {
			Map<String, Integer> columnMap = new HashMap<String,Integer>();
			List<String> columnsthis = rawmeta.getColumns();
			List<String> columnsother = other.rawmeta.getColumns();
			for(String s: columnsthis){
				if(columnsother.contains(s)) {
					columnMap.put(s, 0);
				} else {
					columnMap.put(s, 1);
				}
			}
			for(String s: columnsother){
				if(!columnsthis.contains(s)){
					columnMap.put(s, -1);
				}
			}
			return columnMap;
		} catch (SQLException e){
			return null;
		}
	}
	
	RawMeta getRawMeta(){
		return rawmeta;
	}
	
	RawData getRawData(){
		return rawdata;
	}
	
	public List<String> getColumnList() {
		try {
			List<String> list = rawmeta.getColumns();
			return list;
		} catch (SQLException e) {
			return null;
		}
	}
	
	public Integer getNumberOfRows(){
		try {
			int no = rawdata.getNumberOfRows();
			return no;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public int getVersion(){
		return version;
	}
	
	public Date getImportDate() {
		return importdate;
	}
	
	public String getFilename() {
		return filename;
	}
}
