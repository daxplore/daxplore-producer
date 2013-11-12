package org.daxplore.producer.gui.open;

import java.awt.BorderLayout;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.TitledBorder;

/**
 * Open panel view, displays the open and create daxplore file function as well as the import
 * SPSS file wizard button and information panel. If a file is loaded, the panels will show
 * file information.
 */
@SuppressWarnings("serial")
public class OpenFileView extends JPanel {
	
	private JTextField fileNameField = new JTextField();
	private JTextField importDateField = new JTextField();
	private JTextField creationDateField = new JTextField();
	private JTextField lastImportFileNameField = new JTextField();
	private JTextPane spssFileInfoText = new JTextPane();

	public static final String OPEN_BUTTON_ACTION_COMMAND = "OpenButtonActionCommand";
	public static final String CREATE_BUTTON_ACTION_COMMAND = "CreateButtonActionCommand";
	public static final String IMPORT_BUTTON_ACTION_COMMAND = "ImportButtonActionCommand";
	
	public OpenFileView() {
		getFileNameField().setEditable(false);
		getFileNameField().setBounds(166, 75, 240, 27);
		getFileNameField().setColumns(10);

		getImportDateField().setEditable(false);
		getImportDateField().setBounds(166, 203, 240, 27);
		getImportDateField().setColumns(10);

		getCreationDateField().setEditable(false);
		getCreationDateField().setBounds(166, 108, 240, 27);
		getCreationDateField().setColumns(10);

		getLastImportFileNameField().setEditable(false);
		getLastImportFileNameField().setBounds(166, 168, 240, 27);
		getLastImportFileNameField().setColumns(10);
		
		JPanel guiFilePanel = new JPanel();
		guiFilePanel.setBorder(new TitledBorder(null, "Daxplore file information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		JPanel importSPSSPanel = new JPanel();
		importSPSSPanel.setBorder(new TitledBorder(null, "Import SPSS File", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		GroupLayout gl_openPanel = new GroupLayout(this);
		
		gl_openPanel.setHorizontalGroup(
			gl_openPanel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_openPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_openPanel.createParallelGroup(Alignment.TRAILING)
						.addComponent(importSPSSPanel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 784, Short.MAX_VALUE)
						.addComponent(guiFilePanel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 784, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_openPanel.setVerticalGroup(
			gl_openPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_openPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(guiFilePanel, GroupLayout.PREFERRED_SIZE, 379, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(importSPSSPanel, GroupLayout.PREFERRED_SIZE, 343, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(25, Short.MAX_VALUE))
		);
		importSPSSPanel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane importTableScrollPane = new JScrollPane();
		
		importTableScrollPane.setViewportView(spssFileInfoText);
		importSPSSPanel.add(importTableScrollPane);
		
		JLabel fileNameLabel = new JLabel("Filename:");
		fileNameLabel.setBounds(19, 81, 115, 15);
		
		JLabel importDateLabel = new JLabel("Import date:");
		importDateLabel.setBounds(19, 209, 115, 15);
		
		JLabel creationDateLabel = new JLabel("Creation date:");
		creationDateLabel.setBounds(19, 114, 115, 15);
		
		JLabel lastImportedFileLabel = new JLabel("Last import filename:");
		lastImportedFileLabel.setBounds(19, 174, 135, 15);
		
		guiFilePanel.setLayout(null);
		
		guiFilePanel.add(fileNameLabel);
		guiFilePanel.add(getFileNameField());
		guiFilePanel.add(importDateLabel);
		guiFilePanel.add(getImportDateField());
		guiFilePanel.add(creationDateLabel);
		guiFilePanel.add(getCreationDateField());
		guiFilePanel.add(lastImportedFileLabel);
		guiFilePanel.add(getLastImportFileNameField());
		
		setLayout(gl_openPanel);
	}
	
	public JTextField getFileNameField() {
		return fileNameField;
	}

	public void setFileNameField(JTextField fileNameField) {
		this.fileNameField = fileNameField;
	}

	public JTextField getLastImportFileNameField() {
		return lastImportFileNameField;
	}

	public void setLastImportFileNameField(JTextField lastImportFileNameField) {
		this.lastImportFileNameField = lastImportFileNameField;
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
	
	public void setSpssFileInfoText(String text) {
		spssFileInfoText.setText(text);
	}
}
