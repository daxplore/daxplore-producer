/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui.view.text;

import java.util.Locale;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextTree;
import org.daxplore.producer.gui.GuiSettings;

@SuppressWarnings("serial")
class TextsTableModel extends DefaultTableModel implements TableModelListener {
	
	TextTree textsList;
	private Locale locale1, locale2;

	TextsTableModel(TextTree textsList, Locale locale1, Locale locale2) {
		this.textsList = textsList;
		this.locale1 = locale1;
		this.locale2 = locale2;
	}

	@Override
	public String getColumnName(int col) {
		switch(col) {
		case 0:
			return "Reference";
		case 1:
			if(locale1 != null) {
				return locale1.getDisplayLanguage(GuiSettings.getProgramLocale());
			}
			return "";
		case 2:
			if(locale2 != null) {
				return locale2.getDisplayLanguage(GuiSettings.getProgramLocale());
			}
			return "";
		default:
			throw new IndexOutOfBoundsException("Column out of bounds: " + col);
		}
	}

	@Override
	public int getRowCount() {
		if(textsList == null) return 0;
		return textsList.size();
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public Object getValueAt(int row, int col) {
		switch (col) {
		case 0:
			return textsList.get(row).getRef();
		case 1:
			return textsList.get(row).get(locale1);
		case 2:
			return textsList.get(row).get(locale2);
		default:
			throw new IndexOutOfBoundsException("Column out of bounds: " + col);
		}
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return col > 0;
	}
	
	public int find(TextReference textref) {
		return textsList.indexOf(textref);
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		switch(col) {
		case 0: 
			break;
		case 1: 
			textsList.get(row).put(value.toString(), locale1);
			break;
		case 2: 
			textsList.get(row).put(value.toString(), locale2);
			break;
		}
	}
	
	public void setLocales(Locale locale1, Locale locale2) {
		this.locale1 = locale1;
		this.locale2 = locale2;
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		// TODO Auto-generated method stub
	}
}
