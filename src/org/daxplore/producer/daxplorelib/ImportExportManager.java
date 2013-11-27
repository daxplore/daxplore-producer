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
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Properties;
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

import org.daxplore.producer.daxplorelib.calc.Crosstabs;
import org.daxplore.producer.daxplorelib.metadata.MetaCalculation;
import org.daxplore.producer.daxplorelib.metadata.MetaGroup;
import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;
import org.daxplore.producer.daxplorelib.metadata.MetaScale;
import org.daxplore.producer.daxplorelib.metadata.MetaTimepointShort;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextTree;
import org.daxplore.producer.daxplorelib.raw.RawMeta.RawMetaQuestion;
import org.daxplore.producer.daxplorelib.raw.VariableType;
import org.daxplore.producer.tools.MyTools;
import org.daxplore.producer.tools.NumberlineCoverage;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ImportExportManager {
	
	private Connection connection;
	private DaxploreFile daxploreFile;
	
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
		
		Crosstabs crosstabs = new Crosstabs(connection, daxploreFile.getAbout());
		crosstabs.loadRawToMem();

		MetaGroup perspectives = daxploreFile.getMetaGroupManager().getPerspectiveGroup();
		SortedSet<MetaQuestion> selectedQuestions = new TreeSet<>(new Comparator<MetaQuestion>() {
			@Override
			public int compare(MetaQuestion o1, MetaQuestion o2) {
				return o1.getId().compareTo(o2.getId());
			}
		});
		for(MetaQuestion perspective : perspectives.getQuestions()) {
			selectedQuestions.add(perspective);
		}
		JsonArray dataJSON = new JsonArray();
		for(MetaGroup group : daxploreFile.getMetaGroupManager().getQuestionGroups()) {
			for(MetaQuestion question : group.getQuestions()) {
				selectedQuestions.add(question);
				for(MetaQuestion perspective : perspectives.getQuestions()) {
					dataJSON.add(crosstabs.crosstabs2(question, perspective, 10).toJSONObject());
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
			writeZipString(zout, "data/data.json", plainGson.toJson(dataJSON).replaceAll("(}}},\\{)", "}}},\n{")); 
			
			for(Locale locale : daxploreFile.getAbout().getLocales()) {
				JsonArray questionJSON = new JsonArray();
				for(MetaQuestion q : selectedQuestions) {
					questionJSON.add(q.toJSONObject(locale));
				}
				
				String propertiesJSONString = prettyGson.toJson(getPropertiesJson(locale));
				writeZipString(zout, "properties/usertexts_"+locale.toLanguageTag()+".json", propertiesJSONString);
				
				writeZipString(zout, "meta/questions_"+locale.toLanguageTag()+".json", prettyGson.toJson(questionJSON));
			    
			    String groupJSONString = prettyGson.toJson(daxploreFile.getMetaGroupManager().getQuestionGroupsJSON(locale));
			    writeZipString(zout, "meta/groups_"+locale.toLanguageTag()+".json", groupJSONString);
			    
			    writeZipString(zout, "meta/perspectives_"+locale.toLanguageTag()+".json", prettyGson.toJson(perspectives.toJSONObject(locale)));
			}
	
			zout.flush();
		}
		
		crosstabs.dropRawFromMem();
		
		Logger.getGlobal().log(Level.INFO, "Created file in " + ((System.nanoTime() -time)/Math.pow(10,9)) + "s");
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
	
	private JsonElement getPropertiesJson(Locale locale) throws SQLException, DaxploreException {
		JsonObject json = new JsonObject();
		for(String property: DaxploreProperties.properties) {
			json.addProperty(property, daxploreFile.getTextReferenceManager().get(property).get(locale));
		}
		for(MetaTimepointShort mtp: daxploreFile.getMetaTimepointShortManager().getAll()) {
			json.addProperty("timepoint_" + mtp.getTimeindex(), mtp.getTextRef().get(locale));
		}
		return json;
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
			System.out.print("\n");
			for(RawMetaQuestion rmq : daxploreFile.getRawMeta().getQuestions()) {
				System.out.print(".");
				TextReference fulltext = daxploreFile.getTextReferenceManager().get(rmq.column + "_fulltext");
				fulltext.put(rmq.qtext, locale);
				MetaCalculation calc = new MetaCalculation(rmq.column, connection);
				MetaScale scale = null;
				if(rmq.qtype == VariableType.MAPPED) {
					LinkedList<MetaScale.Option> scaleOptions = new LinkedList<>();
					for(int i = 0; i < rmq.valuelables.size(); i++) {
						Pair<String, Double> s = rmq.valuelables.get(i);
						TextReference ref = daxploreFile.getTextReferenceManager().get(rmq.column + "_option_" + i);
						ref.put(s.getKey(), locale);
						scaleOptions.add(new MetaScale.Option(ref, s.getValue(), new NumberlineCoverage(s.getValue()), true));
					}
					scale = daxploreFile.getMetaScaleManager().create(scaleOptions, new NumberlineCoverage());
				}
				
				TextReference shorttext = daxploreFile.getTextReferenceManager().get(rmq.column + "_shorttext");
				List<MetaTimepointShort> timepoints = new LinkedList<>();
				daxploreFile.getMetaQuestionManager().create(rmq.column, shorttext, fulltext, scale, calc, timepoints);
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
				
				daxploreFile.getRawMeta().importSPSS(importSPSSFile);
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
	void importL10n(Reader reader, L10nFormat format, Locale locale) throws IOException, DaxploreException { //TODO don't write directly to database, talk to managers
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
						tr = daxploreFile.getTextReferenceManager().get(row[0]);
						tr.put(row[1], locale);
					} else {
						throw new DaxploreException("Invalid csv row:" + MyTools.join(row, ", "));
					}
				}
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
	void exportL10n(Writer writer, L10nFormat format, Locale locale) throws IOException, DaxploreException {
		switch(format) {
		case PROPERTIES:
			Properties properties = new SortedProperties();
			
			TextTree allTexts = daxploreFile.getTextReferenceManager().getAll();
			
			for(TextReference tr: allTexts.iterable()) {
				if(tr.has(locale)) {
					properties.setProperty(tr.getRef(), tr.get(locale));
				} else {
					properties.setProperty(tr.getRef(), "");
				}
			}
			
			properties.store(writer, null); //Comment can be null Some documentation comment placed on the first row of the file

			break;
		case CSV:
			try(CSVWriter csvWriter = new CSVWriter(writer)) {
				allTexts = daxploreFile.getTextReferenceManager().getAll();
				for(TextReference tr: allTexts.iterable()) {
					if(tr.has(locale)) {
						csvWriter.writeNext(new String[]{tr.getRef(), tr.get(locale)});
					} else {
						csvWriter.writeNext(new String[]{tr.getRef(), ""});
					}
				}
			}
			break;
		default:
			throw new AssertionError("Unsupported format: " + format);	
		}
	}
}
