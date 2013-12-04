package org.daxplore.producer.daxplorelib;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.daxplore.producer.daxplorelib.ImportExportManager.L10nFormat;
import org.daxplore.producer.daxplorelib.metadata.MetaGroup.MetaGroupManager;
import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;
import org.daxplore.producer.daxplorelib.metadata.MetaQuestion.MetaQuestionManager;
import org.daxplore.producer.daxplorelib.metadata.MetaScale;
import org.daxplore.producer.daxplorelib.metadata.MetaScale.MetaScaleManager;
import org.daxplore.producer.daxplorelib.metadata.MetaTimepointShort;
import org.daxplore.producer.daxplorelib.metadata.MetaTimepointShort.MetaTimepointShortManager;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReferenceManager;
import org.daxplore.producer.daxplorelib.raw.RawData;
import org.daxplore.producer.daxplorelib.raw.RawMeta;
import org.daxplore.producer.daxplorelib.raw.RawMeta.RawMetaQuestion;
import org.daxplore.producer.daxplorelib.raw.VariableOptionInfo;
import org.daxplore.producer.tools.MyTools;
import org.daxplore.producer.tools.NumberlineCoverage;
import org.daxplore.producer.tools.Pair;
import org.xml.sax.SAXException;

import com.google.common.primitives.Doubles;

public class DaxploreFile implements Closeable {
	private Connection connection;
	private About about;
	private File file = null;
	private RawMeta rawMeta;
	private RawData rawData;
	private ImportExportManager importExport;
	
	private TextReferenceManager textReferenceManager;
	private MetaScaleManager metaScaleManager;
	private MetaTimepointShortManager metaTimepointShortManager;
	private MetaQuestionManager metaQuestionManager;
	private MetaGroupManager metaGroupManager;
	
	public static DaxploreFile createFromExistingFile(File file) throws DaxploreException {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			throw new DaxploreException("Sqlite could not be found", e);
		}
		
