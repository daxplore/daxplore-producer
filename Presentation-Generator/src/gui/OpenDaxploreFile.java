package gui;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import daxplorelib.DaxploreException;
import daxplorelib.DaxploreFile;

public final class OpenDaxploreFile implements ActionListener {
	/**
	 * 
	 */
	private final DaxploreGUI daxploreGUI;
	private final JButton openFileButton;

	public OpenDaxploreFile(DaxploreGUI daxploreGUI, JButton openFileButton) {
		this.daxploreGUI = daxploreGUI;
		this.openFileButton = openFileButton;
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == openFileButton) {
			JFileChooser fc = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
			        "Daxplore Files", "daxplore");
			    fc.setFileFilter(filter);
			    
	        int returnVal = fc.showOpenDialog(this.daxploreGUI.frmDaxploreProducer);

	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	           File file = fc.getSelectedFile();
	           System.out.println("Opening: " + file.getName() + ".");
	           try {
				DaxploreFile df = new DaxploreFile(file, false);
				System.out.println(df.getAbout());
			} catch (DaxploreException e1) {
				JOptionPane.showMessageDialog(this.daxploreGUI.frmDaxploreProducer,
						"Incorrect file type!",
						"Daxplore file warning",
						JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			}
	        } else {
	           System.out.println("Open command cancelled by user.");
	        }
		}
	}
}