package gui.opencontroller;

import gui.GUIFile;
import gui.GUIMain;
import gui.view.OpenPanelView;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Random;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.TableColumn;

import org.opendatafoundation.data.FileFormatInfo;
import org.opendatafoundation.data.FileFormatInfo.ASCIIFormat;
import org.opendatafoundation.data.FileFormatInfo.Compatibility;
import org.opendatafoundation.data.spss.SPSSFile;
import org.opendatafoundation.data.spss.SPSSFileException;
import org.opendatafoundation.data.spss.SPSSVariable;

import daxplorelib.DaxploreException;

/**
 * SPSS file import class to GUI.
 * @author jorgenrosen
 *
 */
public class ImportSPSSFileAction implements ActionListener, PropertyChangeListener {
	
	private final GUIMain guiMain;
	private GUIFile guiFile;

	private final JButton importSpssFileButton;
	private final OpenPanelView openPanelView;
	private Task task;
	
	// TODO: To be implemented: user should be able to choose which charset is to be used based on string information.
	public String charsetName = "ISO-8859-1";

	public ImportSPSSFileAction(GUIMain guiMain, GUIFile guiFile, OpenPanelView openPanelView, JButton importSPSSFileButton) {
		this.guiMain = guiMain;
		this.guiFile = guiFile;
		this.openPanelView = openPanelView;
		this.importSpssFileButton = importSPSSFileButton;
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
	
	/**
	 * Used for the progress bar, injected override class.
	 * @author hkfs89
	 * 
	 * TODO: Currently not in use, code kept in place for future use.
	 *
	 */
	class Task extends SwingWorker<Void, Void> {
        /*
         * Main task. Executed in background thread.
         */
        @Override
        public Void doInBackground() {
            Random random = new Random();
            int progress = 0;
            //Initialize progress property.
            setProgress(0);
            while (progress < 100) {
                //Sleep for up to one second.
                try {
                    Thread.sleep(random.nextInt(100));
                } catch (InterruptedException ignore) {}
                //Make random progress.
                progress += random.nextInt(10);
                setProgress(Math.min(progress, 100));
            }
            return null;
        }

        /*
         * Executed in event dispatching thread. Triggers once the progress task is complete.
         */
        @Override
        public void done() {
            importSpssFileButton.setEnabled(true);	// make sure button can be used again.
            guiMain.guiMainFrame.setCursor(null); //turn off the wait cursor
        }
    }
	
	// called when task changes.
	public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
        } 
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (e.getSource() == importSpssFileButton) {
			
			if (guiFile.getSpssFile() == null) {
				JOptionPane.showMessageDialog(this.guiMain.guiMainFrame,
						"You must open an SPSS file before you can import it.",
						"Daxplore file warning",
						JOptionPane.ERROR_MESSAGE);
				return;
				
			}
			
			if (guiFile.getDaxploreFile() == null) {
				JOptionPane.showMessageDialog(this.guiMain.guiMainFrame,
						"Create or open a daxplore project file before you import an SPSS file.",
						"Daxplore file warning",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			Charset charset;
			try{
				charset = Charset.forName(charsetName);
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(this.guiMain.guiMainFrame,
						"Unable to create charset, aborting import.",
						"Daxplore file warning",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			File importFile = guiFile.getSpssFile();
			
			try {
				importSpssFileButton.setEnabled(false);
		        guiMain.guiMainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				guiFile.getDaxploreFile().importSPSS(importFile, charset);
				// Instances of javax.swing.SwingWorker are not reusuable, so
		        // we create new instances as needed.
		        task = new Task();
		        task.addPropertyChangeListener(this);
		        task.execute();
				
				// update open panel text fields to ensure the latest file updates are displayed
				// after a successful spss import.
				openPanelView.updateTextFields(guiMain, guiFile);
				openPanelView.spssFileInfoText.setText(openPanelView.spssFileInfoText.getText() + "\nSPSS file successfully imported!");
				
			} catch (FileNotFoundException e2) {
				JOptionPane.showMessageDialog(this.guiMain.guiMainFrame,
						"Unable to find the SPSS file.",
						"Daxplore file warning",
						JOptionPane.ERROR_MESSAGE);
				e2.printStackTrace();
				return;
			} catch (IOException e2) {
				JOptionPane.showMessageDialog(this.guiMain.guiMainFrame,
						"File import error.",
						"Daxplore file warning",
						JOptionPane.ERROR_MESSAGE);
				e2.printStackTrace();
				return;
			} catch (DaxploreException e2) {
				JOptionPane.showMessageDialog(this.guiMain.guiMainFrame,
						"Unable to import file, aborting operation.",
						"Daxplore file warning",
						JOptionPane.ERROR_MESSAGE);
				e2.printStackTrace();
				return;
			}
		}
	}		
}