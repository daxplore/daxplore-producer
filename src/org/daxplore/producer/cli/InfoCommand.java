package org.daxplore.producer.cli;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.daxplore.producer.daxplorelib.About;
import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.DaxploreFile;
import org.daxplore.producer.daxplorelib.raw.RawImport;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "Detials about the project")
public class InfoCommand {
	
	@Parameter(names={"-v","--version"}, description = "info about version")
	public Integer version = null;
	
	public void run(File file){
		if(!file.exists()) {
			System.out.println("File does not exist");
			return;
		}
		try (DaxploreFile daxplore = DaxploreFile.createFromExistingFile(file)) {
			About about = daxplore.getAbout();
			System.out.println("File: " + file.getName());
			System.out.println("Creation date: " + about.getCreationDate().toString());
			System.out.println("Last updated: " + about.getLastUpdate().toString());
			RawImport imported = daxplore.getImportedData();
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
		} catch (DaxploreException | IOException e) {
			// TODO Auto-generated catch block
			e.getCause().printStackTrace();
		}
	}
}