		try {
			Connection connection =  DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
			return new DaxploreFile(connection, false, file);
		} catch (SQLException e) {
			throw new DaxploreException("Not a sqlite file?", e);
		}
	}
	
	public static DaxploreFile createWithNewFile(File file) throws DaxploreException {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			throw new DaxploreException("Sqlite could not be found", e);
		}
		
		try {
			Connection connection =  DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
			return new DaxploreFile(connection, true, file);
		} catch (SQLException e) {
			throw new DaxploreException("Could not create new sqlite database (No write access?)", e);
		}
	}
	
	private DaxploreFile(Connection connection, boolean createNew, File file) throws DaxploreException {
		this.connection = connection;
		this.file = file;
		try {
			about = new About(connection, createNew);
			about.save();
			rawMeta = new RawMeta(connection);
			rawData = new RawData(connection);
			
			textReferenceManager = new TextReferenceManager(connection);
			metaScaleManager = new MetaScaleManager(connection, textReferenceManager);
			metaTimepointShortManager = new MetaTimepointShortManager(connection, textReferenceManager);
			metaQuestionManager = new MetaQuestionManager(connection, textReferenceManager, metaScaleManager, metaTimepointShortManager);
			metaGroupManager = new MetaGroupManager(connection, textReferenceManager, metaQuestionManager);
			
			importExport = new ImportExportManager(connection, this);
		} catch (SQLException e) {
			throw new DaxploreException("Failed to construct DaxploreFile", e);
		}
	}
	
	public void importFromRaw(Locale locale) throws DaxploreException {
		importExport.importFromRaw(locale);
	}
	
	public About getAbout(){
		return about;
	}
	
	public RawMeta getRawMeta() {
		return rawMeta;
	}
	
	public RawData getRawData() {
		return rawData;
	}
	
	public MetaGroupManager getMetaGroupManager() {
		return metaGroupManager;
	}
	
	public MetaQuestionManager getMetaQuestionManager() {
		return metaQuestionManager;
	}

	public MetaScaleManager getMetaScaleManager() {
		return metaScaleManager;
	}

	public MetaTimepointShortManager getMetaTimepointShortManager() {
		return metaTimepointShortManager;
	}

	public TextReferenceManager getTextReferenceManager() {
		return textReferenceManager;
	}
	
	public void writeUploadFile(File outputFile) throws DaxploreException {
		try (OutputStream os = new FileOutputStream(outputFile)) {
			importExport.writeUploadFile(os);
		} catch (TransformerFactoryConfigurationError | TransformerException | SQLException | DaxploreException
				| SAXException | IOException | ParserConfigurationException e) {
			throw new DaxploreException("Failed to write output file", e);
		}
	}
	
	public void importSPSS(File spssFile, Charset charset) throws FileNotFoundException, IOException, DaxploreException {
		importExport.importSPSS(spssFile, charset);
	}
	
	public void importL10n(Reader reader, L10nFormat format, Locale locale) throws IOException, DaxploreException {
		importExport.importL10n(reader, format, locale);
	}
	
	public void exportL10n(Writer writer, L10nFormat format, Locale locale) throws IOException, DaxploreException {
		importExport.exportL10n(writer, format, locale);
	}
	
	public void saveAll() throws DaxploreException { //TODO: return boolean instead of throwing exception?
		try {
			boolean autocommit = connection.getAutoCommit();
			connection.setAutoCommit(false);
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			
			Logger.getGlobal().log(Level.INFO, "Save initiated");
			about.save();
			textReferenceManager.saveAll();
			metaScaleManager.saveAll();
			metaQuestionManager.saveAll();
			metaGroupManager.saveAll();
			metaTimepointShortManager.saveAll();
			
			connection.commit();
			connection.setAutoCommit(autocommit);
		} catch (SQLException e) {
			Logger.getGlobal().log(Level.SEVERE, "Error while saving data", e);
			throw new DaxploreException("Error while saving data", e);
		}
		Logger.getGlobal().log(Level.INFO, "Save successful");
	}
	
	public int getUnsavedChangesCount() {
		return metaGroupManager.getUnsavedChangesCount() + 
				metaQuestionManager.getUnsavedChangesCount() + 
				metaScaleManager.getUnsavedChangesCount() + 
				metaTimepointShortManager.getUnsavedChangesCount() + 
				textReferenceManager.getUnsavedChangesCount() + 
				about.getUnsavedChangesCount();
	}
	
	@Override
	public void close() throws IOException {
		try {
			connection.close();
		} catch (SQLException e) {
			throw new IOException("Failed to close database connection", e);
		}
	}
	
	public File getFile() {
		return file;
	}
	
	public List<VariableOptionInfo> getRawColumnInfo(String column) throws DaxploreException {
		try {
			List<VariableOptionInfo> infoList = new LinkedList<>();
			List<Pair<Double, Integer>> counts = rawData.getColumnValueCount(column);
			for(Pair<Double, Integer> count: counts) {
				VariableOptionInfo optionInfo = new VariableOptionInfo(count.getKey());
				optionInfo.setCount(count.getValue());
				infoList.add(optionInfo);
			}
			RawMetaQuestion rmq = rawMeta.getQuestion(column);
			for(Pair<String, Double> texts: rmq.valuelables) {
				Double value = texts.getValue();
				for(VariableOptionInfo info: infoList) {
					if(value == info.getValue() || (value!=null && value.equals(info.getValue()))) {
						info.setRawText(texts.getKey());
						break;
					}
				}
			}
			
			return infoList;
		} catch (SQLException e) {
			throw new DaxploreException("Failure to get raw data things", e);
		}
	}
	
	//TODO move to a helper file?
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
			List<MetaScale> uniqueScales = new LinkedList<>();
			List<MetaScale> genericScales = new LinkedList<>();
			LinkedHashMap<MetaScale, MetaScale> scaleMap = new LinkedHashMap<>();
			NextScale: for(MetaScale s: scaleList) {
				//System.out.print("\n.");
				for(MetaScale us: uniqueScales) {
					//System.out.print(",");
					if(us.equalsLocale(s, bylocale)) {
						//System.out.print("+");
						//If scale exists previously, create new generic scale 
						List<MetaScale.Option> oldrefs = us.getOptions();
						List<MetaScale.Option> newrefs = new LinkedList<>();
						for(int i = 0; i < oldrefs.size(); i++) {
							TextReference tr = textReferenceManager.get("generic" + (genericScales.size() +1) + "_option_" + i);
							TextReference oldtr = oldrefs.get(i).getTextRef();
							List<Locale> locs = oldtr.getLocales();
							for(Locale loc: locs) {
								tr.put(oldtr.get(loc), loc);
							}
							newrefs.add(new MetaScale.Option(tr, oldrefs.get(i).getValue(), oldrefs.get(i).getTransformation(), true));
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
					textReferenceManager.remove(o.getTextRef().getRef());
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
	
	//TODO move to a helper file?
	public void replaceAllTimepointsInQuestions() throws DaxploreException, SQLException {
		List<MetaTimepointShort> timepoints = metaTimepointShortManager.getAll();
		int tpAdded = 0, questionsModified = 0;
		for(MetaQuestion question : metaQuestionManager.getAll()) {
			List<MetaTimepointShort> questionTp = new LinkedList<>();
			LinkedList<Pair<Double, Integer>> valueCounts = rawData.getColumnValueCountWhere(about.getTimeSeriesShortColumn(), question.getId());
			questionTp.clear();
			for(Pair<Double, Integer> pair : valueCounts) {
				for(MetaTimepointShort tp : timepoints) {
					if(pair.getKey()!=null && tp.getValue() == pair.getKey() && pair.getValue()>0) {
						questionTp.add(tp);
					}
				}
			}
			if(questionTp.size()>0) {
				questionsModified++;
				tpAdded += questionTp.size();
				question.setTimepoints(questionTp);
			}
		}
		String logString = String.format("replaceAllTimepointsInQuestions: %d questions affected, %d timepoints added", questionsModified, tpAdded);
		Logger.getGlobal().log(Level.INFO, logString);
	}
	
	public void discardChanges() throws DaxploreException {
		try {
			about.discardChanges();
			metaGroupManager.discardChanges();
			metaQuestionManager.discardChanges();
			metaScaleManager.discardChanges();
			metaTimepointShortManager.discardChanges();
			textReferenceManager.discardChanges();
		} catch (SQLException e) {
			throw new DaxploreException("Error when discarding changes", e);
		}
		
	}

}
