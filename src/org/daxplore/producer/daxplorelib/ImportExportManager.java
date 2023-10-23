/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.daxplorelib;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.daxplore.producer.daxplorelib.calc.BarStats;
import org.daxplore.producer.daxplorelib.calc.StatsCalculation;
import org.daxplore.producer.daxplorelib.metadata.MetaGroup;
import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;
import org.daxplore.producer.daxplorelib.metadata.MetaScale;
import org.daxplore.producer.daxplorelib.metadata.MetaScale.Option;
import org.daxplore.producer.daxplorelib.metadata.MetaTimepointShort;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextTree;
import org.daxplore.producer.daxplorelib.raw.RawMetaQuestion;
import org.daxplore.producer.daxplorelib.raw.RawMetaQuestion.RawMetaManager.RawMetaImportResult;
import org.daxplore.producer.daxplorelib.raw.VariableType;
import org.daxplore.producer.daxplorelib.resources.DaxploreProperties;
import org.daxplore.producer.gui.resources.UITexts;
import org.daxplore.producer.tools.MyTools;
import org.daxplore.producer.tools.Pair;
import org.daxplore.producer.tools.SortedProperties;
import org.opendatafoundation.data.FileFormatInfo;
import org.opendatafoundation.data.FileFormatInfo.ASCIIFormat;
import org.opendatafoundation.data.FileFormatInfo.Compatibility;
import org.opendatafoundation.data.spss.SPSSFile;
import org.opendatafoundation.data.spss.SPSSFileException;
import org.xml.sax.SAXException;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class ImportExportManager {

	private DaxploreFile daxploreFile;

	private List<TextReference> emptyTextrefs = new LinkedList<>();

	public enum Formats {
		DATABASE, RESOURCE, JSON, RAW
	}

	public enum L10nFormat {
		PROPERTIES, CSV
	}

	ImportExportManager(DaxploreFile daxploreFile) {
		this.daxploreFile = daxploreFile;
	}

	void writeUploadFile(OutputStream output) throws TransformerFactoryConfigurationError, TransformerException,
			SQLException, DaxploreException, SAXException, IOException, ParserConfigurationException {
		long time = System.nanoTime();
		Logger.getGlobal().log(Level.INFO, "Starting to generate json data");

		emptyTextrefs.clear();

		StatsCalculation crosstabs = new StatsCalculation(daxploreFile.getAbout(), daxploreFile.getRawMetaManager(),
				daxploreFile.getRawDataManager());

		MetaGroup perspectives = daxploreFile.getMetaGroupManager().getPerspectiveGroup();
		MetaGroup perspectivesSecondary = daxploreFile.getMetaGroupManager().getPerspectiveSecondaryGroup();
		SortedSet<MetaQuestion> selectedQuestions = new TreeSet<>(new Comparator<MetaQuestion>() {
			@Override
			public int compare(MetaQuestion o1, MetaQuestion o2) {
				return Integer.compare(o1.getId(), o2.getId());
			}
		});
		for (MetaQuestion perspective : perspectives.getQuestions()) {
			selectedQuestions.add(perspective);
		}

		Map<MetaQuestion, JsonArray> questionData = new HashMap<>();
		List<String> warnings = new LinkedList<String>();
		List<MetaQuestion> listViewQuestions = daxploreFile.getMetaGroupManager().getListViewVariables();
		double maxListViewReferenceDiff = 0;
		for (MetaGroup group : daxploreFile.getMetaGroupManager().getQuestionGroups()) {
			for (MetaQuestion question : group.getQuestions()) {
				JsonArray questionJSON = new JsonArray();
				selectedQuestions.add(question);
				Set<String> usedPerspectiveCombos = new HashSet<>();
				for (MetaQuestion perspective : perspectives.getQuestions()) {
					List<MetaQuestion> selectedPerspectives = new ArrayList<>();
					selectedPerspectives.add(perspective);
					try {
						// Add question data
						BarStats barStats = crosstabs.calculateData(question, selectedPerspectives, 10);
						questionJSON.add(barStats.toJSONObject());
						// Calculate list view interval
						if (listViewQuestions.contains(question) && question.useMean()
								&& question.getMetaMean().useMeanReferenceValue()) {
							double meanReference = question.getMetaMean().getGlobalMean();
							double meanMaxDiff = Math.max(Math.abs(barStats.getMeanMaxValue() - meanReference),
									Math.abs(barStats.getMeanMinValue() - meanReference));
							maxListViewReferenceDiff = Math.max(meanMaxDiff, maxListViewReferenceDiff);
						}
					} catch (DaxploreWarning e) {
						warnings.add(e.getMessage());
					}
					for (MetaQuestion secondaryPerspective : perspectivesSecondary.getQuestions()) {
						if (usedPerspectiveCombos.contains(secondaryPerspective.getId() + "/" + perspective.getId())
								|| perspective.getId() == secondaryPerspective.getId()) {
							continue;
						}
						if (selectedPerspectives.size() == 2) {
							selectedPerspectives.remove(1);
						}
						selectedPerspectives.add(secondaryPerspective);
						try {
							questionJSON
									.add(crosstabs.calculateData(question, selectedPerspectives, 10).toJSONObject());
						} catch (DaxploreWarning e) {
							warnings.add(e.getMessage());
						}
						usedPerspectiveCombos.add(perspective.getId() + "/" + secondaryPerspective.getId());
					}
				}
				questionData.put(question, questionJSON);
			}
		}

		Logger.getGlobal().log(Level.INFO, "Generated data in " + ((System.nanoTime() - time) / Math.pow(10, 9)) + "s");
		time = System.nanoTime();

		try (ZipOutputStream zout = new ZipOutputStream(output)) {
			Gson plainGson = new Gson();
			Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();

			// Add data package manifest
			writeZipString(zout, "manifest.json", prettyGson.toJson(getExportDataPackageManifest()));

			// Generate a new file for each question
			for (MetaQuestion question : questionData.keySet()) {
				// Generate a single json string and replace "}}},{" with "}}},\n{" to create
				// rows in the file
				// TODO use other identifier than column?
				writeZipString(zout, "questions/" + question.getColumn() + ".json",
						plainGson.toJson(questionData.get(question)).replaceAll("(}}},\\{)", "}}},\n{"));
			}

			JsonObject settings = new JsonObject();
			for (String setting : DaxploreProperties.clientSettings) {
				switch (daxploreFile.getSettings().getType(setting)) {
				case BOOL:
					settings.add(setting, new JsonPrimitive(daxploreFile.getSettings().getBool(setting)));
					break;
				case DATE:
					String isoDate = DateTimeFormatter.ISO_INSTANT.format(daxploreFile.getSettings().getDate(setting));
					settings.add(setting, new JsonPrimitive(isoDate));
					break;
				case DOUBLE:
					settings.add(setting, new JsonPrimitive(daxploreFile.getSettings().getDouble(setting)));
					break;
				case INT:
					settings.add(setting, new JsonPrimitive(daxploreFile.getSettings().getInteger(setting)));
					break;
				case STRING:
					settings.add(setting, new JsonPrimitive(daxploreFile.getSettings().getString(setting)));
					break;
				default:
					break;
				}
			}

//			List<MetaQuestion> questions = daxploreFile.getMetaGroupManager().getListViewVariables();
//			int maxReferenceDiff = -1;
//			for (MetaQuestion question : questions) {
//				JsonArray ja = questionData.get(question);
//			}
			settings.add("listview.max_reference_diff", new JsonPrimitive((int) Math.ceil(maxListViewReferenceDiff)));

			writeZipString(zout, "settings.json", prettyGson.toJson(settings));

			// TODO either export a single locale OR update static server presenter to
			// handle multiple locales
			for (Locale locale : daxploreFile.getAbout().getLocales()) {
				JsonArray questionJSON = new JsonArray();
				for (MetaQuestion q : selectedQuestions) {
					questionJSON.add(q.toJSONObject(locale));
					emptyTextrefs.addAll(q.getEmptyTextRefs(locale));
				}

				String projectFilename = "daxplore-"
						+ daxploreFile.getSettings().getString("export.manifest.project_name");
				projectFilename = projectFilename.trim().replaceAll(" ", "_").replaceAll("[^a-zA-Z0-9_-]", "");
				projectFilename = projectFilename.substring(0, Math.min(30, projectFilename.length()));
				String projectMessage = "This is a Daxplore Presenter data package.\nSee https://daxplore.org for more information.";
				writeZipString(zout, projectFilename, projectMessage);

				String propertiesJSONString = prettyGson.toJson(getUITextsJson(locale));
				writeZipString(zout, "usertexts_" + locale.toLanguageTag() + ".json", propertiesJSONString);

				writeZipString(zout, "questions_" + locale.toLanguageTag() + ".json", prettyGson.toJson(questionJSON));

				String groupJSONString = prettyGson
						.toJson(daxploreFile.getMetaGroupManager().getQuestionGroupsJSON(locale));
				emptyTextrefs.addAll(daxploreFile.getMetaGroupManager().getEmptyTextRefs(locale));
				writeZipString(zout, "groups_" + locale.toLanguageTag() + ".json", groupJSONString);

				writeZipString(zout, "perspectives.json",
						prettyGson.toJson(daxploreFile.getMetaGroupManager().getPerspectiveGroupJsonObject()));

				String listViewJSONString = prettyGson
						.toJson(daxploreFile.getMetaGroupManager().getListViewVariablesJSON());
				writeZipString(zout, "listview.json", listViewJSONString);
			}

			zout.flush();
		}

		Logger.getGlobal().log(Level.INFO, "Created file in " + ((System.nanoTime() - time) / Math.pow(10, 9)) + "s");
		if (!emptyTextrefs.isEmpty()) {
			Collections.sort(emptyTextrefs);
			Logger.getGlobal().log(Level.WARNING,
					"Missing text for textref(s):\n\t " + MyTools.join(emptyTextrefs, "\n\t "));
		}

		if (!warnings.isEmpty()) {
			Joiner joiner = Joiner.on("\n");
			throw new DaxploreException(joiner.join(warnings));
		}
	}

	private static void writeZipString(ZipOutputStream zout, String filename, String dataString) throws IOException {
		ZipEntry entry = new ZipEntry(filename);
		zout.putNextEntry(entry);
		ByteBuffer buffer = Charsets.UTF_8.encode(dataString);
		byte[] outbytes = new byte[buffer.limit()];
		buffer.get(outbytes);
		zout.write(outbytes);
		zout.flush();
		zout.closeEntry();
	}

	/**
	 * Get a JSON representation of the export data package manifest content.
	 * 
	 * The manifest contains: - The data package version, which should be synced
	 * with the Daxplore Presenter data version - A list of locales supported in the
	 * exported data
	 * 
	 * @return a JSON element containing the upload manifests
	 * @throws DaxploreException
	 */
	private JsonElement getExportDataPackageManifest() throws DaxploreException {
		JsonObject json = new JsonObject();

		// Add data that allows later identification of the source of the export
		json.add("projectName",
				new JsonPrimitive(daxploreFile.getSettings().getString("export.manifest.project_name")));
		json.add("daxploreFileID", new JsonPrimitive(daxploreFile.getFileNameID()));
		json.add("dataFileID", new JsonPrimitive(daxploreFile.getAbout().getImportFileNameID()));

		// Add timestamp for export
		json.addProperty("exportDate", ZonedDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

		// Add the export version
		json.addProperty("dataPackageVersion", DaxploreProperties.exportDataPackageVersion);
		JsonArray locales = new JsonArray();

		// Add supported locales
		for (Locale locale : daxploreFile.getAbout().getLocales()) {
			// Use toLanguageTag to get the IETF BCP 47 language tag representation
			locales.add(new JsonPrimitive(locale.toLanguageTag()));
		}
		json.add("locales", locales);

		// TODO add used page types (i.e. explorer, profile, etc.) to manifest?

		return json;
	}

	private JsonElement getUITextsJson(Locale locale) throws DaxploreException {
		JsonObject json = new JsonObject();
		for (String property : DaxploreProperties.presenterUITexts) {
			TextReference ref = daxploreFile.getTextReferenceManager().get(property);
			json.addProperty(property, getTextRefForExport(ref, locale));
		}
		for (MetaTimepointShort mtp : daxploreFile.getMetaTimepointShortManager().getAll()) {
			TextReference ref = mtp.getTextRef();
			json.addProperty("timepoint" + mtp.getTimeindex(), getTextRefForExport(ref, locale));
		}
		return json;
	}

	private String getTextRefForExport(TextReference ref, Locale locale) {
		if (!Strings.isNullOrEmpty(ref.getText(locale))) {
			return ref.getText(locale);
		} else {
			emptyTextrefs.add(ref);
			return "";
		}
	}

	String importSPSS(File spssFile, Charset charset, Locale locale)
			throws FileNotFoundException, IOException, DaxploreException {
		FileFormatInfo ffi = new FileFormatInfo();
		ffi.namesOnFirstLine = false;
		ffi.asciiFormat = ASCIIFormat.CSV;
		ffi.compatibility = Compatibility.GENERIC;

		try (SPSSFile importSPSSFile = new SPSSFile(spssFile, charset)) {
			importSPSSFile.logFlag = false;
			importSPSSFile.loadMetadata();
			daxploreFile.getAbout().setImport(importSPSSFile.file.getName());

			// Order important, meta before data
			RawMetaImportResult metaImportResult = daxploreFile.getRawMetaManager().loadFromSPSS(importSPSSFile);
			daxploreFile.getRawDataManager().loadFromSPSS(importSPSSFile);

			List<String> guiMessages = new ArrayList<>();
			for (String column : metaImportResult.removedColumns) {
				for (MetaQuestion mq : daxploreFile.getMetaQuestionManager().getAll()) {
					if (mq.getColumn().equals(column)) {
						daxploreFile.getMetaGroupManager().removeQuestion(mq);
						daxploreFile.getMetaQuestionManager().remove(mq.getId());
						daxploreFile.getMetaMeanManager().remove(mq.getId());
						daxploreFile.getMetaScaleManager().remove(mq.getId());
					}
				}
				// TODO Communicate to GUI, but not on first import
				guiMessages.add(UITexts.get("library.import.removed_variable") + column);
			}

			for (String column : metaImportResult.maintainedColumns) {
				guiMessages.add(UITexts.get("library.import.matched_variable") + column);
			}

			for (String column : metaImportResult.updatedTypeColumns) {
				for (MetaQuestion mq : daxploreFile.getMetaQuestionManager().getAll()) {
					if (mq.getColumn().equals(column)) {
						VariableType vt = daxploreFile.getRawMetaManager().getQuestion(column).getQtype();
						mq.setType(vt);
						mq.getScale().setScaleVariableType(vt);
					}
				}
			}

			for (String column : metaImportResult.addedColumns) {
				RawMetaQuestion rmq = daxploreFile.getRawMetaManager().getQuestion(column);
				VariableType type = rmq.getQtype();
				TextReference fulltext = daxploreFile.getTextReferenceManager()
						.get(column + MetaQuestion.textrefSuffixFullText);

				fulltext.put(rmq.getQtext(), locale);
				List<MetaScale.Option<?>> scaleOptions = new LinkedList<MetaScale.Option<?>>();
				Set<Double> meanExcludedValues = new HashSet<>();

				// initially set all values as excluded for mean calculation
				for (Object value : daxploreFile.getRawDataManager().getColumnValueCount(column).keySet()) {
					if (value instanceof Double) {
						meanExcludedValues.add((Double) value);
					}
				}

				boolean meanValueIncluded = false;
				if (rmq.getValuelabels() != null) {
					int i = 1;
					switch (type) {
					case NUMERIC:
						for (Map.Entry<Object, String> s : rmq.getValuelabels().entrySet()) {
							TextReference ref = daxploreFile.getTextReferenceManager()
									.get(rmq.getColumn() + "_option_" + i);
							ref.put(s.getValue(), locale);
							if (s.getKey() instanceof Double) {
								Collection<Double> vals = new LinkedList<Double>();
								vals.add((Double) s.getKey());
								scaleOptions.add(new MetaScale.Option<Double>(ref, vals, true, true));

								// if there is a mapping for a value, default to include it in mean calculation
								meanValueIncluded |= !meanExcludedValues.remove((Double) s.getKey());
							} else {
								throw new DaxploreException("Trying to add a non number or string as an option");
							}
							i++;
						}
						break;
					case TEXT:
						for (Map.Entry<Object, String> s : rmq.getValuelabels().entrySet()) {
							TextReference ref = daxploreFile.getTextReferenceManager()
									.get(rmq.getColumn() + "_option_" + i);
							ref.put(s.getValue(), locale);
							if (s.getKey() instanceof String) {
								Collection<String> vals = new LinkedList<String>();
								vals.add((String) s.getKey());
								scaleOptions.add(new MetaScale.Option<String>(ref, vals, true, true));
							} else {
								throw new DaxploreException("Trying to add a non number or string as an option");
							}
							i++;
						}
						break;
					}
					// TODO Communicate to GUI, but not on first import
					guiMessages.add(UITexts.get("library.import.added_variable") + column);
				}

				if (!meanValueIncluded) {
					meanExcludedValues.clear();
				}

				TextReference shorttext = daxploreFile.getTextReferenceManager()
						.get(column + MetaQuestion.textrefSuffixShortText);
				TextReference descriptiontext = daxploreFile.getTextReferenceManager()
						.get(column + MetaQuestion.textrefSuffixDescriptionText);
				TextReference titlematchregex = daxploreFile.getTextReferenceManager()
						.get(column + MetaQuestion.textrefSuffixTitleMatchRegex);
				List<MetaTimepointShort> timepoints = new LinkedList<>();
				daxploreFile.getMetaQuestionManager().create(column, type, shorttext, fulltext, descriptiontext,
						titlematchregex, scaleOptions, meanExcludedValues, Double.NaN, timepoints);
			}
			guiMessages.add("");

			return MyTools.join(guiMessages, "\n") + "\n" + MyTools.join(metaImportResult.warnings, "\n");
		} catch (SPSSFileException e2) {
			throw new DaxploreException("Failed to load data from SPSS file", e2);
		}
	}

	/**
	 * 
	 * @param reader A character based reader, compatible with
	 *               {@link Properties#load(Reader)}
	 * @param format
	 * @param locale
	 * @throws IOException
	 * @throws DaxploreException
	 */
	void importL10n(Reader reader, L10nFormat format, Locale locale) throws IOException, DaxploreException {
		switch (format) {
		case PROPERTIES:
			Properties properties = new Properties();
			properties.load(reader);

			Iterator<Entry<Object, Object>> allTexts = properties.entrySet().iterator();
			while (allTexts.hasNext()) {
				Entry<Object, Object> s = allTexts.next();
				TextReference tr = daxploreFile.getTextReferenceManager().get((String) s.getKey());
				tr.put((String) s.getValue(), locale);
			}
			break;
		case CSV:
			try (CSVReader csvReader = new CSVReader(reader)) {
				for (String[] row : csvReader.readAll()) {
					if (row.length == 0) {
						continue;
					} else if (row.length == 2) {
						TextReference tr;
						String refText = row[0];
						if (!Strings.isNullOrEmpty(refText)) {
							tr = daxploreFile.getTextReferenceManager().get(row[0]);
							tr.put(row[1], locale);
						}
					} else {
						throw new DaxploreException("Invalid csv row:" + MyTools.join(row, ", "));
					}
				}
			}
			break;
		default:
			throw new AssertionError("Unsupported format: " + format);
		}
	}

	/**
	 * 
	 * @param writer         A character based writer, compatible with
	 *                       {@link Properties#store(Writer, String)}
	 * @param format
	 * @param locale
	 * @param onlyExportUsed
	 * @throws IOException
	 * @throws DaxploreException
	 */
	void exportL10n(Writer writer, L10nFormat format, Locale locale, boolean onlyExportUsed)
			throws IOException, DaxploreException {
		List<Pair<String, String>> output = new ArrayList<>();
		Set<TextReference> writtenRefs = new HashSet<>();

		if (onlyExportUsed) {
			for (String text : DaxploreProperties.presenterUITexts) {
				TextReference tr = daxploreFile.getTextReferenceManager().get(text);
				output.add(createOutputRow(tr, locale));
			}

			for (MetaGroup group : daxploreFile.getMetaGroupManager().getAll()) {
				output.add(new Pair<String, String>("", ""));
				TextReference tr = group.getTextRef();
				output.add(createOutputRow(tr, locale));

				for (MetaQuestion question : group.getQuestions()) {
					output.add(new Pair<String, String>("", ""));

					tr = question.getShortTextRef();
					if (writtenRefs.add(tr)) {
						output.add(createOutputRow(tr, locale));
					}

					tr = question.getFullTextRef();
					if (writtenRefs.add(tr)) {
						output.add(createOutputRow(tr, locale));
					}

					tr = question.getDescriptionTextRef();
					if (writtenRefs.add(tr)) {
						output.add(createOutputRow(tr, locale));
					}

					tr = question.getTitleMatchRegexTextRef();
					if (tr.hasText(locale) && tr.getText(locale).length() > 0 && writtenRefs.add(tr)) {
						output.add(createOutputRow(tr, locale));
					}

					if (question.useFrequencies() || question.useDichotomizedLine()
							|| daxploreFile.getMetaGroupManager().getPerspectiveGroup().contains(question)) {
						for (Option option : question.getScale().getOptions()) {
							tr = option.getTextRef();
							if (writtenRefs.add(tr)) {
								output.add(createOutputRow(tr, locale));
							}
						}
					}
				}
			}
		} else {
			TextTree allTexts = daxploreFile.getTextReferenceManager().getAll();
			for (TextReference tr : allTexts.iterable()) {
				if (tr.hasLocale(locale)) {
					output.add(new Pair<String, String>(tr.getRef(), tr.getText(locale)));
				} else {
					output.add(new Pair<String, String>(tr.getRef(), ""));
				}
			}
		}

		switch (format) {
		case PROPERTIES:
			Properties properties = new SortedProperties();
			for (Pair<String, String> pair : output) {
				properties.setProperty(pair.getKey(), pair.getValue());
			}
			properties.store(writer, null); // 2nd argument is documentation comment placed on the first row of the
											// file, can be null
			break;
		case CSV:
			try (CSVWriter csvWriter = new CSVWriter(writer)) {
				for (Pair<String, String> pair : output) {
					csvWriter.writeNext(new String[] { pair.getKey(), pair.getValue() });
				}
			}
			break;
		default:
			throw new AssertionError("Unsupported format: " + format);
		}
	}

	private static Pair<String, String> createOutputRow(TextReference tr, Locale locale) {
		if (tr.hasLocale(locale)) {
			return new Pair<String, String>(tr.getRef(), tr.getText(locale));
		} else {
			return new Pair<String, String>(tr.getRef(), "");
		}
	}
}
