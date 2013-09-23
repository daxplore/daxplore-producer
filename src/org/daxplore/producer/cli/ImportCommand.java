package org.daxplore.producer.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.DaxploreFile;
import org.daxplore.producer.daxplorelib.metadata.MetaData;
import org.daxplore.producer.daxplorelib.metadata.MetaData.L10nFormat;
import org.daxplore.producer.tools.SPSSTools;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.FileConverter;

@Parameters(commandDescription = "Import data into project")
public class ImportCommand {

	private ImportSpssCommand spssCommand;
	private ImportMetaStructureCommand metaStructureCommand;
	private ImportMetaTextsCommand metaTextsCommand;
	
	public void run(File projectFile) {
		
	}
	
	public ImportSpssCommand getSpssCommand() {
		if(spssCommand == null) {
			spssCommand = new ImportSpssCommand();
		}
		return spssCommand;
	}
	
	public ImportMetaStructureCommand getStructureCommand() {
		if(metaStructureCommand == null) {
			metaStructureCommand = new ImportMetaStructureCommand();
		}
		return metaStructureCommand;
	}
	
	public ImportMetaTextsCommand getTextsCommand() {
		if(metaTextsCommand == null) {
			metaTextsCommand = new ImportMetaTextsCommand();
		}
		return metaTextsCommand;
	}
	
	@Parameters
	public class ImportSpssCommand {
		@Parameter(names = "--charset", description = "Import with charachterset")
		public String charsetName = "ISO-8859-1";
		
		@Parameter(names = "--test", description = "Checks for Non-ASCII strings, import will not be performed")
		boolean test = false;
		
		@Parameter(description = "file to import", arity = 1, converter = FileConverter.class, required = true)
		public List<File> files;
		
		public void run(File file) {
			File importFile = files.get(0);
			if(!importFile.exists()){
				System.out.println("SPSS file " + importFile.getName() + " does not exist");
				return;
			}
			if(importFile.isDirectory()){
				System.out.println("SPSS file is a directory, I can't import a directory");
				return;
			}
			Charset charset;
			try{
				charset = Charset.forName(charsetName);
			} catch (Exception e){
				System.out.println("Charset error");
				return;
			}
			if(test) {
				System.out.println("Non-ASCII strings:");
				try {
					Set<String> stringset = SPSSTools.getNonAsciiStrings(importFile, charset);
					//CharsetDecoder decoder = charset.newDecoder();
					for(String s: stringset) {
						System.out.println(s);
						//CharBuffer r = decoder.decode(ByteBuffer.wrap(s));
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Failure");
				}
				
			} else {
				try (DaxploreFile daxplorefile = DaxploreFile.createFromExistingFile(file)) {
					System.out.println("Importing...");
					daxplorefile.importSPSS(importFile, charset);
					System.out.println("Data imported");
					daxplorefile.importSPSS(importFile, charset);
					System.out.println("Data imported 2nd time");
				} catch (FileNotFoundException e) {
					System.out.println("Havn't we tested this already");
					e.printStackTrace();
					return;
				} catch (IOException e) {
					e.printStackTrace();
					return;
				} catch (DaxploreException e) {
					// TODO Auto-generated catch block
					System.out.println(e.getMessage());
					Throwable e2 = e.getCause();
					if(e2 != null) {
						e2.printStackTrace();
					}
				}
			}
		}
	}
	
	@Parameters
	public class ImportMetaStructureCommand {
		
		@Parameter(description = "file to import", arity = 1, converter = FileConverter.class, required = true)
		public List<File> files;
		
		public void run(File projectFile) {
			
		}
		
	}
	
	@Parameters(separators = "=")
	public class ImportMetaTextsCommand {
		
		@Parameter(names = "--locale", description = "Locale to export", converter = LocaleConverter.class)
		public Locale locale = new Locale("sv");
		
		@Parameter(description = "file to import", arity = 1, converter = FileConverter.class, required = true)
		public List<File> files;
		
		public void run(File file) {
			try (DaxploreFile dax = DaxploreFile.createFromExistingFile(file)) {
				System.out.println("Importing texts for locale: " + locale.getLanguage());
				
				File infile = files.get(0);
				if(!infile.exists()) {
					System.out.println("File " + infile.getName() + " doesn't exist");
					return;
				}
				if(!infile.canRead()) {
					System.out.println("Can't read from file: " + infile.getName());
					return;
				} 
				try (FileInputStream fis = new FileInputStream(infile);
						InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
						Reader r = new BufferedReader(isr)) {
 					MetaData metadata = dax.getMetaData();
					metadata.importL10n(r, L10nFormat.PROPERTIES, locale);
				} catch (DaxploreException | IOException e) {
					//TODO handle exception
					System.out.println("Error exporting texts");
					e.printStackTrace();
				}

			} catch (DaxploreException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
				return;
			}
		}
	}
}


