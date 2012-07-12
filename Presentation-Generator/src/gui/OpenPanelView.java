package gui;

import java.text.SimpleDateFormat;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;

public class OpenPanelView extends JPanel {
	
	private JTextField fileNameField;
	private JTextField importDateField;
	private JTextField creationDateField;
	private JTextField lastImportFileNameField;
	private JTextPane spssFileInfoText;

	public OpenPanelView() {
		// text fields in open panel.
		setFileNameField(new JTextField());
		getFileNameField().setEditable(false);
		getFileNameField().setBounds(166, 75, 240, 27);
		getFileNameField().setColumns(10);

		setImportDateField(new JTextField());
		getImportDateField().setEditable(false);
		getImportDateField().setBounds(166, 203, 240, 27);
		getImportDateField().setColumns(10);

		setCreationDateField(new JTextField());
		getCreationDateField().setEditable(false);
		getCreationDateField().setBounds(166, 108, 240, 27);
		getCreationDateField().setColumns(10);

		setLastImportFileNameField(new JTextField());
		getLastImportFileNameField().setEditable(false);
		getLastImportFileNameField().setBounds(166, 168, 240, 27);
		getLastImportFileNameField().setColumns(10);
		
		// updateTextFields(this); fix!
	}

	public JTextField getFileNameField() {
		return fileNameField;
	}

	public void setFileNameField(JTextField fileNameField) {
		this.fileNameField = fileNameField;
	}

	public JTextField getImportDateField() {
		return importDateField;
	}

	public void setImportDateField(JTextField importDateField) {
		this.importDateField = importDateField;
	}

	public JTextField getCreationDateField() {
		return creationDateField;
	}

	public void setCreationDateField(JTextField creationDateField) {
		this.creationDateField = creationDateField;
	}

	public JTextField getLastImportFileNameField() {
		return lastImportFileNameField;
	}

	public void setLastImportFileNameField(JTextField lastImportFileNameField) {
		this.lastImportFileNameField = lastImportFileNameField;
	}

	public JTextPane getSpssFileInfoText() {
		return spssFileInfoText;
	}

	public void setSpssFileInfoText(JTextPane spssFileInfoText) {
		this.spssFileInfoText = spssFileInfoText;
	}

	/**
	 * Updates text fields in the open panel dialog to display daxplore file information.
	 * @param daxploreGUI TODO
	 */
	public void updateTextFields(DaxploreGUI daxploreGUI) {
		
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		
		// set the text fields if we have a daxplore file loaded.
		if (daxploreGUI.daxploreDataModel.getDaxploreFile() != null) {
			// update text fields with appropriate data.
			getFileNameField().setText(daxploreGUI.daxploreDataModel.getDaxploreFile().getFile().getName());
			
			// check if it's a newly created file, if so, it doesn't contain certain fields.
			String importFilename = daxploreGUI.daxploreDataModel.getDaxploreFile().getAbout().getImportFilename();
			if (importFilename != null && !"".equals(importFilename)) {
				getLastImportFileNameField().setText(daxploreGUI.daxploreDataModel.getDaxploreFile().getAbout().getImportFilename());
				// date must first be converted to the appropriate format before returned as string.
				if (daxploreGUI.daxploreDataModel.getDaxploreFile().getAbout().getImportDate() != null) {
				getImportDateField().setText(formatter.format(daxploreGUI.daxploreDataModel.getDaxploreFile().getAbout().getImportDate()));
				} else {
					getImportDateField().setText("");
				}
			} else {
				getLastImportFileNameField().setText("");
				getImportDateField().setText("");
			}
			
			getCreationDateField().setText(
			formatter.format(daxploreGUI.daxploreDataModel.getDaxploreFile().getAbout().getCreationDate()));
		}
	}

	/**
	 * Updates text field for the SPSS file information in the open panel dialog.
	 * @param daxploreGUI TODO
	 */
	public void updateSpssFileInfoText(DaxploreGUI daxploreGUI) {
		if (daxploreGUI.daxploreDataModel.getSpssFile() != null) {
			getSpssFileInfoText().setText(
					"SPSS file ready for import: " +
					daxploreGUI.daxploreDataModel.getSpssFile().file.getName() + "\n" +
					daxploreGUI.daxploreDataModel.getSpssFile().file.getAbsolutePath());
		}
	}
}