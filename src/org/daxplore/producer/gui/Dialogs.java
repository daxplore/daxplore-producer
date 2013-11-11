package org.daxplore.producer.gui;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Dialogs {
	
	public static File showExportDialog(Component parent) {
		JFileChooser fc = new JFileChooser(Settings.getWorkingDirectory());
		
		FileFilter filter = new FileNameExtensionFilter("Zip files", "zip");
		fc.addChoosableFileFilter(filter);
		fc.setFileFilter(filter);
		
		int returnVal = fc.showSaveDialog(parent);
		switch(returnVal) {
		case JFileChooser.APPROVE_OPTION:
			Settings.setWorkingDirectory(fc.getCurrentDirectory());
			return fc.getSelectedFile();
		default:
			return null;
		}
	}
	
}
