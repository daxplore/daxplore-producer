package org.daxplore.producer.daxplorelib;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Locale;
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
import org.daxplore.producer.daxplorelib.metadata.MetaData;
import org.daxplore.producer.daxplorelib.metadata.MetaGroup;
import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;
import org.daxplore.producer.daxplorelib.metadata.MetaTimepointShort;
import org.daxplore.producer.daxplorelib.raw.RawImport;
import org.daxplore.producer.daxplorelib.raw.RawMeta;
import org.daxplore.producer.tools.MyTools;
import org.opendatafoundation.data.FileFormatInfo;
import org.opendatafoundation.data.FileFormatInfo.ASCIIFormat;
import org.opendatafoundation.data.FileFormatInfo.Compatibility;
import org.opendatafoundation.data.spss.SPSSFile;
import org.opendatafoundation.data.spss.SPSSFileException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class DaxploreFile implements Closeable {
	private Connection connection;
	private About about;
	private File file = null;
	private MetaData metadata;
	
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
		} catch (SQLException e) {
			throw new DaxploreException("Error creating about", e);
		}
	}
	
	public void importSPSS(File spssFile, Charset charset) throws FileNotFoundException, IOException, DaxploreException{
		FileFormatInfo ffi = new FileFormatInfo();
		ffi.namesOnFirstLine = false;
		ffi.asciiFormat = ASCIIFormat.CSV;
		ffi.compatibility = Compatibility.GENERIC;
		
		try (SPSSFile importSPSSFile = new SPSSFile(spssFile,charset)) {
			importSPSSFile.logFlag = false;
			importSPSSFile.loadMetadata();
		
			boolean autocommit = false;
			try {
				autocommit = connection.getAutoCommit();
				connection.setAutoCommit(false);
				int isolation = connection.getTransactionIsolation();
				connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
				
				RawImport rawImport = new RawImport(connection);
				rawImport.importSPSS(importSPSSFile);
					
				about.setImport(importSPSSFile.file.getName());
				
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
	
	public About getAbout(){
		return about;
	}
	
	public RawImport getImportedData(){
		try {
			RawImport id = new RawImport(connection);
			return id;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public MetaData getMetaData() throws DaxploreException {
		if(metadata != null) {
			return metadata;
		}
		try {
			metadata = new MetaData(connection, getAbout(), getImportedData().getRawData());
			return metadata;
		} catch (SQLException e) {
			throw new DaxploreException("Couldn't get metadata", e);
		}
	}
	
	public RawMeta getRawMeta() throws DaxploreException {
		try {
			return new RawMeta(connection);
		} catch (SQLException e) {
			throw new DaxploreException("Could't get RawMeta", e);
		}
	}
	
	public Crosstabs getCrosstabs() {
		return new Crosstabs(connection, about);
	}
	
	public void saveAll() throws DaxploreException { //TODO: return boolean instead of throwing exception?
		try {
			boolean autocommit = connection.getAutoCommit();
			connection.setAutoCommit(false);
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			
			Logger.getGlobal().log(Level.INFO, "Save initiated");
			about.save();
			metadata.saveAll();
			
			connection.commit();
			connection.setAutoCommit(autocommit);
		} catch (SQLException e) {
			Logger.getGlobal().log(Level.SEVERE, "Error while saving data", e);
			throw new DaxploreException("Error while saving data", e);
		}
		Logger.getGlobal().log(Level.INFO, "Save successful");
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
	
	public void writeUploadFile(File outputFile) throws TransformerFactoryConfigurationError, TransformerException, SQLException, DaxploreException, SAXException, IOException, ParserConfigurationException {
		long time = System.nanoTime();
		Logger.getGlobal().log(Level.INFO, "Starting to generate json data");
		
		Crosstabs crosstabs = getCrosstabs();
		crosstabs.loadRawToMem();
		

		MetaGroup perspectives = getMetaData().getMetaGroupManager().getPerspectiveGroup();
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
		for(MetaGroup group : getMetaData().getMetaGroupManager().getQuestionGroups()) {
			for(MetaQuestion question : group.getQuestions()) {
				selectedQuestions.add(question);
				for(MetaQuestion perspective : perspectives.getQuestions()) {
					dataJSON.add(crosstabs.crosstabs2(question, perspective, 10).toJSONObject());
				}
			}
		}
		
		Logger.getGlobal().log(Level.INFO, "Generated data in " + ((System.nanoTime() -time)/Math.pow(10,9)) + "s");
		time = System.nanoTime();
		
		try (ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(outputFile))) {
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
			
			for(Locale locale : getAbout().getLocales()) {
				JsonArray questionJSON = new JsonArray();
				for(MetaQuestion q : selectedQuestions) {
					questionJSON.add(q.toJSONObject(locale));
				}
				
				String propertiesJSONString = prettyGson.toJson(getPropertiesJson(locale));
				writeZipString(zout, "properties/usertexts_"+locale.toLanguageTag()+".json", propertiesJSONString);
				
				writeZipString(zout, "meta/questions_"+locale.toLanguageTag()+".json", prettyGson.toJson(questionJSON));
			    
			    String groupJSONString = prettyGson.toJson(getMetaData().getMetaGroupManager().getQuestionGroupsJSON(locale));
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
	
	public Document getUploadManifest() throws SAXException, IOException, ParserConfigurationException {
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
		for(Locale locale : about.getLocales()) {
			Element localeElement = doc.createElement("language-BCP47");
			supportedLocales.appendChild(localeElement);
			localeElement.appendChild(doc.createTextNode(locale.toLanguageTag()));
		}
		
		Element defaultLocale = doc.createElement("defaultLocale");
		root.appendChild(defaultLocale);
		Element localeElement = doc.createElement("language-BCP47");
		defaultLocale.appendChild(localeElement);
		localeElement.appendChild(doc.createTextNode(about.getLocales().get(0).toLanguageTag())); // TODO pick default locale properly
		
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
			json.addProperty(property, metadata.getTextsManager().get(property).get(locale));
		}
		for(MetaTimepointShort mtp: metadata.getMetaTimepointManager().getAll()) {
			json.addProperty("timepoint_" + mtp.getTimeindex(), mtp.getTextRef().get(locale));
		}
		return json;
	}

}
