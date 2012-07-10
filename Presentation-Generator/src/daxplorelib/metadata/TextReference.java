package daxplorelib.metadata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.json.simple.JSONAware;

public class TextReference implements JSONAware, Comparable<TextReference> {
	protected static final String sqlDefinition = "CREATE TABLE texts (ref TEXT, locale TEXT, text TEXT, UNIQUE ( ref, locale) )";
	
	protected String reference;
	protected Connection connection;
	
	/**
	 * TextReference is a identifier for strings. It supports localization and generally works like a map.
	 * @param reference The string reference
	 * @param connection Database connection (SQLite may be expected)
	 * @throws SQLException 
	 */
	public TextReference(String reference, Connection connection) throws SQLException {
		this.reference = reference;
		this.connection = connection;
		PreparedStatement stmt = connection.prepareStatement("SELECT * FROM texts WHERE ref = ?");
		stmt.setString(1, reference);
		ResultSet rs = stmt.executeQuery();
		if(!rs.next()) {
			stmt = connection.prepareStatement("INSERT INTO texts (ref, locale) VALUES (?, ?)");
			stmt.setString(1, reference);
			stmt.setString(2, "");
			stmt.executeUpdate();
		}
	}
	
	public String getRef() {
		return reference;
	}
	
	public String get(Locale locale) throws SQLException {
		PreparedStatement stmt = connection.prepareStatement("SELECT text FROM texts WHERE ref = ? AND locale = ?");
		stmt.setString(1, reference);
		stmt.setString(2, locale.toLanguageTag()); //TODO: let Daniel choose locale representation
		stmt.execute();
		ResultSet rs = stmt.getResultSet();
		if(rs.next()) {
			String text = rs.getString("text");
			//TODO: is the following code needed?
			/*if(rs.next()) {
				throw new Exception("more than one result");
			}*/
			return text;
		}
		return null;
	}
	
	public void put(String text, Locale locale) throws SQLException {
		if( get(locale) == null) {
			PreparedStatement stmt = connection.prepareStatement("INSERT INTO texts (ref, locale, text) VALUES (?, ?, ?)");
			stmt.setString(1, reference);
			stmt.setString(2, locale.toLanguageTag());
			stmt.setString(3, text);
			stmt.execute();
		} else {
			PreparedStatement stmt = connection.prepareStatement("UPDATE texts SET text = ? WHERE ref = ? AND locale = ?");
			stmt.setString(1, text);
			stmt.setString(2, reference);
			stmt.setString(3, locale.toLanguageTag());
			stmt.execute();
		}
	}
	
	/**
	 * Get a list of locales set for this TextReference
	 * @return list of locales
	 * @throws SQLException 
	 */
	public List<Locale> getLocales() throws SQLException {
		PreparedStatement stmt = connection.prepareStatement("SELECT locale FROM texts WHERE ref = ?");
		List<Locale> list = new LinkedList<Locale>();
		ResultSet rs = stmt.executeQuery();
		while(rs.next()) {
			list.add(new Locale(rs.getString("locale")));
		}
		return list;
	}
	
	public void remove() throws SQLException {
		PreparedStatement stmt = connection.prepareStatement("DELETE FROM texts WHERE ref = ?");
		stmt.setString(1, reference);
		stmt.executeUpdate();
	}
	
	public static List<TextReference> getAll(Connection connection) throws SQLException {
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT ref FROM texts");
		List<TextReference> list = new LinkedList<TextReference>();
		while(rs.next()) {
			list.add(new TextReference(rs.getString("ref"), connection));
		}
		return list;
	}
	
	public boolean equalsLocale(TextReference other, Locale locale) throws SQLException {
		return get(locale).equals(other.get(locale));
	}

	@Override
	public String toJSONString() {
		return '"'+ reference + '"';
	}

	@Override
	public int compareTo(TextReference o) {
		return reference.compareTo(o.reference);
	}
}
