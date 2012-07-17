package gui;

import java.io.File;

import org.opendatafoundation.data.spss.SPSSFile;
import daxplorelib.DaxploreFile;

/**
 * Main file handler for GUI. Contains instances of daxplore file and SPSS file.
 * @author jorgenrosen
 *
 */
public class GUIFile {
	private DaxploreFile daxploreFile;
	private File spssFile;

	public GUIFile() {
	}

	public DaxploreFile getDaxploreFile() {
		return daxploreFile;
	}

	public void setDaxploreFile(DaxploreFile daxploreFile) {
		this.daxploreFile = daxploreFile;
	}

	public File getSpssFile() {
		return spssFile;
	}

	public void setSpssFile(File spssFile) {
		this.spssFile = spssFile;
	}
}