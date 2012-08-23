package daxplorelib.metadata;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import tools.MyTools;
import tools.Pair;
import tools.SortedProperties;
import daxplorelib.DaxploreException;
import daxplorelib.DaxploreFile;
import daxplorelib.DaxploreTable;
import daxplorelib.SQLTools;
import daxplorelib.metadata.MetaGroup.GroupType;
import daxplorelib.raw.RawMeta;
import daxplorelib.raw.RawMeta.RawMetaQuestion;

public class MetaData {
	
	Connection connection;
	
	public enum Formats {
		DATABASE,RESOURCE,JSON,RAW
	}
	
	public MetaData(Connection connection) throws SQLException{
		this.connection = connection;
		Statement stmt = connection.createStatement();
		SQLTools.createIfNotExists(TextReference.table, connection);
		SQLTools.createIfNotExists(MetaGroup.table, connection);
		SQLTools.createIfNotExists(MetaGroup.table2, connection);
		SQLTools.createIfNotExists(MetaQuestion.table, connection);
		SQLTools.createIfNotExists(MetaScale.table, connection);
		SQLTools.createIfNotExists(MetaCalculation.table, connection);
		stmt.close();
	}

	/* 
	 * Import/export methods that are used to change metadata in batch.
	 * The preferred way too use the library.
	 */
	public void importFromRaw(DaxploreFile daxfile, Locale locale) throws DaxploreException {
		boolean autocommit = true;
		try {
			//save = sqliteDatabase.setSavepoint();
			autocommit = connection.getAutoCommit();
			connection.setAutoCommit(false);
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
		} catch (SQLException e) {
			MyTools.printSQLExeption(e);
			throw new DaxploreException("Failed to disable autocommit", e);
		}
		
		try {
			RawMeta rawmeta = daxfile.getRawMeta();
			Iterator<RawMetaQuestion> iter = rawmeta.getQuestionIterator();	
			
			while(iter.hasNext()) {
				System.out.print(".");
				RawMetaQuestion rmq = iter.next();
				TextReference fulltext = new TextReference(rmq.column + "_fulltext", connection);
				fulltext.put(rmq.qtext, locale);
				MetaCalculation calc = new MetaCalculation(rmq.column, connection);
				MetaScale scale = null;
				switch(rmq.qtype) {
				case MAPPED:
					List<Pair<TextReference, Double>> scalevalues = new LinkedList<Pair<TextReference,Double>>();
					for(int i = 0; i < rmq.valuelables.size(); i++) {
						Pair<String, Double> s = rmq.valuelables.get(i);
						TextReference ref = new TextReference(rmq.column + "_option_" + i, connection);
						ref.put(s.getKey(), locale);
						scalevalues.add(new Pair<TextReference, Double>(ref, s.getValue()));
					}
					scale = new MetaScale(scalevalues, connection);
					break;
				default:
					scale = null;
					break;
				}
				
				TextReference shorttext = new TextReference(rmq.column + "shorttext", connection);
				
				new MetaQuestion(rmq.column, fulltext, shorttext, calc, scale, connection);
			}
		} catch (SQLException e) {
			throw new DaxploreException("Failed to transfer metadata from raw", e);
		}
		
		try {
			connection.setAutoCommit(autocommit);
		} catch (SQLException e) {
			MyTools.printSQLExeption(e);
			throw new DaxploreException("Failed to reenable autocommit", e);
		}
	}
	
