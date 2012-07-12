package daxplorelib.metadata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import tools.MyTools;

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
			stmt.close();
			stmt = connection.prepareStatement("UPDATE metaquestion SET fulltextref = ?, shorttextref = ?, scale = ?, calculation = ? WHERE id = ?");
			stmt.setString(1, fullTextRef.getRef());
			stmt.setString(2, shortTextRef.getRef());
			if(scale != null) {
				stmt.setInt(3, scale.getID());
			} else {
				stmt.setNull(3, java.sql.Types.INTEGER);
			}
			stmt.setInt(4, calculation.getID());
			stmt.setString(5, id);
			stmt.executeUpdate();
			stmt.close();
		} else {
			stmt.close();
			stmt = connection.prepareStatement("INSERT INTO metaquestion (id, fulltextref, shorttextref, scale, calculation) VALUES (?, ?, ?, ?, ?)");
			stmt.setString(1, id);
			stmt.setString(2, fullTextRef.getRef());
			stmt.setString(3, shortTextRef.getRef());
			if(scale != null) {
				stmt.setInt(4, scale.getID());
			} else {
				stmt.setNull(3, java.sql.Types.INTEGER);
			}
			stmt.setInt(5, calculation.getID());
			stmt.executeUpdate();
			stmt.close();
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
				obj.containsKey("scale") ? new MetaScale((JSONArray)obj.get("scale"), connection, true) : null, //MetaScale should be null if there is no scale
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
			TextReference tr = new TextReference(rs.getString("fulltextref"), connection);
			stmt.close();
			return tr;
		} else {
			stmt.close();
			return null;
		}
	}
	
	/**
	 * Updates fulltextref in the database.
	 * @param ref
	 * @throws SQLException
	 */
	public void setFullTextRef(TextReference ref) throws SQLException {
		PreparedStatement stmt = connection.prepareStatement("UPDATE metaquestion SET fulltextref = ? WHERE id = ?");
		stmt.setString(1, ref.getRef());
		stmt.setString(2, id);
		stmt.executeUpdate();
		stmt.close();
	}
	
	public TextReference getShortTextRef() throws SQLException{
		PreparedStatement stmt = connection.prepareStatement("SELECT shorttextref FROM metaquestion WHERE id = ?");
		stmt.setString(1, id);
		ResultSet rs = stmt.executeQuery();
		if(rs.next()) {
			TextReference tr = new TextReference(rs.getString("shorttextref"), connection);
			stmt.close();
			return tr;
		} else {
			stmt.close();
			return null;
		}
	}
	
	/**
	 * Updates shorttextref in the database.
	 * @param ref
	 * @throws SQLException
	 */
	public void setShortTextRef(TextReference ref) throws SQLException {
		PreparedStatement stmt = connection.prepareStatement("UPDATE metaquestion SET shorttextref = ? WHERE id = ?");
		stmt.setString(1, ref.getRef());
		stmt.setString(2, id);
		stmt.executeUpdate();
		stmt.close();
	}
	
	public MetaScale getScale() throws SQLException{
		PreparedStatement stmt = connection.prepareStatement("SELECT scale FROM metaquestion WHERE id = ?");
		stmt.setString(1, id);
		ResultSet rs = stmt.executeQuery();
		if(rs.next()) {
			int scaleid = rs.getInt("scale");
			if(rs.wasNull()) {
				stmt.close();
				return null;
			} else {
				MetaScale ms = new MetaScale(scaleid, connection);
				stmt.close();
				return ms;
			}
		} else {
			stmt.close();
			return null;
		}
	}
	
	/**
	 * Updates metascale in the database.
	 * @param scale
	 * @throws SQLException
	 */
	public void setScale(MetaScale scale) throws SQLException {
		PreparedStatement stmt = connection.prepareStatement("UPDATE metaquestion SET scale = ? WHERE id = ?");
		stmt.setInt(1, scale.getID());
		stmt.setString(2, id);
		stmt.executeUpdate();
		stmt.close();
	}
	
	public MetaCalculation getCalculation() throws SQLException {
		PreparedStatement stmt = connection.prepareStatement("SELECT calculation FROM metaquestion WHERE id = ?");
		stmt.setString(1, id);
		ResultSet rs = stmt.executeQuery();
		if(rs.next()) {
			MetaCalculation mc = new MetaCalculation(rs.getInt("calculation"), connection);
			stmt.close();
			return mc;
		} else {
			stmt.close();
			return null;
		}
	}
	
	public static List<MetaQuestion> getAll(Connection connection) throws SQLException {
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT id FROM metaquestion ORDER BY id asc");
		List<MetaQuestion> list = new LinkedList<MetaQuestion>();
		while(rs.next()) {
			list.add(new MetaQuestion(rs.getString("id"), connection));
		}
		stmt.close();
		return list;
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
			MetaScale scale = getScale();
			if(scale != null) {
				obj.put("options", getScale());
			}
		} catch (SQLException e) {
			MyTools.printSQLExeption(e);
		}
		return obj.toJSONString();
	}
}
