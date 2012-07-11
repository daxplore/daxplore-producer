package cli;

import java.io.File;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import daxplorelib.About;
import daxplorelib.DaxploreException;
import daxplorelib.DaxploreFile;
import daxplorelib.fileformat.ImportedData;

@Parameters(commandDescription = "Detials about the project")
public class InfoCommand {
	
	@Parameter(names={"-v","--version"}, description = "info about version")
	public Integer version = null;
	
	public void run(File projectfile){
		if(!projectfile.exists()) {
			System.out.println("File does not exist");
			return;
		}
		try {
			DaxploreFile daxplore = new DaxploreFile(projectfile, false);
			About about = daxplore.getAbout();
			System.out.println("File: " + projectfile.getName());
			System.out.println("Creation date: " + about.getCreationDate().toString());
			System.out.println("Last updated: " + about.getLastUpdate().toString());
			ImportedData imported = daxplore.getImportedData();
			if(imported != null && imported.hasData()) {
				System.out.print("Imported from: " + about.getImportFilename());
				System.out.println(" on " + about.getImportDate().toString());
				List<String> columns = imported.getColumnList();
				System.out.println("Has " + columns.size() + " columns and " + imported.getNumberOfRows() + " rows");
				System.out.print("Columns: ");
				for(String s: columns){
					System.out.print(s + ", ");
				}
				System.out.print("\n");
			} else {
				System.out.println("Has never imported data");
			}
		} catch (DaxploreException e) {
			// TODO Auto-generated catch block
			e.getOriginalException().printStackTrace();
		}
	}
}
