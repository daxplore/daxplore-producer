/**
 * 
 */
package gui.widget;

import java.util.LinkedList;

import javax.swing.table.DefaultTableModel;

import tools.Pair;

@SuppressWarnings("serial")
public class ColumnTableModel extends DefaultTableModel {
	LinkedList<Pair<Double, Integer>> columnValueList;
	
	public ColumnTableModel(LinkedList<Pair<Double, Integer>> columnValueList) {
		this.columnValueList = columnValueList;
	}
	
	public void setValues(LinkedList<Pair<Double, Integer>> columnValueList) {
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
			return columnValueList.get(rowIndex).getKey();
		case 1:
			return columnValueList.get(rowIndex).getValue();
		default:
			throw new AssertionError("Invalid column");
		}
	}
	
}
