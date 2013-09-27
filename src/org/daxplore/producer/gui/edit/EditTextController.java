package org.daxplore.producer.gui.edit;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.metadata.MetaData.L10nFormat;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReferenceManager;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextTree;
import org.daxplore.producer.gui.MainController;
import org.daxplore.producer.gui.Settings;

import com.google.common.base.Charsets;

public class EditTextController implements ActionListener {

	private Locale[] currentLocales = new Locale[2];
	private MainController mainController;
	private TableRowSorter<TextsTableModel> sorter;
	private EditTextView editTextView;
	private TextTree textsList;
	private TextsTableModel model;
	private JTable table;
	private EditToolbarView editToolbar;
	
	public EditTextController(MainController mainController, EditTextView editTextView) {
		this.mainController = mainController;
		this.editTextView = editTextView;
		editToolbar = new EditToolbarView(this);
	}
	
	/**
	 * current locales has two locales for the two columns in text edit field
	 * @param i = 0 | 1 for first and second columns
	 * @return a Locale or null
	 */
	Locale getCurrentLocale(int i) {
		if(i < currentLocales.length) {
			return currentLocales[i];
		}
		return null;
	}
	
	void setCurrentLocale(Locale locale, int i) {
		if(i < currentLocales.length) {
			currentLocales[i] = locale;
		}
	}
	
	public void loadData() {
		System.out.println("EditPanelView.updateStuff()");
		if(mainController.fileIsSet()) {
			try {
				TextReferenceManager trm = mainController.getDaxploreFile().getMetaData().getTextsManager();
				List<Locale> localeList = mainController.getDaxploreFile().getAbout().getLocales();
				if(localeList.size()==0) {
					return;
				}
				editTextView.setLocales(localeList);
				textsList = trm.getAll();
				loadTable();
			} catch (DaxploreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	void loadTable() {
		model = new TextsTableModel(this, textsList);
		sorter = new TableRowSorter<>(model);
		table = new JTable(model);
		table.setRowSorter(sorter);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        table.setFillsViewportHeight(true);
		editTextView.setTable(table);
	}
	
	void filter(String text) {
        RowFilter<TextsTableModel, Object> rf = null;
        //If current expression doesn't parse, don't update.
        try {
        	String caseInsensitive = "(?i)";
            rf = RowFilter.regexFilter(caseInsensitive + text, 0);
        } catch (java.util.regex.PatternSyntaxException e) {
            return;
        }
        sorter.setRowFilter(rf);
	}
	
	public void jumpToTextReference(TextReference textref) {
		editTextView.setSearchField("");
		int line = model.find(textref);
		Rectangle r = table.getCellRect(line, 0, true);
		table.scrollRectToVisible(r);
		table.setRowSelectionInterval(line, line);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		List<Locale> localeList;
		File file;
		switch(e.getActionCommand()) {
		case "import":
			localeList = Settings.availableLocales();
			file = editToolbar.showImportDialog(localeList);
			if(file != null && file.exists() && file.canRead()) {
				Locale locale = editToolbar.getSelectedLocale();
				
				L10nFormat format;
				String filename = file.toPath().getFileName().toString().toLowerCase();
				//TODO allow different suffixes and user selection of type?
				if(filename.endsWith(".csv")) {
					format = L10nFormat.CSV;
				} else if(filename.endsWith(".properties")) {
					format = L10nFormat.PROPERTIES;
				} else {
					System.out.println("Unsupported file suffix: " + filename); //TODO communicate error properly
					return;
				}
				
				mainController.getDaxploreFile().getAbout().addLocale(locale);
				try {
					mainController.getDaxploreFile().getMetaData().importL10n(
							Files.newBufferedReader(file.toPath(), Charsets.UTF_8), format, locale);
				} catch (FileNotFoundException e1) {
					throw new AssertionError("File exists but is not found");
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (DaxploreException e1) {
					e1.printStackTrace();
				}
			}
			break;
		case "export":
			try {
				
				localeList = mainController.getDaxploreFile().getMetaData().getAllLocales();
				file = editToolbar.showExportDialog(localeList);
				if(file == null) {
					System.out.println("File is null");
					return; //TODO communicate error properly
				} 
				
				L10nFormat format;
				String filename = file.toPath().getFileName().toString().toLowerCase();
				//TODO allow different suffixes and user selection of type?
				if(filename.endsWith(".csv")) {
					format = L10nFormat.CSV;
				} else if(filename.endsWith(".properties")) {
					format = L10nFormat.PROPERTIES;
				} else {
					System.out.println("Unsupported file suffix: " + filename);
					return; //TODO communicate error properly
				}
				Locale locale = editToolbar.getSelectedLocale();
				
				if(file.exists() && file.canWrite()) {
					try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), Charsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
						mainController.getDaxploreFile().getMetaData().exportL10n(writer, format, locale);
					}
				} else if (!file.exists()) {
					try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), Charsets.UTF_8, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
						mainController.getDaxploreFile().getMetaData().exportL10n(writer, format, locale);
					}
				} else {
					System.out.println("File is write protected");
					return; //TODO communicate error properly
				}

			} catch (DaxploreException | IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			break;
		default:
			throw new AssertionError("No such action command: '" + e.getActionCommand() + "'");
		}
	}
	
	public EditToolbarView getToolbar() {
		return editToolbar;
	}
}
