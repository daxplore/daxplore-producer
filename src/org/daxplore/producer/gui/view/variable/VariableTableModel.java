package org.daxplore.producer.gui.view.variable;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;
import org.daxplore.producer.daxplorelib.metadata.MetaScale;
import org.daxplore.producer.daxplorelib.metadata.MetaScale.Option;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;

@SuppressWarnings("serial")
public class VariableTableModel extends DefaultTableModel{

	MetaScale ms;
	List<Integer> values;
	
	public VariableTableModel(MetaScale scale, List<Integer> afterValues) {
		this.ms = scale;
		this.values = afterValues;
	}
	
	public void setAfterValues(List<Integer> values) {
		this.values = values;
	}
	
	@Override
	public int getRowCount() {
		if(ms == null) return 0;
		return ms.getOptionCount();
	}
	

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public String getColumnName(int columnIndex) {
		switch(columnIndex) {
		case 0:
			return "Option";
		case 1:
			return "Text";
		case 2:
			return "Count";
		default:
			throw new AssertionError();
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch(columnIndex) {
		case 0:
			return Integer.class;
		case 1:
			return TextReference.class;
		case 2:
			return Integer.class;
		default:
			throw new AssertionError();
		}
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 1;
	}
	
	@Override
	public void setValueAt(Object aValue, int row, int column) {
		return;
	}
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch(columnIndex) {
		case 0:
			return rowIndex;
		case 1:
			return ms.getOptions().get(rowIndex).getTextRef();
		case 2:
			return values.get(rowIndex);
		default: 
			throw new AssertionError();
		}
	}
	
	@Override
	public void removeRow(int row) {
		List<Option> options= ms.getOptions();
		options.remove(row);
		ms.setOptions(options);
		fireTableRowsDeleted(row, row);
	}
	
	@Override
	public void insertRow(int row, Vector rowData) {
		if(rowData.size() == 1 && rowData.get(0) instanceof MetaQuestion) {
			List<Option> options= ms.getOptions();
			options.add(row, (Option)rowData.get(0));
			ms.setOptions(options);
			fireTableRowsInserted(row, row);
		}
	}
	
	@Override
	public void moveRow(int start, int end, int to) {
		List<Option> options= ms.getOptions();
		List<Option> moveList = new LinkedList<>();
		
		for(int i = start; i <= end; i++) {
			moveList.add(options.remove(start)); //TODO: check if it works with more rows than 1
		}
		int j = to;
		for(Option mq: moveList) {
			options.add(j, mq);
			j++;
		}
		ms.setOptions(options);
		
        int shift = to - start;
        int first, last;
        if (shift < 0) {
            first = to;
            last = end;
        }
        else {
            first = start;
            last = to + end - start;
        }
        
        fireTableRowsUpdated(first, last);
	}
	
}
