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

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.DaxploreFile;

import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "Create new project")
public class CreateCommand {
	public void run(File file) {
		if(file.exists()) {
			System.out.println("File already exists");
			return;
		}
		try (DaxploreFile created = DaxploreFile.createWithNewFile(file)) {
			
		} catch (DaxploreException | IOException e) {
			System.out.println("Couldn't create file");
			Throwable e2 = e.getCause();
			if(e2 != null) {
				e2.printStackTrace();
			}
			e.printStackTrace();
			return;
		}
	}
}
