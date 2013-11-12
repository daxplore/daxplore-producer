package org.daxplore.producer.gui;

import java.awt.Component;
import java.io.File;
import java.util.List;
import java.util.Locale;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.daxplore.producer.gui.edit.EditTextView.LocaleItem;

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
	
	public static FileLocalePair showImportDialog(Component parent, List<Locale> localeList) {
		LocalizationFileChooser ifc = new LocalizationFileChooser(localeList);
		
		FileFilter filter = new FileNameExtensionFilter("language files", "csv", "properties");
		ifc.addChoosableFileFilter(filter);
		ifc.setFileFilter(filter);
		
		int returnVal = ifc.showOpenDialog(parent);
		switch(returnVal) {
		case JFileChooser.APPROVE_OPTION:
			return new FileLocalePair(ifc.getSelectedFile(), ifc.getSelectedLocale());
		default:
			return null;
		}
	}
	
	public static FileLocalePair showExportDialog(Component parent, List<Locale> localeList) {
		LocalizationFileChooser efc = new LocalizationFileChooser(localeList);
		int returnVal = efc.showSaveDialog(parent);
		switch(returnVal) {
		case JFileChooser.APPROVE_OPTION:
			Settings.setWorkingDirectory(efc.getCurrentDirectory());
			return new FileLocalePair(efc.getSelectedFile(), efc.getSelectedLocale());
		default:
			return null;
		}
	}
	
	public static class FileLocalePair {
		public final File file;
		public final Locale locale;
		public FileLocalePair(File file, Locale locale) {
			this.file = file;
			this.locale = locale;
		}
	}
	
	@SuppressWarnings("serial")
	private static class LocalizationFileChooser extends JFileChooser {
		private JComboBox<LocaleItem> localeBox;
		private Locale selectedLocale;
		
		public LocalizationFileChooser(List<Locale> localeList) {
			super(Settings.getWorkingDirectory());
			localeBox = new JComboBox<>();
			localeBox.addItem(null);
			for(Locale loc: localeList) {
				localeBox.addItem(new LocaleItem(loc));
			}
			setAccessory(localeBox);
		}
		
		@Override
		public void approveSelection() {
			if(localeBox.getSelectedItem() != null) {
				selectedLocale = ((LocaleItem)localeBox.getSelectedItem()).getLocale();
				super.approveSelection();
			} else {
				System.out.println("No locale selected during import");
			}
		}
		
		public Locale getSelectedLocale() {
			return selectedLocale;
		}
	}
	
}
