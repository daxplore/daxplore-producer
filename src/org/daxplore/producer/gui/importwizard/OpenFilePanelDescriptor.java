package org.daxplore.producer.gui.importwizard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.daxplore.producer.gui.Settings;
import org.opendatafoundation.data.spss.SPSSFile;
import org.opendatafoundation.data.spss.SPSSFileException;


public class OpenFilePanelDescriptor extends ImportWizardDescriptor implements ActionListener {
	
	public static final String OPEN_SPSS_FILE_BUTTON_COMMAND = "OPEN_SPSS_FILE";
	public static final String IDENTIFIER = "FILE_IMPORT_PANEL";
	
	OpenFilePanel openFilePanel;
    
    public OpenFilePanelDescriptor() {
    	super(IDENTIFIER, new OpenFilePanel());
    	openFilePanel = (OpenFilePanel) super.getPanelComponent();
    	openFilePanel.addOpenFileButtonActionListener(this);
    	openFilePanel.openFileButton.setActionCommand(OPEN_SPSS_FILE_BUTTON_COMMAND);
    	openFilePanel.addCheckBoxImportMetaDataActionListener(this);
    	openFilePanel.addCheckBoxImportRawActionListener(this);
    	
    }
    
    @Override
    public Object getNextPanelDescriptor() {
    	return CharsetPanelDescriptor.IDENTIFIER;
    }
    
    @Override
    public Object getBackPanelDescriptor() {
        return null;
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(OpenFilePanelDescriptor.OPEN_SPSS_FILE_BUTTON_COMMAND))
            openSpssFileAction();
		
		setNextButtonAccordingToFile();
		
	}
	
	@Override
	public void aboutToDisplayPanel() {
		if (getWizard().getmainController().getSpssFile() == null)
            getWizard().setNextFinishButtonEnabled(false);
	}
	
    private void setNextButtonAccordingToFile() {
    	// keep next button disabled until a file has been loaded into memory. And, check the boxes.
        if ((getWizard().getmainController().getSpssFile() != null) &&
        	 (openFilePanel.checkBoxImportRaw.isSelected() == true || openFilePanel.checkBoxImportMetaData.isSelected() == true))
        	getWizard().setNextFinishButtonEnabled(true);
        else
        	// in ANY other case, just keep it disabled, we don't want any bypass code.
        	getWizard().setNextFinishButtonEnabled(false);           
    }

	/**
	 * Opens up a file dialogue with options to open SPSS files.
	 */
    public void openSpssFileAction() {
		JFileChooser fc = new JFileChooser(Settings.getWorkingDirectory());
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"SPSS Files", "sav");
		fc.setFileFilter(filter);

		int returnVal = fc.showOpenDialog(getWizard());

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			Settings.setWorkingDirectory(fc.getCurrentDirectory());
			File file = fc.getSelectedFile();
			System.out.println("Opening file: " + file.getName() + ".");

			// save SPSS file.
			try (SPSSFile spssFile = new SPSSFile(file, "r")) {
				spssFile.logFlag = false;
				spssFile.loadMetadata();
				spssFile.close();
				
				getWizard().getmainController().setSpssFile(file);
				String text = "File selected: " + getWizard().getmainController().getSpssFile().getName();
				openFilePanel.fileOpenLabel.setText(text);
				System.out.println(text);
				
			} catch (FileNotFoundException e1) {
				System.out.println("SPSS file open failed.");
				JOptionPane.showMessageDialog(this.getWizard(),
						"You must select a valid SPSS file.",
						"Daxplore file warning", JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (SPSSFileException e1) {
				System.out.println("Not a valid SPSS file.");
				JOptionPane.showMessageDialog(this.getWizard(),
						"You must select a valid SPSS file.",
						"Daxplore file warning", JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			}
		}
	}
}
