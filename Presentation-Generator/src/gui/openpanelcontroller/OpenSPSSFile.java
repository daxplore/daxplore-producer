package gui.openpanelcontroller;

import gui.GUIFile;
import gui.GUIMain;
import gui.view.OpenPanelView;
import gui.view.ImportSPSSWizardDialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.opendatafoundation.data.spss.SPSSFile;

import daxplorelib.DaxploreException;

/**
 * Handles operations of the open spss file button in open panel.
 * @author hkfs89
 *
 */
public class OpenSPSSFile implements ActionListener {

	private final GUIMain guiMain;
	private GUIFile guiFile;
	private final JButton openSPSSFileButton;
	private final OpenPanelView openPanelView;

	public OpenSPSSFile(GUIMain guiMain, GUIFile guiFile, OpenPanelView openPanelView, JButton openSPSSFileButton) {
		this.guiMain = guiMain;
		this.guiFile = guiFile;
		this.openPanelView = openPanelView;
		this.openSPSSFileButton = openSPSSFileButton;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == openSPSSFileButton) {
			JFileChooser fc = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
					"SPSS Files", "sav");
			fc.setFileFilter(filter);

			int returnVal = fc.showOpenDialog(this.guiMain.guiMainFrame);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				
				try {
	        		if(guiFile.getSpssFile() != null) {
						guiFile.getSpssFile().close();	        			
	        		}
				} catch (IOException e1) {
					System.out.println("Unable to close SPSS file.");
					e1.printStackTrace();
				}
				
				File file = fc.getSelectedFile();
				System.out.println("Importing: " + file.getName() + ".");
				
				// import SPSS file.
				try {
					guiFile.setSpssFile(new SPSSFile(file,"rw"));
					
					ImportSPSSWizardDialog spssApprove = new ImportSPSSWizardDialog(this.guiMain, this.guiFile.getSpssFile());
					spssApprove.setVisible(true);
					openPanelView.updateSpssFileInfoText(guiMain, guiFile);
					
				} catch (FileNotFoundException e1) {
					System.out.println("SPSS file open failed.");
					JOptionPane.showMessageDialog(this.guiMain.guiMainFrame,
							"You must select a valid SPSS file.",
							"Daxplore file warning",
							JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				}
			}
		}
	}
}
