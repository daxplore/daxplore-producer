package daxplorelib.metadata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

public class MetaGroup implements JSONAware{
	protected static final String sqlDefinition = "CREATE TABLE metagroup (id INTEGER PRIMARY KEY, textref TEXT, index INTEGER, type INTEGER)";
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
		}
		
		if(questions != null){
			removeQuestions();
			addQuestions(questions);
		}
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
	
	public void setTextRef(TextReference ref) {
		//TODO: stub
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
		PreparedStatement stmt = connection.prepareStatement("SELECT index FROM metagroup WHERE id = ?");
		stmt.setInt(1, id);
		ResultSet rs = stmt.executeQuery();
		if(rs.next()) {
			return rs.getInt("index");
		} else {
			return 0;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public String toJSONString() {
		try {
			JSONObject obj = new JSONObject();
			obj.put("textref", getTextRef());
			obj.put("index", getIndex());
			JSONArray questions = new JSONArray();
			for(MetaQuestion q : getQuestions()){
				questions.add(q.getId());
			}
			obj.put("questions", questions);
			return obj.toJSONString();
		} catch (SQLException e) {
			return "";
		}
	}
}
