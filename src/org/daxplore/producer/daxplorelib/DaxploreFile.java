/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.daxplore.producer.daxplorelib.ImportExportManager.L10nFormat;
import org.daxplore.producer.daxplorelib.metadata.MetaGroup.MetaGroupManager;
import org.daxplore.producer.daxplorelib.metadata.MetaMean.MetaMeanManager;
import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;
import org.daxplore.producer.daxplorelib.metadata.MetaQuestion.MetaQuestionManager;
import org.daxplore.producer.daxplorelib.metadata.MetaScale.MetaScaleManager;
import org.daxplore.producer.daxplorelib.metadata.MetaTimepointShort;
import org.daxplore.producer.daxplorelib.metadata.MetaTimepointShort.MetaTimepointShortManager;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReferenceManager;
import org.daxplore.producer.daxplorelib.raw.RawData.RawDataManager;
import org.daxplore.producer.daxplorelib.raw.RawMetaQuestion;
import org.daxplore.producer.daxplorelib.raw.RawMetaQuestion.RawMetaManager;
import org.daxplore.producer.daxplorelib.raw.VariableOptionInfo;
import org.daxplore.producer.daxplorelib.resources.DaxploreProperties;
import org.xml.sax.SAXException;

import com.google.common.base.Strings;

public class DaxploreFile implements Closeable {

	private Connection connection;
	private Settings settings;
	private About about;
	private File file = null;
	private ImportExportManager importExport;

