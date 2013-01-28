package gui.importwizard;

import java.awt.event.ActionListener;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JCheckBox;
import java.awt.FlowLayout;

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
