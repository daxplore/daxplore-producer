package gui.importwizard;

import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class OpenFilePanel extends JPanel {

	protected JButton openFileButton;
	protected JLabel fileOpenLabel;
	
	/**
	 * Constructor.
	 */
	public OpenFilePanel() {
		
		openFileButton = new JButton("Open SPSS File");
		add(openFileButton);
		
		fileOpenLabel = new JLabel("No file selected");
		add(fileOpenLabel);	
	}
	
	public void addOpenFileButtonActionListener(ActionListener l) {
		openFileButton.addActionListener(l);
	}
}
