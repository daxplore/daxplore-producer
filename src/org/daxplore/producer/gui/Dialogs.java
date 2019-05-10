/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.DaxploreFile;
import org.daxplore.producer.daxplorelib.metadata.MetaGroup;
import org.daxplore.producer.daxplorelib.metadata.MetaGroup.GroupType;
import org.daxplore.producer.daxplorelib.metadata.MetaGroup.MetaGroupManager;
import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReferenceManager;
import org.daxplore.producer.daxplorelib.resources.DaxploreProperties;
import org.daxplore.producer.gui.resources.UITexts;
import org.daxplore.producer.gui.utility.DisplayLocale;

public class Dialogs {
	
	public static boolean confirmOverwrite(Component parent, String fileName) {
		String[] options = {UITexts.get("dialog.options.overwrite"), UITexts.get("dialog.options.cancel")};
		int answer = JOptionPane.showOptionDialog(parent,
				UITexts.format("dialog.overwrite.question", fileName),
				UITexts.get("dialog.overwrite.title"),
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				options,
				options[1]);

		return answer == 0; // 0 == index of overwrite in array
	}
	
	/**
	 * Show an dialog to the user, asking if they want the .daxplore save file to be upgraded.
	 * 
	 * Used when opening a {@link DaxploreFile} with an old file version. The upgrade is instantly written to the file on disk,
	 * so need to be properly confirmed by the user.
	 *  
	 * @param parent is the the GUI element to show this dialog in relation to
	 * @param fileName is the name of the file that will upgrade
	 * @param fileVersion is the pre-upgrade .daxplore file version
	 * @return true if the file should be upgraded
	 */
	public static boolean confirmDaxploreFileUpgrade(Component parent, String fileName, int fileVersion) {
		String[] options = {UITexts.get("dialog.options.upgrade"), UITexts.get("dialog.options.cancel")};
		int answer = JOptionPane.showOptionDialog(parent,
				UITexts.format("dialog.upgrade.question", fileName, fileVersion, DaxploreProperties.daxploreFileVersion),
				UITexts.get("dialog.upgrade.title"),
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				options,
				options[1]);
		return answer == 0; // 0 is the index of "upgrade" in array
	}
	
	public static boolean editTextRefDialog(Component parent, List<Locale> locales, TextReference textRef) {
		JButton[] buttons = getOkCancelOptions();
		
		DialogsPanels editor = DialogsPanels.textRefEditPanel(buttons[0], locales, textRef, false);
		int answer = JOptionPane.showOptionDialog(parent,
				editor,
				"Edit texts",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE,
				null,
				buttons,
				null);
		
		if(answer == 0) { // 0 == index of okButton in buttons array
			
			Map<Locale, String> newTexts = editor.getNewTexts();
			for(Locale l : newTexts.keySet()) {
				textRef.put(newTexts.get(l), l);
			}
			
			return true;
		}
		return false;
	}
	
	public static boolean editGroupDialog(Component parent, TextReferenceManager textManager, List<Locale> locales, MetaGroup metaGroup) {
		TextReference textRef = metaGroup.getTextRef();
		JButton[] buttons = getOkCancelOptions();
		
		DialogsPanels editor = DialogsPanels.editGroupPanel(buttons[0], locales, textRef);
		int answer = JOptionPane.showOptionDialog(parent,
				editor,
				"Edit group",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE,
				null,
				buttons,
				null);
		
		if(answer == 0) { // 0 == index of okButton in buttons array
			String textRefId = editor.getNewTextRefId(); 
			
			Map<Locale, String> newTexts = editor.getNewTexts();
			for(Locale l : newTexts.keySet()) {
				textRef.put(newTexts.get(l), l);
			}
			
			if(!textRefId.equals(textRef.getRef()) && DaxploreFile.isValidColumnName(textRefId)) {
				try {
					TextReference newTextRef = textManager.get(textRefId);
					for(Locale l : textRef.getLocales()) {
						newTextRef.put(textRef.getText(l), l);
					}
					metaGroup.setTextRef(newTextRef);
					textManager.remove(textRef);
				} catch (DaxploreException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			return true;
		}
		return false;
	}
	
	public static MetaGroup createGroupDialog(Component parent, TextReferenceManager textManager, MetaGroupManager metaGroupManager, List<Locale> locales) {
		
		JButton[] buttons = getOkCancelOptions();

		DialogsPanels editor = DialogsPanels.createGroupPanel(metaGroupManager, buttons[0], locales);
		
		int answer = JOptionPane.showOptionDialog(parent,
				editor,
				"Create new group",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE,
				null,
				buttons,
				null);
		
		if(answer == 0) { // 0 == index of okButton in buttons array
			String textRefId = editor.getNewTextRefId();
			try {
				TextReference textRef = textManager.get(textRefId);
				Map<Locale, String> newTexts = editor.getNewTexts();
				for(Locale l : newTexts.keySet()) {
					textRef.put(newTexts.get(l), l);
				}
				GroupType type = editor.isHeaderGroup() ? GroupType.HEADER : GroupType.QUESTIONS;
				MetaGroup metaGroup = metaGroupManager.create(textRef, Integer.MAX_VALUE, type, new LinkedList<MetaQuestion>());
				
				return metaGroup;
			} catch (DaxploreException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return null;
	}
	
	/**
	 * Creates a JButton[] with ok and cancel options.
	 * Ok has index 0
	 * Cancel has index 1
	 * @return a JButton[]
	 */
	private static JButton[] getOkCancelOptions() {
		final JButton okButton = new JButton(UITexts.get("dialog.options.ok"));
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane pane = getOptionPane((JComponent)e.getSource());
                pane.setValue(okButton);
			}
		});
		final JButton cancelButton = new JButton(UITexts.get("dialog.options.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane pane = getOptionPane((JComponent)e.getSource());
                pane.setValue(cancelButton);
			}
		});
		JButton[] buttons = {okButton, cancelButton};
		return buttons;
	}
	
