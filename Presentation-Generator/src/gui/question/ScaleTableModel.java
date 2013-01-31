package gui.question;

import java.util.List;

import javax.swing.table.DefaultTableModel;

import daxplorelib.metadata.MetaScale;

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
		return 1;
	}

	@Override
	public String getColumnName(int columnIndex) {
		return null;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return MetaScale.Option.class;
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
		return optionList.get(rowIndex);
	}

}
