package org.daxplore.producer.gui.importwizard;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
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
    
    @Override
	public Object getNextPanelDescriptor() {
        return FINISH;
    }
    
    @Override
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
	public static TableModel spssTable(File file) {
		File temp;
		FileFormatInfo ffi = new FileFormatInfo();
		ffi.namesOnFirstLine = false;
		ffi.asciiFormat = ASCIIFormat.CSV;
		ffi.compatibility = Compatibility.GENERIC;

		String[] columns = null;
		Object[][] data = null;
		try (SPSSFile sf = new SPSSFile(file, "r")) {
			sf.logFlag = false;
			sf.loadMetadata();
			temp = File.createTempFile("spsscsv", ".csv.tmp");
			sf.exportData(temp, ffi);
			columns =  new String[sf.getVariableCount()];
			data =  new Object[sf.getRecordCount()][sf.getVariableCount()];
			for(int i = 0; i < sf.getVariableCount(); i++){
				SPSSVariable var = sf.getVariable(i);
				columns[i] = var.getShortName();
				// just for testing output so we can see data is being processed.
				System.out.print(sf.getVariable(i).getShortName() + ", ");
			}
		} catch (SPSSFileException | IOException e) {
			return null;
		}

		try (FileInputStream fis = new FileInputStream(file);
				InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
				BufferedReader br = new BufferedReader(isr)) {
			
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
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return new DefaultTableModel(data, columns);
	}
}
