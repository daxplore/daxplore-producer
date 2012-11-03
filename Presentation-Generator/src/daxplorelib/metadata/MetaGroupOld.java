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

import daxplorelib.DaxploreTable;

import tools.MyTools;

public class MetaGroupOld implements JSONAware{
	protected static final DaxploreTable table = new DaxploreTable("CREATE TABLE metagroup (id INTEGER PRIMARY KEY, textref TEXT, ind INTEGER, type INTEGER)", "metagroup");
	protected static final DaxploreTable table2 = new DaxploreTable("CREATE TABLE metagrouprel (groupid INTEGER, questionid TEXT)", "metagrouprel");
	
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
	
	public MetaGroupOld(int id, Connection connection) {
		this.connection = connection;
		this.id = id;
	}
	
	public MetaGroupOld(TextReference textref, int index, GroupType type, List<MetaQuestionOld> questions, Connection connection) throws SQLException {
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
			stmt.close();
			stmt = connection.prepareStatement("INSERT INTO metagroup (textref, ind, type) VALUES (?, ?, ?)");
			stmt.setString(1, textref.getRef());
			stmt.setInt(2, index);
			stmt.setInt(3, type.asInt());
			stmt.execute();
			rs = stmt.getGeneratedKeys();
			rs.next();
			id = rs.getInt(1);
		}
		stmt.close();
		if(questions != null){
			removeQuestions();
			addQuestions(questions);
		}
	}
	
	public MetaGroupOld(JSONObject obj, GroupType type, Connection connection) throws SQLException {
		this(new TextReference((String)obj.get("textref"), connection), (Integer)obj.get("index"), type, null, connection);
		List<MetaQuestionOld> questions = new LinkedList<MetaQuestionOld>();
		JSONArray questionArr = (JSONArray) obj.get("questions");
		for(int i = 0; i < questionArr.size(); i++) {
			questions.add(new MetaQuestionOld((String)questionArr.get(i), connection));
		}
		addQuestions(questions);
	}
	
	protected void addQuestions(List<MetaQuestionOld> questions) throws SQLException{
		PreparedStatement stmt = connection.prepareStatement("INSERT INTO metagrouprel (groupid, questionid) VALUES (?, ?)");
		for(MetaQuestionOld q: questions){
			stmt.setInt(1, id);
			stmt.setString(2, q.getId());
			stmt.executeUpdate();
		}
		stmt.close();
	}
	
	protected void removeQuestions() throws SQLException {
		PreparedStatement stmt = connection.prepareStatement("DELETE FROM metagrouprel WHERE groupid = ?");
		stmt.setInt(1, id);
		stmt.executeUpdate();
		stmt.close();
	}
	
	public TextReference getTextRef() throws SQLException {
		PreparedStatement stmt = connection.prepareStatement("SELECT textref FROM metagroup WHERE id = ?");
		stmt.setInt(1, id);
		ResultSet rs = stmt.executeQuery();
		if(rs.next()) {
			TextReference tr = new TextReference(rs.getString("textref"), connection);
			stmt.close();
			return tr;
		} else {
			stmt.close();
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
		stmt.close();
	}
	
	public GroupType getType() throws SQLException {
		PreparedStatement stmt = connection.prepareStatement("SELECT type FROM metagroup WHERE groupid = ?");
		stmt.setInt(1, id);
		ResultSet rs = stmt.executeQuery();
		if(rs.next()) {
			int type = rs.getInt("type");
			GroupType gt = GroupType.fromInt(type);
			stmt.close();
			return gt;
		} else {
			stmt.close();
			return null;
		}
	}

	public List<MetaQuestionOld> getQuestions() throws SQLException {
		List<MetaQuestionOld> list = new LinkedList<MetaQuestionOld>();
		PreparedStatement stmt = connection.prepareStatement("SELECT questionid FROM metagrouprel WHERE groupid = ?");
		stmt.setInt(1, id);
		ResultSet rs = stmt.executeQuery();
		while(rs.next()) {
			list.add(new MetaQuestionOld(rs.getString("questionid"), connection));
		}
		stmt.close();
		return list;
	}
	
	public int getIndex() throws SQLException {
		PreparedStatement stmt = connection.prepareStatement("SELECT ind FROM metagroup WHERE id = ?");
		stmt.setInt(1, id);
		ResultSet rs = stmt.executeQuery();
		if(rs.next()) {
			int ind = rs.getInt("ind");
			stmt.close();
			return ind;
		} else {
			stmt.close();
			return 0;
		}
	}
	
	public void setIndex(int index) throws SQLException {
		PreparedStatement stmt = connection.prepareStatement("UPDATE metagroup SET ind = ? WHERE id = ?");
		stmt.setInt(1, index);
		stmt.setInt(2, id);
		stmt.executeUpdate();
		stmt.close();
	}
	
	public static List<MetaGroupOld> getAll(Connection connection) throws SQLException {
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT id FROM metagroup ORDER BY id ASC");
		List<MetaGroupOld> list = new LinkedList<MetaGroupOld>();
		while(rs.next()) {
			list.add(new MetaGroupOld(rs.getInt("id"), connection));
		}
		stmt.close();
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
			for(MetaQuestionOld q : getQuestions()){
				questions.add(q.getId());
			}
			obj.put("questions", questions);
		} catch (SQLException e) {
			MyTools.printSQLExeption(e);
		}
		return obj.toJSONString();
	}
}