	@SuppressWarnings({ "rawtypes" })
	public void importStructure(Reader r) throws IOException {
		JSONParser parser = new JSONParser();
		ContainerFactory containerFactory = new ContainerFactory(){
			public List creatArrayContainer() {
				return new LinkedList();
			}
			public Map createObjectContainer() {
				return null;
			}                    
		};
		
		try {
			JSONObject json = (JSONObject) parser.parse(r, containerFactory);
			Iterator iter = json.keySet().iterator();
			while (iter.hasNext()) {
				String key = (String) iter.next();
				if("question".equals(key)) {
					List questions = (List) json.get(key);
					for(Object o: questions) {
						JSONObject q = (JSONObject)o;
						new MetaQuestion(q, connection);
					}
				} else if("groups".equals(key)) {
					JSONObject groups = (JSONObject)json.get(key);
					Iterator giter = json.keySet().iterator();
					while(giter.hasNext()) {
						String gkey = (String)giter.next();
						if("quesions".equals(gkey)) {
							List qgroups = (List) groups.get(gkey);
							for(Object o: qgroups) {
								JSONObject g = (JSONObject)o;
								new MetaGroup(g, GroupType.QUESTIONS, connection);
							}
						} else if ("perspectives".equals(gkey)) {
							JSONObject g = (JSONObject)groups.get(gkey);
							new MetaGroup(g, GroupType.PERSPECTIVE, connection);
						}
					}
				}
			}
		} catch (ParseException pe) {
			System.out.println(pe);
		} catch (SQLException e) {
			MyTools.printSQLExeption(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void exportStructure(Writer w) throws DaxploreException, IOException, SQLException{
		JSONObject jsonroot = new JSONObject();
		
		//export questions
		List<MetaQuestion> questions = getAllQuestions();
		JSONArray questionArr = new JSONArray();
		for(MetaQuestion q: questions) {
			questionArr.add(q);
		}
		jsonroot.put("questions", questionArr);
		
		//export groups
		List<MetaGroup> groups = getAllGroups();
		JSONObject groupobj = new JSONObject();
		JSONArray grouparr = new JSONArray();
		MetaGroup perspectives = null;
		for(MetaGroup g: groups) {
			if(g.getType() == GroupType.QUESTIONS) {
				grouparr.add(g);				
			} else {
				perspectives = g;
			}
		}
		groupobj.put("questions", grouparr);
		if(perspectives != null) {
			groupobj.put("perspectives", perspectives);
		}
		jsonroot.put("groups", groupobj);
		
		w.write(jsonroot.toJSONString());
		w.close();
	}
	
	/**
	 * 
	 * @param reader A character based reader, compatible with {@link Properties#load(Reader)}
	 * @param format
	 * @param locale
	 * @throws IOException
	 * @throws DaxploreException 
	 */
	public void importL10n(Reader reader, Locale locale) throws IOException, DaxploreException {
		boolean autocommit = true;
		try {
			//save = sqliteDatabase.setSavepoint();
			autocommit = connection.getAutoCommit();
			connection.setAutoCommit(false);
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
		} catch (SQLException e) {
			MyTools.printSQLExeption(e);
			throw new DaxploreException("Failed to disable autocommit", e);
		}
		
		Properties properties = new Properties();
		properties.load(reader);
		
		Iterator<Entry<Object, Object>> allTexts = properties.entrySet().iterator();
		try {
		while(allTexts.hasNext()) {
			Entry<Object, Object> s = allTexts.next();
			TextReference tr = new TextReference((String)s.getKey(), connection);
			tr.put((String)s.getValue(), locale);
		}
		} catch (SQLException e) {
			MyTools.printSQLExeption(e);
			throw new DaxploreException("Error on Text import", e);
		}
		
		try {
			connection.setAutoCommit(autocommit);
		} catch (SQLException e) {
			MyTools.printSQLExeption(e);
			throw new DaxploreException("Failed to reenable autocommit", e);
		}
	}

	/**
	 * 
	 * @param writer A character based writer, compatible with {@link Properties#store(Writer, String)}
	 * @param format
	 * @param locale
	 * @throws IOException
	 * @throws DaxploreException 
	 */
	public void exportL10n(Writer writer, Locale locale) throws IOException, DaxploreException {
		Properties properties = new SortedProperties();
		
		try {
			List<TextReference> allTexts = getAllTextReferences();
			for(TextReference tr: allTexts) {
				if(tr.has(locale)) {
					properties.setProperty(tr.getRef(), tr.get(locale));
				} else {
					properties.setProperty(tr.getRef(), "");
				}
			}
		} catch (SQLException e) {
			MyTools.printSQLExeption(e);
			throw new DaxploreException("Error on Text export", e);
		}
		
		properties.store(writer, null); //Comment can be null Some documentation comment placed on the first row of the file
	}
	
	public void importConfig(Reader r){
		
	}
	
	public void exportConfig(Writer w){
		
	}
	
	public void consolidateScales(Locale bylocale) throws DaxploreException {
		//TODO: very slow. optimize
		
		boolean autocommit = true;
		try {
			//save = sqliteDatabase.setSavepoint();
			autocommit = connection.getAutoCommit();
			connection.setAutoCommit(false);
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
		} catch (SQLException e) {
			MyTools.printSQLExeption(e);
			throw new DaxploreException("Failed to disable autocommit", e);
		}
		
		try {
			//System.out.println("Consolidating...");
			List<MetaScale> scaleList = MetaScale.getAll(connection);
			List<MetaScale> uniqueScales = new LinkedList<MetaScale>();
			List<MetaScale> genericScales = new LinkedList<MetaScale>();
			LinkedHashMap<MetaScale, MetaScale> scaleMap = new LinkedHashMap<MetaScale, MetaScale>();
			NextScale: for(MetaScale s: scaleList) {
				//System.out.print("\n.");
				for(MetaScale us: uniqueScales) {
					//System.out.print(",");
					if(us.equalsLocale(s, bylocale)) {
						//System.out.print("+");
						//If scale exists previously, create new generic scale 
						List<Pair<TextReference, Double>> oldrefs = us.getRefereceList();
						List<Pair<TextReference, Double>> newrefs = new LinkedList<Pair<TextReference, Double>>();
						for(int i = 0; i < oldrefs.size(); i++) {
							TextReference tr = new TextReference("generic" + (genericScales.size() +1) + "_option_" + i, connection);
							TextReference oldtr = oldrefs.get(i).getKey();
							List<Locale> locs = oldtr.getLocales();
							for(Locale loc: locs) {
								tr.put(oldtr.get(loc), loc);
							}
							newrefs.add(new Pair<TextReference, Double>(tr, oldrefs.get(i).getValue()));
						}
						MetaScale gs = new MetaScale(newrefs, connection);
						//System.out.println("\n" + us.toJSONString() +" -> " + gs.toJSONString());
						genericScales.add(gs);
						scaleMap.put(s, gs);
						uniqueScales.remove(us);
						continue NextScale;
					}
				}
				for(MetaScale gs: genericScales) {
					if(gs.equalsLocale(s, bylocale)) {
						scaleMap.put(s, gs);
						continue NextScale;
					}
				}
				uniqueScales.add(s);
			}
			connection.commit();
			//System.out.println(" " + genericScales.size() + " scales.");
			
			//System.out.println("Altering questions to use generic scales...");
			//Change all questions to use new generic scales
			List<MetaQuestion> allquestions = MetaQuestion.getAll(connection);
			for(MetaQuestion q: allquestions) {
				if(scaleMap.containsKey(q.getScale())) {
					q.setScale(scaleMap.get(q.getScale()));
				}
			}
			connection.commit();
			
			System.out.print("Removing old scales");
			//remove old unused scales
			List<TextReference> toBeRemoved = new LinkedList<TextReference>();
			for(MetaScale s: scaleMap.keySet()) {
				List<Pair<TextReference, Double>> refs = s.getRefereceList();
				s.remove();
				for(Pair<TextReference, Double> p: refs) {
					toBeRemoved.add(p.getKey());
					//p.getKey().remove();
				}
			}
			System.out.println(".");
			for(TextReference tr: toBeRemoved) {
				tr.remove();
			}
			connection.commit();
			System.out.println("Done");
			
		} catch (SQLException e) {
			throw new DaxploreException("Failed to consolidate scales", e);
		}
		
		try {
			connection.setAutoCommit(autocommit);
		} catch (SQLException e) {
			MyTools.printSQLExeption(e);
			throw new DaxploreException("Failed to reenable autocommit", e);
		}
	}
	
	public void clearNullStrings() throws DaxploreException {
		boolean autocommit = true;
		try {
			//save = sqliteDatabase.setSavepoint();
			autocommit = connection.getAutoCommit();
			connection.setAutoCommit(false);
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
		} catch (SQLException e) {
			MyTools.printSQLExeption(e);
			throw new DaxploreException("Failed to disable autocommit", e);
		}
		
		List<TextReference> reflist = getAllTextReferences();
		try {
			for(TextReference tr: reflist) {
				tr.clearNulls();
			}
			connection.commit();
		} catch (SQLException e) {
			throw new DaxploreException("Faild while clearing nulls from strings", e);
		}
		
		try {
			connection.setAutoCommit(autocommit);
		} catch (SQLException e) {
			MyTools.printSQLExeption(e);
			throw new DaxploreException("Failed to reenable autocommit", e);
		}
	}
	
	public List<MetaGroup> getAllGroups() throws DaxploreException {
		try {
			return MetaGroup.getAll(connection);
		} catch (SQLException e) {
			throw new DaxploreException("SQLExpection while trying to get groups", e);
		}
	}
	
	public List<MetaQuestion> getAllQuestions() throws DaxploreException {
		try {
			return MetaQuestion.getAll(connection);
		} catch (SQLException e) {
			throw new DaxploreException("SQLExpection while trying to get groups", e);
		}
	}
	
	public List<MetaScale> getAllScales() throws DaxploreException {
		try {
			return MetaScale.getAll(connection);
		} catch (SQLException e) {
			throw new DaxploreException("SQLExpection while trying to get groups", e);
		}
	}
	
	public List<TextReference> getAllTextReferences() throws DaxploreException {
		try {
			return TextReference.getAll(connection);
		} catch (SQLException e) {
			throw new DaxploreException("SQLExpection while trying to get groups", e);
		}
	}
	
	public List<Locale> getAllLocales() throws DaxploreException {
		try {
			return TextReference.getAllLocales(connection);
		} catch (SQLException e){
			throw new DaxploreException("SQLException while trying to get locales", e);
		}
	}
	
	public List<DaxploreTable> getTables() {
		List<DaxploreTable> list = new LinkedList<DaxploreTable>();
		list.add(MetaQuestion.table);
		list.add(MetaGroup.table);
		list.add(MetaGroup.table2);
		list.add(MetaScale.table);
		list.add(MetaCalculation.table);
		list.add(TextReference.table);
		return list;
	}
	
	public void save(){
		
	}
}
