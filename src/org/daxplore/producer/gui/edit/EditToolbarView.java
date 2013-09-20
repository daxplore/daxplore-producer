package org.daxplore.producer.gui.edit;

import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.daxplore.producer.gui.Settings;
import org.daxplore.producer.gui.edit.EditTextView.LocaleItem;

@SuppressWarnings("serial")
public class EditToolbarView extends JPanel {

	private JButton importButton, exportButton;
	private Locale selectedLocale;
	
	
	public EditToolbarView(ActionListener listner) {
		setLayout(new GridLayout(1, 2));
		importButton = new JButton("Import texts...");
		importButton.setActionCommand("import");
		importButton.addActionListener(listner);
		exportButton = new JButton("Export texts...");
		exportButton.setActionCommand("export");
		exportButton.addActionListener(listner);
		add(importButton);
		add(exportButton);
	}
	
	public File showImportDialog(List<Locale> localeList) {
		LocalizationFileChooser ifc = new LocalizationFileChooser(localeList);
		
		FileFilter filter = new FileNameExtensionFilter("language files", "csv", "properties");
		ifc.addChoosableFileFilter(filter);
		ifc.setFileFilter(filter);
		
		int returnVal = ifc.showOpenDialog(this);
		switch(returnVal) {
		case JFileChooser.APPROVE_OPTION:
			return ifc.getSelectedFile();
		default:
			return null;
		}
	}
	
	public File showExportDialog(List<Locale> localeList) {
		LocalizationFileChooser efc = new LocalizationFileChooser(localeList);
		int returnVal = efc.showSaveDialog(this);
		switch(returnVal) {
		case JFileChooser.APPROVE_OPTION:
			Settings.setWorkingDirectory(efc.getCurrentDirectory());
			return efc.getSelectedFile();
		default:
			return null;
		}
	}
	
	public Locale getSelectedLocale() {
		return selectedLocale;
	}
	
	private class LocalizationFileChooser extends JFileChooser {
		
		private JComboBox<LocaleItem> localeBox;
		
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
				selectedLocale = ((LocaleItem)localeBox.getSelectedItem()).loc;
				super.approveSelection();
			} else {
				System.out.println("No locale selected during import");
			}
		}
	}
}
