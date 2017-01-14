/**
 * 
 */
package org.daxplore.producer.gui.widget;

import java.util.SortedMap;

import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
public class ColumnTableModel extends DefaultTableModel {
	SortedMap<Object, Integer> columnValueList;
	
	public ColumnTableModel(SortedMap<Object, Integer> columnValueList) {
		this.columnValueList = columnValueList;
	}
	
	public void setValues(SortedMap<Object, Integer> columnValueList) {
		this.columnValueList = columnValueList;
		fireTableDataChanged();
	}
	
	@Override
	public int getRowCount() {
		if(columnValueList == null) return 0;
		return columnValueList.size();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public String getColumnName(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return "Value";
		case 1:
			return "Count";
		default:
			throw new AssertionError("Invalid column");
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			//TODO: fix when string values are supported
			return Double.class;
		case 1:
			return Integer.class;
		default:
			throw new AssertionError("Invalid column");
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}
	
	@Override
	public void setValueAt(Object aValue, int row, int column) {
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0:
			return columnValueList.keySet().toArray()[rowIndex];
		case 1:
			return columnValueList.values().toArray()[rowIndex];
		default:
			throw new AssertionError("Invalid column");
		}
	}
	
}
