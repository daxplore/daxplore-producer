package gui.edit;

import gui.MainController;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;

import daxplorelib.DaxploreException;
import daxplorelib.metadata.MetaData.L10nFormat;
import daxplorelib.metadata.TextReference;
import daxplorelib.metadata.TextReference.TextReferenceManager;

public class EditTextController implements ActionListener {

	private Locale[] currentLocales = new Locale[2];
	private MainController mainController;
	private TableRowSorter<TextsTableModel> sorter;
	private EditTextView editTextView;
	private List<TextReference> textsList;
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
	 * @param i = 0 | 1 for first and secound columns
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
				List<Locale> localeList = trm.getAllLocales();
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
		sorter = new TableRowSorter<TextsTableModel>(model);
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
			localeList = new LinkedList<Locale>();
			localeList.add(new Locale("sv"));
			localeList.add(new Locale("en")); //TODO
			file = editToolbar.showImportDialog(localeList);
			if(file != null && file.exists() && file.canRead()) {
				Locale locale = editToolbar.getSelectedLocale();
				try {
					mainController.getDaxploreFile().getMetaData().importL10n(
							Files.newBufferedReader(file.toPath(), Charset.forName("UTF-8")), L10nFormat.PROPERTIES, locale);
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
				
				BufferedWriter writer;
				Charset cs = Charset.forName("UTF-8");
				if(file == null) {
					System.out.println("File is null");
					return;
				} else if(file.exists() && file.canWrite()) {
					writer = Files.newBufferedWriter(file.toPath(), cs, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
				} else if (!file.exists()) {
					writer = Files.newBufferedWriter(file.toPath(), cs, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
				} else {
					System.out.println("Files is write protected");
					return;
				}
				Locale locale = editToolbar.getSelectedLocale();
				mainController.getDaxploreFile().getMetaData().exportL10n(writer, L10nFormat.PROPERTIES, locale);

			} catch (DaxploreException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			break;
		}
	}
	
	public EditToolbarView getToolbar() {
		return editToolbar;
	}
}
