package cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.List;
import java.util.Set;

import org.opendatafoundation.data.FileFormatInfo;
import org.opendatafoundation.data.FileFormatInfo.ASCIIFormat;
import org.opendatafoundation.data.FileFormatInfo.Compatibility;
import org.opendatafoundation.data.spss.SPSSFile;
import org.opendatafoundation.data.spss.SPSSFileException;

import tools.SPSSTools;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.FileConverter;

import daxplorelib.DaxploreException;
import daxplorelib.DaxploreFile;

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
		
		public void run(File projectFile) {
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
					Set<String> stringset = SPSSTools.getNonAsciiStrings(importFile);
					//CharsetDecoder decoder = charset.newDecoder();
					for(String s: stringset) {
						System.out.println(s);
						//CharBuffer r = decoder.decode(ByteBuffer.wrap(s.getBytes()));
						//System.out.println(r.toString());
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Failure");
				}
				
			} else {
			try {
					DaxploreFile daxplorefile = new DaxploreFile(projectFile, false);
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
					Exception e2 = e.getOriginalException();
					if(e2 != null) {
						e2.printStackTrace();
					}
					e.printStackTrace();
					return;
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
	
	@Parameters
	public class ImportMetaTextsCommand {
		
		@Parameter(description = "file to import", arity = 1, converter = FileConverter.class, required = true)
		public List<File> files;
		
		public void run(File projectFile) {
			
		}
	}
}


