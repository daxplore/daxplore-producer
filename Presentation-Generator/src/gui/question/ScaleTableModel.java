package gui.question;

import javax.swing.table.DefaultTableModel;

import tools.NumberlineCoverage;
import daxplorelib.metadata.MetaScale;
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
		switch(column) {
		case 0:
			break;
		case 1:
			if(aValue instanceof NumberlineCoverage) {
				ms.getOptions().get(row).setTransformation((NumberlineCoverage)aValue);
				fireTableCellUpdated(row, column);
			}
			break;
		case 2:
			if(aValue instanceof Double) {
				ms.getOptions().get(row).setValue((Double)aValue);
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

}
