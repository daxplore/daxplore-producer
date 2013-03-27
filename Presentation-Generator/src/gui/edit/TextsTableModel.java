package gui.edit;

import java.util.Collections;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import daxplorelib.metadata.textreference.TextReference;
import daxplorelib.metadata.textreference.TextTree;

@SuppressWarnings("serial")
class TextsTableModel extends DefaultTableModel implements TableModelListener {
	
	private final EditTextController controller;
	TextTree textsList;

	TextsTableModel(EditTextController controller, TextTree textsList) {
		this.textsList = textsList;
		this.controller = controller;
	}

	public String getColumnName(int col) {
		switch(col) {
		case 0:
			return "Reference";
		case 1:
			return controller.getCurrentLocale(0).getDisplayLanguage();
		case 2:
			return controller.getCurrentLocale(1).getDisplayLanguage();
		}
		throw new AssertionError();
	}

	public int getRowCount() {
		if(textsList == null) return 0;
		return textsList.size();
	}

	public int getColumnCount() {
		return 3;
	}

	public Object getValueAt(int row, int col) {
		switch (col) {
		case 0:
			return textsList.get(row).getRef();
		case 1:
			return textsList.get(row).get(controller.getCurrentLocale(0));
		case 2:
			return textsList.get(row).get(controller.getCurrentLocale(1));
		}
		throw new AssertionError();
	}

	public boolean isCellEditable(int row, int col) {
		return col > 0;
	}
	
	public int find(TextReference textref) {
		return textsList.indexOf(textref);
	}

	public void setValueAt(Object value, int row, int col) {
		textsList.get(row).put(value.toString(), controller.getCurrentLocale(col-1));
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		// TODO Auto-generated method stub
	}
}