	private RawMetaManager rawMetaManager;
	private RawDataManager rawDataManager;
	private TextReferenceManager textReferenceManager;
	private MetaScaleManager metaScaleManager;
	private MetaMeanManager metaMeanManager;
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
			Connection connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
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
			file.delete();
			Connection connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
			return new DaxploreFile(connection, true, file);
		} catch (SQLException e) {
			throw new DaxploreException("Could not create new sqlite database (No write access?)", e);
		}
	}

	private DaxploreFile(Connection connection, boolean createNew, File file) throws DaxploreException {
		this.connection = connection;
		this.file = file;
		
		// Compare file version with system version and adapt accordingly
		try {
			try (Statement stmt = connection.createStatement()) {
				int systemVersion = DaxploreProperties.daxploreFileVersion;
				ResultSet rs = stmt.executeQuery("PRAGMA user_version");
				int filetypeVersion = rs.getInt("user_version");
				
				// Check if file version is newer than system version
				if (filetypeVersion > systemVersion) {
					throw new DaxploreException("File version type is "
							+ filetypeVersion 
							+ ", but system only supports up to file version: "
							+ systemVersion
							+ ". Please upgrade your Daxplore Producer."); 
				}
				
				// Check if file version is too old
				// TODO prompt user: cancel opening or continue to upgrade file
				if (filetypeVersion  < 4) {
					stmt.executeUpdate("PRAGMA foregin_keys = off;");
					stmt.executeUpdate("ALTER TABLE metaquestion RENAME TO metaquestion_tmp;");
					stmt.executeUpdate("CREATE TABLE metaquestion (id INTEGER PRIMARY KEY, col TEXT NOT NULL, type STRING NOT NULL, displaytypes STRING NOT NULL);");
					stmt.executeUpdate("INSERT INTO metaquestion (id, col, type, displaytypes) SELECT id, col, type, displaytypes FROM metaquestion_tmp;");
					stmt.executeUpdate("DROP TABLE metaquestion_tmp;");
					stmt.executeUpdate("PRAGMA foregin_keys = on;");
					stmt.executeUpdate("DELETE FROM settings WHERE key='filetypeversionmajor';");
					stmt.executeUpdate("DELETE FROM settings WHERE key='filetypeversionminor';");
					stmt.executeUpdate("PRAGMA user_version = 4;");
				}
			}
			
			settings = new Settings(connection);
			about = new About(connection, settings, createNew);
			rawMetaManager = new RawMetaManager(connection);
			rawDataManager = new RawDataManager(connection, rawMetaManager);
			
			for(String key : DaxploreProperties.clientSettings) {
				if(!settings.has(key)) {
					settings.putSetting(key, DaxploreProperties.clientSettingsDefaults.get(key));
				}
			}
			
			textReferenceManager = new TextReferenceManager(connection);
			metaScaleManager = new MetaScaleManager(connection, textReferenceManager);
			metaMeanManager = new MetaMeanManager(connection);
			metaTimepointShortManager = new MetaTimepointShortManager(connection, textReferenceManager);
			metaQuestionManager = new MetaQuestionManager(connection, textReferenceManager, metaScaleManager, metaMeanManager, metaTimepointShortManager);
			metaGroupManager = new MetaGroupManager(connection, textReferenceManager, metaQuestionManager);
			
			importExport = new ImportExportManager(this);
		} catch (SQLException e) {
			throw new DaxploreException("Failed to construct DaxploreFile", e);
		}
	}

	public About getAbout() {
		return about;
	}

	public Settings getSettings() {
		return settings;
	}

	public RawMetaManager getRawMetaManager() {
		return rawMetaManager;
	}

	public RawDataManager getRawDataManager() {
		return rawDataManager;
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

	public MetaMeanManager getMetaMeanManager() {
		return metaMeanManager;
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
		} catch (TransformerFactoryConfigurationError | TransformerException | SQLException | SAXException | IOException
				| ParserConfigurationException e) {
			throw new DaxploreException("Failed to write output file", e);
		}
	}

	public String importSPSS(File spssFile, Charset charset, Locale locale)
			throws FileNotFoundException, IOException, DaxploreException {
		return importExport.importSPSS(spssFile, charset, locale);
	}

	public void importL10n(Reader reader, L10nFormat format, Locale locale) throws IOException, DaxploreException {
		importExport.importL10n(reader, format, locale);
	}

	public void exportL10n(Writer writer, L10nFormat format, Locale locale, boolean onlyExportUsed)
			throws IOException, DaxploreException {
		importExport.exportL10n(writer, format, locale, onlyExportUsed);
	}

	public void saveAll() throws DaxploreException { // TODO: return boolean instead of throwing exception?
		try {
			boolean autocommit = connection.getAutoCommit();
			connection.setAutoCommit(false);
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);

			settings.putSetting("lastupdate", ZonedDateTime.now());
			settings.saveAll();
			about.save();
			rawMetaManager.saveAll();
			rawDataManager.saveAll();
			textReferenceManager.saveAll();
			metaQuestionManager.saveAll();
			metaScaleManager.saveAll(); // run after metaQuestionmanager's saveAll (foreign key)
			metaMeanManager.saveAll(); // run after metaQuestionmanager's saveAll (foreign key)
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
		return metaGroupManager.getUnsavedChangesCount() + metaQuestionManager.getUnsavedChangesCount()
				+ metaScaleManager.getUnsavedChangesCount() + metaMeanManager.getUnsavedChangesCount()
				+ metaTimepointShortManager.getUnsavedChangesCount() + textReferenceManager.getUnsavedChangesCount()
				+ about.getUnsavedChangesCount();
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
		if (!rawMetaManager.hasColumn(column)) {
			throw new DaxploreException("Tried to get data for non-existing column: " + column);
		}
		RawMetaQuestion rmq = rawMetaManager.getQuestion(column);
		List<VariableOptionInfo> infoList = new LinkedList<>();
		SortedMap<Object, Integer> counts = rawDataManager.getColumnValueCount(column);
		for (Map.Entry<Object, Integer> count : counts.entrySet()) {
			VariableOptionInfo optionInfo = new VariableOptionInfo(count.getKey(), count.getValue(), rmq.getQtype());
			infoList.add(optionInfo);
		}
		if (rmq.getValuelabels() != null) {
			switch (rmq.getQtype()) {
			case NUMERIC:
				for (Map.Entry<Object, String> texts : rmq.getValuelabels().entrySet()) {
					Double value = (Double) texts.getKey();
					for (VariableOptionInfo info : infoList) {
						if (value == info.getValue() || (value != null && value.equals(info.getValue()))) {
							info.setRawText(texts.getValue());
							break;
						}
					}
				}
				break;
			case TEXT:
				for (Map.Entry<Object, String> texts : rmq.getValuelabels().entrySet()) {
					String value = (String) texts.getKey();
					for (VariableOptionInfo info : infoList) {
						if (value == info.getValue() || (value != null && value.equals(info.getValue()))) {
							info.setRawText(texts.getValue());
							break;
						}
					}
				}
				break;
			}
		}
		return infoList;
	}

	// TODO move to a helper file?
	public void replaceAllTimepointsInQuestions() throws DaxploreException, SQLException {
		List<MetaTimepointShort> timepoints = metaTimepointShortManager.getAll();
		int tpAdded = 0, questionsModified = 0;
		for (MetaQuestion question : metaQuestionManager.getAll()) {
			List<MetaTimepointShort> questionTp = new LinkedList<>();
			SortedMap<Double, Integer> valueCounts = rawDataManager
					.getColumnValueCountWhere(about.getTimeSeriesShortColumn(), question.getColumn());
			questionTp.clear();
			for (Double key : valueCounts.keySet()) {
				for (MetaTimepointShort tp : timepoints) {
					if (key != null && tp.getValue() == key && valueCounts.get(key) > 0) {
						questionTp.add(tp);
					}
				}
			}
			if (questionTp.size() > 0) {
				questionsModified++;
				tpAdded += questionTp.size();
				question.setTimepoints(questionTp);
			}
		}
		String logString = String.format("replaceAllTimepointsInQuestions: %d questions affected, %d timepoints added",
				questionsModified, tpAdded);
		Logger.getGlobal().log(Level.INFO, logString);
	}

	public void discardChanges() throws DaxploreException {
		try {
			about.discardChanges();
			settings.discardChanges();
			rawMetaManager.discardChanges();
			rawDataManager.discardChanges();
			metaGroupManager.discardChanges();
			metaMeanManager.discardChanges();
			metaQuestionManager.discardChanges();
			metaScaleManager.discardChanges();
			metaTimepointShortManager.discardChanges();
			textReferenceManager.discardChanges();
		} catch (SQLException e) {
			throw new DaxploreException("Error when discarding changes", e);
		}

	}

	public static boolean isValidColumnName(String refstring) {
		return !Strings.isNullOrEmpty(refstring); // TODO improve old restraints: &&
													// refstring.matches("^[\\pL\\pN_\\.\\$\\#\\@]+$");
	}
}
