/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.daxplorelib.metadata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.daxplore.producer.daxplorelib.DaxploreTable;
import org.daxplore.producer.daxplorelib.SQLTools;

public class MetaCalculation {
	private static final DaxploreTable table = new DaxploreTable("CREATE TABLE metacalc (id INTEGER NOT NULL, column TEXT)", "metacalc");
	private int id;
	private Connection connection;
	
	MetaCalculation(int id, Connection connection) throws SQLException {
		this.id = id;
		this.connection = connection;
		SQLTools.createIfNotExists(MetaCalculation.table, connection);
	}
	
	public MetaCalculation(String column, Connection connection) throws SQLException {
		SQLTools.createIfNotExists(MetaCalculation.table, connection);
		try(PreparedStatement stmt = connection.prepareStatement("SELECT * FROM metacalc WHERE column = ?")) {
			stmt.setString(1, column);
			try (ResultSet rs = stmt.executeQuery()) {
				if(rs.next()) {
					this.id = rs.getInt("id");
				}
			}
		}
	}
	
	public String getColumn() throws SQLException {
		try (PreparedStatement stmt = connection.prepareStatement("SELECT column FROM metacalc WHERE id = ?")) {
			stmt.setInt(1, id);
			try(ResultSet rs = stmt.executeQuery()) {
				if(!rs.next()) {
					return null;
				}
				String column = rs.getString("column");
				return column;
			}
		}
	}
	
	public int getID(){
		return id;
	}
}
