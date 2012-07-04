package daxplorelib.metadata;

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

import tools.Pair;

import daxplorelib.DaxploreException;
import daxplorelib.DaxploreFile;
import daxplorelib.SQLTools;
import daxplorelib.fileformat.RawMeta;
import daxplorelib.fileformat.RawMeta.RawMetaQuestion;

public class MetaData {
	
	Connection connection;
	
	public enum Formats {
		DATABASE,RESOURCE,JSON,RAW
	}
	
	public MetaData(Connection database) throws SQLException{
		this.connection = database;
		if(SQLTools.tableExists("metadata", database)){
			Statement stmt = database.createStatement();
			stmt.executeUpdate("CREATE TABLE metadata ()");
			stmt.executeUpdate(TextReference.sqlDefinition);
		} else {
			
		}

		
	}

	/* 
	 * Import/export methods that are used to change metadata in batch.
	 * The preferred way too use the library.
	 */
	public void importFromRaw(DaxploreFile daxfile) throws DaxploreException {
		try {
			RawMeta rawmeta = daxfile.getRawMeta();
			Iterator<RawMetaQuestion> iter = rawmeta.getQuestionIterator();
			
			Locale locale = new Locale("SV_se");
			
			while(iter.hasNext()) {
				RawMetaQuestion rmq = iter.next();
				TextReference fulltext = new TextReference(rmq.column + "_fulltext", connection);
				fulltext.put(rmq.qtext, locale);
				MetaCalculation calc = new MetaCalculation(rmq.column, connection);
				List<Pair<TextReference, Double>> scalevalues = new LinkedList<Pair<TextReference,Double>>();
				for(int i = 0; i < rmq.valuelables.size(); i++) {
					Pair<String, Double> s = rmq.valuelables.get(i);
					TextReference ref = new TextReference(rmq.column + "_option_" + i, connection);
					ref.put(s.getKey(), locale);
					scalevalues.add(new Pair<TextReference, Double>(fulltext, s.getValue()));
				}
				MetaScale scale = new MetaScale(scalevalues, connection);
				
				TextReference shorttext = new TextReference(rmq.column + "shorttext", connection);
				
				new MetaQuestion(rmq.column, fulltext, shorttext, calc, scale, connection);
			}
		} catch (SQLException e) {
			throw new DaxploreException("Failed to transfer metadata from raw", e);
		}
	}
	
	public void importStructure(Reader r, Formats format){
		
	}
	
	public void importL10n(Reader r, Formats format, Locale locale) {
		
	}
	
	public void importConfig(Reader r, Formats format){
		
	}
	
	public void exportStructure(Writer w, Formats format){
		
	}
	
	public void exportL10n(Writer w, Formats format, Locale locale) {
		
	}
	
	public void exportConfig(Writer w, Formats format){
		
	}
	
	public void consolidateScales(Locale bylocale) throws DaxploreException {
		try {
			List<MetaScale> scaleList = MetaScale.getAll(connection);
			List<MetaScale> uniqueScales = new LinkedList<MetaScale>();
			List<MetaScale> genericScales = new LinkedList<MetaScale>();
			LinkedHashMap<MetaScale, MetaScale> scaleMap = new LinkedHashMap<MetaScale, MetaScale>();
			L: for(MetaScale s: scaleList) {
				for(MetaScale us: uniqueScales) {
					if(us.equalsLocale(s, bylocale)) {
						//If scale exists previously, create new generic scale 
						List<Pair<TextReference, Double>> oldrefs = us.getRefereceList();
						List<Pair<TextReference, Double>> newrefs = new LinkedList<Pair<TextReference, Double>>();
						for(int i = 0; i < oldrefs.size(); i++) {
							TextReference tr = new TextReference("generic" + (uniqueScales.size() +1) + "_option_" + i, connection);
							TextReference oldtr = oldrefs.get(i).getKey();
							List<Locale> locs = oldtr.getLocales();
							for(Locale loc: locs) {
								tr.put(oldtr.get(loc), loc);
							}
							newrefs.add(new Pair<TextReference, Double>(tr, oldrefs.get(i).getValue()));
						}
						MetaScale gs = new MetaScale(newrefs, connection);
						genericScales.add(gs);
						scaleMap.put(s, gs);
						uniqueScales.remove(us);
						continue L;
					}
				}
				for(MetaScale gs: genericScales) {
					if(gs.equalsLocale(s, bylocale)) {
						scaleMap.put(s, gs);
						continue L;
					}
				}
			}
			
			//Change all questions to use new generic scales
			List<MetaQuestion> allquestions = MetaQuestion.getAll(connection);
			for(MetaQuestion q: allquestions) {
				if(scaleMap.containsKey(q.getScale())) {
					q.setScale(scaleMap.get(q.getScale()));
				}
			}
			
			//remove old unused scales
			for(MetaScale s: scaleMap.keySet()) {
				List<Pair<TextReference, Double>> refs = s.getRefereceList();
				s.remove();
				for(Pair<TextReference, Double> p: refs) {
					p.getKey().remove();
				}
			}
			
			
		} catch (SQLException e) {
			throw new DaxploreException("Failed to consolidate scales", e);
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
	
	public void save(){
		
	}
}
