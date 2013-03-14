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

import tools.SmallMap;
import daxplorelib.DaxploreTable;
import daxplorelib.SQLTools;


public class TextReference implements Comparable<TextReference> {
	protected static final DaxploreTable table = new DaxploreTable("CREATE TABLE texts (ref TEXT, locale TEXT, text TEXT, UNIQUE ( ref, locale) )", "texts");
	
	public static class TextReferenceManager {
		Connection connection;
		Map<String, TextReference> textMap = new HashMap<String, TextReference>();
		List<TextReference> toBeRemoved = new LinkedList<TextReference>();
		
		public TextReferenceManager(Connection connection) {
			this.connection = connection;
		}
		
		public void init() throws SQLException {
			SQLTools.createIfNotExists(table, connection);
		}
		
		/**
		 * Get a TextReference from refstring. Creates one if it doesn't exist.
		 * @param refstring
		 * @return
		 * @throws SQLException
		 */
		public TextReference get(String refstring) throws SQLException {
			if(textMap.containsKey(refstring)) {
				//System.out.print("e");
				return textMap.get(refstring);
			} else {
				boolean newTextReference = true;
				PreparedStatement stmt = connection.prepareStatement("SELECT * FROM texts where ref = ?");
				stmt.setString(1, refstring);
				ResultSet rs = stmt.executeQuery();
				Map<Locale, String> localeMap = new SmallMap<Locale, String>();
				while(rs.next()) {
					String loc = rs.getString("locale");
					if(loc != null && !"".equals(loc)) { //TODO verify that we want to create it?
						localeMap.put(new Locale(rs.getString("locale")), rs.getString("text"));						
					}
					newTextReference = false;
				}
				TextReference tr = new TextReference(refstring, localeMap);
				tr.modified = newTextReference;
				//System.out.print(newTextReference ? "n": "o");
				textMap.put(refstring, tr);
				return tr;
			}
		}
		
		public void remove(String refstring) throws SQLException {
			toBeRemoved.add(textMap.remove(refstring));
		}
		
		public void saveAll() throws SQLException {
			PreparedStatement insertTextrefStmt = connection.prepareStatement("INSERT OR REPLACE INTO texts (ref, locale, text) VALUES (?, ? , ?)");
			PreparedStatement selectLocalesStmt = connection.prepareStatement("SELECT locale FROM texts WHERE ref = ?");
			PreparedStatement deleteTextrefLocaleStmt = connection.prepareStatement("DELETE FROM texts WHERE ref = ? AND locale = ?");
			
			for(TextReference tr: textMap.values()) {
				if(tr.modified) {
					//first get existing locales
					Set<Locale> oldLocs = new HashSet<Locale>();
					selectLocalesStmt.setString(1, tr.reference);
					ResultSet rs = selectLocalesStmt.executeQuery();
					while(rs.next()) {
						String loc = rs.getString("locale");
						if(loc != null && !"".equals(loc)) {
							oldLocs.add(new Locale(loc));
						}
					}
					if(tr.localeMap.size() > 0) {
						for(Locale l: tr.localeMap.keySet()) {
							oldLocs.remove(l);
							insertTextrefStmt.setString(1, tr.reference);
							insertTextrefStmt.setString(2, l.toLanguageTag());
							insertTextrefStmt.setString(3, tr.get(l));
							insertTextrefStmt.addBatch();
						}
						insertTextrefStmt.executeBatch();
						
						deleteTextrefLocaleStmt.setString(1, tr.reference);
						deleteTextrefLocaleStmt.setNull(2, Types.VARCHAR);
						deleteTextrefLocaleStmt.executeUpdate();
					} else {
						insertTextrefStmt.setString(1, tr.reference);
						insertTextrefStmt.setNull(2, Types.VARCHAR);
						insertTextrefStmt.setNull(3, Types.VARCHAR);
						insertTextrefStmt.executeUpdate();
					}
						
					for(Locale l: oldLocs) {
						deleteTextrefLocaleStmt.setString(1, tr.reference);
						deleteTextrefLocaleStmt.setString(2, l.toLanguageTag());
						deleteTextrefLocaleStmt.addBatch();
					}
					deleteTextrefLocaleStmt.executeBatch();
					
					tr.modified = false;
				}
			}
			
			//Delete those marked to be removed
			PreparedStatement deleteTextrefStmt = connection.prepareStatement("DELETE FROM texts WHERE ref = ?");
			for(TextReference tr: toBeRemoved) {
				deleteTextrefStmt.setString(1, tr.reference);
				deleteTextrefStmt.addBatch();
			}
			deleteTextrefStmt.executeBatch();
			toBeRemoved.clear();
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
			ResultSet rs = connection.createStatement().executeQuery("SELECT ref FROM texts");
			while(rs.next()) {
				get(rs.getString("ref"));
			}
			LinkedList<TextReference> list = new LinkedList<TextReference>(textMap.values());
			Collections.sort(list);
			return list;
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
	public int compareTo(TextReference o) {
		return reference.compareTo(o.reference);
	}
}
