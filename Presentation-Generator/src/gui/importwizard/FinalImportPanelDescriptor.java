package gui.importwizard;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.opendatafoundation.data.FileFormatInfo;
import org.opendatafoundation.data.FileFormatInfo.ASCIIFormat;
import org.opendatafoundation.data.FileFormatInfo.Compatibility;
import org.opendatafoundation.data.spss.SPSSFile;
import org.opendatafoundation.data.spss.SPSSFileException;
import org.opendatafoundation.data.spss.SPSSVariable;


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
    	
    	// this shouldn't happen, but in case there is no file loaded, just exit.
    	if (getWizard().getmainController().getSpssFile() == null)
    		return;
    	
    	TableModel model = spssTable(getWizard().getmainController().getSpssFile());
    	finalImportPanel.showTable(model);
    }
    
    @Override
    public void aboutToHidePanel() {
    	// not implemented.
    }
    
	/**
	 * Creates a temporary file on disc and imports SPSS file information as well as outputs it to a table display.
	 * @param sf
	 * @return TableColumn
	 */
	public TableModel spssTable(File file) {
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
			System.out.println("No SPSS file found, exiting.");
			e1.printStackTrace();
			return null;
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
	
		TableModel model = new DefaultTableModel(data, columns);
		
		return model;
	}
}
