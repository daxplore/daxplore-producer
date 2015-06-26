/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui.view.text;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import org.daxplore.producer.gui.menu.ActionManager;
import org.daxplore.producer.gui.resources.GuiTexts;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class EditTextController implements ActionListener, DocumentListener {

	private EventBus eventBus;
	private DaxploreFile daxploreFile;
	
	private Locale[] currentLocales = new Locale[2];
	private TableRowSorter<TextsTableModel> sorter;
	private EditTextView editTextView;
	private TextTree textsList;
	private TextsTableModel model;
	
	enum EditTextCommand {
		UPDATE_COLUMN_1, UPDATE_COLUMN_2
	}
	
	public EditTextController(EventBus eventBus, GuiTexts texts, ActionManager actionManager) {
		this.eventBus = eventBus;
		eventBus.register(this);
		
		editTextView = new EditTextView(texts, actionManager, this);
	}
	
	@Subscribe
	public void on(DaxploreFileUpdateEvent e) {
		try {
			this.daxploreFile = e.getDaxploreFile();
			loadData();
		} catch (Exception ex) {
			Logger.getGlobal().log(Level.SEVERE, "Failed to handle event", ex);
		}
	}
	
	@Subscribe
	public void on(LocaleAddedOrRemovedEvent e) {
		try {
			loadData();
		} catch (Exception ex) {
			Logger.getGlobal().log(Level.SEVERE, "Failed to handle event", ex);
		}
	}
	
	@Subscribe
	public void on(RawImportEvent e) {
		try {
			loadData();
		} catch (Exception ex) {
			Logger.getGlobal().log(Level.SEVERE, "Failed to handle event", ex);
		}
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
		if(model != null) {
			model.setLocales(currentLocales[0], currentLocales[1]);
		}
	}
	
	public void loadData() {
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

			SelectedLocalesTableModel localeTableModel = new SelectedLocalesTableModel(eventBus, daxploreFile.getAbout());
			editTextView.setLocaleTable(localeTableModel);
		}
	}
	
	void loadTable() {
		int scrollPosition = editTextView.getScrollbarPosition();
		
		model = new TextsTableModel(textsList, currentLocales[0], currentLocales[1]);
		sorter = new TableRowSorter<>(model);
		editTextView.setTextTable(model);
		editTextView.setTextTableSorter(sorter);
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
		editTextView.jumpToLine(line);
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
