package org.daxplore.producer.gui.importwizard;

import java.awt.FlowLayout;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class OpenFilePanel extends JPanel {

	// open panel entities.
	protected JButton openFileButton;
	protected JLabel fileOpenLabel;
	protected JCheckBox checkBoxImportRaw;
	protected JCheckBox checkBoxImportMetaData;
	
	/**
	 * Constructor.
	 */
	public OpenFilePanel() {
		
		checkBoxImportRaw = new JCheckBox("Import Raw");
		
		checkBoxImportMetaData = new JCheckBox("Import MetaData");
		
		openFileButton = new JButton("Open SPSS File");
		
		fileOpenLabel = new JLabel("No file selected");
		setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		add(checkBoxImportRaw);
		add(checkBoxImportMetaData);
		add(openFileButton);
		add(fileOpenLabel);
	}
	
	public void addCheckBoxImportRawActionListener(ActionListener l) {
		checkBoxImportRaw.addActionListener(l);
	}
	
	public void addCheckBoxImportMetaDataActionListener(ActionListener l) {
		checkBoxImportMetaData.addActionListener(l);
	}
	
	public void addOpenFileButtonActionListener(ActionListener l) {
		openFileButton.addActionListener(l);
	}
}
