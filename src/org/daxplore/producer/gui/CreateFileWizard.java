/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui;

import java.awt.BorderLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.DaxploreFile;
import org.daxplore.producer.gui.event.DaxploreFileUpdateEvent;
import org.daxplore.producer.gui.resources.GuiTexts;
import org.daxplore.producer.gui.utility.DisplayCharset;
import org.daxplore.producer.gui.utility.DisplayLocale;
import org.daxplore.producer.tools.CharsetTest;
import org.daxplore.producer.tools.SPSSTools;
import org.opendatafoundation.data.spss.SPSSFile;
import org.opendatafoundation.data.spss.SPSSFileException;
import org.opendatafoundation.data.spss.SPSSVariable;
import org.qdwizard.Screen;
import org.qdwizard.Wizard;

import com.google.common.base.Charsets;
import com.google.common.eventbus.EventBus;

@SuppressWarnings("serial")
public class CreateFileWizard extends Wizard {
	
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
			setCanFinish(false);
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
			JFileChooser jfc = new JFileChooser(CreateFileWizard.preferences.getWorkingDirectory());
			
			FileFilter filter = new FileNameExtensionFilter("Daxplore files", "daxplore");
			jfc.addChoosableFileFilter(filter);
			jfc.setFileFilter(filter);
			
			int returnVal = jfc.showSaveDialog(this);
			
