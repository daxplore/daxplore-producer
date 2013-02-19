package gui.edit;

import gui.edit.EditTextView.LocaleItem;

import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

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
			localeBox = new JComboBox<LocaleItem>();
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
