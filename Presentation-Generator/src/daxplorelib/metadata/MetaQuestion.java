package daxplorelib.metadata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

/*
 * questionobject
{
	id: string
	data: <dataobject>,
	longtext: <stringreference>,
	shorttext: <stringreference>,
	options: [<stringreference>],
}
 */

public class MetaQuestion implements JSONAware, Comparable<MetaQuestion>{
	protected static final String sqlDefinition = "CREATE TABLE metaquestion (id TEXT PRIMARY KEY, fulltextref TEXT, shorttextref TEXT, scale INTEGER )";
	String id;
	MetaCalculation calculation;
	MetaScale scale;
	
	Connection connection;
	
	public MetaQuestion(String id, String fullTextRef, String shortTextRef, MetaCalculation calculation, MetaScale scale, Connection connection) throws SQLException{
		this.id = id;
		this.connection = connection;
		
		PreparedStatement stmt = connection.prepareStatement("SELECT * FROM metaquestion WHERE id = ?");
		stmt.setString(1, id);
		stmt.execute();
		if(stmt.getResultSet().next()) {
			stmt = connection.prepareStatement("UPDATE metaquestion SET fulltextref = ?, shorttextref = ?, scale = ? WHERE id = ?");
			stmt.setString(1, fullTextRef);
			stmt.setString(2, shortTextRef);
			stmt.setInt(3, scale.getID());
			stmt.setString(4, id);
			stmt.execute();
		} else {
			stmt = connection.prepareStatement("INSERT INTO metaquestion (id, fulltextref, shorttextref, scale) VALUES (?, ?, ?, ?)");
			stmt.setString(1, id);
			stmt.setString(2, fullTextRef);
			stmt.setString(3, shortTextRef);
			stmt.setInt(4, scale.getID());
			stmt.execute();
		}
		
		this.calculation = calculation;
		this.scale = scale;
	}
	
	public MetaQuestion(String id, Connection connection) {
		this.id = id;
		this.connection = connection;
	}
	
	public MetaQuestion(JSONObject obj, Connection connection) throws SQLException{
		this((String)obj.get("id"), 
				(String)obj.get("fulltext"), 
				(String)obj.get("shorttext"), 
				new MetaCalculation((String)obj.get("data")), 
				new MetaScale((JSONArray)obj.get("scale"), connection),
				connection
				);
	}
	
	public String getId(){
		return id;
	}
	
	public String getFullTextRef() throws SQLException{
		PreparedStatement stmt = connection.prepareStatement("SELECT fulltextref FROM metaquestion WHERE id = ?");
		stmt.setString(1, id);
		ResultSet rs = stmt.executeQuery();
		if(rs.next()) {
			return rs.getString("fulltextref");
		} else {
			return null;
		}
	}
	
	public String getShortTextRef() throws SQLException{
		PreparedStatement stmt = connection.prepareStatement("SELECT shorttextref FROM metaquestion WHERE id = ?");
		stmt.setString(1, id);
		ResultSet rs = stmt.executeQuery();
		if(rs.next()) {
			return rs.getString("shorttextref");
		} else {
			return null;
		}
	}
	
	public MetaScale getScale(){
		return scale;
	}

	@Override
	public int compareTo(MetaQuestion arg0) {
		return id.compareTo(arg0.id);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public String toJSONString() {
		JSONObject obj = new JSONObject();
		obj.put("id", id);
		try {
			obj.put("fulltext", getFullTextRef());
			obj.put("shorttext", getShortTextRef());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		obj.put("options", scale);
		
		return obj.toJSONString();
	}
}
