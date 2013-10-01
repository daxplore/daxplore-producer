package org.daxplore.producer.gui.open;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.DaxploreFile;
import org.daxplore.producer.gui.MainController;
import org.daxplore.producer.gui.Settings;
import org.daxplore.producer.gui.importwizard.CharsetPanelDescriptor;
import org.daxplore.producer.gui.importwizard.FinalImportPanelDescriptor;
import org.daxplore.producer.gui.importwizard.ImportWizardDescriptor;
import org.daxplore.producer.gui.importwizard.ImportWizardDialog;
import org.daxplore.producer.gui.importwizard.OpenFilePanelDescriptor;

/**
 * Daxplore file creation controller. Controls all action logic in the open panel view.
 */
public final class OpenFileController implements ActionListener {

	private final MainController mainController;
	private final OpenFileView openFileView;

	public OpenFileController(MainController mainController) {
		this.mainController = mainController;
		this.openFileView = new OpenFileView();
		openFileView.addActionListener(this);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		switch(e.getActionCommand()) {
		case OpenFileView.CREATE_BUTTON_ACTION_COMMAND:
            createButtonPressed();
            break;
		case OpenFileView.OPEN_BUTTON_ACTION_COMMAND:
            openButtonPressed();
            break;
		case OpenFileView.IMPORT_BUTTON_ACTION_COMMAND:
			importButtonPressed();
			break;
		default:
			throw new AssertionError("Undefined action command: '" + e.getActionCommand() + "'");
		}
	}

	/**
	 * Method triggers when create button is pressed. Loads a create file panel and controls
	 * creation of daxplore files.
	 */
	@SuppressWarnings("serial")
	public void createButtonPressed() {
		final JFileChooser fc = new JFileChooser(Settings.getWorkingDirectory()) {
	        // override default operation of approveSelection() so it can handle overwriting files.
	        @Override
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

		int returnVal = fc.showSaveDialog(this.mainController.getMainWindow());

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			try {
				Settings.setWorkingDirectory(fc.getCurrentDirectory());
				if (mainController.getDaxploreFile() != null) {
					mainController.getDaxploreFile().close();
				}
			} catch (IOException e) {
				System.out.println("Failed to close old daxplore file");
				e.printStackTrace();
				//TODO communicate error to user? Still allow opening a new file?
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

		int returnVal = fc.showOpenDialog(mainController.getMainWindow());

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			Settings.setWorkingDirectory(fc.getCurrentDirectory());
			try {
				Settings.setWorkingDirectory(fc.getCurrentDirectory());
				if (mainController.getDaxploreFile() != null) {
					mainController.getDaxploreFile().close();
				}
			} catch (IOException e) {
				System.out.println("Failed to close old daxplore file");
				e.printStackTrace();
				//TODO communicate error to user? Still allow opening a new file?
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
				JOptionPane.showMessageDialog(mainController.getMainWindow(),
						"You must select a valid daxplore file.",
						"Daxplore file warning", JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			}
		} else {
			System.out.println("Open command cancelled by user.");
		}
	}
	
	public void importButtonPressed() {
		if (mainController.getDaxploreFile() == null) {
			JOptionPane
					.showMessageDialog(
							mainController.getMainWindow(),
							"Create or open a daxplore project file before you import an SPSS file.",
							"Daxplore file warning", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		ImportWizardDialog importWizardDialog = new ImportWizardDialog(mainController);
		
		ImportWizardDescriptor openFilePanelDescriptor = new OpenFilePanelDescriptor();
        importWizardDialog.registerWizardPanel(OpenFilePanelDescriptor.IDENTIFIER, openFilePanelDescriptor);
        
		ImportWizardDescriptor charsetPanelDescriptor = new CharsetPanelDescriptor();
        importWizardDialog.registerWizardPanel(CharsetPanelDescriptor.IDENTIFIER, charsetPanelDescriptor);
        
		ImportWizardDescriptor finalImportPanelDescriptor = new FinalImportPanelDescriptor();
        importWizardDialog.registerWizardPanel(FinalImportPanelDescriptor.IDENTIFIER, finalImportPanelDescriptor);
        
        importWizardDialog.setCurrentPanel(OpenFilePanelDescriptor.IDENTIFIER);
        
		importWizardDialog.setVisible(true);
	}
	
	/**
	 * Updates text field for the SPSS file information in the open panel dialog.
	 * @param mainController
	 */
	public void updateSpssFileInfoText() {
		if (mainController.getSpssFile() != null) {
			openFileView.setSpssFileInfoText("SPSS file successfully imported!\n"
					+ mainController.getSpssFile().getName() + "\n" + mainController.getSpssFile().getAbsolutePath());
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

	public Component getView() {
		return openFileView;
	}
}
