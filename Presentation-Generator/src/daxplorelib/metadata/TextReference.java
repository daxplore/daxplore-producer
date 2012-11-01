package daxplorelib.metadata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONAware;

import daxplorelib.DaxploreTable;
import daxplorelib.SQLTools;

public class TextReference implements JSONAware, Comparable<TextReference> {
	protected static final DaxploreTable table = new DaxploreTable("CREATE TABLE texts (ref TEXT, locale TEXT, text TEXT, UNIQUE ( ref, locale) )", "texts");
	
	public class TextReferenceManager {
		Connection connection;
		Map<String, TextReference> textMap = new HashMap<String, TextReference>();
		
		public TextReferenceManager(Connection connection) {
			this.connection = connection;
		}
		
		public void init() throws SQLException {
			if(!SQLTools.tableExists(table.name, connection)) {
				Statement stmt = connection.createStatement();
				stmt.execute(table.sql);
			}
		}
		
		public TextReference get(String refstring) throws SQLException {
			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM texts where ref = ?");
			stmt.setString(1, refstring);
			ResultSet rs = stmt.executeQuery();
			Map<Locale, String> localeMap = new HashMap<Locale, String>();
			while(rs.next()) {
				localeMap.put(new Locale(rs.getString("locale")), rs.getString("text"));
			}
			TextReference tr = new TextReference(refstring, localeMap);
			textMap.put(refstring, tr);
			return tr;
		}
		
		public TextReference create(String refstring) {
			TextReference tr = new TextReference(refstring, new HashMap<Locale, String>());
			tr.modified = true;
			textMap.put(refstring, tr);
			return tr;
		}
		
		public void remove(String refstring) throws SQLException {
			textMap.remove(refstring);
			PreparedStatement stmt = connection.prepareStatement("DELETE FROM texts WHERE ref = ?");
			stmt.setString(1, refstring);
			stmt.executeUpdate();
			stmt.close();
		}
		
		public void saveAll() throws SQLException {
			PreparedStatement insertStmt = connection.prepareStatement("INSERT OR REPLACE INTO texts (ref, locale, text) VALUES (?, ? , ?)");
			PreparedStatement localesStmt = connection.prepareStatement("SELECT locale FROM texts WHERE ref = ?");
			PreparedStatement deleteStmt = connection.prepareStatement("DELETE FROM texts WHERE ref = ? AND locale = ?");

			for(TextReference tr: textMap.values()) {
				if(tr.modified) {
					//first get existing locales
					Set<Locale> oldLocs = new HashSet<Locale>();
					localesStmt.setString(1, reference);
					ResultSet rs = localesStmt.executeQuery();
					while(rs.next()) {
						String loc = rs.getString("locale");
						if(loc != null && !"".equals(loc)) {
							oldLocs.add(new Locale(loc));
						}
					}
					if(tr.localeMap.size() > 0) {
						for(Locale l: tr.localeMap.keySet()) {
							oldLocs.remove(l);
							insertStmt.setString(1, tr.reference);
							insertStmt.setString(2, l.toLanguageTag());
							insertStmt.setString(3, tr.get(l));
							insertStmt.addBatch();
						}
						insertStmt.executeBatch();
						deleteStmt.setString(1, tr.reference);
						deleteStmt.setNull(2, Types.VARCHAR);
						deleteStmt.execute();
					} else {
						insertStmt.setString(1, tr.reference);
						insertStmt.setNull(2, Types.VARCHAR);
						insertStmt.setNull(3, Types.VARCHAR);
						insertStmt.execute();
					}
						
					for(Locale l: oldLocs) {
						deleteStmt.setString(1, tr.reference);
						deleteStmt.setString(2, l.toLanguageTag());
						deleteStmt.addBatch();
					}
					deleteStmt.executeBatch();
					
					tr.modified = false;
				}
			}
		}
		

		public List<Locale> getAllLocales() throws SQLException {
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT DISTINCT locale FROM texts ORDER BY locale");
			List<Locale> list = new LinkedList<Locale>();
			while(rs.next()) {
				String loc = rs.getString("locale");
				if(!rs.wasNull() && !"".equals(loc)) {
					list.add(new Locale(loc));
				}
			}
			stmt.close();
			return list;
		}

		public List<TextReference> getAll() throws SQLException {
			return null;//TODO: implement
		}
		
	}
	
	
	protected String reference;
	
	protected Map<Locale, String> localeMap;
	
	protected boolean modified = false;
	
	
	protected TextReference(String refstring, Map<Locale,String> localeMap) {
		this.reference = refstring;
		this.localeMap = localeMap;
	}
	
	public String getRef() {
		return reference;
	}
	
	public String get(Locale locale) {
		return localeMap.get(locale);
	}
	
	public void put(String text, Locale locale) {
		localeMap.put(locale, text);
		modified = true;
	}
	
	public boolean has(Locale locale) {
		return localeMap.containsKey(locale);
	}
	
	/**
	 * Get a list of locales set for this TextReference
	 * @return list of locales
	 * @throws SQLException 
	 */
	public List<Locale> getLocales() {
		return new LinkedList<Locale>(localeMap.keySet());
	}
	
	public boolean equalsLocale(TextReference other, Locale locale) throws SQLException {
		if(has(locale) && other.has(locale)) {
			return get(locale).equals(other.get(locale));
		} else return false;
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
