package org.daxplore.producer.gui;

import java.awt.Component;
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
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.metadata.MetaGroup;
import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;
import org.daxplore.producer.daxplorelib.metadata.MetaGroup.GroupType;
import org.daxplore.producer.daxplorelib.metadata.MetaGroup.MetaGroupManager;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReferenceManager;
import org.daxplore.producer.gui.resources.GuiTexts;
import org.daxplore.producer.gui.utility.DisplayLocale;
import org.daxplore.producer.gui.view.build.EditGroupTextPanel;

public class Dialogs {
	
	public static boolean editGroupDialog(Component parent, TextReferenceManager textManager, GuiTexts texts, List<Locale> locales, MetaGroup metaGroup) {
		TextReference textRef = metaGroup.getTextRef();
		JButton[] buttons = getOkCancelOptions(texts);
		
		EditGroupTextPanel editor = new EditGroupTextPanel(texts, buttons[0], locales, textRef);
		int answer = JOptionPane.showOptionDialog(parent,
				editor,
				"Edit group",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE,
				null,
				buttons,
				buttons[0]);
		
		if(answer == 0) { // 0 == index of okayButton in buttons array
			String textRefId = editor.getNewTextRefId(); 
			
			Map<Locale, String> newTexts = editor.getNewTexts();
			for(Locale l : newTexts.keySet()) {
				textRef.put(newTexts.get(l), l);
			}
			
			if(!textRefId.equals(textRef.getRef()) && TextReferenceManager.isValidTextRefId(textRefId)) {
				try {
					TextReference newTextRef = textManager.get(textRefId);
					for(Locale l : textRef.getLocales()) {
						newTextRef.put(textRef.get(l), l);
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
	
	public static MetaGroup createGroupDialog(Component parent, TextReferenceManager textManager, MetaGroupManager metaGroupManager, GuiTexts texts, List<Locale> locales) {
		int nextid;
		try {
			nextid = metaGroupManager.getHighestId();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
		
		JButton[] buttons = getOkCancelOptions(texts);

		EditGroupTextPanel editor = new EditGroupTextPanel(texts, buttons[0], locales, "group_" + nextid);
		
		int answer = JOptionPane.showOptionDialog(parent,
				editor,
				"Create new group",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE,
				null,
				buttons,
				buttons[0]);
		
		if(answer == 0) { // 0 == index of okayButton in buttons array
			String textRefId = editor.getNewTextRefId();
			try {
				TextReference textRef = textManager.get(textRefId);
				Map<Locale, String> newTexts = editor.getNewTexts();
				for(Locale l : newTexts.keySet()) {
					textRef.put(newTexts.get(l), l);
				}
				MetaGroup metaGroup = metaGroupManager.create(textRef, Integer.MAX_VALUE, GroupType.QUESTIONS, new LinkedList<MetaQuestion>());
				
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
	private static JButton[] getOkCancelOptions(GuiTexts texts) {
		final JButton okButton = new JButton(texts.get("dialog.options.ok"));
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane pane = getOptionPane((JComponent)e.getSource());
                pane.setValue(okButton);
			}
		});
		final JButton cancelButton = new JButton(texts.get("dialog.options.cancel"));
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
			return fc.getSelectedFile();
		default:
			return null;
		}
	}
	
	public static FileLocalePair showImportDialog(Component parent, List<Locale> localeList, DaxplorePreferences preferences) {
		LocalizationFileChooser ifc = new LocalizationFileChooser(localeList, preferences);
		
		FileFilter filter = new FileNameExtensionFilter("language files", "csv", "properties");
		ifc.addChoosableFileFilter(filter);
		ifc.setFileFilter(filter);
		
		int returnVal = ifc.showOpenDialog(parent);
		switch(returnVal) {
		case JFileChooser.APPROVE_OPTION:
			preferences.setWorkingDirectory(ifc.getCurrentDirectory());
			return new FileLocalePair(ifc.getSelectedFile(), ifc.getSelectedLocale());
		default:
			return null;
		}
	}
	
	public static FileLocalePair showExportDialog(Component parent, List<Locale> localeList, DaxplorePreferences preferences) {
		LocalizationFileChooser efc = new LocalizationFileChooser(localeList, preferences);
		int returnVal = efc.showSaveDialog(parent);
		switch(returnVal) {
		case JFileChooser.APPROVE_OPTION:
			preferences.setWorkingDirectory(efc.getCurrentDirectory());
			return new FileLocalePair(efc.getSelectedFile(), efc.getSelectedLocale());
		default:
			return null;
		}
	}
	
	public static class FileLocalePair {
		public final File file;
		public final Locale locale;
		public FileLocalePair(File file, Locale locale) {
			this.file = file;
			this.locale = locale;
		}
	}
	
	@SuppressWarnings("serial")
	private static class LocalizationFileChooser extends JFileChooser {
		private JComboBox<DisplayLocale> localeBox;
		private Locale selectedLocale;
		
		public LocalizationFileChooser(List<Locale> localeList, DaxplorePreferences preferences) {
			super(preferences.getWorkingDirectory());
			localeBox = new JComboBox<>();
			localeBox.addItem(null);
			for(Locale loc: localeList) {
				localeBox.addItem(new DisplayLocale(loc));
			}
			setAccessory(localeBox);
		}
		
		@Override
		public void approveSelection() {
			if(localeBox.getSelectedItem() != null) {
				selectedLocale = ((DisplayLocale)localeBox.getSelectedItem()).locale;
				super.approveSelection();
			} else {
				System.out.println("No locale selected during import");
			}
		}
		
		public Locale getSelectedLocale() {
			return selectedLocale;
		}
	}
	
}
