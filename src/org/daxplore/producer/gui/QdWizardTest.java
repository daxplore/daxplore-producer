package org.daxplore.producer.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Set;
import java.util.SortedMap;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.tools.CharsetTest;
import org.daxplore.producer.tools.SPSSTools;
import org.opendatafoundation.data.spss.SPSSFile;
import org.opendatafoundation.data.spss.SPSSFileException;
import org.qdwizard.Screen;
import org.qdwizard.Wizard;

import com.google.common.base.Charsets;

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
	
	public static class ChooseEncodingPanel extends Screen implements ActionListener {

		private enum TextCommand {
			ENCODING, LOCALE
		}

		private static final String ENCODING_COMBO_BOX_LIST_LABEL = "<Select encoding type>"; //TODO: externalize
		private static final String LOCALE_COMBO_BOX_LIST_LABEL = "<Select a language>";
		private static final String COMBO_BOX_SEPARATOR =  "----------------------";
		
		private boolean validLocale = false;
		private boolean validEncoding = false;
		
		private JScrollPane encodingListPanel;
		
		@Override
		public String getName() {
			return "Choose encoding and language";
		}

		@Override
		public String getDescription() {
			return "To import metadata correctly we need to know what language and character encoding the SPSS file uses. " + 
					"If you don't know what character set to use, try different ones until the strings below look right";
		}
		
		private class DisplayLocale {
			public Locale locale;
			public String alternativeText;
			public DisplayLocale(Locale locale) {
				this.locale = locale;
			}
			public DisplayLocale(String text) {
				alternativeText = text;
			}
			public String toString() {
				if(locale!=null) {
					return locale.getDisplayLanguage(Locale.ENGLISH);
				} else {
					return alternativeText;
				}
			}
		}
		
		@Override
		public void initUI() {
			setLayout(new BorderLayout(0, 0));
			
			JPanel localePanel = new JPanel();
			localePanel.setBorder(BorderFactory.createTitledBorder("Select language"));
			JComboBox<DisplayLocale> localeComboBox = new JComboBox<>();
			localePanel.add(localeComboBox);
			//TODO better locale handling
			localeComboBox.addItem(new DisplayLocale(LOCALE_COMBO_BOX_LIST_LABEL));
			localeComboBox.addItem(new DisplayLocale(new Locale("sv")));
			localeComboBox.addItem(new DisplayLocale(Locale.ENGLISH));
			localeComboBox.setActionCommand(TextCommand.LOCALE.toString());
			localeComboBox.addActionListener(this);
			add(localePanel, BorderLayout.NORTH);
			
			
			JPanel encodingPanel = new JPanel(new BorderLayout());
			encodingPanel.setBorder(BorderFactory.createTitledBorder("Select character encoding"));
			JComboBox<String> encodingComboBox = new JComboBox<>();
			encodingComboBox.addItem(ENCODING_COMBO_BOX_LIST_LABEL);
			encodingComboBox.addItem(Charsets.US_ASCII.name());
			encodingComboBox.addItem(Charsets.UTF_8.name());
			encodingComboBox.addItem(Charsets.ISO_8859_1.name());
			encodingComboBox.addItem(COMBO_BOX_SEPARATOR);
			SortedMap<String, Charset> cset = Charset.availableCharsets();
			for (String charname : cset.keySet()) {
				if(CharsetTest.charset8bitTest(cset.get(charname))){
					encodingComboBox.addItem(charname);
				}
			}
			encodingPanel.add(encodingComboBox, BorderLayout.NORTH);
			encodingComboBox.setActionCommand(TextCommand.ENCODING.toString());
			encodingComboBox.addActionListener(this);
			
			encodingListPanel = new JScrollPane();
			encodingListPanel.setBounds(0, 0, 0, -36);
			JPanel tablePanel = new JPanel();
			encodingListPanel.setViewportView(tablePanel);
			encodingPanel.add(encodingListPanel, BorderLayout.CENTER);

			add(encodingPanel, BorderLayout.CENTER);
			
			updateWizardProblem();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			switch (TextCommand.valueOf(e.getActionCommand())) {
			case LOCALE:
				validLocale = false;
				JComboBox<DisplayLocale> localeSource = (JComboBox<DisplayLocale>) e.getSource();
				
				Locale locale = ((DisplayLocale)localeSource.getSelectedItem()).locale;
				if(locale != null) {
					data.put("locale", locale);
					validLocale = true;
				}
				break;
			case ENCODING:
				validEncoding = false;
				JComboBox<String> charsetSource = (JComboBox<String>) e.getSource();
				
				String charsetType = (String) charsetSource.getSelectedItem();
				if (charsetType.equals(ENCODING_COMBO_BOX_LIST_LABEL) || charsetType.equals(COMBO_BOX_SEPARATOR)) {
					return;
				}
				
				if(charsetType != null && !charsetType.isEmpty()) {
					Charset charset = Charset.forName(charsetType);
					try {
						Set<String> encodedStrings = SPSSTools.getNonAsciiStrings((File)data.get("spssFile"), charset);
						
						DefaultComboBoxModel<String> stringList = new DefaultComboBoxModel<>();
						for (String es : encodedStrings) {
							stringList.addElement(es);
						}
						
						JList<String> encodedStringsList = new JList<>(stringList);
						
						data.put("charset", charset);
						encodingListPanel.getViewport().setView(encodedStringsList);
						encodingListPanel.validate();
						validEncoding = true;
					} catch (DaxploreException e1) {
						setProblem("Unsupported encoding");
					}
				}
				break;
			default:
				throw new AssertionError("Not defined: " + e.getActionCommand());
			}
			
			updateWizardProblem();
		}
		
		private void updateWizardProblem() {
			if(!validLocale) {
				setProblem("Select a language");
			} else if(!validEncoding) {
				setProblem("Select an encoding");
			} else {
				setProblem(null);
			}
		}
	}
	
	public static class ReviewDataPanel extends Screen {

		@Override
		public String getName() {
			return "Review your data";
		}

		@Override
		public String getDescription() {
			return "Make sure that all your data looks right before finalizing the import";
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
		} else if(OpenSPSSPanel.class.equals(screen)) {
			return ChooseEncodingPanel.class;
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
