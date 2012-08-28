package gui.importwizard;

import java.awt.Cursor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.opendatafoundation.data.FileFormatInfo;
import org.opendatafoundation.data.FileFormatInfo.ASCIIFormat;
import org.opendatafoundation.data.FileFormatInfo.Compatibility;
import org.opendatafoundation.data.spss.SPSSFile;
import org.opendatafoundation.data.spss.SPSSFileException;
import org.opendatafoundation.data.spss.SPSSVariable;

import daxplorelib.DaxploreException;


public class FinalImportPanelDescriptor extends ImportWizardDescriptor {

	public static final String IDENTIFIER = "FINAL_IMPORT_PANEL";
	FinalImportPanel finalImportPanel;
	
	public FinalImportPanelDescriptor() {
        super(IDENTIFIER, new FinalImportPanel());
        finalImportPanel = (FinalImportPanel) super.getPanelComponent();
    }
    
    public Object getNextPanelDescriptor() {
        return FINISH;
    }
    
    public Object getBackPanelDescriptor() {
        return CharsetPanelDescriptor.IDENTIFIER;
    }
    
    @Override
    public void aboutToDisplayPanel() {
    	
    	DefaultTableModel model = spssTable(getWizard().getGuiMain().getGuiFile().getSpssFile());
    	finalImportPanel.showTable(model);
    }
    
    /**
     * Handles SPSS file import.
     * TODO: Update with more information.
     */
	public void importSpssFileAction() {

		// this should never happen the way the dialog is designed. But we keep this for safety.
		if (getWizard().getGuiMain().getGuiFile().getSpssFile() == null) {
			JOptionPane.showMessageDialog(this.getWizard().getGuiMain().getGuiMainFrame(),
					"You must open an SPSS file before you can import it.",
					"Daxplore file warning", JOptionPane.ERROR_MESSAGE);
			return;

		}

		if (getWizard().getGuiMain().getGuiFile().getDaxploreFile() == null) {
			JOptionPane
					.showMessageDialog(
							this.getWizard().getGuiMain().getGuiMainFrame(),
							"Create or open a daxplore project file before you import an SPSS file.",
							"Daxplore file warning", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Charset charset;
		try {
			charset = Charset.forName(getWizard().getModel().getCharsetName());
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(this.getWizard().getGuiMain().getGuiMainFrame(),
					"Unable to create charset, aborting import.",
					"Daxplore file warning", JOptionPane.ERROR_MESSAGE);
			return;
		}

		File importFile = getWizard().getGuiMain().getGuiFile().getSpssFile();

		try {
			
			getWizard().getGuiMain().getGuiMainFrame().setCursor(
					Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			getWizard().getGuiMain().getGuiFile().getDaxploreFile().importSPSS(importFile, charset);

			// TODO: Display information that file has been imported.

		} catch (FileNotFoundException e2) {
			JOptionPane.showMessageDialog(this.getWizard().getGuiMain().getGuiMainFrame(),
					"Unable to find the SPSS file.", "Daxplore file warning",
					JOptionPane.ERROR_MESSAGE);
			e2.printStackTrace();
			return;
		} catch (IOException e2) {
			JOptionPane.showMessageDialog(this.getWizard().getGuiMain().getGuiMainFrame(),
					"File import error.", "Daxplore file warning",
					JOptionPane.ERROR_MESSAGE);
			e2.printStackTrace();
			return;
		} catch (DaxploreException e2) {
			JOptionPane.showMessageDialog(this.getWizard().getGuiMain().getGuiMainFrame(),
					"Unable to import file, aborting operation.",
					"Daxplore file warning", JOptionPane.ERROR_MESSAGE);
			e2.printStackTrace();
			return;
		}
	}
	
	/**
	 * Creates a temporary file on disc and imports SPSS file information as well as outputs it to a table display.
	 * @param sf
	 * @return TableColumn
	 */
	public DefaultTableModel spssTable(File file) {
		File temp;
		FileFormatInfo ffi = new FileFormatInfo();
		ffi.namesOnFirstLine = false;
		ffi.asciiFormat = ASCIIFormat.CSV;
		ffi.compatibility = Compatibility.GENERIC;
		BufferedReader br = null;
		
		SPSSFile sf = null;
		try {
			sf = new SPSSFile(file, "r");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
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
			// just for testing output so we can see data is being processed.
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
	
		DefaultTableModel model = new DefaultTableModel(data, columns);
		
		return model;
	}
}
