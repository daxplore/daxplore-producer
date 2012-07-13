package gui.openpanel;


import gui.GUIMain;
import gui.view.OpenPanelView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import daxplorelib.DaxploreException;
import daxplorelib.DaxploreFile;


/**
 * Handles the open operations of the daxplore project file.
 * Nested classes are DaxploreGUI and JButton.
 */
public final class OpenDaxploreFile implements ActionListener {

	private final GUIMain guiMain;
	private final JButton openFileButton;
	private final OpenPanelView openPanelView;

	public OpenDaxploreFile(GUIMain daxploreGUI, OpenPanelView openPanelView, JButton openFileButton) {
		this.guiMain = daxploreGUI;
		this.openPanelView = openPanelView;
		this.openFileButton = openFileButton;
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == openFileButton) {
			JFileChooser fc = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
			        "Daxplore Files", "daxplore");
			    fc.setFileFilter(filter);
			    
	        int returnVal = fc.showOpenDialog(this.guiMain.frmDaxploreProducer);

	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	        	try {
	        		if(guiMain.getDaxploreFile() != null) {
						guiMain.getDaxploreFile().close();	        			
	        		}
				} catch (DaxploreException e2) {
					System.out.println("Couldn't close file");
					e2.printStackTrace();
					return;
				}
	           File file = fc.getSelectedFile();
	           System.out.println("Opening: " + file.getName() + ".");
	           try {
				guiMain.setDaxploreFile(new DaxploreFile(file, false));
				
				// print the contents of daxplore file about section, just for testing.
				System.out.println("Daxplore file content: " + guiMain.getDaxploreFile().getAbout());
				
				// update text fields so that file information is properly shown.
				openPanelView.updateTextFields(guiMain);
				
			} catch (DaxploreException e1) {
				JOptionPane.showMessageDialog(this.guiMain.frmDaxploreProducer,
						"You must select a valid daxplore file.",
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