package daxplorelib.metadata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import daxplorelib.DaxploreTable;

import tools.MyTools;
import tools.Pair;

public class CopyOfMetaScale implements JSONAware {
	protected static final DaxploreTable table = new DaxploreTable("CREATE TABLE metascale (id INTEGER, textref STRING, ord INTEGER, value REAL)", "metascale");

	public class MetaScaleManager {
		Map<Integer, CopyOfMetaScale> scaleMap = new HashMap<Integer, CopyOfMetaScale>();
		Connection connection;
		
		public MetaScaleManager(Connection connection) {
			this.connection = connection;
		}
		
		protected void init() {
			
		}
		
		public CopyOfMetaScale getMetaScale(int id) {
			if(scaleMap.containsKey(id)) {
				return scaleMap.get(id);
			} else {
				//get stuff from db here and create a metascale
				return null;
			}
		}
		
		public CopyOfMetaScale createMetaScale() {
			//create MetaScale here (look at MetaScale(List<Pair<TextReference,Double>>, Connection) for code
			return null;
		}
		
		public void remove(int id) {
			
		}
		
		public void saveAll() {
			for(CopyOfMetaScale ms: scaleMap.values()) {
				if(ms.modified) {
					//Save here
					ms.modified = false;
				}
			}
			//save all unsaved MetaScales
		}
	}
	
	public class Option {
		TextReference textRef;
		double value;
		String transformation;
		
		public Option(TextReference textRef, double value, String transformation) {
			this.textRef = textRef; this.value = value; this.transformation = transformation;
		}
	}
	
	
	/** Each Option's position is defined by the order of this list */
	List<Option> options;
	
	Connection connection;
	int id;
	boolean modified = false;
	
	public CopyOfMetaScale(int id, List<Option> options) {
		this.id = id;
		this.options = options;
	}
	
	public CopyOfMetaScale(List<Pair<TextReference, Double>> options, Connection connection) throws SQLException {
		this.connection = connection;
	    StringBuilder builder = new StringBuilder();
	    builder.append("SELECT * FROM metascale WHERE textref IN (");
	    for(Pair<TextReference, Double> tr : options) {
	        builder.append("?");
	        if(options.indexOf(tr) < options.size() -1 ) {
	        	builder.append(",");
	        }
	    }
	    builder.append(") ORDER BY id, ord");
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
	    stmt.close();
	    
	    boolean found = false;
	    stmt = connection.prepareStatement("SELECT textref, value FROM metascale WHERE id = ? ORDER BY ord ASC");
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
	    stmt.close();
	    
	    if(!found) {
	    	stmt = connection.prepareStatement("SELECT max(id) FROM metascale");
	    	rs = stmt.executeQuery();
	    	rs.next();
	    	id = rs.getInt(1) +1; //TODO: Handle null
	    	stmt = connection.prepareStatement("INSERT INTO metascale (id, textref, ord, value) VALUES (?, ?, ?, ?)");
	    	for(int i = 0; i < options.size(); i++) {
	    		stmt.setInt(1, id);
		    	stmt.setString(2, options.get(i).getKey().getRef());
		    	stmt.setInt(3, i);
		    	stmt.setDouble(4, options.get(i).getValue());
		    	stmt.execute();	    		
	    	}
	    	stmt.close();
	    }
	}
	
	public CopyOfMetaScale(JSONArray obj, Connection connection, boolean extra) throws SQLException {
		this(JSONtoList(obj, connection), connection);
	}
	
	public List<Pair<TextReference, Double>> getRefereceList() throws SQLException {
		PreparedStatement stmt = connection.prepareStatement("SELECT textref, value FROM metascale WHERE id = ? ORDER BY ord ASC");
		stmt.setInt(1, id);
		ResultSet rs = stmt.executeQuery();
		List<Pair<TextReference, Double>> list = new LinkedList<Pair<TextReference, Double>>();
		while(rs.next()) {
			list.add(new Pair<TextReference, Double>(new TextReference(rs.getString("textref"), connection), rs.getDouble("value")));
		}
		stmt.close();
		return list;
	}
	
	public static List<CopyOfMetaScale> getAll(Connection connection) throws SQLException {
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT DISTINCT id FROM metascale ORDER BY id");
		List<CopyOfMetaScale> list = new LinkedList<CopyOfMetaScale>();
		while(rs.next()) {
			//list.add(new MetaScale(rs.getInt("id"), connection));
		}
		stmt.close();
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
		stmt.close();
	}
	
	public boolean equalsLocale(CopyOfMetaScale other, Locale locale) throws SQLException {
		List<Pair<TextReference, Double>> otherRefList = other.getRefereceList();
		List<Pair<TextReference, Double>> thisRefList = getRefereceList();
		
		if(otherRefList.size() != thisRefList.size()) { return false; }
		
		for(int i = 0; i < thisRefList.size(); i++) {
			if(!thisRefList.get(i).getValue().equals(otherRefList.get(i).getValue())) { return false; }
			if(!thisRefList.get(i).getKey().equalsLocale(otherRefList.get(i).getKey(), locale)) { return false; }
			if(thisRefList.get(i).getKey().get(locale).equals("")) { return false; }
		}
		
		return true;
	}
	
	public boolean equals(Object obj) {
		if(obj instanceof CopyOfMetaScale) {
			CopyOfMetaScale other = (CopyOfMetaScale)obj;
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
