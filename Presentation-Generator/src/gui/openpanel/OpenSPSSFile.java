package gui.openpanel;

import gui.DaxploreGUI;

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

public class OpenSPSSFile implements ActionListener {

	private final DaxploreGUI daxploreGUI;
	private final JButton openSPSSFileButton;

	public OpenSPSSFile(DaxploreGUI daxploreGUI, JButton openSPSSFileButton) {
		this.daxploreGUI = daxploreGUI;
		this.openSPSSFileButton = openSPSSFileButton;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == openSPSSFileButton) {
			JFileChooser fc = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
					"SPSS Files", "sav");
			fc.setFileFilter(filter);

			int returnVal = fc.showOpenDialog(this.daxploreGUI.frmDaxploreProducer);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				
				try {
	        		if(daxploreGUI.getSpssFile() != null) {
						daxploreGUI.getSpssFile().close();	        			
	        		}
				} catch (IOException e1) {
					System.out.println("Unable to close SPSS file.");
					e1.printStackTrace();
				}
				
				File file = fc.getSelectedFile();
				System.out.println("Importing: " + file.getName() + ".");
				
				// import SPSS file.
				try {
					daxploreGUI.setSpssFile(new SPSSFile(file,"rw"));
					
					OpenSPSSDialog spssApprove = new OpenSPSSDialog(this.daxploreGUI, this.daxploreGUI.getSpssFile());
					spssApprove.setVisible(true);
					daxploreGUI.updateSpssFileInfoText();
					
				} catch (FileNotFoundException e1) {
					System.out.println("SPSS file open failed.");
					JOptionPane.showMessageDialog(this.daxploreGUI.frmDaxploreProducer,
							"You must select a valid SPSS file.",
							"Daxplore file warning",
							JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				}
			}
		}
	}
}
