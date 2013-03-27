package gui.timeseries;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import daxplorelib.metadata.MetaQuestion;
import daxplorelib.metadata.MetaTimepointShort;
import daxplorelib.metadata.MetaTimepointShort.MetaTimepointShortManager;
import daxplorelib.metadata.textreference.TextReference;

@SuppressWarnings("serial")
public class TimeSeriesTableModel extends DefaultTableModel {
	
	private MetaTimepointShortManager timeManager;
	
	public TimeSeriesTableModel(MetaTimepointShortManager mtsm) {
		this.timeManager = mtsm;
	}
	
	@Override
	public int getRowCount() {
		try {
			return timeManager.getAll().size();
		} catch (SQLException|NullPointerException e) { //TODO figure out why this is needed. (Swing?)
			return 0;
		}
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
		MetaTimepointShort row;
		try {
			row = timeManager.getAll().get(rowIndex);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		switch (columnIndex) {
		case 0:
			return row.getTextRef();
		case 1:
			return row.getValue();
		default:
			throw new AssertionError("Invalid column");
		}
	}
	
	
	@Override
	public void removeRow(int row) {
		MetaTimepointShort tp;
		try {
			tp = timeManager.getAll().get(row);
			timeManager.remove(tp.getId());
			fireTableRowsDeleted(row, row);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void insertRow(int row, Vector rowData) {
		if(rowData.size() == 1 && rowData.get(0) instanceof MetaTimepointShort) {
			MetaTimepointShort newTime = (MetaTimepointShort)rowData.get(0);
			List<MetaTimepointShort> timelist;
			try {
				timelist = timeManager.getAll();
			} catch (SQLException e) {
				e.printStackTrace();
				return;
			}
			int timeindex = timelist.get(row).getTimeindex() + 1;
			newTime.setTimeindex(timeindex++);
			for(int i = row+1; i < timelist.size(); i++) {
				timelist.get(i).setTimeindex(timeindex++);
			}
			fireTableRowsInserted(row, row);
		}
	}
	
	@Override
	public void moveRow(int start, int end, int to) {
		if(start!=end) {
			throw new ArrayIndexOutOfBoundsException("Can't support moving many rows");
		}
		
		List<MetaTimepointShort> timeList;
		List<MetaTimepointShort> moveList = new LinkedList<MetaTimepointShort>();
		try {
			timeList = timeManager.getAll();
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		
		for(int i = start; i <= end; i++) {
			moveList.add(timeList.remove(start)); //TODO: check if it works with more rows than 1
		}
		int j = to;
		for(MetaTimepointShort mq: moveList) {
			timeList.add(j, mq);
			j++;
		}

		for(int i = 0; i < timeList.size(); i++) {
			timeList.get(i).setTimeindex(i);
		}
		
        int shift = to - start;
        int first, last;
        if (shift < 0) {
            first = to;
            last = end;
        } else {
            first = start;
            last = to + end - start;
        }
        
        fireTableRowsUpdated(first, last);
	}
	
}
