package commandclient;

import java.io.File;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.FileConverter;

import daxplorelib.About;
import daxplorelib.DaxploreException;
import daxplorelib.DaxploreFile;
import daxplorelib.fileformat.ImportedData;

@Parameters(commandDescription = "Detials about the project")
public class InfoCommand {

	@Parameter(description = "daxplore project file", converter = FileConverter.class, required = true)
	public List<File> projectfiles;
	
	@Parameter(description = "info about version")
	public Integer version;
	
	public void run(){
		try {
			File projectfile = projectfiles.get(0);
			DaxploreFile daxplore = new DaxploreFile(projectfile, false);
			About about = daxplore.getAbout();
			if(version != null){
				System.out.println("File: " + projectfile.getName());
				System.out.println("Creation date: " + about.getCreationDate().toString());
				System.out.println("Last updated: " + about.getLastUpdate().toString());
				List<ImportedData> importlist = about.getImportedDataVersions();
				int activeVersion = about.getActiveRawData();
				System.out.println("Imported data versions");
				for(ImportedData data : importlist){
					if(data.getVersion() == activeVersion){
						System.out.print("* ");
					} else {
						System.out.print("  ");
					}
					System.out.println(data.getVersion() + ": " + data.getFilename() + " on " + data.getImportDate().toString());
				}
			} else {
				
			}
		} catch (DaxploreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
