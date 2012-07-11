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

public class MetaGroup implements JSONAware{
	protected static final String sqlDefinition = "CREATE TABLE metagroup (id INTEGER PRIMARY KEY, textref TEXT, ind INTEGER, type INTEGER)";
	protected static final String sqlDefinition2 = "CREATE TABLE metagrouprel (groupid INTEGER, questionid TEXT)";		
	
	protected int id;
	private Connection connection;
	
	public enum GroupType {
		QUESTIONS(0), PERSPECTIVE(1);
		
		protected final int type;
		GroupType(int type) {
			this.type = type;
		}
		
		public int asInt() {
			return type;
		}
		
		public static GroupType fromInt(int i) {
			switch(i) {
			case 0:
				return QUESTIONS;
			case 1:
				return PERSPECTIVE;
			}
			return null;
		}
	}
	
	public MetaGroup(int id, Connection connection) {
		this.connection = connection;
		this.id = id;
	}
	
	public MetaGroup(TextReference textref, int index, GroupType type, List<MetaQuestion> questions, Connection connection) throws SQLException {
		this.connection = connection;
		
		// Check if group already exists.
		PreparedStatement stmt = connection.prepareStatement("SELECT id FROM metagroup WHERE textref = ? AND type = ?");
		stmt.setString(1, textref.getRef());
		stmt.setInt(2, type.asInt());
		ResultSet rs = stmt.executeQuery();
		if(rs.next()) {
			id = rs.getInt("id");
		} else {
			//if not create new group
			stmt = connection.prepareStatement("INSERT INTO metagroup (textref, ind, type) VALUES (?, ?, ?)");
			stmt.setString(1, textref.getRef());
			stmt.setInt(2, index);
			stmt.setInt(3, type.asInt());
			stmt.execute();
			rs = stmt.getGeneratedKeys();
			rs.next();
			id = rs.getInt(1);
		}
		
		if(questions != null){
			removeQuestions();
			addQuestions(questions);
		}
	}
	
	public MetaGroup(JSONObject obj, GroupType type, Connection connection) throws SQLException {
		this(new TextReference((String)obj.get("textref"), connection), (Integer)obj.get("index"), type, null, connection);
		List<MetaQuestion> questions = new LinkedList<MetaQuestion>();
		JSONArray questionArr = (JSONArray) obj.get("questions");
		for(int i = 0; i < questionArr.size(); i++) {
			questions.add(new MetaQuestion((String)questionArr.get(i), connection));
		}
		addQuestions(questions);
	}
	
	protected void addQuestions(List<MetaQuestion> questions) throws SQLException{
		PreparedStatement stmt = connection.prepareStatement("INSERT INTO metagrouprel (groupid, questionid) VALUES (?, ?)");
		for(MetaQuestion q: questions){
			stmt.setInt(1, id);
			stmt.setString(2, q.getId());
			stmt.executeUpdate();
		}
	}
	
	protected void removeQuestions() throws SQLException {
		PreparedStatement stmt = connection.prepareStatement("DELETE FROM metagrouprel WHERE groupid = ?");
		stmt.setInt(1, id);
		stmt.executeUpdate();
	}
	
	public TextReference getTextRef() throws SQLException {
		PreparedStatement stmt = connection.prepareStatement("SELECT textref FROM metagroup WHERE id = ?");
		stmt.setInt(1, id);
		ResultSet rs = stmt.executeQuery();
		if(rs.next()) {
			return new TextReference(rs.getString("textref"), connection);
		} else {
			return null;
		}
	}
	
	/**
	 * Updates textref in the database.
	 * @param ref
	 * @throws SQLException 
	 */
	public void setTextRef(TextReference ref) throws SQLException {
		PreparedStatement stmt = connection.prepareStatement("UPDATE metagroup SET shorttextref = ? WHERE id = ?");
		stmt.setString(1, ref.getRef());
		stmt.setInt(2, id);
		stmt.executeUpdate();
	}
	
	public GroupType getType() throws SQLException {
		PreparedStatement stmt = connection.prepareStatement("SELECT type FROM metagroup WHERE groupid = ?");
		stmt.setInt(1, id);
		ResultSet rs = stmt.executeQuery();
		if(rs.next()) {
			int type = rs.getInt("type");
			return GroupType.fromInt(type);
		} else {
			return null;
		}
	}

	public List<MetaQuestion> getQuestions() throws SQLException {
		List<MetaQuestion> list = new LinkedList<MetaQuestion>();
		PreparedStatement stmt = connection.prepareStatement("SELECT questionid FROM metagrouprel WHERE groupid = ?");
		stmt.setInt(1, id);
		ResultSet rs = stmt.executeQuery();
		while(rs.next()) {
			list.add(new MetaQuestion(rs.getString("questionid"), connection));
		}
		return list;
	}
	
	public int getIndex() throws SQLException {
		PreparedStatement stmt = connection.prepareStatement("SELECT ind FROM metagroup WHERE id = ?");
		stmt.setInt(1, id);
		ResultSet rs = stmt.executeQuery();
		if(rs.next()) {
			return rs.getInt("ind");
		} else {
			return 0;
		}
	}
	
	public static List<MetaGroup> getAll(Connection connection) throws SQLException {
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT id FROM metagroup ORDER BY id ASC");
		List<MetaGroup> list = new LinkedList<MetaGroup>();
		while(rs.next()) {
			list.add(new MetaGroup(rs.getInt("id"), connection));
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String toJSONString() {
		JSONObject obj = new JSONObject();
		try {
			obj.put("textref", getTextRef());
			obj.put("index", getIndex());
			JSONArray questions = new JSONArray();
			for(MetaQuestion q : getQuestions()){
				questions.add(q.getId());
			}
			obj.put("questions", questions);
		} catch (SQLException e) {
			MyTools.printSQLExeption(e);
		}
		return obj.toJSONString();
	}
}
