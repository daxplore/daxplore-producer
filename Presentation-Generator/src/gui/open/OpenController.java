package gui.open;

import gui.GuiFile;
import gui.GuiMain;

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

	private final GuiMain guiMain;
	private final OpenPanelView openPanelView;

	public OpenController(GuiMain guiMain, OpenPanelView openPanelView) {
		this.guiMain = guiMain;
		this.openPanelView = openPanelView;
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
				if (guiMain.getGuiFile().getDaxploreFile() != null) {
					guiMain.getGuiFile().getDaxploreFile().close();
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
				guiMain.getGuiFile().setDaxploreFile(DaxploreFile.createWithNewFile(file));
				updateTextFields();
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
				if (guiMain.getGuiFile().getDaxploreFile() != null) {
					guiMain.getGuiFile().getDaxploreFile().close();
				}
			} catch (DaxploreException e2) {
				System.out.println("Couldn't close file");
				e2.printStackTrace();
				return;
			}
			File file = fc.getSelectedFile();
			System.out.println("Opening: " + file.getName() + ".");
			try {
				guiMain.getGuiFile().setDaxploreFile(DaxploreFile
						.createFromExistingFile(file));

				// print the contents of daxplore file about section, just for
				// testing.
				System.out.println("Daxplore file content: "
						+ guiMain.getGuiFile().getDaxploreFile().getAbout());

				// update text fields so that file information is properly
				// shown.
				updateTextFields();

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
	
	/**
	 * Updates text field for the SPSS file information in the open panel dialog.
	 * @param guiMain
	 */
	public void updateSpssFileInfoText() {
		if (guiMain.getGuiFile().getSpssFile() != null) {
			openPanelView.spssFileInfoText.setText(
					"SPSS file successfully imported!\n" +
						guiMain.getGuiFile().getSpssFile().getName() + "\n" +
						guiMain.getGuiFile().getSpssFile().getAbsolutePath());
		}
	}

	/**
	 * Updates text fields in the open panel dialog to display daxplore file information.
	 * @param guiMain
	 */
	public void updateTextFields() {
		
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		
		// set the text fields if we have a daxplore file loaded.
		if (guiMain.getGuiFile().getDaxploreFile() != null) {
			// update text fields with appropriate data.
			openPanelView.getFileNameField().setText(guiMain.getGuiFile().getDaxploreFile().getFile().getName());
			
			// check if it's a newly created file, if so, it doesn't contain certain fields.
			String importFilename = guiMain.getGuiFile().getDaxploreFile().getAbout().getImportFilename();
			if (importFilename != null && !"".equals(importFilename)) {
				openPanelView.getLastImportFileNameField().setText(guiMain.getGuiFile().getDaxploreFile().getAbout().getImportFilename());
				// date must first be converted to the appropriate format before returned as string.
				if (guiMain.getGuiFile().getDaxploreFile().getAbout().getImportDate() != null) {
				openPanelView.getImportDateField().setText(formatter.format(guiMain.getGuiFile().getDaxploreFile().getAbout().getImportDate()));
				} else {
					openPanelView.getImportDateField().setText("");
				}
			} else {
				openPanelView.getLastImportFileNameField().setText("");
				openPanelView.getImportDateField().setText("");
			}
			
			openPanelView.getCreationDateField().setText(
			formatter.format(guiMain.getGuiFile().getDaxploreFile().getAbout().getCreationDate()));
		}
	}
}
