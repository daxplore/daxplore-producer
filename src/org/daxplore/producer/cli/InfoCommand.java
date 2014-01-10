/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.cli;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.daxplore.producer.daxplorelib.About;
import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.DaxploreFile;
import org.daxplore.producer.daxplorelib.raw.RawData;
import org.daxplore.producer.daxplorelib.raw.RawMeta;

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
			RawMeta rawMeta = daxplore.getRawMeta();
			RawData rawData = daxplore.getRawData();
			if(rawMeta.hasData() && rawData.hasData()) {
				System.out.print("Imported from: " + about.getImportFilename());
				System.out.println(" on " + about.getImportDate().toString());
				List<String> columns = rawMeta.getColumns();
				System.out.println("Has " + columns.size() + " columns and " + rawData.getNumberOfRows() + " rows");
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
