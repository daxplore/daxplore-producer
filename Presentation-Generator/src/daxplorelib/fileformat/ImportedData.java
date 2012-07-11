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
	protected String filename;
	protected Date importdate = null;
	
	public ImportedData(Connection sqliteDatabase) throws SQLException{
		this.database = sqliteDatabase;

		this.rawmeta = new RawMeta(database);
		this.rawdata = new RawData(this.rawmeta, database);
	}
	
	public void importSPSS(SPSSFile spssFile, Charset charset) throws SQLException, DaxploreException{	
		rawmeta.importSPSS(spssFile, charset);
		rawdata.importSPSS(spssFile, charset, rawmeta);
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
	
	public boolean hasData() {
		return rawmeta.hasData() && rawdata.hasData();
	}
}
