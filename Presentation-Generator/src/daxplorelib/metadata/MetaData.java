package daxplorelib.metadata;

import java.io.Reader;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
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
	public void importFromRaw(DaxploreFile daxfile) throws DaxploreException, SQLException {
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
	
	public void consolidateTexts(Locale bylocale) {
		
	}
	
	public void save(){
		
	}
}
