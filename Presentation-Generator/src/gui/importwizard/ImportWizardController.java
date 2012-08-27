package gui.importwizard;

import gui.GUIFile;
import gui.GUIMain;
import gui.open.OpenPanelView;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableColumn;

import org.opendatafoundation.data.FileFormatInfo;
import org.opendatafoundation.data.FileFormatInfo.ASCIIFormat;
import org.opendatafoundation.data.FileFormatInfo.Compatibility;
import org.opendatafoundation.data.spss.SPSSFile;
import org.opendatafoundation.data.spss.SPSSFileException;
import org.opendatafoundation.data.spss.SPSSVariable;

import daxplorelib.DaxploreException;

import tools.SPSSTools;

/**
 * This class is responsible for reacting to events generated by pushing any of the
 * three buttons, 'Next', 'Previous', and 'Cancel.' Based on what button is pressed,
 * the controller will update the model to show a new panel and reset the state of
 * the buttons as necessary.
 */
public class ImportWizardController implements ActionListener {
    
	private ImportWizardDialog hostPanel;
    
    /**
     * This constructor accepts a reference to the Wizard component that created it,
     * which it uses to update the button components and access the WizardModel.
     * @param w A callback to the Wizard component that created this controller.
     */    
    public ImportWizardController(ImportWizardDialog w) {
    	this.hostPanel = w;
    }

