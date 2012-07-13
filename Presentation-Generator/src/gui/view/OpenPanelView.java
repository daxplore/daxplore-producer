package gui.view;

import gui.GUIFile;
import gui.GUIMain;
import gui.openpanelcontroller.CreateDaxploreFile;
import gui.openpanelcontroller.ImportSPSSFile;
import gui.openpanelcontroller.OpenDaxploreFile;
import gui.openpanelcontroller.OpenSPSSFile;

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
	public JTextPane spssFileInfoText = new JTextPane();
	
	public OpenPanelView(GUIMain guiMain, GUIFile guiFile) {
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
		
		JPanel daxploreFilePanel = new JPanel();
		daxploreFilePanel.setBorder(new TitledBorder(null, "Daxplore file information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		JPanel importSPSSPanel = new JPanel();
		importSPSSPanel.setBorder(new TitledBorder(null, "Import SPSS File", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		GroupLayout gl_openPanel = new GroupLayout(this);
		
		gl_openPanel.setHorizontalGroup(
			gl_openPanel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_openPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_openPanel.createParallelGroup(Alignment.TRAILING)
						.addComponent(importSPSSPanel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 784, Short.MAX_VALUE)
						.addComponent(daxploreFilePanel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 784, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_openPanel.setVerticalGroup(
			gl_openPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_openPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(daxploreFilePanel, GroupLayout.PREFERRED_SIZE, 379, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(importSPSSPanel, GroupLayout.PREFERRED_SIZE, 343, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(25, Short.MAX_VALUE))
		);
		
		JButton openSPSSFileButton = new JButton("Open SPSS file...");
		openSPSSFileButton.setBounds(20, 50, 153, 27);
		openSPSSFileButton.addActionListener(new OpenSPSSFile(guiMain, guiFile, this, openSPSSFileButton));
		
		JScrollPane importTableScrollPane = new JScrollPane();
		importTableScrollPane.setBounds(20, 89, 763, 217);
		
		// progress bar for import spss panel goes here.
		JProgressBar importSpssFileProgressBar = new JProgressBar();
		importSpssFileProgressBar.setBounds(600, 307, 183, 19);
		importSPSSPanel.add(importSpssFileProgressBar);
		
		JButton importSpssFileButton = new JButton("");
		importSpssFileButton.addActionListener(new ImportSPSSFile(guiMain, guiFile, this, importSpssFileButton, importSpssFileProgressBar));
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
		
		daxploreFilePanel.setLayout(null);
		daxploreFilePanel.add(fileNameLabel);
		daxploreFilePanel.add(fileNameField);
		daxploreFilePanel.add(importDateLabel);
		daxploreFilePanel.add(importDateField);
		daxploreFilePanel.add(creationDateLabel);
		daxploreFilePanel.add(creationDateField);
		daxploreFilePanel.add(lastImportedFileLabel);
		daxploreFilePanel.add(lastImportFileNameField);
		
		JButton openFileButton = new JButton("Open file...");
		openFileButton.setBounds(19, 35, 135, 27);
		daxploreFilePanel.add(openFileButton);
		openFileButton.setToolTipText("Opens a daxplore file");
		openFileButton.addActionListener(new OpenDaxploreFile(guiMain, guiFile, this, openFileButton));
		
		JButton createNewFileButton = new JButton("Create new file...");
		createNewFileButton.setBounds(168, 35, 135, 27);
		daxploreFilePanel.add(createNewFileButton);
		createNewFileButton.addActionListener(new CreateDaxploreFile(guiMain, guiFile, this, createNewFileButton));
		createNewFileButton.setToolTipText("Creates a new daxplore project file");
		
		setLayout(gl_openPanel);
	}
	
	/**
	 * Updates text field for the SPSS file information in the open panel dialog.
	 * @param guiMain TODO
	 */
	public void updateSpssFileInfoText(GUIMain guiMain, GUIFile guiFile) {
		if (guiFile.getSpssFile() != null) {
			spssFileInfoText.setText(
					"SPSS file ready for import: " +
					guiFile.getSpssFile().file.getName() + "\n" +
					guiFile.getSpssFile().file.getAbsolutePath());
		}
	}

	/**
	 * Updates text fields in the open panel dialog to display daxplore file information.
	 * @param guiMain TODO
	 */
	public void updateTextFields(GUIMain guiMain, GUIFile guiFile) {
		
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		
		// set the text fields if we have a daxplore file loaded.
		if (guiFile.getDaxploreFile() != null) {
			// update text fields with appropriate data.
			fileNameField.setText(guiFile.getDaxploreFile().getFile().getName());
			
			// check if it's a newly created file, if so, it doesn't contain certain fields.
			String importFilename = guiFile.getDaxploreFile().getAbout().getImportFilename();
			if (importFilename != null && !"".equals(importFilename)) {
				lastImportFileNameField.setText(guiFile.getDaxploreFile().getAbout().getImportFilename());
				// date must first be converted to the appropriate format before returned as string.
				if (guiFile.getDaxploreFile().getAbout().getImportDate() != null) {
				importDateField.setText(formatter.format(guiFile.getDaxploreFile().getAbout().getImportDate()));
				} else {
					importDateField.setText("");
				}
			} else {
				lastImportFileNameField.setText("");
				importDateField.setText("");
			}
			
			creationDateField.setText(
			formatter.format(guiFile.getDaxploreFile().getAbout().getCreationDate()));
		}
	}


}