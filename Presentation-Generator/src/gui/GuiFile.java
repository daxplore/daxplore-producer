package gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.opendatafoundation.data.spss.SPSSFile;
import org.opendatafoundation.data.spss.SPSSFileException;

import daxplorelib.DaxploreFile;

/**
 * Main file handler for GUI. Contains instances of DaxploreFile and File.
 * @author jorgenrosen
 *
 */
public class GuiFile {
	private DaxploreFile daxploreFile = null;
	private File spssFile = null;

	public GuiFile() {
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
	
	public void resetDaxploreFile() {
		daxploreFile = null;
	}
	
	public void resetSpssFile() {
		spssFile = null;
	}
	
	/**
	 * Returns true if a daxplore file is loaded into the system.
	 */
	public boolean isSet() {
		if (daxploreFile == null)
			return true;
		else
			return false;
	}
}