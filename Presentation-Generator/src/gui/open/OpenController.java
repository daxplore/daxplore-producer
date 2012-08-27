package gui.open;

import gui.GUIFile;
import gui.GUIMain;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import daxplorelib.DaxploreException;
import daxplorelib.DaxploreFile;

/**
 * Daxplore file creation controller. Controls all action logic in the open panel view.
 * @author hkfs89
 *
 */
public final class OpenController implements ActionListener {

	private final GUIMain guiMain;
	private final OpenPanelView openPanelView;
	private GUIFile guiFile;

	public OpenController(GUIMain guiMain, GUIFile guiFile, OpenPanelView openPanelView) {
		this.guiMain = guiMain;
		this.openPanelView = openPanelView;
		this.guiFile = guiFile;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(OpenPanelView.CREATE_BUTTON_ACTION_COMMAND))
            createButtonPressed();
        else if (e.getActionCommand().equals(OpenPanelView.OPEN_BUTTON_ACTION_COMMAND))
            openButtonPressed();
	}

	/**
	 * Method triggers when create button is pressed. Loads a create file panel and controls
	 * creation of daxplore files.
	 */
	public void createButtonPressed() {
		
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

		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"Daxplore Files", "daxplore");
		fc.setFileFilter(filter);

		int returnVal = fc.showSaveDialog(this.guiMain.getGuiMainFrame());

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
				openPanelView.updateTextFields(guiFile);
			} catch (DaxploreException e1) {
				System.out.println("Saving daxplore file failure.");
				e1.printStackTrace();
			}
			System.out.println("Saving: " + file.getName());
		}
	}

	/**
	 * Method triggers when open button is pressed and executes an open file dialog panel.
	 */
	public void openButtonPressed() {
		
		JFileChooser fc = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"Daxplore Files", "daxplore");
		fc.setFileFilter(filter);

		int returnVal = fc.showOpenDialog(this.guiMain.getGuiMainFrame());

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
			File file = fc.getSelectedFile();
			System.out.println("Opening: " + file.getName() + ".");
			try {
				guiFile.setDaxploreFile(DaxploreFile
						.createFromExistingFile(file));

				// print the contents of daxplore file about section, just for
				// testing.
				System.out.println("Daxplore file content: "
						+ guiFile.getDaxploreFile().getAbout());

				// update text fields so that file information is properly
				// shown.
				openPanelView.updateTextFields(guiFile);

			} catch (DaxploreException e1) {
				JOptionPane.showMessageDialog(this.guiMain.getGuiMainFrame(),
						"You must select a valid daxplore file.",
						"Daxplore file warning", JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			}
		} else {
			System.out.println("Open command cancelled by user.");
		}
	}
}
