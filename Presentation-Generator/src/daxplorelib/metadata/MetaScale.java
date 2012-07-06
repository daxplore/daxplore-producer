package daxplorelib.metadata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import tools.MyTools;
import tools.Pair;

public class MetaScale implements JSONAware {
	public static final String sqlDefinition = "CREATE TABLE metascale (id INTEGER, textref STRING, order INTEGER, value REAL)";
	
	Connection connection;
	int id;
	
	public MetaScale(int id, Connection connection) {
		this.connection = connection;
		this.id = id;
	}
	
	public MetaScale(List<Pair<TextReference, Double>> options, Connection connection) throws SQLException {
		this.connection = connection;
	    StringBuilder builder = new StringBuilder();
	    builder.append("SELECT * FROM metascale WHERE textref IN (");
	    for(Pair<TextReference, Double> tr : options) {
	        builder.append("?");
	        if(options.indexOf(tr) < options.size() -1 ) {
	        	builder.append(",");
	        }
	    }
	    builder.append(") ORDER BY id, order");
	    PreparedStatement stmt = connection.prepareStatement(builder.toString());
	    for(int i = 0; i < options.size(); i++) {
	    	stmt.setString(i+1, options.get(i).getKey().getRef());
	    }
	    ResultSet rs = stmt.executeQuery();
	    
	    /*
	     * See if the scale exists in the database. Search for list in list.
	     */
	    List<Integer> hits = new LinkedList<Integer>();
	    while(rs.next()) {
	    	for(int i = 0; i < options.size(); i++) {
	    		if(!options.get(i).getKey().getRef().equals(rs.getString("textref")) || 
	    				!options.get(i).getValue().equals(rs.getDouble("value"))) {
	    			break;
	    		} else if(i == options.size() -1) {
	    			hits.add(rs.getInt("id"));
	    		} else if(!rs.next()){
	    			break;
	    		}
	    	}
	    }
	    
	    boolean found = false;
	    stmt = connection.prepareStatement("SELECT textref, value FROM metascale WHERE id = ? ORDER BY order ASC");
	    for(Integer i: hits) {
	    	stmt.setInt(1, i);
	    	rs = stmt.executeQuery();
	    	for(Pair<TextReference, Double> tr : options) {
	    		rs.next();
	    		if(!tr.getKey().getRef().equals(rs.getString("textref"))) {
	    			break;
	    		} else if (!tr.getValue().equals(rs.getDouble("value"))) {
	    			break;
	    		}
	    	}
	    	if(rs.next()) {
	    		continue;
	    	}
	    	id = i;
	    	found = true;
	    	break;
	    }
	    
	    if(!found) {
	    	stmt = connection.prepareStatement("SELECT max(id) FROM metascale");
	    	rs = stmt.executeQuery();
	    	rs.next();
	    	id = rs.getInt(1) +1; //TODO: Handle null
	    	stmt = connection.prepareStatement("INSERT INTO metascalse (id, textref, order, value) VALUES (?, ?, ?, ?)");
	    	for(int i = 0; i < options.size(); i++) {
	    		stmt.setInt(1, id);
		    	stmt.setString(2, options.get(i).getKey().getRef());
		    	stmt.setInt(3, i);
		    	stmt.setDouble(4, options.get(i).getValue());
		    	stmt.execute();	    		
	    	}
	    }
	}
	
	public MetaScale(JSONArray obj, Connection connection) throws SQLException {
		this(JSONtoList(obj, connection), connection);
	}
	
	public List<Pair<TextReference, Double>> getRefereceList() throws SQLException {
		PreparedStatement stmt = connection.prepareStatement("SELECT textref, value FROM metascale WHERE id = ? ORDER BY order ASC");
		stmt.setInt(1, id);
		ResultSet rs = stmt.executeQuery();
		List<Pair<TextReference, Double>> list = new LinkedList<Pair<TextReference, Double>>();
		while(rs.next()) {
			list.add(new Pair<TextReference, Double>(new TextReference(rs.getString("textref"), connection), rs.getDouble("value")));
		}
		return list;
	}
	
	public static List<MetaScale> getAll(Connection connection) throws SQLException {
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT UNIQUE(id) AS uid FROM metascale ORDER BY id");
		List<MetaScale> list = new LinkedList<MetaScale>();
		while(rs.next()) {
			list.add(new MetaScale(rs.getInt("uid"), connection));
		}
		return list;
	}
	
	protected static List<Pair<TextReference, Double>> JSONtoList(JSONArray obj, Connection connection) throws SQLException {
		List<Pair<TextReference, Double>> list = new LinkedList<Pair<TextReference, Double>>();
		for (int i = 0; i < obj.size(); i++) {
			JSONObject o = (JSONObject)obj.get(i);
			list.add(new Pair<TextReference, Double>(
					new TextReference((String)o.get("textref"), connection), 
					Double.parseDouble((String)o.get("value"))));
		}
		return list;
	}
	
	public int getID(){
		return id;
	}
	
	public void remove() throws SQLException {
		PreparedStatement stmt = connection.prepareStatement("DELETE FROM metascale WHERE id = ?");
		stmt.setInt(1, id);
		stmt.executeUpdate();
	}
	
	public boolean equalsLocale(MetaScale other, Locale locale) throws SQLException {
		List<Pair<TextReference, Double>> otherRefList = other.getRefereceList();
		List<Pair<TextReference, Double>> thisRefList = getRefereceList();
		
		if(otherRefList.size() != thisRefList.size()) { return false; }
		
		for(int i = 0; i < thisRefList.size(); i++) {
			if(!thisRefList.get(i).getValue().equals(otherRefList.get(i).getValue())) { return false; }
			if(!thisRefList.get(i).getKey().equalsLocale(otherRefList.get(i).getKey(), locale)) { return false; }
		}
		
		return true;
	}
	
	public boolean equals(Object obj) {
		if(obj instanceof MetaScale) {
			MetaScale other = (MetaScale)obj;
			return id == other.id;
		} else {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public String toJSONString() {
		JSONArray arr = new JSONArray();
		try {
			List<Pair<TextReference, Double>> list = getRefereceList();
			for(Pair<TextReference, Double> tr : list) {
				JSONObject o = new JSONObject();
				o.put("textref", tr.getKey().getRef());
				o.put("value", tr.getValue());
				arr.add(o);
			}
		} catch (SQLException e) {
			MyTools.printSQLExeption(e);
		}
		return arr.toJSONString();
	}
}
