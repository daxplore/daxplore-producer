package gui.opencontroller;

import gui.GUIFile;
import gui.view.ImportSPSSWizardDialog;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.opendatafoundation.data.spss.SPSSFile;
import org.opendatafoundation.data.spss.SPSSFileException;

public final class OpenSpssFileAction implements ActionListener {
	/**
	 * 
	 */
	private GUIFile guiFile;
	private Component hostPanel;

	/**
	 * @param importSPSSWizardDialog
	 */
	public OpenSpssFileAction(Component hostPanel, GUIFile guiFile) {
		this.hostPanel = hostPanel;
		this.guiFile = guiFile;
	}

	public void actionPerformed(ActionEvent e) {
		JFileChooser fc = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"SPSS Files", "sav");
		fc.setFileFilter(filter);

		int returnVal = fc.showOpenDialog(this.hostPanel);

		if (returnVal == JFileChooser.APPROVE_OPTION) {

			File file = fc.getSelectedFile();
			System.out.println("Opening file: " + file.getName() + ".");

			// import SPSS file.
			try {
				SPSSFile spssFile = new SPSSFile(file, "r");
				spssFile.logFlag = false;
				spssFile.loadMetadata();
				spssFile.close();
				
				guiFile.setSpssFile(file);
				
			} catch (FileNotFoundException e1) {
				System.out.println("SPSS file open failed.");
				JOptionPane.showMessageDialog(this.hostPanel,
						"You must select a valid SPSS file.",
						"Daxplore file warning", JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (SPSSFileException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
}