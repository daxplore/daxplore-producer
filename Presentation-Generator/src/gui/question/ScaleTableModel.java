package gui.question;

import java.util.List;

import javax.swing.table.DefaultTableModel;

import tools.NumberlineCoverage;
import daxplorelib.metadata.MetaScale;
import daxplorelib.metadata.textreference.TextReference;

@SuppressWarnings("serial")
public class ScaleTableModel extends DefaultTableModel {
	
	List<MetaScale.Option> optionList;
	
	public ScaleTableModel(MetaScale ms) {
		optionList = ms.getOptions();
	}
	
	@Override
	public int getRowCount() {
		if(optionList == null) return 0;
		return optionList.size();
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
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		MetaScale.Option opt = optionList.get(rowIndex);
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
