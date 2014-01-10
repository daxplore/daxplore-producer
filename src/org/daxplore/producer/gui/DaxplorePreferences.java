/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui;

import java.io.File;
import java.util.prefs.Preferences;

public class DaxplorePreferences {
	private Preferences preferences;
	
	private static final String workingDirectoryKey = "working_directory";

	public DaxplorePreferences() {
		 preferences = Preferences.userRoot().node("org/daxplore/producer");
	}
	
	public void setWorkingDirectory(File directory) {
		preferences.put(workingDirectoryKey, directory.getPath());
	}
	
	public File getWorkingDirectory() {
		String pathname = preferences.get(workingDirectoryKey, null);
		if(pathname == null) {
			return null;
		}
		return new File(pathname);
	}
}
