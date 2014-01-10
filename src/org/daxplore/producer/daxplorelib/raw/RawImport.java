/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.daxplorelib.raw;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.daxplore.producer.daxplorelib.DaxploreException;
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
	
	public RawData getRawData(){
		return rawdata;
	}
	
	public List<String> getColumnList() {
		try {
			List<String> list = rawmeta.getColumns();
			return list;
		} catch (DaxploreException e) {
			return null;
		}
	}
	
	public Integer getNumberOfRows(){
		try {
			int no = rawdata.getNumberOfRows();
			return no;
		} catch (DaxploreException e) {
			e.printStackTrace();
			return null;
		}
	}
}
