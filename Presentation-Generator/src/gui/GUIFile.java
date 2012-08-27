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
public class GUIFile {
	private DaxploreFile daxploreFile = null;
	private File spssFile = null;
	private SPSSFile spssFileRaw = null;

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
		
		try {
			this.spssFileRaw = new SPSSFile(spssFile, "r");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.spssFileRaw.logFlag = false;
		try {
			this.spssFileRaw.loadMetadata();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SPSSFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			this.spssFileRaw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public SPSSFile getSpssFileRaw() {
		return spssFileRaw;
	}
	
	public void resetSpssFile() {
		spssFile = null;
		spssFileRaw = null;
	}
}