package gui.view;

import javax.swing.JPanel;
import javax.swing.JTextField;

public class FinalImportPanel extends JPanel {
	private JTextField txtFinalImportText;
	public FinalImportPanel() {
		
		txtFinalImportText = new JTextField();
		txtFinalImportText.setText("Final Import Text");
		add(txtFinalImportText);
		txtFinalImportText.setColumns(10);
	}

}
