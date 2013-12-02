package org.daxplore.producer.gui.view.variable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.table.DefaultTableModel;

import org.daxplore.producer.daxplorelib.raw.VariableOptionInfo;
import org.daxplore.producer.tools.NumberlineCoverage;

@SuppressWarnings("serial")
public class RawVariableTableModel extends DefaultTableModel {
	
	private List<VariableOptionInfo> data;
	private Map<Double, Integer> toNumberMap = new HashMap<>();
	
	private List<Integer> availableToNumbers = new LinkedList<>();

	public RawVariableTableModel(List<VariableOptionInfo> data) {
		this.data = data;
	}
	
	@Override
	public int getRowCount() {
		if(data == null) return 0;
		return data.size();
	}

	@Override
	public int getColumnCount() {
		return 4;
	}

	@Override
	public String getColumnName(int columnIndex) {
		switch(columnIndex) {
		case 0:
			return "Value";
		case 1:
			return "Text";
		case 2:
			return "Count";
		case 3:
			return "To option";
		default:
			throw new AssertionError();
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch(columnIndex) {
		case 0:
			return Double.class;
		case 1:
			return String.class;
		case 2:
			return Integer.class;
		case 3:
			return Integer.class;
		default:
			throw new AssertionError();
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 3;
	}
	
	@Override
	public void setValueAt(Object aValue, int row, int column) {
		if(column == 3) {
			Integer val = (Integer)aValue;
			if(availableToNumbers.contains(val) || val == null) {
				toNumberMap.put(data.get(row).getValue(), val);
				//TODO alert someone
			}
		}
	}
	
	public void setAvailableToNumbers(List<Integer> toNumbers) {
		availableToNumbers = toNumbers;
		boolean modified = false;
		for(Map.Entry<Double, Integer> entry: toNumberMap.entrySet()) {
			if(!availableToNumbers.contains(entry.getValue())) {
				toNumberMap.put(entry.getKey(), null);
				modified = true;
			}
		}
		if(modified) {
			//TODO alert someone
		}
	}
	
	public NumberlineCoverage getNumberlineCoverageForOption(Integer option) {
		NumberlineCoverage nc = new NumberlineCoverage();
		for(Map.Entry<Double, Integer> entry: toNumberMap.entrySet()) {
			if(entry.getValue().equals(option)) {
				nc.addNumber(entry.getKey());
			}
		}
		return nc;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		VariableOptionInfo opt = data.get(rowIndex);
		switch(columnIndex) {
		case 0:
			return opt.getValue();
		case 1:
			return opt.getRawText();
		case 2:
			return opt.getCount();
		case 3:
			return toNumberMap.get(opt.getValue());
		default: 
			throw new AssertionError();
		}
	}
	
}
