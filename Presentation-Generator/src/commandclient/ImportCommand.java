package commandclient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.FileConverter;

import daxplorelib.DaxploreException;
import daxplorelib.DaxploreFile;

@Parameters(commandDescription = "Import data into project")
public class ImportCommand {

	@Parameter(names = "--spss", description = "Import spss file")
	public boolean spssImport = true;
	
	@Parameter(names = "--create", description = "Create a new project")
	public boolean create = false;
	
	@Parameter(names = "--charset", description = "Import with charachterset")
	public String charsetName = "ISO-8859-1";
	
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
		try {
			DaxploreFile daxplorefile = new DaxploreFile(projectFile, create);
			daxplorefile.importSPSS(importFile, charset);
			System.out.println("Data imported");
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
			e.printStackTrace();
			return;
		}
		
	}
}
