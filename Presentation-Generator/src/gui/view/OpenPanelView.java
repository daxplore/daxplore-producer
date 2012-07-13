package gui.view;

import gui.GUIMain;
import gui.openpanel.CreateDaxploreFile;
import gui.openpanel.ImportSPSSFile;
import gui.openpanel.OpenDaxploreFile;
import gui.openpanel.OpenSPSSFile;

import java.awt.FlowLayout;
import java.text.SimpleDateFormat;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;

public class OpenPanelView extends JPanel {
	
	private JTextField fileNameField = new JTextField();
	private JTextField importDateField = new JTextField();;
	private JTextField creationDateField = new JTextField();
	private JTextField lastImportFileNameField = new JTextField();
	private JTextPane spssFileInfoText = new JTextPane();
	private GUIMain guiMain;

	public OpenPanelView(GUIMain guiMain) {
		// text fields in open panel.
		this.guiMain = guiMain;
		
		fileNameField.setEditable(false);
		fileNameField.setBounds(166, 75, 240, 27);
		fileNameField.setColumns(10);

		importDateField.setEditable(false);
		importDateField.setBounds(166, 203, 240, 27);
		importDateField.setColumns(10);

		creationDateField.setEditable(false);
		creationDateField.setBounds(166, 108, 240, 27);
		creationDateField.setColumns(10);

		lastImportFileNameField.setEditable(false);
		lastImportFileNameField.setBounds(166, 168, 240, 27);
		lastImportFileNameField.setColumns(10);
		
		JPanel metaDataPanel = new JPanel();
		metaDataPanel.setBorder(new TitledBorder(null, "Daxplore file information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		JPanel importSPSSPanel = new JPanel();
		importSPSSPanel.setBorder(new TitledBorder(null, "Import SPSS File", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		GroupLayout gl_openPanel = new GroupLayout(this);
		
		gl_openPanel.setHorizontalGroup(
			gl_openPanel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_openPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_openPanel.createParallelGroup(Alignment.TRAILING)
						.addComponent(importSPSSPanel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 784, Short.MAX_VALUE)
						.addComponent(metaDataPanel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 784, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_openPanel.setVerticalGroup(
			gl_openPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_openPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(metaDataPanel, GroupLayout.PREFERRED_SIZE, 379, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(importSPSSPanel, GroupLayout.PREFERRED_SIZE, 343, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(25, Short.MAX_VALUE))
		);
		
		JButton openSPSSFileButton = new JButton("Open SPSS file...");
		openSPSSFileButton.setBounds(20, 50, 153, 27);
		openSPSSFileButton.addActionListener(new OpenSPSSFile(guiMain, this, openSPSSFileButton));
		
		JScrollPane importTableScrollPane = new JScrollPane();
		importTableScrollPane.setBounds(20, 89, 763, 217);
		
		// progress bar for import spss panel goes here.
		JProgressBar importSpssFileProgressBar = new JProgressBar();
		importSpssFileProgressBar.setBounds(600, 307, 183, 19);
		importSPSSPanel.add(importSpssFileProgressBar);
		
		JButton importSpssFileButton = new JButton("");
		importSpssFileButton.addActionListener(new ImportSPSSFile(guiMain, this, importSpssFileButton, importSpssFileProgressBar));
		importSpssFileButton.setToolTipText("Import SPSS file");
		importSpssFileButton.setIcon(new ImageIcon(GUIMain.class.getResource("/gui/resources/Arrow-Up-48.png")));
		importSpssFileButton.setBounds(332, 19, 90, 58);
		
		importTableScrollPane.setViewportView(spssFileInfoText);
		importSPSSPanel.setLayout(null);
		importSPSSPanel.add(importSpssFileButton);
		importSPSSPanel.add(openSPSSFileButton);
		importSPSSPanel.add(importTableScrollPane);
		
		JLabel fileNameLabel = new JLabel("Filename:");
		fileNameLabel.setBounds(19, 81, 115, 15);
		
		JLabel importDateLabel = new JLabel("Import date:");
		importDateLabel.setBounds(19, 209, 115, 15);
		
		JLabel creationDateLabel = new JLabel("Creation date:");
		creationDateLabel.setBounds(19, 114, 115, 15);
		
		JLabel lastImportedFileLabel = new JLabel("Last import filename:");
		lastImportedFileLabel.setBounds(19, 174, 135, 15);
		
		metaDataPanel.setLayout(null);
		metaDataPanel.add(fileNameLabel);
		metaDataPanel.add(getFileNameField());
		metaDataPanel.add(importDateLabel);
		metaDataPanel.add(getImportDateField());
		metaDataPanel.add(creationDateLabel);
		metaDataPanel.add(getCreationDateField());
		metaDataPanel.add(lastImportedFileLabel);
		metaDataPanel.add(getLastImportFileNameField());
		
		JButton openFileButton = new JButton("Open file...");
		openFileButton.setBounds(19, 35, 135, 27);
		metaDataPanel.add(openFileButton);
		openFileButton.setToolTipText("Opens a daxplore file");
		openFileButton.addActionListener(new OpenDaxploreFile(guiMain, this, openFileButton));
		
		JButton createNewFileButton = new JButton("Create new file...");
		createNewFileButton.setBounds(168, 35, 135, 27);
		metaDataPanel.add(createNewFileButton);
		createNewFileButton.addActionListener(new CreateDaxploreFile(guiMain, this, createNewFileButton));
		createNewFileButton.setToolTipText("Creates a new daxplore project file");
		
		setLayout(gl_openPanel);
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

	public String getSpssFileInfoText() {
		return spssFileInfoText.getText();
	}

	public void setSpssFileInfoText(String spssFileInfoText) {
		this.spssFileInfoText.setText(spssFileInfoText);
	}
	
	/**
	 * Updates text field for the SPSS file information in the open panel dialog.
	 * @param daxploreGUI TODO
	 */
	public void updateSpssFileInfoText(GUIMain daxploreGUI) {
		if (daxploreGUI.daxploreDataModel.getSpssFile() != null) {
			spssFileInfoText.setText(
					"SPSS file ready for import: " +
					daxploreGUI.daxploreDataModel.getSpssFile().file.getName() + "\n" +
					daxploreGUI.daxploreDataModel.getSpssFile().file.getAbsolutePath());
		}
	}

	/**
	 * Updates text fields in the open panel dialog to display daxplore file information.
	 * @param daxploreGUI TODO
	 */
	public void updateTextFields(GUIMain daxploreGUI) {
		
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


}