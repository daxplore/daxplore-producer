package gui.openpanelcontroller;

import gui.GUIFile;
import gui.GUIMain;
import gui.view.OpenPanelView;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.sun.media.sound.Toolkit;

import daxplorelib.DaxploreException;
import daxplorelib.DaxploreFile;

/**
 * SPSS file import class to GUI.
 * @author jorgenrosen
 *
 */
public class ImportSPSSFile implements ActionListener, PropertyChangeListener {
	
	private final GUIMain guiMain;
	private final ImportSPSSFile importSpssFileInstance; // for instance save.
	private GUIFile guiFile;
	
	public ImportSPSSFile getImportSpssFileInstance() {
		return importSpssFileInstance;
	}

	private final JButton importSpssFileButton;
	private final JProgressBar importSpssFileProgressBar;
	private final OpenPanelView openPanelView;
	private Task task;
	
	// TODO: To be implemented: user should be able to choose which charset is to be used based on string information.
	public String charsetName = "ISO-8859-1";

	public ImportSPSSFile(GUIMain guiMain, GUIFile guiFile, OpenPanelView openPanelView, JButton importSPSSFileButton, JProgressBar importSPSSFileProgressBar) {
		this.guiMain = guiMain;
		this.guiFile = guiFile;
		this.openPanelView = openPanelView;
		this.importSpssFileButton = importSPSSFileButton;
		this.importSpssFileProgressBar = importSPSSFileProgressBar;
		importSpssFileInstance = this;  // saving instance.
	}
	
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
            int progress = (Integer) evt.getNewValue();
            importSpssFileProgressBar.setValue(progress);
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
			
			File importFile = guiFile.getSpssFile().file;
			
			try {
				importSpssFileButton.setEnabled(false);
		        guiMain.guiMainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				guiFile.getDaxploreFile().importSPSS(importFile, charset);
				//Instances of javax.swing.SwingWorker are not reusuable, so
		        //we create new instances as needed.
		        task = new Task();
		        task.addPropertyChangeListener(importSpssFileInstance);
		        task.execute();
				
				// update open panel text fields to ensure the latest file updates are displayed
				// after a successful spss import.
				openPanelView.updateTextFields(guiMain, guiFile);
				openPanelView.setSpssFileInfoText(openPanelView.getSpssFileInfoText() + "\nSPSS file successfully imported!");
				
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