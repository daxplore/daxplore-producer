/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui.view.variable;

import java.util.List;

import javax.swing.table.DefaultTableModel;

import org.daxplore.producer.daxplorelib.metadata.MetaMean;
import org.daxplore.producer.daxplorelib.raw.RawMetaQuestion;
import org.daxplore.producer.daxplorelib.raw.VariableOptionInfo;

@SuppressWarnings("serial")
public class MeanExcludedTableModel extends DefaultTableModel {
	private RawMetaQuestion rmq;
	private List<VariableOptionInfo> variableList;
	private MetaMean mm;
	
	public MeanExcludedTableModel(RawMetaQuestion rmq, List<VariableOptionInfo> variableList, MetaMean mm) {
		this.rmq = rmq;
		this.variableList = variableList;
		this.mm = mm;
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
			return "Included";
		default:
			throw new AssertionError();
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch(columnIndex) {
		case 0:
			return rmq.getQtype().javatype();
		case 1:
			return String.class;
		case 2:
			return Integer.class;
		case 3:
			return Boolean.class;
		default:
			throw new AssertionError();
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 3 && (variableList.get(rowIndex).getValue() instanceof Double);
	}
	
	@Override
	public void setValueAt(Object aValue, int row, int column) {
		Object rowValue = variableList.get(row).getValue(); 
		if (rowValue instanceof Double) {
			mm.setExcludedValue((Double)rowValue, (Boolean)aValue);
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
			if (opt.getValue() instanceof Double) {
				return !mm.isExcluded((Double)opt.getValue());
			}
			return false;
		default: 
			throw new AssertionError();
		}
	}
}