	private static JOptionPane getOptionPane(JComponent parent) {
		if (!(parent instanceof JOptionPane)) {
			return getOptionPane((JComponent)parent.getParent());
		}
		return (JOptionPane)parent;
		
	}
	
	public static File showExportDialog(Component parent, DaxplorePreferences preferences) {
		JFileChooser fc = new JFileChooser(preferences.getWorkingDirectory());
		
		FileFilter filter = new FileNameExtensionFilter("Zip files", "zip");
		fc.addChoosableFileFilter(filter);
		fc.setFileFilter(filter);
		
		int returnVal = fc.showSaveDialog(parent);
		switch(returnVal) {
		case JFileChooser.APPROVE_OPTION:
			preferences.setWorkingDirectory(fc.getCurrentDirectory());
			File fileToCreate = fc.getSelectedFile();
			String filename = fileToCreate.toString();
			if(!filename.toLowerCase().endsWith(".zip")) {
				filename += ".zip";
				fileToCreate = new File(filename);
			}
			return fileToCreate;
		default:
			return null;
		}
	}
	
	public static ImportTextsDialogResult showImportTextsDialog(DaxplorePreferences preferences, Component parent, List<Locale> localeList) {
		TextImportPanel importPanel = new TextImportPanel(localeList);
		JButton[] buttons = getOkCancelOptions();
		int returnVal = JOptionPane.showOptionDialog(parent,
				importPanel,
				"Import texts",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE,
				null,
				buttons,
				buttons[0]);
		if(returnVal != JOptionPane.OK_OPTION) {
			return null;
		}
		JFileChooser fc = new JFileChooser(preferences.getWorkingDirectory());
		FileFilter filter = new FileNameExtensionFilter("language files", "csv", "properties");
		fc.addChoosableFileFilter(filter);
		fc.setFileFilter(filter);
		
		returnVal = fc.showOpenDialog(parent);
		switch(returnVal) {
		case JFileChooser.APPROVE_OPTION:
			preferences.setWorkingDirectory(fc.getCurrentDirectory());
			return new ImportTextsDialogResult(fc.getSelectedFile(), importPanel.getImportLocale());
		default:
			return null;
		}
	}
	
	public static ExportDialogResult showExportDialog(DaxplorePreferences preferences, Component parent, List<Locale> localeList) {
		TextExportPanel exportPanel = new TextExportPanel(localeList);
		JButton[] buttons = getOkCancelOptions();
		int returnVal = JOptionPane.showOptionDialog(parent,
				exportPanel,
				"Export texts",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE,
				null,
				buttons,
				buttons[0]);
		if(returnVal != JOptionPane.OK_OPTION) {
			return null;
		}
		
		JFileChooser fc = new JFileChooser(preferences.getWorkingDirectory());
		returnVal = fc.showSaveDialog(parent);
		switch(returnVal) {
		case JFileChooser.APPROVE_OPTION:
			preferences.setWorkingDirectory(fc.getCurrentDirectory());
			return new ExportDialogResult(fc.getSelectedFile(), exportPanel.getExportLocale(), exportPanel.isOnlyExportUsed());
		default:
			return null;
		}
	}

	public static class ImportTextsDialogResult {
		public final File file;
		public final Locale locale;
		public ImportTextsDialogResult(File file, Locale locale) {
			this.file = file;
			this.locale = locale;
		}
	}
	
	public static class ExportDialogResult {
		public final File file;
		public final Locale locale;
		public final boolean onlyExportUsed;
		public ExportDialogResult(File file, Locale locale, boolean onlyExportUsed) {
			this.file = file;
			this.locale = locale;
			this.onlyExportUsed = onlyExportUsed;
		}
	}
	
	@SuppressWarnings("serial")
	private static class TextExportPanel extends JPanel {
		private JComboBox<DisplayLocale> localeBox;
		private JComboBox<String> filterBox;
		
		public TextExportPanel(List<Locale> localeList) {
			setLayout(new GridLayout(0,2));
			
			add(new Label("Locale "), 0);
			
			localeBox = new JComboBox<>();
			localeBox.addItem(null);
			for(Locale loc: localeList) {
				localeBox.addItem(new DisplayLocale(loc));
			}
			
			add(localeBox, 1);
			
			add(new Label("Export "), 2);
			
			filterBox = new JComboBox<>();
			filterBox.addItem("Only export used texts");
			filterBox.addItem("Export all texts");
			
			add(filterBox, 3);
		}
		
		public Locale getExportLocale() {
			return ((DisplayLocale)(localeBox.getSelectedItem())).locale;
		}
		
		public boolean isOnlyExportUsed() {
			return filterBox.getSelectedIndex() == 0;
		}
	}
	
	@SuppressWarnings("serial")
	private static class TextImportPanel extends JPanel {
		private JComboBox<DisplayLocale> localeBox;

		public TextImportPanel(List<Locale> localeList) {
			setLayout(new GridLayout(0, 2));

			add(new Label("Locale "), 0);

			localeBox = new JComboBox<>();
			localeBox.addItem(null);
			for (Locale loc : localeList) {
				localeBox.addItem(new DisplayLocale(loc));
			}

			add(localeBox, 1);
		}

		public Locale getImportLocale() {
			return ((DisplayLocale) (localeBox.getSelectedItem())).locale;
		}
	}
}
