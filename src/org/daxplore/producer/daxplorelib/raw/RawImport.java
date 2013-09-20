package org.daxplore.producer.daxplorelib.raw;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.DaxploreTable;
import org.daxplore.producer.daxplorelib.SQLTools;
import org.opendatafoundation.data.spss.SPSSFile;

public class RawImport {

	private Connection connection;
	protected RawMeta rawmeta;
	protected RawData rawdata;
	protected String filename;
	protected Date importdate = null;
	
	public RawImport(Connection sqliteDatabase) throws SQLException{
		this.connection = sqliteDatabase;

		this.rawmeta = new RawMeta(connection);
		this.rawdata = new RawData(connection);
	}
	
	public void importSPSS(SPSSFile spssFile) throws SQLException, DaxploreException{	
		rawmeta.importSPSS(spssFile);
		rawdata.importSPSS(spssFile);
	}
	
	public void importSPSSData(SPSSFile spssFile) throws SQLException, DaxploreException {
		rawdata.importSPSS(spssFile);
	}
	
	public void importSPSSMeta(SPSSFile spssFile) throws SQLException {
		rawmeta.importSPSS(spssFile);
	}
	
	/**
	 * Compare the columns of two different versions.
	 * 
	 * @param other ImportedData to compare to.
	 * @return Map of all columns with the values 0 if they exist in both, -1 if it only exists in other and 1 if it only exists in this
	 */
	public Map<String, Integer> compareColumns(RawImport other){
		try {
			Map<String, Integer> columnMap = new HashMap<>();
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
	
	public RawData getRawData(){
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
	
	public List<DaxploreTable> getTables() {
		List<DaxploreTable> list = new LinkedList<>();
		if(SQLTools.tableExists("rawdata", connection)){
			list.add(RawMeta.table);
			list.add(rawdata.table);
		}
		return list;
	}
}
