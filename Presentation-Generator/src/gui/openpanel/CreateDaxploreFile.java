package gui.openpanel;

import gui.DaxploreGUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import daxplorelib.DaxploreException;
import daxplorelib.DaxploreFile;

public final class CreateDaxploreFile implements ActionListener {

	private final DaxploreGUI daxploreGUI;
	private final JButton createFileButton;

	public CreateDaxploreFile(DaxploreGUI daxploreGUI, JButton createFileButton) {
		this.daxploreGUI = daxploreGUI;
		this.createFileButton = createFileButton;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == createFileButton) {
			JFileChooser fc = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
			        "Daxplore Files", "daxplore");
			    fc.setFileFilter(filter);
			    
	        int returnVal = fc.showSaveDialog(this.daxploreGUI.frmDaxploreProducer);

	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	        	try {
	        		if(daxploreGUI.getDaxploreFile() != null) {
						daxploreGUI.getDaxploreFile().close();	        			
	        		}
				} catch (DaxploreException e2) {
					System.out.println("Couldn't close file");
					e2.printStackTrace();
					return;
				}
	           File file = fc.getSelectedFile();
	           try {
				daxploreGUI.setDaxploreFile(new DaxploreFile(file, true));
				daxploreGUI.updateTextFields();
			} catch (DaxploreException e1) {
				System.out.println("Saving daxplore file failure.");
				e1.printStackTrace();
			}
	           System.out.println("Saving: " + file.getName());
	        }
		}
	}
}
