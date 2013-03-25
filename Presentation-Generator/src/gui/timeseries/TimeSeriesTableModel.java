package gui.timeseries;

import java.util.List;

import javax.swing.table.DefaultTableModel;

import daxplorelib.metadata.MetaTimepointShort;
import daxplorelib.metadata.TextReference;

@SuppressWarnings("serial")
public class TimeSeriesTableModel extends DefaultTableModel {
	
	private List<MetaTimepointShort> timepoints;
	
	public TimeSeriesTableModel(List<MetaTimepointShort> timepoints) {
		this.timepoints = timepoints;
	}
	
	@Override
	public int getRowCount() {
		if(timepoints == null) return 0;
		return timepoints.size();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public String getColumnName(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return "";
		case 1:
			return "";
		default:
			throw new AssertionError("Invalid column");
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return TextReference.class;
		case 1:
			return Double.class;
		default:
			throw new AssertionError("Invalid column");
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}
	
	@Override
	public void setValueAt(Object aValue, int row, int column) {
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		MetaTimepointShort row = timepoints.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return row.getTextRef();
		case 1:
			return row.getValue();
		default:
			throw new AssertionError("Invalid column");
		}
	}
	
}
