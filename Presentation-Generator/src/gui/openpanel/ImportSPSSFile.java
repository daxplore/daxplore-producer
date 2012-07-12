package gui.openpanel;

import gui.DaxploreGUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import daxplorelib.DaxploreException;
import daxplorelib.DaxploreFile;

public class ImportSPSSFile implements ActionListener {
	
	private final DaxploreGUI daxploreGUI;
	private final JButton importSPSSFileButton;
	public String charsetName = "ISO-8859-1";

	public ImportSPSSFile(DaxploreGUI daxploreGUI, JButton importSPSSFileButton) {
		this.daxploreGUI = daxploreGUI;
		this.importSPSSFileButton = importSPSSFileButton;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (e.getSource() == importSPSSFileButton) {
			
			if (daxploreGUI.getSpssFile() == null) {
				JOptionPane.showMessageDialog(this.daxploreGUI.frmDaxploreProducer,
						"You must open an SPSS file before you can import it.",
						"Daxplore file warning",
						JOptionPane.ERROR_MESSAGE);
				return;
				
			}
			
			if (daxploreGUI.getDaxploreFile() == null) {
				JOptionPane.showMessageDialog(this.daxploreGUI.frmDaxploreProducer,
						"Create or open a daxplore project file before you import an SPSS file.",
						"Daxplore file warning",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			Charset charset;
			try{
				charset = Charset.forName(charsetName);
			} catch (Exception e1) {
				System.out.println("Charset error");
				return;
			}
			File importFile = daxploreGUI.getSpssFile().file;
			
			try {
				daxploreGUI.getDaxploreFile().importSPSS(importFile, charset);
				JOptionPane.showMessageDialog(this.daxploreGUI.frmDaxploreProducer,
						"File imported successfully.",
						"Daxplore import",
						JOptionPane.INFORMATION_MESSAGE);
				
				daxploreGUI.updateTextFields();
				
			} catch (FileNotFoundException e2) {
				System.out.println("SPSS file not found.");
				e2.printStackTrace();
				return;
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
				return;
			} catch (DaxploreException e2) {
				System.out.println(e2);
				e2.printStackTrace();
				return;
			}
		}
	}
		
}