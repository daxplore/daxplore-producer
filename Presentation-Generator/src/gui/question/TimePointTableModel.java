package gui.question;

import java.awt.Checkbox;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import tools.Pair;
import daxplorelib.About;
import daxplorelib.metadata.MetaQuestion;
import daxplorelib.metadata.MetaTimepointShort;
import daxplorelib.metadata.MetaTimepointShort.MetaTimepointShortManager;
import daxplorelib.metadata.textreference.TextReference;
import daxplorelib.raw.RawData;

@SuppressWarnings("serial")
public class TimePointTableModel extends DefaultTableModel {
	
	
	LinkedHashMap<MetaTimepointShort, Integer> timePoints = new LinkedHashMap<MetaTimepointShort, Integer>();
	List<Double> counts = new LinkedList<Double>();
	MetaQuestion mq;
	
	public TimePointTableModel(MetaTimepointShortManager mtsm, RawData rawData, About about, MetaQuestion question) throws SQLException {
		this.mq = question;
		LinkedList<Pair<Double, Integer>> timePointCount = rawData.getColumnValueCountWhere(about.getTimeSeriesShortColumn(), mq.getId());
		for(MetaTimepointShort tp: mtsm.getAll()) {
			Double tpVal = tp.getValue();
			Integer tpCount = 0;
			for(Pair<Double, Integer> pair: timePointCount) {
				if(tpVal.equals(pair.getKey())) {
					tpCount = pair.getValue();
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
			System.out.println("Checkbox data class: " + aValue.getClass() + " at row: " + row);
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
