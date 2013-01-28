package gui.edit;

import gui.MainController;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;

import daxplorelib.DaxploreException;
import daxplorelib.metadata.TextReference;
import daxplorelib.metadata.TextReference.TextReferenceManager;

public class EditTextController {

	private Locale[] currentLocales = new Locale[2];
	private MainController mainController;
	private TableRowSorter<TextsTableModel> sorter;
	private EditTextView editTextView;
	private List<TextReference> textsList;
	private TextsTableModel model;
	private JTable table;
	
	public EditTextController(MainController mainController, EditTextView editTextView) {
		this.mainController = mainController;
		this.editTextView = editTextView;
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
}