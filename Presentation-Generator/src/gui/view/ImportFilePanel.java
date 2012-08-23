package gui.view;

import javax.swing.JPanel;
import javax.swing.JTextField;

public class ImportFilePanel extends JPanel {
	private JTextField txtFileImportHere;
	public ImportFilePanel() {
		
		txtFileImportHere = new JTextField();
		txtFileImportHere.setText("File import here");
		add(txtFileImportHere);
		txtFileImportHere.setColumns(10);
	}

}
