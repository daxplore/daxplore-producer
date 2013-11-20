package org.daxplore.producer.gui.view.text;

import java.util.Locale;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextTree;

@SuppressWarnings("serial")
class TextsTableModel extends DefaultTableModel implements TableModelListener {
	
	private final EditTextController controller;
	TextTree textsList;

	TextsTableModel(EditTextController controller, TextTree textsList) {
		this.textsList = textsList;
		this.controller = controller;
	}

	@Override
	public String getColumnName(int col) {
		switch(col) {
		case 0:
			return "Reference";
		case 1:
		case 2:
			Locale locale = controller.getCurrentLocale(col-1);
			if(locale != null) {
				return locale.getDisplayLanguage(Locale.ENGLISH);
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
			return textsList.get(row).get(controller.getCurrentLocale(0));
		case 2:
			return textsList.get(row).get(controller.getCurrentLocale(1));
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
		textsList.get(row).put(value.toString(), controller.getCurrentLocale(col-1));
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		// TODO Auto-generated method stub
	}
}