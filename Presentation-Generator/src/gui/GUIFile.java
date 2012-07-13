package gui;

import org.opendatafoundation.data.spss.SPSSFile;

import daxplorelib.DaxploreFile;

public class GUIFile {
	private DaxploreFile daxploreFile;
	private SPSSFile spssFile;

	public GUIFile() {
	}

	public DaxploreFile getDaxploreFile() {
		return daxploreFile;
	}

	public void setDaxploreFile(DaxploreFile daxploreFile) {
		this.daxploreFile = daxploreFile;
	}

	public SPSSFile getSpssFile() {
		return spssFile;
	}

	public void setSpssFile(SPSSFile spssFile) {
		this.spssFile = spssFile;
	}
}