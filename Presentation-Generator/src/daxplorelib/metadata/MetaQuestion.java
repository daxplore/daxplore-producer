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
	protected static final String sqlDefinition = "CREATE TABLE metaquestion (id TEXT PRIMARY KEY, fulltextref TEXT, shorttextref TEXT, scale INTEGER, calculation INTEGER )";
	String id;
	MetaCalculation calculation;
	
	final Connection connection;
	
	public MetaQuestion(String id, TextReference fullTextRef, TextReference shortTextRef, MetaCalculation calculation, MetaScale scale, Connection connection) throws SQLException{
		this.id = id;
		this.connection = connection;
		
		PreparedStatement stmt = connection.prepareStatement("SELECT * FROM metaquestion WHERE id = ?");
		stmt.setString(1, id);
		stmt.execute();
		if(stmt.getResultSet().next()) {
			stmt = connection.prepareStatement("UPDATE metaquestion SET fulltextref = ?, shorttextref = ?, scale = ?, calculation = ? WHERE id = ?");
			stmt.setString(1, fullTextRef.getRef());
			stmt.setString(2, shortTextRef.getRef());
			stmt.setInt(3, scale.getID());
			stmt.setInt(4, calculation.getID());
			stmt.setString(5, id);
			stmt.execute();
		} else {
			stmt = connection.prepareStatement("INSERT INTO metaquestion (id, fulltextref, shorttextref, scale, calculation) VALUES (?, ?, ?, ?)");
			stmt.setString(1, id);
			stmt.setString(2, fullTextRef.getRef());
			stmt.setString(3, shortTextRef.getRef());
			stmt.setInt(4, scale.getID());
			stmt.setInt(5, calculation.getID());
			stmt.execute();
		}
		
		this.calculation = calculation;
	}
		
	public MetaQuestion(String id, Connection connection) {
		this.id = id;
		this.connection = connection;
	}
	
	public MetaQuestion(JSONObject obj, Connection connection) throws SQLException{
		this((String)obj.get("id"), 
				new TextReference((String)obj.get("fulltext"), connection), 
				new TextReference((String)obj.get("shorttext"), connection), 
				new MetaCalculation((String)obj.get("data"), connection), 
				new MetaScale((JSONArray)obj.get("scale"), connection),
				connection
				);
	}
	
	public String getId(){
		return id;
	}
	
	public TextReference getFullTextRef() throws SQLException{
		PreparedStatement stmt = connection.prepareStatement("SELECT fulltextref FROM metaquestion WHERE id = ?");
		stmt.setString(1, id);
		ResultSet rs = stmt.executeQuery();
		if(rs.next()) {
			return new TextReference(rs.getString("fulltextref"), connection);
		} else {
			return null;
		}
	}
	
	public void setFullTextRef(TextReference ref) {
		//TODO: stub
	}
	
	public TextReference getShortTextRef() throws SQLException{
		PreparedStatement stmt = connection.prepareStatement("SELECT shorttextref FROM metaquestion WHERE id = ?");
		stmt.setString(1, id);
		ResultSet rs = stmt.executeQuery();
		if(rs.next()) {
			return new TextReference(rs.getString("shorttextref"), connection);
		} else {
			return null;
		}
	}
	
	public void setShortTextRef(TextReference ref) {
		//TODO: stub
	}
	
	public MetaScale getScale() throws SQLException{
		PreparedStatement stmt = connection.prepareStatement("SELECT scale FROM metaquestion WHERE id = ?");
		stmt.setString(1, id);
		ResultSet rs = stmt.executeQuery();
		if(rs.next()) {
			return new MetaScale(rs.getInt("scale"), connection);
		} else {
			return null;
		}
	}
	
	public void setScale(MetaScale scale) {
		//TODO: stub
	}
	
	public MetaCalculation getCalculation() throws SQLException {
		PreparedStatement stmt = connection.prepareStatement("SELECT calculation FROM metaquestion WHERE id = ?");
		stmt.setString(1, id);
		ResultSet rs = stmt.executeQuery();
		if(rs.next()) {
			return new MetaCalculation(rs.getInt("calculation"), connection);
		} else {
			return null;
		}
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
			obj.put("options", getScale());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return obj.toJSONString();
	}
}
