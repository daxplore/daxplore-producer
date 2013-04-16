package daxplorelib.metadata.textreference;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import tools.SmallMap;
import daxplorelib.DaxploreTable;
import daxplorelib.SQLTools;

public class TextReferenceManager {
	public static final DaxploreTable table = new DaxploreTable("CREATE TABLE texts (ref TEXT NOT NULL, locale TEXT, text TEXT NOT NULL, UNIQUE ( ref, locale) )", "texts");

	Connection connection;
	Map<String, TextReference> toBeRemoved = new HashMap<String, TextReference>();
	
	TextTree textTree = new TextTree();
	
	int nNew = 0;
	
	public TextReferenceManager(Connection connection) {
		this.connection = connection;
	}
	
	public void init() throws SQLException {
		SQLTools.createIfNotExists(TextReferenceManager.table, connection);
	}
	
	/**
	 * Get a TextReference from refstring. Creates one if it doesn't exist.
	 * @param refstring
	 * @return
	 * @throws SQLException
	 */
	public TextReference get(String refstring) throws SQLException {
		TextReference tr = textTree.get(refstring);
		if(tr == null) {
			nNew++;
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
				nNew--;
			}
			tr = new TextReference(refstring, localeMap);
			tr.modified = newTextReference;
			//System.out.print(newTextReference ? "n": "o");
			textTree.add(tr);
		}
		return tr;
	}
	
	public void remove(String refstring) throws SQLException {
		toBeRemoved.put(refstring, textTree.remove(refstring));
	}
	
	public void saveAll() throws SQLException {
		PreparedStatement insertTextrefStmt = connection.prepareStatement("INSERT OR REPLACE INTO texts (ref, locale, text) VALUES (?, ? , ?)");
		PreparedStatement selectLocalesStmt = connection.prepareStatement("SELECT locale FROM texts WHERE ref = ?");
		PreparedStatement deleteTextrefLocaleStmt = connection.prepareStatement("DELETE FROM texts WHERE ref = ? AND locale = ?");
		
		int nRemoved = 0;
		int nModified = 0;

		//Delete those marked to be removed
		PreparedStatement deleteTextrefStmt = connection.prepareStatement("DELETE FROM texts WHERE ref = ?");
		for(TextReference tr: toBeRemoved.values()) {
			nRemoved++;
			deleteTextrefStmt.setString(1, tr.reference);
			deleteTextrefStmt.addBatch();
		}
		deleteTextrefStmt.executeBatch();
		toBeRemoved.clear();
		
		for(TextReferenceReference trr: textTree) {
			TextReference tr = (TextReference)trr;
			if(tr.modified) {
				nModified++;
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
						insertTextrefStmt.executeUpdate(); //TODO: figure out why batch dosn't work here.
					}
					
					deleteTextrefLocaleStmt.setString(1, tr.reference);
					deleteTextrefLocaleStmt.setNull(2, Types.VARCHAR);
					deleteTextrefLocaleStmt.executeUpdate();
				} else {
					insertTextrefStmt.setString(1, tr.reference);
					insertTextrefStmt.setNull(2, Types.VARCHAR);
					insertTextrefStmt.setString(3, "");
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
		
		
		if(nModified != 0 || nNew != 0 || nRemoved != 0) {
			String logString = String.format("TextReferences: Saved %d (%d new), %d removed", nModified, nNew, nRemoved);
			Logger.getGlobal().log(Level.INFO, logString);
			nNew = 0;
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

	public TextTree getAll() throws SQLException {
		// make sure all references are cached before returning the content of the tree
		ResultSet rs = connection.createStatement().executeQuery("SELECT ref FROM texts");
		while(rs.next()) {
			String id = rs.getString("ref");
			if(!textTree.contains(id) && !toBeRemoved.containsKey(id)) {
				get(rs.getString("ref"));
			}
		}
		return textTree;
	}
	
}