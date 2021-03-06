/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui.view.variable;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;

import javax.swing.table.DefaultTableModel;

import org.daxplore.producer.daxplorelib.About;
import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;
import org.daxplore.producer.daxplorelib.metadata.MetaTimepointShort;
import org.daxplore.producer.daxplorelib.metadata.MetaTimepointShort.MetaTimepointShortManager;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;
import org.daxplore.producer.daxplorelib.raw.RawData.RawDataManager;

@SuppressWarnings("serial")
public class TimePointTableModel extends DefaultTableModel {
	
	LinkedHashMap<MetaTimepointShort, Integer> timePoints = new LinkedHashMap<>();
	List<Double> counts = new LinkedList<>();
	MetaQuestion mq;
	
	public TimePointTableModel(MetaTimepointShortManager mtsm, RawDataManager rawDataManager, About about, MetaQuestion question) throws DaxploreException {
		this.mq = question;
		SortedMap<Double, Integer> timePointCount = rawDataManager.getColumnValueCountWhere(about.getTimeSeriesShortColumn(), mq.getColumn());
		for(MetaTimepointShort tp: mtsm.getAll()) {
			Double tpVal = tp.getValue();
			Integer tpCount = 0;
			for(Double key : timePointCount.keySet()) {
				if(tpVal.equals(key)) {
					tpCount = timePointCount.get(key);
					break;
				}
			}
			timePoints.put(tp, tpCount);
		}
	}
	
	@Override
	public int getRowCount() {
		if(timePoints == null) return 0;
		return timePoints.size();
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public String getColumnName(int columnIndex) {
		switch(columnIndex) {
		case 0:
			return "Time Point";
		case 1:
			return "Count";
		case 2:
			return "Use";
		default:
			throw new AssertionError();
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch(columnIndex) {
		case 0:
			return TextReference.class;
		case 1:
			return Integer.class;
		case 2:
			return Boolean.class;
		default:
			throw new AssertionError();
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 1:
			return false;
		default:
			return true;
		}
	}
	
	@Override
	public void setValueAt(Object aValue, int row, int column) {
		switch(column) {
		case 0:
			break;
		case 1:
			break;
		case 2:
			if(aValue instanceof Boolean) {
				MetaTimepointShort tp = getAtIndex(row);
				List<MetaTimepointShort> tpList = mq.getTimepoints();
				if((Boolean)aValue) {
					if(!tpList.contains(tp)) {
						tpList.add(tp);
						mq.setTimepoints(tpList);
					}
				} else {
					if(tpList.remove(tp)) {
						mq.setTimepoints(tpList);
					}
				}
			}
			break;
		default:
			throw new AssertionError();
		}
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch(columnIndex) {
		case 0:
			return getAtIndex(rowIndex).getTextRef();
		case 1:
			return timePoints.get(getAtIndex(rowIndex));
		case 2:
			return mq.getTimepoints().contains(getAtIndex(rowIndex));
		default: 
			throw new AssertionError();
		}
	}
	
	private MetaTimepointShort getAtIndex(int index) {
		int i = 0;
		for(MetaTimepointShort tp: timePoints.keySet()) {
			if(i == index) {
				return tp;
			}
			i++;
		}
		throw new IndexOutOfBoundsException();
	}
}
