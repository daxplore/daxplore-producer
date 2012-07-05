package gui.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import daxplorelib.DaxploreException;
import daxplorelib.DaxploreFile;

public class ImportController {

	// variables used for importing SPSS files.
	public boolean spssImport = true;
	public boolean create = false;
	public String charsetName = "ISO-8859-1";
	public List<File> files;

	/**
	 * Run the import command and read SPSS file. Taken from command client.
	 * @param projectFile
	 */
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
