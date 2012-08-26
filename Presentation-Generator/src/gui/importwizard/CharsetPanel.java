package gui.importwizard;

import javax.swing.JPanel;
import javax.swing.JTextField;

public class CharsetPanel extends JPanel {
	private JTextField txtCharsetEditGoes;
	public CharsetPanel() {
		
		txtCharsetEditGoes = new JTextField();
		txtCharsetEditGoes.setText("Charset edit goes here");
		add(txtCharsetEditGoes);
		txtCharsetEditGoes.setColumns(10);
	}

}
