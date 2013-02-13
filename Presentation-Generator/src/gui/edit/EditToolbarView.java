package gui.edit;

import gui.edit.EditTextView.LocaleItem;

import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.io.File;
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
	
	public File showImportDialog() {
		ImportFileChooser ifc = new ImportFileChooser();
		int returnVal = ifc.showOpenDialog(this);
		switch(returnVal) {
		case JFileChooser.APPROVE_OPTION:
			return ifc.getSelectedFile();
		default:
			return null;
		}
	}
	
	public Locale getSelectedLocale() {
		return selectedLocale;
	}
	
	private class ImportFileChooser extends JFileChooser {
		
		private JComboBox<LocaleItem> localeBox;

		public ImportFileChooser() {
			localeBox = new JComboBox<LocaleItem>();
			localeBox.addItem(null);
			localeBox.addItem(new LocaleItem(new Locale("sv")));
			localeBox.addItem(new LocaleItem(new Locale("en"))); //TODO
			//localeBox.addActionListener(this);
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
