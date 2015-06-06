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
import org.daxplore.producer.daxplorelib.raw.RawMeta.RawMetaQuestion;
import org.daxplore.producer.daxplorelib.raw.VariableOptionInfo;
import org.daxplore.producer.tools.NumberlineCoverage;

@SuppressWarnings("serial")
public class RawVariableTableModel extends DefaultTableModel {
	
	private RawMetaQuestion rmq;
	private List<VariableOptionInfo> variableList;
	private Map<Double, Integer> toNumberMap = new HashMap<>();
	private MetaScale ms;
	
	List<Integer> availableToNumbers;

	public RawVariableTableModel(RawMetaQuestion rmq, List<VariableOptionInfo> variableList, MetaScale ms, List<Integer> availableToNumbers) {
		this.rmq = rmq;
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
					//TODO: drop cast when support for string values are implemented
					int index = ms.getOptionIndex((Double)info.getValue());
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
	
	@Override
	public void setValueAt(Object aValue, int row, int column) {
		if (column == 3) {
			Integer toOptionIndex = (Integer) aValue;
			Integer oldToOptionIndex = (Integer) getValueAt(row, column);
			
			if (toOptionIndex == oldToOptionIndex) {
				return;
			}
			
			Double rawValue = (Double)getValueAt(row, 0);
			
			if(oldToOptionIndex != null) {
				ms.getOptions().get(oldToOptionIndex).removeValue(rawValue);
			}
			
			if (toOptionIndex != null) {
				ms.getOptions().get(toOptionIndex).addValue(rawValue);
			}
			toNumberMap.put(rawValue, toOptionIndex);
			
			fireTableCellUpdated(row, column);
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
