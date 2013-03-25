package daxplorelib.metadata;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Properties;

import tools.MyTools;
import tools.NumberlineCoverage;
import tools.Pair;
import tools.SortedProperties;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import daxplorelib.DaxploreException;
import daxplorelib.DaxploreFile;
import daxplorelib.DaxploreTable;
import daxplorelib.SQLTools;
import daxplorelib.metadata.MetaGroup.MetaGroupManager;
import daxplorelib.metadata.MetaQuestion.MetaQuestionManager;
import daxplorelib.metadata.MetaScale.MetaScaleManager;
import daxplorelib.metadata.MetaTimepointShort.MetaTimepointShortManager;
import daxplorelib.metadata.TextReference.TextReferenceManager;
import daxplorelib.raw.RawMeta;
import daxplorelib.raw.RawMeta.RawMetaQuestion;

public class MetaData {
	
	Connection connection;

	MetaQuestionManager metaQuestionManager;
	MetaScaleManager metaScaleManager;
	TextReferenceManager textsManager;
	MetaGroupManager metaGroupManager;
	MetaTimepointShortManager metaTimepointManager;
	
	public enum Formats {
		DATABASE,RESOURCE,JSON,RAW
	}
	
	public enum L10nFormat {
		PROPERTIES, CSV
	}
	
