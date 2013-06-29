package gui.open;

import gui.MainController;
import gui.Settings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;

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

	private final MainController mainController;
	private final OpenFileView openFileView;

	public OpenController(MainController mainController, OpenFileView openFileView) {
		this.mainController = mainController;
		this.openFileView = openFileView;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(OpenFileView.CREATE_BUTTON_ACTION_COMMAND))
            createButtonPressed();
        else if (e.getActionCommand().equals(OpenFileView.OPEN_BUTTON_ACTION_COMMAND))
            openButtonPressed();
	}

	/**
	 * Method triggers when create button is pressed. Loads a create file panel and controls
	 * creation of daxplore files.
	 */
	public void createButtonPressed() {
		
		final JFileChooser fc = new JFileChooser(Settings.getWorkingDirectory()) {

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

		int returnVal = fc.showSaveDialog(this.mainController.getMainFrame());

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			Settings.setWorkingDirectory(fc.getCurrentDirectory());
			try {
				if (mainController.getDaxploreFile() != null) {
					mainController.getDaxploreFile().close();
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
				mainController.setDaxploreFile(DaxploreFile.createWithNewFile(file));
				updateTextFields();
				// activate the button panel.
				mainController.updateStuff();
			} catch (DaxploreException e1) {
				System.out.println("Saving daxplore file failure.");
				e1.printStackTrace();
				return;
			}
			System.out.println("Saving: " + file.getName());
		}
	}

	/**
	 * Method triggers when open button is pressed and executes an open file dialog panel.
	 */
	public void openButtonPressed() {
		
		JFileChooser fc = new JFileChooser(Settings.getWorkingDirectory());
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"Daxplore Files", "daxplore");
		fc.setFileFilter(filter);

		int returnVal = fc.showOpenDialog(this.mainController.getMainFrame());

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			Settings.setWorkingDirectory(fc.getCurrentDirectory());
			try {
				if (mainController.getDaxploreFile() != null) {
					mainController.getDaxploreFile().close();
				}
			} catch (DaxploreException e2) {
				System.out.println("Couldn't close file");
				e2.printStackTrace();
				return;
			}
			File file = fc.getSelectedFile();
			System.out.println("Opening: " + file.getName() + ".");
			try {
				mainController.setDaxploreFile(DaxploreFile
						.createFromExistingFile(file));

				// print the contents of daxplore file about section, just for
				// testing.
				System.out.println("Daxplore file content: "
						+ mainController.getDaxploreFile().getAbout());

				// update text fields so that file information is properly
				// shown.
				updateTextFields();
				mainController.updateStuff();

			} catch (DaxploreException e1) {
				JOptionPane.showMessageDialog(this.mainController.getMainFrame(),
						"You must select a valid daxplore file.",
						"Daxplore file warning", JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			}
		} else {
			System.out.println("Open command cancelled by user.");
		}
	}
	
	/**
	 * Updates text field for the SPSS file information in the open panel dialog.
	 * @param mainController
	 */
	public void updateSpssFileInfoText() {
		if (mainController.getSpssFile() != null) {
			openFileView.spssFileInfoText.setText(
					"SPSS file successfully imported!\n" +
						mainController.getSpssFile().getName() + "\n" +
						mainController.getSpssFile().getAbsolutePath());
		}
	}

	/**
	 * Updates text fields in the open panel dialog to display daxplore file information.
	 * @param mainController
	 */
	public void updateTextFields() {
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		
		// set the text fields if we have a daxplore file loaded.
		if (mainController.getDaxploreFile() != null) {
			// update text fields with appropriate data.
			openFileView.getFileNameField().setText(mainController.getDaxploreFile().getFile().getName());
			
			// check if it's a newly created file, if so, it doesn't contain certain fields.
			String importFilename = mainController.getDaxploreFile().getAbout().getImportFilename();
			if (importFilename != null && !"".equals(importFilename)) {
				openFileView.getLastImportFileNameField().setText(mainController.getDaxploreFile().getAbout().getImportFilename());
				// date must first be converted to the appropriate format before returned as string.
				if (mainController.getDaxploreFile().getAbout().getImportDate() != null) {
				openFileView.getImportDateField().setText(formatter.format(mainController.getDaxploreFile().getAbout().getImportDate()));
				} else {
					openFileView.getImportDateField().setText("");
				}
			} else {
				openFileView.getLastImportFileNameField().setText("");
				openFileView.getImportDateField().setText("");
			}
			
			openFileView.getCreationDateField().setText(
			formatter.format(mainController.getDaxploreFile().getAbout().getCreationDate()));
		}
	}
}
