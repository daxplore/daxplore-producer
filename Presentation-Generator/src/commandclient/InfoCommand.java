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
	
	@Parameter(names={"-v","--version"}, description = "info about version")
	public Integer version = null;
	
	public void run(File projectfile){
		try {
			DaxploreFile daxplore = new DaxploreFile(projectfile, false);
			About about = daxplore.getAbout();
			if(version == null){
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
				System.out.println("File: " + projectfile.getName());
				System.out.println("Version: " + version);
				ImportedData imported = about.getImportedData(version);
				System.out.println("Imported from: " + imported.getFilename() + " on " + imported.getImportDate().toString());
				List<String> columns = imported.getColumnList();
				System.out.println("Has " + columns.size() + " columns and " + imported.getNumberOfRows() + " rows");
				System.out.print("Columns: ");
				for(String s: columns){
					System.out.print(s + ", ");
				}
				System.out.print("\n");
			}
		} catch (DaxploreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}