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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
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

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.daxplore.producer.daxplorelib.calc.StatsCalculation;
import org.daxplore.producer.daxplorelib.metadata.MetaGroup;
import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;
import org.daxplore.producer.daxplorelib.metadata.MetaScale;
import org.daxplore.producer.daxplorelib.metadata.MetaScale.Option;
import org.daxplore.producer.daxplorelib.metadata.MetaTimepointShort;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextTree;
import org.daxplore.producer.daxplorelib.raw.RawMeta.RawMetaQuestion;
import org.daxplore.producer.daxplorelib.raw.VariableType;
import org.daxplore.producer.daxplorelib.resources.DaxploreProperties;
import org.daxplore.producer.tools.MyTools;
import org.daxplore.producer.tools.Pair;
import org.daxplore.producer.tools.SortedProperties;
import org.opendatafoundation.data.FileFormatInfo;
import org.opendatafoundation.data.FileFormatInfo.ASCIIFormat;
import org.opendatafoundation.data.FileFormatInfo.Compatibility;
import org.opendatafoundation.data.spss.SPSSFile;
import org.opendatafoundation.data.spss.SPSSFileException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class ImportExportManager {
	
	private Connection connection;
	private DaxploreFile daxploreFile;
	
	private List<TextReference> emptyTextrefs = new LinkedList<>();
	
	public enum Formats {
		DATABASE, RESOURCE, JSON, RAW
	}
	
	public enum L10nFormat {
		PROPERTIES, CSV
	}
	
	ImportExportManager(Connection connection, DaxploreFile daxploreFile) {
		this.connection = connection;
		this.daxploreFile = daxploreFile;
	}
	
	void writeUploadFile(OutputStream output) throws TransformerFactoryConfigurationError, TransformerException, SQLException, DaxploreException, SAXException, IOException, ParserConfigurationException {
		long time = System.nanoTime();
		Logger.getGlobal().log(Level.INFO, "Starting to generate json data");
		
		emptyTextrefs.clear();
		
		StatsCalculation crosstabs = new StatsCalculation(connection, daxploreFile.getAbout());
		crosstabs.loadRawToMem();

		MetaGroup perspectives = daxploreFile.getMetaGroupManager().getPerspectiveGroup();
		SortedSet<MetaQuestion> selectedQuestions = new TreeSet<>(new Comparator<MetaQuestion>() {
			@Override
			public int compare(MetaQuestion o1, MetaQuestion o2) {
				return Integer.compare(o1.getId(), o2.getId());
			}
		});
		for(MetaQuestion perspective : perspectives.getQuestions()) {
			selectedQuestions.add(perspective);
		}
		JsonArray dataJSON = new JsonArray();
		List<String> warnings = new LinkedList<String>();
		for(MetaGroup group : daxploreFile.getMetaGroupManager().getQuestionGroups()) {
			for(MetaQuestion question : group.getQuestions()) {
				selectedQuestions.add(question);
				for(MetaQuestion perspective : perspectives.getQuestions()) {
					try {
						dataJSON.add(crosstabs.calculateData(daxploreFile.getAbout(), question, perspective, 10).toJSONObject());
					} catch (DaxploreWarning e) {
						warnings.add(e.getMessage());
					}
				}
			}
		}
		
		Logger.getGlobal().log(Level.INFO, "Generated data in " + ((System.nanoTime() -time)/Math.pow(10,9)) + "s");
		time = System.nanoTime();
		
		try (ZipOutputStream zout = new ZipOutputStream(output)) {
			//manifest
			Document manifest = getUploadManifest();
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			DOMSource xmlSource = new DOMSource(manifest);
			ZipEntry entry = new ZipEntry("manifest.xml");
		    zout.putNextEntry(entry);
			StreamResult streamResult = new StreamResult(zout);
			transformer.transform(xmlSource, streamResult);
			zout.flush();
			zout.closeEntry();

			StreamResult streamResultSystem = new StreamResult(System.out);
			transformer.transform(xmlSource, streamResultSystem);
			
			Gson plainGson = new Gson();
			Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
			
			// Generate a single json string and replace "}}},{" with "}}},\n{" to create rows in the file
			writeZipString(zout, "data.json", plainGson.toJson(dataJSON).replaceAll("(}}},\\{)", "}}},\n{")); 
			
			JsonObject boolSettings = new JsonObject();
			for(String setting : DaxploreProperties.clientBoolSettings) {
				boolSettings.add(setting, new JsonPrimitive(daxploreFile.getSettings().getBool(setting)));
			}
			writeZipString(zout, "boolsettings.json", prettyGson.toJson(boolSettings));
			
			for(Locale locale : daxploreFile.getAbout().getLocales()) {
				JsonArray questionJSON = new JsonArray();
				for(MetaQuestion q : selectedQuestions) {
					questionJSON.add(q.toJSONObject(locale));
					emptyTextrefs.addAll(q.getEmptyTextRefs(locale));
				}
				
				String propertiesJSONString = prettyGson.toJson(getUITextsJson(locale));
				writeZipString(zout, "usertexts_"+locale.toLanguageTag()+".json", propertiesJSONString);
				
				writeZipString(zout, "questions_"+locale.toLanguageTag()+".json", prettyGson.toJson(questionJSON));
			    
			    String groupJSONString = prettyGson.toJson(daxploreFile.getMetaGroupManager().getQuestionGroupsJSON(locale));
			    emptyTextrefs.addAll(daxploreFile.getMetaGroupManager().getEmptyTextRefs(locale));
			    writeZipString(zout, "groups_"+locale.toLanguageTag()+".json", groupJSONString);
			    
			    writeZipString(zout, "perspectives_"+locale.toLanguageTag()+".json", prettyGson.toJson(perspectives.toJSONObject(locale)));
			}
	
			zout.flush();
		}
		
		crosstabs.dropRawFromMem();
		
		Logger.getGlobal().log(Level.INFO, "Created file in " + ((System.nanoTime() -time)/Math.pow(10,9)) + "s");
		if(!emptyTextrefs.isEmpty()) {
			Collections.sort(emptyTextrefs);
			Logger.getGlobal().log(Level.WARNING, "Missing text for textref(s):\n\t " + MyTools.join(emptyTextrefs, "\n\t "));
		}
		
		if(!warnings.isEmpty()) {
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
	
	private Document getUploadManifest() throws SAXException, IOException, ParserConfigurationException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setSchema(getUploadFileManifestSchema());
		Document doc = dbf.newDocumentBuilder().newDocument();
		doc.setXmlStandalone(true);
		
		Element root = doc.createElement("daxploreUploadFileManifest");
		doc.appendChild(root);
		root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		root.setAttribute("xsi:noNamespaceSchemaLocation", "UploadFileManifest.xsd");
		
		Element version = doc.createElement("fileVersion");
		root.appendChild(version);
		Element major = doc.createElement("major");
		version.appendChild(major);
		major.appendChild(doc.createTextNode(""+DaxploreProperties.filetypeversionmajor));
		Element minor = doc.createElement("minor");
		version.appendChild(minor);
		minor.appendChild(doc.createTextNode(""+DaxploreProperties.filetypeversionminor));
		
		Element supportedLocales = doc.createElement("supportedLocales");
		root.appendChild(supportedLocales);
		for(Locale locale : daxploreFile.getAbout().getLocales()) {
			Element localeElement = doc.createElement("language-BCP47");
			supportedLocales.appendChild(localeElement);
			localeElement.appendChild(doc.createTextNode(locale.toLanguageTag()));
		}
		
		Element defaultLocale = doc.createElement("defaultLocale");
		root.appendChild(defaultLocale);
		Element localeElement = doc.createElement("language-BCP47");
		defaultLocale.appendChild(localeElement);
		localeElement.appendChild(doc.createTextNode(daxploreFile.getAbout().getLocales().get(0).toLanguageTag())); // TODO pick default locale properly
		
		return doc;
	}
	
	private static Schema getUploadFileManifestSchema() throws SAXException, IOException {
		try (InputStream stream = DaxploreFile.class.getResourceAsStream("UploadFileManifest.xsd")) {
			SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = sf.newSchema(new StreamSource(stream));
			return schema;
		}
	}
	
	private JsonElement getUITextsJson(Locale locale) throws DaxploreException {
		JsonObject json = new JsonObject();
		for(String property: DaxploreProperties.presenterUITexts) {
			TextReference ref = daxploreFile.getTextReferenceManager().get(property);
			json.addProperty(property, getTextRefForExport(ref, locale));
		}
		for(MetaTimepointShort mtp: daxploreFile.getMetaTimepointShortManager().getAll()) {
			TextReference ref = mtp.getTextRef();
			json.addProperty("timepoint" + mtp.getTimeindex(), getTextRefForExport(ref, locale));
		}
		return json;
	}
	
	private String getTextRefForExport(TextReference ref, Locale locale) {
		if(!Strings.isNullOrEmpty(ref.getText(locale))) { 
			return ref.getText(locale);
		} else {
			emptyTextrefs.add(ref);
			return "";
		}
	}
	
	/* 
	 * Import/export methods that are used to change metadata in batch.
	 * The preferred way too use the library.
	 */
	void importFromRaw(Locale locale) throws DaxploreException {
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
			
			for(RawMetaQuestion rmq : daxploreFile.getRawMeta().getQuestions()) {
				String column = rmq.column;
				VariableType type = rmq.qtype;
				TextReference fulltext = daxploreFile.getTextReferenceManager().get(column + "_fulltext");
				
				fulltext.put(rmq.qtext, locale);
				List<MetaScale.Option<?>> scaleOptions = new LinkedList<MetaScale.Option<?>>();
				Set<Double> meanExcludedValues = null;
				if(rmq.valuelables != null) {
					int i = 1;
					switch(type) {
					case NUMERIC:
//						LinkedList<MetaScale.Option<Double>> scaleOptionsDouble = new LinkedList<>();
						for(Map.Entry<Object, String> s: rmq.valuelables.entrySet()) {
							TextReference ref = daxploreFile.getTextReferenceManager().get(rmq.column + "_option_" + i);
							ref.put(s.getValue(), locale);
							if(s.getKey() instanceof Double) {
								Collection<Double> vals = new LinkedList<Double>();
								vals.add((Double)s.getKey());
								scaleOptions.add(new MetaScale.Option<Double>(ref, vals, true));
							} else {
								throw new DaxploreException("Trying to add a non number or string as an option");
							}
							i++;
						}
						meanExcludedValues = new HashSet<>();
						break;
					case TEXT:
						for(Map.Entry<Object, String> s: rmq.valuelables.entrySet()) {
							TextReference ref = daxploreFile.getTextReferenceManager().get(rmq.column + "_option_" + i);
							ref.put(s.getValue(), locale);
							if(s.getKey() instanceof String) {
								Collection<String> vals = new LinkedList<String>();
								vals.add((String)s.getKey());
								scaleOptions.add(new MetaScale.Option<String>(ref, vals, true));
							} else {
								throw new DaxploreException("Trying to add a non number or string as an option");
							}
							i++;
						}
						break;
					}
				}
				
				TextReference shorttext = daxploreFile.getTextReferenceManager().get(column + "_shorttext");
				TextReference descriptiontext = daxploreFile.getTextReferenceManager().get(column + "_description");
				List<MetaTimepointShort> timepoints = new LinkedList<>();
				daxploreFile.getMetaQuestionManager().create(column, type, shorttext, fulltext, descriptiontext, scaleOptions, meanExcludedValues, Double.NaN, timepoints);
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
	
	void importSPSS(File spssFile, Charset charset)
			throws FileNotFoundException, IOException, DaxploreException {
		FileFormatInfo ffi = new FileFormatInfo();
		ffi.namesOnFirstLine = false;
		ffi.asciiFormat = ASCIIFormat.CSV;
		ffi.compatibility = Compatibility.GENERIC;
		
		try (SPSSFile importSPSSFile = new SPSSFile(spssFile, charset)) {
			importSPSSFile.logFlag = false;
			importSPSSFile.loadMetadata();
		
			boolean autocommit = false;
			try {
				autocommit = connection.getAutoCommit();
				connection.setAutoCommit(false);
				int isolation = connection.getTransactionIsolation();
				connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
				
				daxploreFile.getRawMeta().importSPSS(importSPSSFile); //Order important, meta before data
				daxploreFile.getRawData().importSPSS(importSPSSFile);
					
				daxploreFile.getAbout().setImport(importSPSSFile.file.getName());
				
				connection.commit();
				connection.setTransactionIsolation(isolation);
				connection.setAutoCommit(autocommit);
			} catch (SQLException e) {
				MyTools.printSQLExeption(e);
				try {
					connection.rollback();
					connection.setAutoCommit(autocommit);
				} catch (SQLException e1) {
					throw new DaxploreException("Import error. Could not rollback.", e);
				}
				throw new DaxploreException("Import error.", e);
			}
		} catch (SPSSFileException e2) {
			throw new DaxploreException("SPSSFileException", e2);
		}
	}
	
	/**
	 * 
	 * @param reader A character based reader, compatible with {@link Properties#load(Reader)}
	 * @param format
	 * @param locale
	 * @throws IOException
	 * @throws DaxploreException 
	 */
	void importL10n(Reader reader, L10nFormat format, Locale locale) throws IOException, DaxploreException {
		switch(format) {
		case PROPERTIES:
			Properties properties = new Properties();
			properties.load(reader);
			
			Iterator<Entry<Object, Object>> allTexts = properties.entrySet().iterator();
			while(allTexts.hasNext()) {
				Entry<Object, Object> s = allTexts.next();
				TextReference tr = daxploreFile.getTextReferenceManager().get((String)s.getKey());
				tr.put((String)s.getValue(), locale);
			}
			break;
		case CSV:
			try (CSVReader csvReader = new CSVReader(reader)) {
				for (String[] row : csvReader.readAll()) {
					if(row.length==0) {
						continue;
					} else if(row.length==2) {
						TextReference tr;
						String refText = row[0];
						if(!Strings.isNullOrEmpty(refText)){
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
	 * @param writer A character based writer, compatible with {@link Properties#store(Writer, String)}
	 * @param format
	 * @param locale
	 * @param onlyExportUsed 
	 * @throws IOException
	 * @throws DaxploreException 
	 */
	void exportL10n(Writer writer, L10nFormat format, Locale locale, boolean onlyExportUsed) throws IOException, DaxploreException {
		List<Pair<String, String>> output = new ArrayList<>();
		Set<TextReference> writtenRefs = new HashSet<>();
		
		if (onlyExportUsed) {
			for(String text : DaxploreProperties.presenterUITexts) {
				TextReference tr = daxploreFile.getTextReferenceManager().get(text);
				output.add(createOutputRow(tr, locale));
			}
			
			for(MetaGroup group : daxploreFile.getMetaGroupManager().getAll()) {
				output.add(new Pair<String, String>("", ""));
				TextReference tr = group.getTextRef();
				output.add(createOutputRow(tr, locale));
				
				for(MetaQuestion question : group.getQuestions()) {
					output.add(new Pair<String, String>("", ""));
					
					tr = question.getShortTextRef();
					if(writtenRefs.add(tr)) {
						output.add(createOutputRow(tr, locale));
					}
					
					tr = question.getFullTextRef();
					if(writtenRefs.add(tr)) {
						output.add(createOutputRow(tr, locale));
					}
					
					tr = question.getDescriptionTextRef();
					if(writtenRefs.add(tr)) {
						output.add(createOutputRow(tr, locale));
					}
					
					if(question.useFrequencies() || daxploreFile.getMetaGroupManager().getPerspectiveGroup().contains(question)) {
						for(Option option : question.getScale().getOptions()) {
							tr = option.getTextRef();
							if(writtenRefs.add(tr)) {
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

		switch(format) {
		case PROPERTIES:
			Properties properties = new SortedProperties();
			for(Pair<String, String> pair : output) {
				properties.setProperty(pair.getKey(), pair.getValue());
			}
			properties.store(writer, null); // 2nd argument is documentation comment placed on the first row of the file, can be null
			break;
		case CSV:
			try(CSVWriter csvWriter = new CSVWriter(writer)) {
				for(Pair<String, String> pair : output) {
					csvWriter.writeNext(new String[]{pair.getKey(), pair.getValue()});
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
