package gui.question;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import tools.NumberlineCoverage;
import daxplorelib.metadata.MetaQuestion;
import daxplorelib.metadata.MetaScale;
import daxplorelib.metadata.MetaScale.Option;
import daxplorelib.metadata.textreference.TextReference;

@SuppressWarnings("serial")
public class ScaleTableModel extends DefaultTableModel {
	
	MetaScale ms;
	
	public ScaleTableModel(MetaScale ms) {
		this.ms = ms;
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
			return "Text Reference";
		case 1:
			return "From numbers";
		case 2:
			return "To number";
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
			return NumberlineCoverage.class;
		case 2:
			return Double.class;
		default:
			throw new AssertionError();
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}
	
	@Override
	public void setValueAt(Object aValue, int row, int column) {
		MetaScale.Option opt = ms.getOptions().get(row);
		switch(column) {
		case 0:
			break;
		case 1:
			if(aValue instanceof NumberlineCoverage) {
				opt.setTransformation((NumberlineCoverage)aValue);
				fireTableCellUpdated(row, column);
			}
			break;
		case 2:
			if(aValue instanceof Double) {
				opt.setValue((Double)aValue);
				fireTableCellUpdated(row, column);
			}
			break;
		default:
			throw new AssertionError();
		}
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		MetaScale.Option opt = ms.getOptions().get(rowIndex);
		switch(columnIndex) {
		case 0:
			return opt.getTextRef();
		case 1:
			return opt.getTransformation();
		case 2:
			return opt.getValue();
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
	
	@SuppressWarnings("rawtypes")
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
		List<Option> moveList = new LinkedList<Option>();
		
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
