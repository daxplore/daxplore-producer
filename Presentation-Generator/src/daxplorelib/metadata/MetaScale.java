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

public class MetaScale implements JSONAware{
	public static final String sqlDefinition = "CREATE TABLE metascale (id INTEGER, textref STRING, order INTEGER)";
	
	Connection connection;
	int id;
	
	public MetaScale(int id, Connection connection) {
		this.connection = connection;
		this.id = id;
	}
	
	public MetaScale(List<TextReference> options, Connection connection) throws SQLException {
		this.connection = connection;
	    StringBuilder builder = new StringBuilder();
	    builder.append("SELECT * FROM metascale WHERE textref IN (");
	    for(TextReference tr : options) {
	        builder.append("?");
	        if(options.indexOf(tr) < options.size() -1 ) {
	        	builder.append(",");
	        }
	    }
	    builder.append(") OREDER BY id, order");
	    PreparedStatement stmt = connection.prepareStatement(builder.toString());
	    for(int i = 0; i < options.size(); i++) {
	    	stmt.setString(i+1, options.get(i).getRef());
	    }
	    ResultSet rs = stmt.executeQuery();
	    
	    /*
	     * See if the scale exists in the database. Search for list in list.
	     */
	    List<Integer> hits = new LinkedList<Integer>();
	    while(rs.next()){
	    	for(int i = 0; i < options.size(); i++) {
	    		if(!options.get(i).getRef().equals(rs.getString("textref"))) {
	    			break;
	    		} else if(i == options.size() -1) {
	    			hits.add(rs.getInt("id"));
	    		} else {
	    			if(!rs.next()){
	    				break;
	    			}
	    		}
	    	}
	    }
	    
	    boolean found = false;
	    stmt = connection.prepareStatement("SELECT textref FROM metascale WHERE id = ? ORDER BY order ASC");
	    for(Integer i: hits) {
	    	stmt.setInt(1, i);
	    	rs = stmt.executeQuery();
	    	for(TextReference tr : options) {
	    		rs.next();
	    		if(!tr.getRef().equals(rs.getString("textref"))) {
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
	    	id = rs.getInt(1) +1;
	    	stmt = connection.prepareStatement("INSERT INTO metascalse (id, textref, order) VALUES (?, ?, ?)");
	    	for(int i = 0; i < options.size(); i++) {
	    		stmt.setInt(1, id);
		    	stmt.setString(2, options.get(0).getRef());
		    	stmt.setInt(3, i);
		    	stmt.execute();	    		
	    	}
	    }
	}
	
	public MetaScale(JSONArray obj, Connection connection) throws SQLException {
		this(JSONtoList(obj, connection), connection);
	}
	
	public List<TextReference> getRefereceList() throws SQLException {
		PreparedStatement stmt = connection.prepareStatement("SELECT textref FROM metascale WHERE id = ? ORDER BY order ASC");
		stmt.setInt(1, id);
		ResultSet rs = stmt.executeQuery();
		List<TextReference> list = new LinkedList<TextReference>();
		while(rs.next()) {
			list.add(new TextReference(rs.getString("textref"), connection));
		}
		return list;
	}
	
	protected static List<TextReference> JSONtoList(JSONArray obj, Connection connection) {
		List<TextReference> list = new LinkedList<TextReference>();
		for (int i = 0; i < obj.size(); i++) {
			list.add(new TextReference((String)obj.get(i), connection));
		}
		return list;
	}
	
	public int getID(){
		return id;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String toJSONString() {
		JSONArray arr = new JSONArray();
		List<TextReference> list;
		try {
			list = getRefereceList();
			for(TextReference tr : list) {
				arr.add(tr.getRef());
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return arr.toJSONString();
	}
}
