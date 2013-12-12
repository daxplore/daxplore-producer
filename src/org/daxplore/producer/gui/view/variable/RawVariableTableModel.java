package org.daxplore.producer.gui.view.variable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.DefaultTableModel;

import org.daxplore.producer.daxplorelib.metadata.MetaScale;
import org.daxplore.producer.daxplorelib.raw.VariableOptionInfo;
import org.daxplore.producer.tools.NumberlineCoverage;

@SuppressWarnings("serial")
public class RawVariableTableModel extends DefaultTableModel {
	
	private List<VariableOptionInfo> variableList;
	private Map<Double, Integer> toNumberMap = new HashMap<>();
	private MetaScale ms;
	
	List<Integer> availableToNumbers;

	public RawVariableTableModel(List<VariableOptionInfo> variableList, MetaScale ms, List<Integer> availableToNumbers) {
		this.variableList = variableList;
		this.ms = ms;
		this.availableToNumbers = availableToNumbers;
		
		remapFromMetaScale();
	}
	
	public void remapFromMetaScale() {
		if(ms != null) {
			toNumberMap.clear();
			for(VariableOptionInfo info: variableList) {
				if(info.getValue() != null) {
					int index = ms.matchIndex(info.getValue());
					if(index != -1 && availableToNumbers.contains(index)) {
						toNumberMap.put(info.getValue(), index);
					}
				}
			}
		}
	}
	
	@Override
	public int getRowCount() {
		if(variableList == null) return 0;
		return variableList.size();
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
		return columnIndex == 3 && variableList.get(rowIndex).getValue() != null;
	}
	
	@Override
	public void setValueAt(Object aValue, int row, int column) {
		if(column == 3) {
			Integer val = (Integer)aValue;
			Integer oldVal = (Integer)getValueAt(row, column);
			if(val == oldVal){
				return;
			}
			if(val == null) {
				Double value = (Double)getValueAt(row, 0);
				if(value == null){
					return;
				}
 				int oldIndex = toNumberMap.get(value);
				
				NumberlineCoverage oldPlace = ms.getOptions().get(oldIndex).getTransformation();
				oldPlace.removeNumber(value);
				ms.getOptions().get(oldIndex).setTransformation(oldPlace);
				
				fireTableCellUpdated(row, column);
				
			}else if(availableToNumbers.contains(val) ) {
				Double value = (Double)getValueAt(row, 0);
				int oldIndex = toNumberMap.get(value);
				
				NumberlineCoverage oldPlace = ms.getOptions().get(oldIndex).getTransformation();
				oldPlace.removeNumber(value);
				ms.getOptions().get(oldIndex).setTransformation(oldPlace);
				
				NumberlineCoverage newPlace = ms.getOptions().get(val).getTransformation();
				newPlace.addNumber(value);
				ms.getOptions().get(val).setTransformation(newPlace);
				
				toNumberMap.put(variableList.get(row).getValue(), val);
				
				fireTableCellUpdated(row, column);
			} 
		}
	}
	
	public void setAvailableToNumbers(List<Integer> toNumbers) {
		availableToNumbers = toNumbers;
		boolean modified = false;
		for(Map.Entry<Double, Integer> entry: toNumberMap.entrySet()) {
			if(!availableToNumbers.contains(entry.getValue())) {
				Double value = entry.getKey();
				int oldIndex = entry.getValue();
				
				NumberlineCoverage oldPlace = ms.getOptions().get(oldIndex).getTransformation();
				oldPlace.removeNumber(value);
				ms.getOptions().get(oldIndex).setTransformation(oldPlace);
				
				toNumberMap.put(entry.getKey(), null);
				
				modified = true;
			}
		}
		if(modified) {
			fireTableDataChanged();
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
		VariableOptionInfo opt = variableList.get(rowIndex);
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
