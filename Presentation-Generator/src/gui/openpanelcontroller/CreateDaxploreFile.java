package gui.openpanelcontroller;

import gui.GUIFile;
import gui.GUIMain;
import gui.view.OpenPanelView;

import java.awt.Dialog;
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

	private final GUIMain guiMain;
	private final JButton createFileButton;
	private final OpenPanelView openPanelView;
	private GUIFile guiFile;

	public CreateDaxploreFile(GUIMain guiMain, GUIFile guiFile, OpenPanelView openPanelView, JButton createFileButton) {
		this.guiMain = guiMain;
		this.openPanelView = openPanelView;
		this.createFileButton = createFileButton;
		this.guiFile = guiFile;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		final JFileChooser fc = new JFileChooser() {

	        private static final long serialVersionUID = 7919427933588163126L;
	        
	        // override default operation of approveSelection() so it can handle overwriting files.
	        public void approveSelection() {
	            File f = getSelectedFile();
	            if (f.exists() && getDialogType() == SAVE_DIALOG) {
	                int result = JOptionPane.showConfirmDialog(this,
	                        "The file exists, overwrite?", "Existing file",
	                        JOptionPane.YES_NO_CANCEL_OPTION);
	                switch (result) {
	                case JOptionPane.YES_OPTION:
	                    super.approveSelection();
	                    return;
	                case JOptionPane.CANCEL_OPTION:
	                    cancelSelection();
	                    return;
	                default:
	                    return;
	                }
	            }
	            super.approveSelection();
	        }
	    };
	    
		if (e.getSource() == createFileButton) {
			
			FileNameExtensionFilter filter = new FileNameExtensionFilter("Daxplore Files", "daxplore");
			fc.setFileFilter(filter);

			int returnVal = fc.showSaveDialog(this.guiMain.guiMainFrame);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try {
					if (guiFile.getDaxploreFile() != null) {
						guiFile.getDaxploreFile().close();
					}
				} catch (DaxploreException e2) {
					System.out.println("Couldn't close file");
					e2.printStackTrace();
					return;
				}

				// set the appropriate file ending.
				File file = fc.getSelectedFile();
				String name = file.getName();
				if (name.indexOf('.') == -1) {
					name += ".daxplore";
					file = new File(file.getParentFile(), name);
				}

				try {
					guiFile.setDaxploreFile(DaxploreFile.createWithNewFile(file));
					openPanelView.updateTextFields(guiMain, guiFile);
				} catch (DaxploreException e1) {
					System.out.println("Saving daxplore file failure.");
					e1.printStackTrace();
				}
				System.out.println("Saving: " + file.getName());
			}
		}
	}
}