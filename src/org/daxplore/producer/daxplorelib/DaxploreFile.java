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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
import org.daxplore.producer.tools.MyTools;
import org.xml.sax.SAXException;

import com.google.common.base.Strings;

public class DaxploreFile implements Closeable {

	private File file = null;
	private ImportExportManager importExport;
	private int daxploreFileVersion;
	private int fileApplicationID;
	private boolean initialized = false;

	private Connection connection;
	private Settings settings;
	private About about;

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
			DaxploreFile df = new DaxploreFile(connection, true, file);
			df.initializeInternalStructures();
			return df;
		} catch (SQLException e) {
			throw new DaxploreException("Could not create new sqlite database (No write access?)", e);
		}
	}

	private DaxploreFile(Connection connection, boolean createNew, File file) throws DaxploreException {
		this.connection = connection;
		this.file = file;
	
		if (createNew) {
			fileApplicationID = DaxploreProperties.daxploreFileApplicationID;
			daxploreFileVersion = DaxploreProperties.daxploreFileVersion;
		} else {
			try (Statement stmt = connection.createStatement()) {
				ResultSet rs = stmt.executeQuery("PRAGMA application_id;");
				fileApplicationID = rs.getInt("application_id");
			} catch (SQLException e) {
				throw new DaxploreException("Failed to check \"PRAGMA application_id\" for opened file.", e);
			}
			try (Statement stmt = connection.createStatement()) {
				ResultSet rs = stmt.executeQuery("PRAGMA user_version;");
				daxploreFileVersion = rs.getInt("user_version");
			} catch (SQLException e) {
				throw new DaxploreException("Failed to check \"PRAGMA user_version\" for opened file.", e);
			}
		}
	}
	
		
	/**
	 * Get the SQLite "PRAGMA application_id" from the save file.
	 * @return the file's SQLite application_id
	 */
	public int getFileApplicationVersion() {
		return fileApplicationID;
	}

	/**
	 * Check if a loaded file uses the correct application_id for a .daxplore save file.
	 * 
	 * This assumes that the file is already confirmed to be a SQLite file and that a SQL connection has been made.
	 * 
	 * @return true if the file uses the correct SQLite application_id
	 * @throws DaxploreException thrown in case there is an error checking application_id
	 */
	public boolean hasValidApplicationID() {
		return fileApplicationID == DaxploreProperties.daxploreFileApplicationID;
	}
	
	/**
	 * Get the .daxplore save file version, as defined by SQLite "PRAGMA user_version"
	 * 
	 * @return the save file version 
	 */
	public int getFileVersion() {
		return daxploreFileVersion;
	}
	
	/**
	 * Get a hash based ID based on the filename
	 * 
	 * Only intended for internal use, to keep track of exported files without uploading actual filenames.
	 * Not intended to be robust on the level of a UUID.
	 * 
	 * @return A short ID
	 * @throws DaxploreException 
	 */
	public String getFileNameID () throws DaxploreException {
		try {
			// Add short, randomized IDs based on the used daxplore filename and spss filename
			// Only a few characters are needed, as this is only used for internal reference to keep track of exported versions.
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update((byte) 131);
			return MyTools.bytesToHex(md.digest(getFile().getName().getBytes(StandardCharsets.UTF_8))).substring(0, 8);
		} catch (NoSuchAlgorithmException e) {
			throw new DaxploreException("Failed to create hash", e);
		}
	}
	
	/**
	 * Upgrade the file to the current .daxplore save file version.
	 * 
	 * Implemented as a sequence of SQL changes. This will instantly write the changes
	 * to the .daxplore save file on disk, without further user input.
	 *  
	 * Compare {@link #getFileApplicationVersion()} with {@link DaxploreProperties#daxploreFileVersion}
	 * to check if the file needs to be upgraded.
	 *  
	 * @throws DaxploreException thrown if upgrade failed
	 */
	public void upgradeFileVersion() throws DaxploreException {
		try {
		boolean autocommit = connection.getAutoCommit();
		connection.setAutoCommit(false);
		connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
		Statement stmt = connection.createStatement();
		if (daxploreFileVersion < 4) {
			stmt.executeUpdate("PRAGMA foregin_keys = off;");
			stmt.executeUpdate("ALTER TABLE metaquestion RENAME TO metaquestion_tmp;");
			stmt.executeUpdate("CREATE TABLE metaquestion (id INTEGER PRIMARY KEY, col TEXT NOT NULL, type STRING NOT NULL, displaytypes STRING NOT NULL);");
			stmt.executeUpdate("INSERT INTO metaquestion (id, col, type, displaytypes) SELECT id, col, type, displaytypes FROM metaquestion_tmp;");
			stmt.executeUpdate("DROP TABLE metaquestion_tmp;");
			stmt.executeUpdate("PRAGMA foregin_keys = on;");
			stmt.executeUpdate("DELETE FROM settings WHERE key='filetypeversionmajor';");
			stmt.executeUpdate("DELETE FROM settings WHERE key='filetypeversionminor';");
			stmt.executeUpdate("PRAGMA user_version = 4;");
			daxploreFileVersion = 4;
		}
		if (daxploreFileVersion < 5) {
			stmt.executeUpdate("UPDATE texts SET ref='explorer.perspective_picker.all_respondents' WHERE ref='allRespondents';");
			
			stmt.executeUpdate("UPDATE texts SET ref='explorer.chart_tab.dichotomized_line' WHERE ref='chartTabDichotomized';");
			stmt.executeUpdate("UPDATE texts SET ref='explorer.chart_tab.frequency_bar' WHERE ref='chartTabFrequencies';");
			stmt.executeUpdate("UPDATE texts SET ref='explorer.chart_tab.mean_bar' WHERE ref='chartTabMeans';");

			stmt.executeUpdate("UPDATE texts SET ref='explorer.chart.dichotomized_line.subtitle_start' WHERE ref='dichotomizedSubtitleStart';");
			stmt.executeUpdate("UPDATE texts SET ref='explorer.chart.dichotomized_line.subtitle_separator' WHERE ref='dichotomizedSubtitleSeparator';");
			stmt.executeUpdate("UPDATE texts SET ref='explorer.chart.dichotomized_line.subtitle_or' WHERE ref='dichotomizedSubtitleOr';");
			stmt.executeUpdate("UPDATE texts SET ref='explorer.chart.dichotomized_line.subtitle_end' WHERE ref='dichotomizedSubtitleEnd';");
			
			stmt.executeUpdate("UPDATE texts SET ref='explorer.chart.frequency_bar.legend.missing_data' WHERE ref='explorer.freq.legend.missing_data';");
			stmt.executeUpdate("UPDATE texts SET ref='explorer.chart.frequency_bar.tooltip.single_timepoint' WHERE ref='explorer.freq.tooltip.single';");
			stmt.executeUpdate("UPDATE texts SET ref='explorer.chart.frequency_bar.tooltip.single_timepoint_missing' WHERE ref='explorer.freq.tooltip.single_missing_data';");
			stmt.executeUpdate("UPDATE texts SET ref='explorer.chart.frequency_bar.tooltip.multiple_timepoints' WHERE ref='explorer.freq.tooltip.timepoints';");
			stmt.executeUpdate("UPDATE texts SET ref='explorer.chart.frequency_bar.tooltip.multiple_timepoints_missing' WHERE ref='explorer.freq.tooltip.timepoints_missing_data';");
			
			stmt.executeUpdate("UPDATE texts SET ref='explorer.chart.mean_bar_vertical.image.filename' WHERE ref='explorer.meanprofile.image.filename';");
			
			stmt.executeUpdate("UPDATE texts SET ref='explorer.perspective_picker.header' WHERE ref='perspectivesHeader' OR 'explorer.perspective.header';");
			stmt.executeUpdate("UPDATE texts SET ref='explorer.perspective_picker.secondary_perspective.header' WHERE ref='explorer.perspective.header_secondary';");
			stmt.executeUpdate("UPDATE texts SET ref='explorer.perspective_picker.secondary_perspective.button.none' WHERE ref='explorer.perspective.secondary_none_button';");
			
			stmt.executeUpdate("UPDATE texts SET ref='common.button.save_chart_as_image' WHERE ref='imageSaveButton';");
			
			stmt.executeUpdate("INSERT INTO texts(ref, locale, text) SELECT 'explorer.chart.mean_bar_vertical.reference.value', locale, text FROM texts WHERE ref='listReferenceValue';");
			stmt.executeUpdate("INSERT INTO texts(ref, locale, text) SELECT 'explorer.chart.mean_bar_vertical.reference.better', locale, text FROM texts WHERE ref='listReferenceBetter';");
			stmt.executeUpdate("INSERT INTO texts(ref, locale, text) SELECT 'explorer.chart.mean_bar_vertical.reference.worse', locale, text FROM texts WHERE ref='listReferenceWorse';");
			stmt.executeUpdate("INSERT INTO texts(ref, locale, text) SELECT 'explorer.chart.mean_bar_vertical.reference.comparable', locale, text FROM texts WHERE ref='listReferenceComparable';");
			stmt.executeUpdate("INSERT INTO texts(ref, locale, text) SELECT 'explorer.chart.mean_bar_vertical.x_axis_description', locale, text FROM texts WHERE ref='listXAxisDescription';");

			stmt.executeUpdate("UPDATE texts SET ref='profile.chart.mean_bar_vertical.reference.better' WHERE ref='listReferenceBetter';");
			stmt.executeUpdate("UPDATE texts SET ref='profile.chart.mean_bar_vertical.reference.comparable' WHERE ref='listReferenceComparable';");
			stmt.executeUpdate("UPDATE texts SET ref='profile.chart.mean_bar_vertical.reference.value' WHERE ref='listReferenceValue';");
			stmt.executeUpdate("UPDATE texts SET ref='profile.chart.mean_bar_vertical.reference.worse' WHERE ref='listReferenceWorse';");
			stmt.executeUpdate("UPDATE texts SET ref='profile.chart.mean_bar_vertical.x_axis_description' WHERE ref='listXAxisDescription';");
			
			stmt.executeUpdate("UPDATE texts SET ref='explorer.chart.mean_bar_horizontal.legend.missing_data' WHERE ref='meanbars_legend_missingData';");
			stmt.executeUpdate("UPDATE texts SET ref='explorer.chart.mean_bar_horizontal.legend.reference_value' WHERE ref='meanbars_legend_referenceValue';");
			stmt.executeUpdate("UPDATE texts SET ref='explorer.chart.mean_bar_horizontal.tooltip.few_respondents' WHERE ref='meanbars_tooltip_fewRespondents';");
			stmt.executeUpdate("UPDATE texts SET ref='explorer.chart.mean_bar_horizontal.tooltip.mean' WHERE ref='meanbars_tooltip_mean';");
			stmt.executeUpdate("UPDATE texts SET ref='explorer.chart.mean_bar_horizontal.tooltip.missing_data' WHERE ref='meanbars_tooltip_missingData';");
			stmt.executeUpdate("UPDATE texts SET ref='explorer.chart.mean_bar_horizontal.tooltip.reference_value' WHERE ref='meanbars_tooltip_referenceValue';");
			stmt.executeUpdate("UPDATE texts SET ref='explorer.chart.mean_bar_horizontal.tooltip.respondents' WHERE ref='meanbars_tooltip_respondents';");
			
			stmt.executeUpdate("UPDATE texts SET ref='explorer.perspective_picker.button.select_all' WHERE ref='perspectivesAllButton';");
			stmt.executeUpdate("UPDATE texts SET ref='explorer.perspective_picker.button.show_less' WHERE ref='perspectivesLessButton';");
			stmt.executeUpdate("UPDATE texts SET ref='explorer.perspective_picker.button.show_more' WHERE ref='perspectivesMoreButton';");
			stmt.executeUpdate("UPDATE texts SET ref='explorer.perspective_picker.button.select_none' WHERE ref='perspectivesNoneButton';");
			
			stmt.executeUpdate("UPDATE texts SET ref='profile.chart.mean_bar_vertical.image.watermark' WHERE ref='profile.image.water_stamp' OR ref='profile.image.watermark';");
			
			stmt.executeUpdate("UPDATE texts SET ref='user_profile.grid.image.filename' WHERE ref='profile_user.grid_image.filename';");
			stmt.executeUpdate("UPDATE texts SET ref='user_profile.mean_bar_vertical.image.filename' WHERE ref='profile_user.image.filename';");
			stmt.executeUpdate("UPDATE texts SET ref='user_profile.grid.image.watermark' WHERE ref='profile_user.image.water_stamp';");
			stmt.executeUpdate("UPDATE texts SET ref='user_profile.chart.mean_bar_vertical.image.filename' WHERE ref='profile_user.chart_image.filename';");
			stmt.executeUpdate("UPDATE texts SET ref='user_profile.image.watermark' WHERE ref='profile_user.image.watermark';");
			
			stmt.executeUpdate("UPDATE texts SET ref='explorer.question_picker.section_header' WHERE ref='questionsHeader';");
			
			stmt.executeUpdate("UPDATE texts SET ref='user_profile.paste_data.header' WHERE ref='userProfileHeaderText';");
			stmt.executeUpdate("UPDATE texts SET ref='user_profile.paste_data.instruction' WHERE ref='userProfilePasteDataDescription';");
			stmt.executeUpdate("UPDATE texts SET ref='user_profile.paste_data.submit.button' WHERE ref='userPasteDataSubmitButton' OR 'user-paste-data-submit-button';");
			stmt.executeUpdate("UPDATE texts SET ref='user_profile.paste_data.submit.explanation' WHERE ref='userProfilePasteDataSubmitExplanation';");
			stmt.executeUpdate("UPDATE texts SET ref='user_profile.paste_data.error_log.header' WHERE ref='userProfilePasteDataErrorLogHeader';");
			stmt.executeUpdate("UPDATE texts SET ref='user_profile.paste_data.error_log.header.number_bounds' WHERE ref='userPasteDataErrorTextNumberBoundsErrors';");
			stmt.executeUpdate("UPDATE texts SET ref='user_profile.paste_data.error_log.header.no_number' WHERE ref='userPasteDataErrorTextNoNumberErrors';");
			stmt.executeUpdate("UPDATE texts SET ref='user_profile.paste_data.error_log.header.no_row' WHERE ref='userPasteDataErrorTextNoRowErrors ';");
			
			stmt.executeUpdate("PRAGMA user_version = 5;");
			daxploreFileVersion = 5;
		}
		
		connection.commit();
		connection.setAutoCommit(autocommit);
		} catch (SQLException e) {
			throw new DaxploreException("Failed to upgrade file.", e);
		}
	}
	
	public void initializeInternalStructures() throws DaxploreException {
		if (!hasValidApplicationID()) {
			throw new DaxploreException("File does not have a valid application ID. Check hasValidApplicationID before initializing the internal structures to avoid Exceptions.");
		}
		
		if (daxploreFileVersion != DaxploreProperties.daxploreFileVersion) {
			throw new DaxploreException("Daxplore file has file version " + daxploreFileVersion
					+ ". This version of Daxplore Producer only supports .daxplore save file version " + DaxploreProperties.daxploreFileVersion
					+ ". Run DaxploreFile's upgradeFileVersion method before initializing the internal structures.");
		}
		
		try {
			settings = new Settings(connection);
			about = new About(connection, settings);
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
			initialized = true;
		} catch (SQLException e) {
			throw new DaxploreException("Failed to construct DaxploreFile", e);
		}
	}

	public About getAbout() {
		if (!initialized) { throw new Error("Initialize DaxploreFile before using it."); }
		return about;
	}

	public Settings getSettings() {
		if (!initialized) { throw new Error("Initialize DaxploreFile before using it."); }
		return settings;
	}

	public RawMetaManager getRawMetaManager() {
		if (!initialized) { throw new Error("Initialize DaxploreFile before using it."); }
		return rawMetaManager;
	}

	public RawDataManager getRawDataManager() {
		if (!initialized) { throw new Error("Initialize DaxploreFile before using it."); }
		return rawDataManager;
	}

	public MetaGroupManager getMetaGroupManager() {
		if (!initialized) { throw new Error("Initialize DaxploreFile before using it."); }
		return metaGroupManager;
	}

	public MetaQuestionManager getMetaQuestionManager() {
		if (!initialized) { throw new Error("Initialize DaxploreFile before using it."); }
		return metaQuestionManager;
	}

	public MetaScaleManager getMetaScaleManager() {
		if (!initialized) { throw new Error("Initialize DaxploreFile before using it."); }
		return metaScaleManager;
	}

	public MetaMeanManager getMetaMeanManager() {
		if (!initialized) { throw new Error("Initialize DaxploreFile before using it."); }
		return metaMeanManager;
	}

	public MetaTimepointShortManager getMetaTimepointShortManager() {
		if (!initialized) { throw new Error("Initialize DaxploreFile before using it."); }
		return metaTimepointShortManager;
	}

	public TextReferenceManager getTextReferenceManager() {
		if (!initialized) { throw new Error("Initialize DaxploreFile before using it."); }
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