	public MetaData(Connection connection) throws SQLException{
		this.connection = connection;
		textsManager = new TextReferenceManager(connection);
		textsManager.init();
		
		metaScaleManager = new MetaScaleManager(connection, textsManager);
		metaScaleManager.init();
		
		metaTimepointManager = new MetaTimepointShortManager(connection, textsManager);
		metaTimepointManager.init();
		
		metaQuestionManager = new MetaQuestionManager(connection, textsManager, metaScaleManager, metaTimepointManager);
		metaQuestionManager.init();
		
		metaGroupManager = new MetaGroupManager(connection, textsManager, metaQuestionManager);
		metaGroupManager.init();
		
		SQLTools.createIfNotExists(MetaCalculation.table, connection);
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
			System.out.print("\n");
			while(iter.hasNext()) {
				System.out.print(".");
				RawMetaQuestion rmq = iter.next();
				TextReference fulltext = textsManager.get(rmq.column + "_fulltext");
				fulltext.put(rmq.qtext, locale);
				MetaCalculation calc = new MetaCalculation(rmq.column, connection);
				MetaScale scale = null;
				switch(rmq.qtype) {
				case MAPPED:
					LinkedList<MetaScale.Option> scaleOptions = new LinkedList<MetaScale.Option>();
					for(int i = 0; i < rmq.valuelables.size(); i++) {
						Pair<String, Double> s = rmq.valuelables.get(i);
						TextReference ref = textsManager.get(rmq.column + "_option_" + i);
						ref.put(s.getKey(), locale);
						scaleOptions.add(new MetaScale.Option(ref, s.getValue(), new NumberlineCoverage(s.getValue())));
					}
					scale = metaScaleManager.create(scaleOptions, new NumberlineCoverage());
					break;
				default:
					scale = null;
					break;
				}
				
				TextReference shorttext = textsManager.get(rmq.column + "_shorttext");
				List<MetaTimepointShort> timepoints = new LinkedList<MetaTimepointShort>();
				metaQuestionManager.create(rmq.column, shorttext, fulltext, scale, calc, timepoints);
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
	
	public void importStructure(Reader r) throws IOException {
		//TODO: XML
	}
	
	public void exportStructure(Writer w) throws DaxploreException, IOException, SQLException{
		//TODO: XML
	}
	
	/**
	 * 
	 * @param reader A character based reader, compatible with {@link Properties#load(Reader)}
	 * @param format
	 * @param locale
	 * @throws IOException
	 * @throws DaxploreException 
	 */
	public void importL10n(Reader reader, L10nFormat format, Locale locale) throws IOException, DaxploreException {
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
		
		switch(format) {
		case PROPERTIES:
			Properties properties = new Properties();
			properties.load(reader);
			
			Iterator<Entry<Object, Object>> allTexts = properties.entrySet().iterator();
			try {
				while(allTexts.hasNext()) {
					Entry<Object, Object> s = allTexts.next();
					TextReference tr = textsManager.get((String)s.getKey());
					tr.put((String)s.getValue(), locale);
				}
			}catch (SQLException e) {
				MyTools.printSQLExeption(e);
				throw new DaxploreException("Error on Text import", e);
			}
			break;
		case CSV:
			CSVReader csvReader = new CSVReader(reader);
			try {
				for (String[] row : csvReader.readAll()) {
					if(row.length==0) {
						continue;
					} else if(row.length==2) {
						TextReference tr;
						tr = textsManager.get(row[0]);
						tr.put(row[1], locale);
					} else {
						throw new DaxploreException("Invalid csv row:" + MyTools.join(row, ", "));
					}
				}
			} catch (SQLException e) {
				MyTools.printSQLExeption(e);
				throw new DaxploreException("Error on Text import", e);
			} finally {
				csvReader.close();
			}
			break;
		default:
			throw new AssertionError("Unsupported format: " + format);	
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
	public void exportL10n(Writer writer, L10nFormat format, Locale locale) throws IOException, DaxploreException {
		switch(format) {
		case PROPERTIES:
			Properties properties = new SortedProperties();
			
			List<TextReference> allTexts = getAllTextReferences();
			for(TextReference tr: allTexts) {
				if(tr.has(locale)) {
					properties.setProperty(tr.getRef(), tr.get(locale));
				} else {
					properties.setProperty(tr.getRef(), "");
				}
			}
			
			properties.store(writer, null); //Comment can be null Some documentation comment placed on the first row of the file

			break;
		case CSV:
			CSVWriter csvWriter = new CSVWriter(writer);
			allTexts = getAllTextReferences();
			for(TextReference tr: allTexts) {
				if(tr.has(locale)) {
					csvWriter.writeNext(new String[]{tr.getRef(), tr.get(locale)});
				} else {
					csvWriter.writeNext(new String[]{tr.getRef(), ""});
				}
			}
			csvWriter.close();
			break;
		default:
			throw new AssertionError("Unsupported format: " + format);	
		}
	}
	
	public void importConfig(Reader r){
		
	}
	
	public void exportConfig(Writer w){
		
	}
	
	public void consolidateScales(Locale bylocale) throws DaxploreException {
		boolean autocommit = true;
		try {
			autocommit = connection.getAutoCommit();
			connection.setAutoCommit(false);
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
		} catch (SQLException e) {
			MyTools.printSQLExeption(e);
			throw new DaxploreException("Failed to disable autocommit", e);
		}
		
		try {
			List<MetaScale> scaleList = metaScaleManager.getAll();
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
						List<MetaScale.Option> oldrefs = us.getOptions();
						List<MetaScale.Option> newrefs = new LinkedList<MetaScale.Option>();
						for(int i = 0; i < oldrefs.size(); i++) {
							TextReference tr = textsManager.get("generic" + (genericScales.size() +1) + "_option_" + i);
							TextReference oldtr = oldrefs.get(i).textRef;
							List<Locale> locs = oldtr.getLocales();
							for(Locale loc: locs) {
								tr.put(oldtr.get(loc), loc);
							}
							newrefs.add(new MetaScale.Option(tr, oldrefs.get(i).value, oldrefs.get(i).transformation));
						}
						MetaScale gs = metaScaleManager.create(newrefs, new NumberlineCoverage());
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
			
			//Change all questions to use new generic scales
			List<MetaQuestion> allquestions = metaQuestionManager.getAll();
			for(MetaQuestion q: allquestions) {
				if(scaleMap.containsKey(q.getScale())) {
					q.setScale(scaleMap.get(q.getScale()));
				}
			}
			
			System.out.print("Removing old scales");
			//remove old unused scales
			for(MetaScale s: scaleMap.keySet()) {
				List<MetaScale.Option> refs = s.getOptions();
				for(MetaScale.Option o: refs) {
					textsManager.remove(o.textRef.getRef());
				}
				metaScaleManager.remove(s.getId());
			}
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
	
	public List<MetaGroup> getAllGroups() throws DaxploreException {
		try {
			return metaGroupManager.getAll();
		} catch (SQLException e) {
			throw new DaxploreException("SQLExpection while trying to get groups", e);
		}
	}
	
	public List<MetaQuestion> getAllQuestions() throws DaxploreException {
		try {
			return metaQuestionManager.getAll();
		} catch (SQLException e) {
			throw new DaxploreException("SQLExpection while trying to get questions", e);
		}
	}
	
	public List<MetaScale> getAllScales() throws DaxploreException {
		try {
			return metaScaleManager.getAll();
		} catch (SQLException e) {
			throw new DaxploreException("SQLExpection while trying to get scales", e);
		}
	}
	
	public List<TextReference> getAllTextReferences() throws DaxploreException {
		try {
			return textsManager.getAll();
		} catch (SQLException e) {
			throw new DaxploreException("SQLExpection while trying to get textreferences", e);
		}
	}
	
	public List<Locale> getAllLocales() throws DaxploreException {
		try {
			return textsManager.getAllLocales();
		} catch (SQLException e){
			throw new DaxploreException("SQLException while trying to get locales", e);
		}
	}
	
	public List<DaxploreTable> getTables() {
		List<DaxploreTable> list = new LinkedList<DaxploreTable>();
		
		list.add(MetaCalculation.table);
		
		list.add(MetaGroup.groupTable);
		list.add(MetaGroup.groupRelTable);
		
		list.add(MetaQuestion.table);
		list.add(MetaQuestion.timePointTable);
		
		list.add(MetaScale.maintable);
		list.add(MetaScale.optiontable);
		
		list.add(MetaTimepointShort.pointTable);
		
		list.add(TextReference.table);

		return list;
	}
	
	public void saveAll() throws DaxploreException, SQLException {
		textsManager.saveAll();
		metaScaleManager.saveAll();
		metaQuestionManager.saveAll();
		metaGroupManager.saveAll();
		metaTimepointManager.saveAll();
	}
	
	public MetaQuestionManager getMetaQuestionManager() {
		return metaQuestionManager;
	}

	public MetaScaleManager getMetaScaleManager() {
		return metaScaleManager;
	}

	public TextReferenceManager getTextsManager() {
		return textsManager;
	}

	public MetaGroupManager getMetaGroupManager() {
		return metaGroupManager;
	}
	
	public MetaTimepointShortManager getMetaTimepointManager() {
		return metaTimepointManager;
	}
}
