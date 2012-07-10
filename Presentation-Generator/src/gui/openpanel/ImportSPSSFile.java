package gui.openpanel;

import gui.DaxploreGUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import daxplorelib.DaxploreFile;

public final class ImportSPSSFile implements ActionListener {
	
	private final DaxploreGUI daxploreGUI;
	private final JButton importSPSSFileButton;

	public ImportSPSSFile(DaxploreGUI daxploreGUI, JButton importSPSSFileButton) {
		this.daxploreGUI = daxploreGUI;
		this.importSPSSFileButton = importSPSSFileButton;
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == importSPSSFileButton) {
			JFileChooser fc = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
					"SPSS Files", "SPSS");
			fc.setFileFilter(filter);

			int returnVal = fc.showOpenDialog(this.daxploreGUI.frmDaxploreProducer);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				System.out.println("Importing: " + file.getName() + ".");
				
				// import SPSS file. Awaiting charset information.
				// daxploreGUI.setDaxploreFile(new DaxploreFile(file, <charset>);
			}
		}
	}
}