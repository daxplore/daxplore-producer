package org.daxplore.producer.gui;

import java.io.File;
import java.util.prefs.Preferences;

public class DaxplorePreferences {
	private Preferences preferences;
	
	private String workingDirectoryKey = "working_directory";

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
