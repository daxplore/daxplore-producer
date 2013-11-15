package org.daxplore.producer.gui.edit;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Locale;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.DaxploreFile;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReferenceManager;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextTree;
import org.daxplore.producer.gui.event.DaxploreFileUpdateEvent;
import org.daxplore.producer.gui.event.EmptyEvents.LocaleAddedOrRemovedEvent;
import org.daxplore.producer.gui.event.EmptyEvents.RawImportEvent;
import org.daxplore.producer.gui.resources.GuiTexts;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class EditTextController implements ActionListener, DocumentListener {

	private DaxploreFile daxploreFile;
	
	private Locale[] currentLocales = new Locale[2];
	private TableRowSorter<TextsTableModel> sorter;
	private EditTextView editTextView;
	private TextTree textsList;
	private TextsTableModel model;
	private JTable table;
	
	enum EditTextCommand {
		UPDATE_COLUMN_1, UPDATE_COLUMN_2
	}
	
	public EditTextController(EventBus eventBus, GuiTexts texts) {
		eventBus.register(this);
		
		editTextView = new EditTextView(texts, this);
	}
	
	@Subscribe
	public void on(DaxploreFileUpdateEvent e) {
		this.daxploreFile = e.getDaxploreFile();
		loadData();
	}
	
	@Subscribe
	public void on(LocaleAddedOrRemovedEvent e) {
		loadData();
	}
	
	@Subscribe
	public void on(RawImportEvent e) {
		loadData();
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
		if(daxploreFile != null) {
			try {
				TextReferenceManager trm = daxploreFile.getTextReferenceManager();
				List<Locale> localeList = daxploreFile.getAbout().getLocales();
				if(localeList.size()==0) {
					return;
				}
				editTextView.setLocales(localeList);
				textsList = trm.getAll();
				loadTable();
			} catch (DaxploreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	void loadTable() {
		int scrollPosition = editTextView.getScrollbarPosition();
		
		model = new TextsTableModel(this, textsList);
		sorter = new TableRowSorter<>(model);
		table = new JTable(model);
		table.setRowSorter(sorter);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        table.setFillsViewportHeight(true);
		editTextView.setTable(table);
		
		editTextView.setScrollbarPosition(scrollPosition);
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
		switch(EditTextCommand.valueOf(e.getActionCommand())) {
		case UPDATE_COLUMN_1:
			Locale locale = editTextView.getSelectedLocale1();
			if(locale != null) {
				setCurrentLocale(locale, 0);
				loadTable();
			}
			break;
		case UPDATE_COLUMN_2:
			locale = editTextView.getSelectedLocale2();
			if(locale != null) {
				setCurrentLocale(locale, 1);
				loadTable();
			}
			break;
		default:
			throw new AssertionError("No such action command: '" + e.getActionCommand() + "'");
		}
	}
	
	public Component getView() {
		return editTextView;
	}
	
	// Document listener methods
	@Override
	public void removeUpdate(DocumentEvent e) {
		filter(editTextView.getFilterText());
	}
	
	@Override
	public void insertUpdate(DocumentEvent e) {
		filter(editTextView.getFilterText());
	}
	
	@Override
	public void changedUpdate(DocumentEvent e) {
		filter(editTextView.getFilterText());
	}
}
