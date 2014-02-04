/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
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
import org.daxplore.producer.tools.MyTools;
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
			return texts.get("wizard.createfile.name");
		}

		@Override
		public String getDescription() {
			return texts.get("wizard.createfile.description");
		}

		@Override
		public void initUI() {
			setCanFinish(false);
			fileNameTextField = new JTextField(30);
			fileNameTextField.setEditable(false);
			JButton chooseButton = new JButton(texts.get("wizard.createfile.selectfile"));
			chooseButton.addActionListener(this);
			add(fileNameTextField);
			add(chooseButton);
			setProblem(texts.get("wizard.createfile.nofile"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser jfc = new JFileChooser(CreateFileWizard.preferences.getWorkingDirectory());
			
			FileFilter filter = new FileNameExtensionFilter(
					texts.get("general.filetype.daxplorefile"), "daxplore");
			jfc.addChoosableFileFilter(filter);
			jfc.setFileFilter(filter);
			
			int returnVal = jfc.showSaveDialog(this);
			
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				preferences.setWorkingDirectory(jfc.getCurrentDirectory());
				setCanFinish(false);
				setProblem(texts.get("wizard.createfile.nofile"));
				File fileToCreate = jfc.getSelectedFile();
				
				String filename = fileToCreate.toString();
				if(!filename.endsWith(".daxplore")) {
					filename += ".daxplore";
					fileToCreate = new File(filename);
				}
				
				if(fileToCreate.exists()) {
					int selectedOption = JOptionPane.showConfirmDialog(this,
							texts.format("dialog.overwrite.question", fileToCreate.getName()),
							texts.get("dialog.overwrite.title"),
							JOptionPane.OK_CANCEL_OPTION);
					if(selectedOption != JOptionPane.OK_OPTION) {
						return;
					}
				}
				
				try {
					fileNameTextField.setText(fileToCreate.getCanonicalPath());
				} catch (IOException e1) {
					fileNameTextField.setText(fileToCreate.getAbsolutePath());
				}
				
				if(fileToCreate.isDirectory()) {
					setProblem(texts.get("wizard.createfile.directory"));
					return;
				}
				
				if(!fileToCreate.exists() && !canCreate(fileToCreate)) {
					setProblem(texts.get("wizard.createfile.cantcreate "));
					return;
				}
				
				setProblem(null);
				data.put("daxploreFile", fileToCreate);
				setCanFinish(true);
			}
		}
		
	}
	
	public static class ChooseEncodingPanel extends Screen implements ActionListener {

		private enum TextCommand {
			ENCODING, LOCALE
		}

		private boolean validLocale = false;
		private boolean validEncoding = false;
		
		private JScrollPane encodingListPanel;
		
		@Override
		public String getName() {
			return texts.get("wizard.textimport.name");
		}

		@Override
		public String getDescription() {
			return texts.get("wizard.textimport.description");
		}
		
		@Override
		public void initUI() {
			setLayout(new BorderLayout(0, 0));
			
			JPanel localePanel = new JPanel();
			localePanel.setBorder(BorderFactory.createTitledBorder(texts.get("wizard.textimport.languageborder")));
			JComboBox<DisplayLocale> localeComboBox = new JComboBox<>();
			localePanel.add(localeComboBox);
			//TODO better locale handling
			localeComboBox.addItem(new DisplayLocale(texts.get("wizard.textimport.selectlanguage")));
			localeComboBox.addItem(new DisplayLocale(new Locale("sv")));
			localeComboBox.addItem(new DisplayLocale(Locale.ENGLISH));
			localeComboBox.setActionCommand(TextCommand.LOCALE.toString());
			localeComboBox.addActionListener(this);
			add(localePanel, BorderLayout.NORTH);
			
			
			JPanel encodingPanel = new JPanel(new BorderLayout());
			JPanel encodingPickerPanel = new JPanel(new BorderLayout());
			JLabel instruction = new JLabel(texts.get("wizard.textimport.encodetext"));
			encodingPickerPanel.add(instruction, BorderLayout.NORTH);
			
			encodingPanel.setBorder(BorderFactory.createTitledBorder(texts.get("wizard.textimport.encodingborder")));
			JComboBox<DisplayCharset> encodingComboBox = new JComboBox<>();
			encodingComboBox.addItem(new DisplayCharset(Charsets.UTF_8));
			encodingComboBox.addItem(new DisplayCharset(Charsets.ISO_8859_1));
			if(Charset.isSupported("windows-1252")) {
				encodingComboBox.addItem(new DisplayCharset(Charset.forName("windows-1252")));
			}
			encodingComboBox.addItem(new DisplayCharset(texts.get("wizard.textimport.comboseparator")));
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
			
			actionPerformed(new ActionEvent(encodingComboBox, ActionEvent.ACTION_PERFORMED, TextCommand.ENCODING.toString()));
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
						setProblem(texts.get("wizard.textimport.badencoding"));
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
				setProblem(texts.get("wizard.textimport.selectlanguage"));
			} else if(!validEncoding) {
				setProblem(texts.get("wizard.textimport.selectencoding"));
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
			return texts.get("wizard.opendata.name");
		}

		@Override
		public String getDescription() {
			return texts.get("wizard.opendata.description");
		}

		@Override
		public void initUI() {
			setLayout(new BorderLayout(0, 0));
			JPanel fileChoosePanel = new JPanel();
			fileNameTextField = new JTextField(30);
			fileNameTextField.setEditable(false);
			JButton chooseButton = new JButton(texts.get("wizard.opendata.selectfile"));
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
			
			setProblem(texts.get("wizard.opendata.nofile"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			
			JFileChooser jfc = new JFileChooser(CreateFileWizard.preferences.getWorkingDirectory());
			
			FileFilter filter = new FileNameExtensionFilter(texts.get("wizard.opendata.spssfilter"), "sav");
			jfc.addChoosableFileFilter(filter);
			jfc.setFileFilter(filter);
			
			int returnVal = jfc.showOpenDialog(this);
			switch(returnVal) {
			case JFileChooser.APPROVE_OPTION:
				setProblem(null);
				File spssFile = jfc.getSelectedFile();
				preferences.setWorkingDirectory(jfc.getCurrentDirectory());
				try {
					fileNameTextField.setText(spssFile.getCanonicalPath());
				} catch (IOException e1) {
					fileNameTextField.setText(spssFile.getAbsolutePath());
				}
				if(spssFile.isDirectory()) {
					setProblem(texts.get("wizard.opendata.directory"));
				} else if(!spssFile.exists()) {
					setProblem(texts.get("wizard.opendata.filenotexist"));
				} else if(!spssFile.canRead()) {
					setProblem(texts.get("wizard.opendata.cantread"));
				} else {
					try (SPSSFile importSPSSFile = new SPSSFile(spssFile, Charsets.UTF_8)) {
						importSPSSFile.logFlag = false;
						importSPSSFile.loadMetadata();
						
						TableModel model = spssTable(importSPSSFile);
						reviewTable.setModel(model);
						
					} catch (IOException | SPSSFileException e1) {
						setProblem(texts.get("wizard.opendata.notvalidspss"));
						e1.printStackTrace();
						break;
					}
					data.put("spssFile", spssFile);
				}
				break;
			default:
				break;
			}
		}
		
		private TableModel spssTable(SPSSFile sf) throws FileNotFoundException, IOException, SPSSFileException {
			String[] columns = new String[sf.getVariableCount()];
			List<String> problemVars = new LinkedList<>();
			for(int i = 0; i < sf.getVariableCount(); i++){
				SPSSVariable var = sf.getVariable(i);
				if(!DaxploreFile.isValidColumnName(var.getName())) {
					problemVars.add(var.getName());
					//setProblem("\""+ var.getName() + "\" is not an allowed variable name");
				}
				columns[i] = var.getName();
			}

			Object[][] data =  new Object[sf.getRecordCount()][sf.getVariableCount()];
			Iterator<Object[]> iter = sf.getDataIterator();
			int i = 0;
			while(iter.hasNext()){
				data[i] = iter.next();
				i++;
			}
			
			if(!problemVars.isEmpty()) {
				if(problemVars.size() == 1) {
					setProblem(texts.format("wizard.opendata.illegalvariable", problemVars.get(0)));
				} else {
					setProblem(texts.format("wizard.opendata.illegalvariables", MyTools.join(problemVars, "', '")));
				}
			}
			
			return new DefaultTableModel(data, columns);
		}
	}

	private EventBus eventBus;
	private JFrame window;
	// static variable access for use by the screen subclasses 
	static GuiTexts texts;
	static DaxplorePreferences preferences;
	
	public CreateFileWizard(JFrame window, EventBus eventBus, GuiTexts texts, DaxplorePreferences preferences) {
		super(new Wizard.Builder(texts.get("wizard.general.title"), OpenSPSSPanel.class, window));
		this.eventBus = eventBus;
		this.window = window;
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
			JOptionPane.showMessageDialog(window, e.getMessage());
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
