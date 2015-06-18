/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui.view.variable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.DefaultTableModel;

import org.daxplore.producer.daxplorelib.metadata.MetaScale;
import org.daxplore.producer.daxplorelib.metadata.MetaScale.Option;
import org.daxplore.producer.daxplorelib.raw.RawMeta.RawMetaQuestion;
import org.daxplore.producer.daxplorelib.raw.VariableOptionInfo;

@SuppressWarnings("serial")
public class RawVariableTableModel extends DefaultTableModel {
	
	private RawMetaQuestion rmq;
	private List<VariableOptionInfo> variableList;
	private Map<Object, Integer> toNumberMap = new HashMap<>();
	@SuppressWarnings("rawtypes")
	private MetaScale ms;
	
	List<Integer> availableToNumbers;

	@SuppressWarnings("rawtypes")
	public RawVariableTableModel(RawMetaQuestion rmq, List<VariableOptionInfo> variableList, MetaScale ms, List<Integer> availableToNumbers) {
		this.rmq = rmq;
		this.variableList = variableList;
		this.ms = ms;
		this.availableToNumbers = availableToNumbers;
		
		remapFromMetaScale();
	}
	
	@SuppressWarnings("unchecked")
	public void remapFromMetaScale() {
		if(ms != null) {
			toNumberMap.clear();
			for(VariableOptionInfo info: variableList) {
				if(info.getValue() != null) {
					int index = ms.getOptionIndex(info.getValue());
					if(index != -1 && availableToNumbers.contains(index)) {
						toNumberMap.put((Double)info.getValue(), index);
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
			return rmq.qtype.javatype();
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
	
	@SuppressWarnings("unchecked")
	@Override
	public void setValueAt(Object aValue, int row, int column) {
		if (column == 3) {
			Integer toOptionIndex = (Integer) aValue;
			Integer oldToOptionIndex = (Integer) getValueAt(row, column);
			
			if (toOptionIndex == oldToOptionIndex) {
				return;
			}
			
			Object rawValue = getValueAt(row, 0);
			
			switch (ms.getType()) {
			case NUMERIC:
				if(oldToOptionIndex != null) {
					MetaScale.Option<Double> currentOption = (Option<Double>) ms.getOptions().get(oldToOptionIndex);
					currentOption.removeValue((Double)rawValue);
				}
				
				if (toOptionIndex != null) {
					MetaScale.Option<Double> toOption = (Option<Double>) ms.getOptions().get(toOptionIndex);
					toOption.addValue((Double)rawValue);
				}
				break;
			case TEXT:
				if(oldToOptionIndex != null) {
					MetaScale.Option<String> currentOption = (Option<String>) ms.getOptions().get(oldToOptionIndex);
					currentOption.removeValue((String)rawValue);
				}
				
				if (toOptionIndex != null) {
					MetaScale.Option<String> toOption = (Option<String>) ms.getOptions().get(toOptionIndex);
					toOption.addValue((String)rawValue);
				}
				break;
			}

			toNumberMap.put(rawValue, toOptionIndex);
			
			fireTableCellUpdated(row, column);
		}
	}
	
	public void setAvailableToNumbers(List<Integer> toNumbers) {
		availableToNumbers = toNumbers;
		boolean modified = false;
		for(Map.Entry<Object, Integer> entry: toNumberMap.entrySet()) {
			if(!availableToNumbers.contains(entry.getValue())) {
				toNumberMap.put(entry.getKey(), null);
				modified = true;
			}
		}
		if(modified) {
			fireTableDataChanged();
		}
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
