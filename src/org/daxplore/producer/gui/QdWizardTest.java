package org.daxplore.producer.gui;

import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.opendatafoundation.data.spss.SPSSFile;
import org.opendatafoundation.data.spss.SPSSFileException;
import org.qdwizard.Screen;
import org.qdwizard.Wizard;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;

@SuppressWarnings("serial")
public class QdWizardTest extends Wizard{

	public static class CreateFilePanel extends Screen implements ActionListener {
		
		private JTextField fileNameTextField;
		
		@Override
		public String getName() {
			return "Choose file to create";
		}

		@Override
		public String getDescription() {
			return "Create a new daxplore file that will contain your project";
		}

		@Override
		public void initUI() {
			fileNameTextField = new JTextField(30);
			fileNameTextField.setEditable(false);
			JButton chooseButton = new JButton("Select file");
			chooseButton.addActionListener(this);
			add(fileNameTextField);
			add(chooseButton);
			setProblem("No file selected");
		}
		

		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser jfc = new JFileChooser();
			
			FileFilter filter = new FileNameExtensionFilter("Daxplore files", "daxplore");
			jfc.addChoosableFileFilter(filter);
			jfc.setFileFilter(filter);
			
			int returnVal = jfc.showSaveDialog(this);
			switch(returnVal) {
			case JFileChooser.APPROVE_OPTION:
				File fileToCreate = jfc.getSelectedFile();
				try {
					fileNameTextField.setText(fileToCreate.getCanonicalPath());
				} catch (IOException e1) {
					fileNameTextField.setText(fileToCreate.getAbsolutePath());
				}
				if(fileToCreate.isDirectory()) {
					setProblem("File can't be directory");
				} else if(fileToCreate.exists()) {
					setProblem("File already exists");
				} else if(!canCreate(fileToCreate)) {
					setProblem("Can't create file");
				} else {
					setProblem(null);
					data.put("daxploreFile", fileToCreate);
				}
				break;
			}
		}
		
	}
	
	public static class OpenSPSSPanel extends Screen implements ActionListener {
		
		private JTextField fileNameTextField;
		
		@Override
		public String getName() {
			return "Choose a SPSS file to import";
		}

		@Override
		public String getDescription() {
			return "Import data from a SPSS to your project";
		}

		@Override
		public void initUI() {
			fileNameTextField = new JTextField(30);
			fileNameTextField.setEditable(false);
			JButton chooseButton = new JButton("Select file");
			chooseButton.addActionListener(this);
			add(fileNameTextField);
			add(chooseButton);
			setProblem("No file selected");
			setCanFinish(true);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser jfc = new JFileChooser();
			
			FileFilter filter = new FileNameExtensionFilter("SPSS files", "sav");
			jfc.addChoosableFileFilter(filter);
			jfc.setFileFilter(filter);
			
			int returnVal = jfc.showSaveDialog(this);
			switch(returnVal) {
			case JFileChooser.APPROVE_OPTION:
				File spssFile = jfc.getSelectedFile();
				try {
					fileNameTextField.setText(spssFile.getCanonicalPath());
				} catch (IOException e1) {
					fileNameTextField.setText(spssFile.getAbsolutePath());
				}
				if(spssFile.isDirectory()) {
					setProblem("File can't be directory");
				} else if(!spssFile.exists()) {
					setProblem("File doesn't exist");
				} else if(!spssFile.canRead()) {
					setProblem("Can't read the selected file");
				} else {
					try (SPSSFile importSPSSFile = new SPSSFile(spssFile, Charsets.UTF_8)) {
						importSPSSFile.logFlag = false;
						importSPSSFile.loadMetadata();
					} catch (IOException | SPSSFileException e1) {
						setProblem("Selected file is not a valid SPSS file");
						break;
					}
					data.put("spssFile", spssFile);
					setProblem(null);
				}
				break;
			}
		}
	}
	
	public static class ChooseEncodingPanel extends Screen {

		@Override
		public String getName() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getDescription() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void initUI() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public static class ReviewDataPanel extends Screen {

		@Override
		public String getName() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getDescription() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void initUI() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JFrame window = new JFrame();
		
		window.setVisible(true);
		window.setSize(800, 600);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		QdWizardTest wizard = new QdWizardTest(window);
		
		wizard.show();
	}
	
	
	public QdWizardTest(JFrame window) {
		super(new Wizard.Builder("Create new project", CreateFilePanel.class, window));
	}
	
	@Override
	public Class<? extends Screen> getPreviousScreen(Class<? extends Screen> screen) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Class<? extends Screen> getNextScreen(Class<? extends Screen> screen) {
		if(CreateFilePanel.class.equals(screen)) {
			return OpenSPSSPanel.class;
		}
		return null;
	}


	@Override
	public void finish() {
		// TODO Auto-generated method stub
		
	}
	
	
	public static boolean canCreate(File file) {
		if(!file.exists()) {
			try {
				file.createNewFile();
				file.delete();
				return true;
			} catch (IOException e) {
				return false;
			}
		}
		return false;
	}

}