			switch(returnVal) {
			case JFileChooser.APPROVE_OPTION:
				preferences.setWorkingDirectory(jfc.getCurrentDirectory());
				setCanFinish(false);
				setProblem("No file selected");
				File fileToCreate = jfc.getSelectedFile();
				
				String filename = fileToCreate.toString();
				if(!filename.endsWith(".daxplore")) {
					filename += ".daxplore";
					fileToCreate = new File(filename);
				}
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
					setCanFinish(true);
				}
				break;
			}
		}
		
	}
	
	public static class ChooseEncodingPanel extends Screen implements ActionListener {

		private enum TextCommand {
			ENCODING, LOCALE
		}

		private static final String LOCALE_COMBO_BOX_LIST_LABEL = "<Select a language>"; //TODO: externalize
		private static final String COMBO_BOX_SEPARATOR =  "----------------------";
		private static final String SELECT_CHARSET_INSTRUCTION = "If you don't know what character set to use, try different ones until the strings below look right";
		
		private boolean validLocale = false;
		private boolean validEncoding = false;
		
		private JScrollPane encodingListPanel;
		
		@Override
		public String getName() {
			return "Choose encoding and language";
		}

		@Override
		public String getDescription() {
			return "To import metadata correctly we need to know what language and character encoding the SPSS file uses.";
					
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
			JPanel encodingPickerPanel = new JPanel(new BorderLayout());
			Label instruction = new Label(SELECT_CHARSET_INSTRUCTION);
			encodingPickerPanel.add(instruction, BorderLayout.NORTH);
			
			encodingPanel.setBorder(BorderFactory.createTitledBorder("Select character encoding"));
			JComboBox<DisplayCharset> encodingComboBox = new JComboBox<>();
			encodingComboBox.addItem(new DisplayCharset(Charsets.UTF_8));
			encodingComboBox.addItem(new DisplayCharset(Charsets.ISO_8859_1));
			if(Charset.isSupported("windows-1252")) {
				encodingComboBox.addItem(new DisplayCharset(Charset.forName("windows-1252")));
			}
			encodingComboBox.addItem(new DisplayCharset(COMBO_BOX_SEPARATOR));
			for(Charset charset: Charset.availableCharsets().values()) {
				if(CharsetTest.charset8bitTest(charset) && CharsetTest.charsetSPSSTest(charset)){
					encodingComboBox.addItem(new DisplayCharset(charset));
				}
			}
			encodingComboBox.setActionCommand(TextCommand.ENCODING.toString());
			encodingComboBox.addActionListener(this);
			encodingPickerPanel.add(encodingComboBox, BorderLayout.SOUTH);
			
			encodingPanel.add(encodingPickerPanel, BorderLayout.NORTH);
			
			encodingListPanel = new JScrollPane();
			encodingListPanel.setBounds(0, 0, 0, -36);
			JPanel tablePanel = new JPanel();
			encodingListPanel.setViewportView(tablePanel);
			encodingPanel.add(encodingListPanel, BorderLayout.CENTER);

			add(encodingPanel, BorderLayout.CENTER);
			encodingComboBox.setSelectedItem(Charsets.UTF_8.name());
			
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
				JComboBox<DisplayCharset> charsetSource = (JComboBox<DisplayCharset>) e.getSource();
				
				Charset charset = ((DisplayCharset) charsetSource.getSelectedItem()).charset;
				
				if(charset != null) {
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
	
	public static class OpenSPSSPanel extends Screen implements ActionListener {
		
		private JTextField fileNameTextField;
		private JTable reviewTable;
		
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
			setLayout(new BorderLayout(0, 0));
			JPanel fileChoosePanel = new JPanel();
			fileNameTextField = new JTextField(30);
			fileNameTextField.setEditable(false);
			JButton chooseButton = new JButton("Select file");
			chooseButton.addActionListener(this);
			fileChoosePanel.add(fileNameTextField);
			fileChoosePanel.add(chooseButton);
			add(fileChoosePanel, BorderLayout.NORTH);
			
			
			reviewTable = new JTable() {
				@Override
				public boolean isCellEditable(int rowIndex, int colIndex) {
					return false; //Disallow the editing of any cell
				}
			};
					
			reviewTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			reviewTable.setFillsViewportHeight(true);
			
			JScrollPane scrollPane = new JScrollPane(reviewTable);
			add(scrollPane, BorderLayout.CENTER);
			
			setProblem("No file selected");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			
			JFileChooser jfc = new JFileChooser(CreateFileWizard.preferences.getWorkingDirectory());
			
			FileFilter filter = new FileNameExtensionFilter("SPSS files", "sav");
			jfc.addChoosableFileFilter(filter);
			jfc.setFileFilter(filter);
			
			int returnVal = jfc.showSaveDialog(this);
			switch(returnVal) {
			case JFileChooser.APPROVE_OPTION:
				File spssFile = jfc.getSelectedFile();
				preferences.setWorkingDirectory(jfc.getCurrentDirectory());
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
						
						TableModel model = spssTable(importSPSSFile);
						reviewTable.setModel(model);
						
					} catch (IOException | SPSSFileException e1) {
						setProblem("Selected file is not a valid SPSS file");
						e1.printStackTrace();
						break;
					}
					data.put("spssFile", spssFile);
					setProblem(null);
				}
				break;
			}
		}
		
		private static TableModel spssTable(SPSSFile sf) throws FileNotFoundException, IOException, SPSSFileException {
			String[] columns = new String[sf.getVariableCount()];
			
			for(int i = 0; i < sf.getVariableCount(); i++){
				SPSSVariable var = sf.getVariable(i);
				columns[i] = var.getShortName();
			}

			Object[][] data =  new Object[sf.getRecordCount()][sf.getVariableCount()];
			Iterator<Object[]> iter = sf.getDataIterator();
			int i = 0;
			while(iter.hasNext()){
				data[i] = iter.next();
				i++;
			}
			
			return new DefaultTableModel(data, columns);
		}
	}

	private EventBus eventBus;
	// static variable access for use by the screen subclasses 
	static GuiTexts texts;
	static DaxplorePreferences preferences;
	
	public CreateFileWizard(JFrame window, EventBus eventBus, GuiTexts texts, DaxplorePreferences preferences) {
		super(new Wizard.Builder("Create new project", OpenSPSSPanel.class, window));
		this.eventBus = eventBus;
		CreateFileWizard.texts = texts;
		CreateFileWizard.preferences = preferences;
		data.put("preferences", preferences);
	}
	
	@Override
	public Class<? extends Screen> getPreviousScreen(Class<? extends Screen> screen) {
		if(OpenSPSSPanel.class.equals(screen)) {
			return null;
		} else if(ChooseEncodingPanel.class.equals(screen)){
			return OpenSPSSPanel.class;
		} else if(CreateFilePanel.class.equals(screen)) {
			return ChooseEncodingPanel.class;
		}
		return null;
	}

	@Override
	public Class<? extends Screen> getNextScreen(Class<? extends Screen> screen) {
		if(OpenSPSSPanel.class.equals(screen)) {
			return ChooseEncodingPanel.class;
		} else if(ChooseEncodingPanel.class.equals(screen)){
			return CreateFilePanel.class;
		} else if(CreateFilePanel.class.equals(screen)) {
			return null;
		}
		return null;
	}

	@Override
	public void finish() {
		try {
			DaxploreFile daxploreFile = DaxploreFile.createWithNewFile((File)data.get("daxploreFile"));
			daxploreFile.importSPSS((File)data.get("spssFile"), (Charset)data.get("charset"));
			daxploreFile.importFromRaw((Locale)data.get("locale"));
			daxploreFile.getAbout().addLocale((Locale)data.get("locale"));
			daxploreFile.saveAll();
			
			eventBus.post(new DaxploreFileUpdateEvent(daxploreFile));
		} catch (DaxploreException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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
