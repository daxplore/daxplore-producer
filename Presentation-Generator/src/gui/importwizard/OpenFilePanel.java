package gui.importwizard;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class OpenFilePanel extends JPanel {

	private JButton openFileButton;
	private JLabel fileOpenLabel;
	
	public OpenFilePanel() {
		
		openFileButton = new JButton("Open SPSS File");
		add(openFileButton);
		
		fileOpenLabel = new JLabel("No file selected");
		add(fileOpenLabel);
		
	}
	
	public void openFileButtonActionListener(ActionListener l) {
		openFileButton.addActionListener(l);
	}

}