    /**
     * Calling method for the action listener interface. This class listens for actions
     * performed by the buttons in the Wizard class, and calls methods below to determine
     * the correct course of action.
     * @param evt The ActionEvent that occurred.
     */    
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        
        if (evt.getActionCommand().equals(ImportWizardDialog.CANCEL_BUTTON_ACTION_COMMAND))
            cancelButtonPressed();
        else if (evt.getActionCommand().equals(ImportWizardDialog.BACK_BUTTON_ACTION_COMMAND))
			backButtonPressed();
		else if (evt.getActionCommand().equals(ImportWizardDialog.NEXT_BUTTON_ACTION_COMMAND))
			nextButtonPressed();
    }
    
	public void importSpssFileAction() {

		if (hostPanel.getGuiMain().getGuiFile().getSpssFile() == null) {
			JOptionPane.showMessageDialog(this.hostPanel.getGuiMain().getGuiMainFrame(),
					"You must open an SPSS file before you can import it.",
					"Daxplore file warning", JOptionPane.ERROR_MESSAGE);
			return;

		}

		if (hostPanel.getGuiMain().getGuiFile().getDaxploreFile() == null) {
			JOptionPane
					.showMessageDialog(
							this.hostPanel.getGuiMain().getGuiMainFrame(),
							"Create or open a daxplore project file before you import an SPSS file.",
							"Daxplore file warning", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Charset charset;
		try {
			charset = Charset.forName(hostPanel.getModel().getCharsetName());
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(this.hostPanel.getGuiMain().getGuiMainFrame(),
					"Unable to create charset, aborting import.",
					"Daxplore file warning", JOptionPane.ERROR_MESSAGE);
			return;
		}

		File importFile = hostPanel.getGuiMain().getGuiFile().getSpssFile();

		try {
			// importSpssFileButton.setEnabled(false); // TODO: Update it for
			// the wizard.
			hostPanel.getGuiMain().getGuiMainFrame().setCursor(
					Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			hostPanel.getGuiMain().getGuiFile().getDaxploreFile().importSPSS(importFile, charset);

			// update open panel text fields to ensure the latest file updates
			// are displayed
			// after a successful spss import.
			//openPanelView.updateTextFields(guiFile);
			//openPanelView.spssFileInfoText
			//		.setText(openPanelView.spssFileInfoText.getText()
			//				+ "\nSPSS file successfully imported!");

		} catch (FileNotFoundException e2) {
			JOptionPane.showMessageDialog(this.hostPanel.getGuiMain().getGuiMainFrame(),
					"Unable to find the SPSS file.", "Daxplore file warning",
					JOptionPane.ERROR_MESSAGE);
			e2.printStackTrace();
			return;
		} catch (IOException e2) {
			JOptionPane.showMessageDialog(this.hostPanel.getGuiMain().getGuiMainFrame(),
					"File import error.", "Daxplore file warning",
					JOptionPane.ERROR_MESSAGE);
			e2.printStackTrace();
			return;
		} catch (DaxploreException e2) {
			JOptionPane.showMessageDialog(this.hostPanel.getGuiMain().getGuiMainFrame(),
					"Unable to import file, aborting operation.",
					"Daxplore file warning", JOptionPane.ERROR_MESSAGE);
			e2.printStackTrace();
			return;
		}
	}
    
    private void cancelButtonPressed() {
        
        hostPanel.close(ImportWizardDialog.CANCEL_RETURN_CODE);
    }

    private void nextButtonPressed() {
 
        ImportWizardModel model = hostPanel.getModel();
        ImportWizardDescriptor descriptor = model.getCurrentPanelDescriptor();
        
        //  If it is a finishable panel, close down the dialog. Otherwise,
        //  get the ID that the current panel identifies as the next panel,
        //  and display it.
        
        Object nextPanelDescriptor = descriptor.getNextPanelDescriptor();
        
        if (nextPanelDescriptor instanceof ImportWizardDescriptor.FinishIdentifier) {
            hostPanel.close(ImportWizardDialog.FINISH_RETURN_CODE);
        } else {        
            hostPanel.setCurrentPanel(nextPanelDescriptor);
        }
        
    }

    private void backButtonPressed() {
 
        ImportWizardModel model = hostPanel.getModel();
        ImportWizardDescriptor descriptor = model.getCurrentPanelDescriptor();
 
        //  Get the descriptor that the current panel identifies as the previous
        //  panel, and display it.
        
        Object backPanelDescriptor = descriptor.getBackPanelDescriptor();        
        hostPanel.setCurrentPanel(backPanelDescriptor);
        
    }

    public void resetButtonsToPanelRules() {
    
        //  Reset the buttons to support the original panel rules,
        //  including whether the next or back buttons are enabled or
        //  disabled, or if the panel is finishable.
        
        ImportWizardModel model = hostPanel.getModel();
        ImportWizardDescriptor descriptor = model.getCurrentPanelDescriptor();
        
        model.setCancelButtonText(ImportWizardDialog.CANCEL_TEXT);
        
        //  If the panel in question has another panel behind it, enable
        //  the back button. Otherwise, disable it.
        
        model.setBackButtonText(ImportWizardDialog.BACK_TEXT);
        
        if (descriptor.getBackPanelDescriptor() != null)
            model.setBackButtonEnabled(Boolean.TRUE);
        else
            model.setBackButtonEnabled(Boolean.FALSE);

        //  If the panel in question has one or more panels in front of it,
        //  enable the next button. Otherwise, disable it.
 
        if (descriptor.getNextPanelDescriptor() != null)
            model.setNextFinishButtonEnabled(Boolean.TRUE);
        else
            model.setNextFinishButtonEnabled(Boolean.FALSE);
 
        //  If the panel in question is the last panel in the series, change
        //  the Next button to Finish. Otherwise, set the text back to Next.
        
        if (descriptor.getNextPanelDescriptor() instanceof ImportWizardDescriptor.FinishIdentifier) {
            model.setNextFinishButtonText(ImportWizardDialog.FINISH_TEXT);
        } else {
            model.setNextFinishButtonText(ImportWizardDialog.NEXT_TEXT);
        }
        
    }
    
    /**
	 * Creates a temporary file on disc and imports SPSS file information as well as outputs it to a table display.
	 * @param sf
	 * @return TableColumn
	 */
	public TableColumn SPSSTable(SPSSFile sf) {
		File temp;
		FileFormatInfo ffi = new FileFormatInfo();
		ffi.namesOnFirstLine = false;
		ffi.asciiFormat = ASCIIFormat.CSV;
		ffi.compatibility = Compatibility.GENERIC;
		BufferedReader br = null;
		
		JTable table = null;
		
		try {
			sf.logFlag = false;
			sf.loadMetadata();
			temp = File.createTempFile("spsscsv", ".csv.tmp");
			sf.exportData(temp, ffi);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (SPSSFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		String[] columns = new String[sf.getVariableCount()];
		Object[][] data = new Object[sf.getRecordCount()][sf.getVariableCount()];
		for(int i = 0; i < sf.getVariableCount(); i++){
			SPSSVariable var = sf.getVariable(i);
			columns[i] = var.getShortName();
			System.out.print(sf.getVariable(i).getShortName() + ", ");
		}

		try {
			br = new BufferedReader(new FileReader(temp));
			String line;
			int l = 0;
			while((line = br.readLine()) != null){
				StringTokenizer st = new StringTokenizer(line, ",");
				int c = 0;
				while(st.hasMoreTokens()){
					data[l][c] = st.nextToken();
					c++;
				}
				l++;
			}
		} catch (FileNotFoundException e) {
			System.out.println("FileNotFoundException");
			e.printStackTrace();
			
		} catch (IOException e) {
			System.out.println("IOException");
			System.out.print(e.toString());
			e.printStackTrace();
		}
		
		// disallow editing of table fields through this model.
		/* TableModel model = new TableModel(data, columns) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int rowIndex, int mColIndex) {
		        return false;
		      }
		    }; */
		TableColumn column = new TableColumn();
		for (int i = 0; i < 5; i++) {
			column = table.getColumnModel().getColumn(i);
			column.setPreferredWidth(50);
		}
		return column;
	}
}