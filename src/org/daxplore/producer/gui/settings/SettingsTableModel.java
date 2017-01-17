package org.daxplore.producer.gui.settings;

import javax.swing.table.DefaultTableModel;

import org.daxplore.producer.daxplorelib.Settings;
import org.daxplore.producer.daxplorelib.resources.DaxploreProperties;
import org.daxplore.producer.gui.resources.UITexts;

public class SettingsTableModel extends DefaultTableModel{
	Settings settings;
	
	public SettingsTableModel(Settings settings) {
		this.settings = settings;
	}
	
	@Override
	public int getRowCount() {
		return DaxploreProperties.clientSettings.size();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public String getColumnName(int columnIndex) {
		switch(columnIndex) {
		case 0:
			return "Setting name";
		case 1: 
			return "Value";
		}
		return null;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 1;
	}
	
	@Override
	public void setValueAt(Object aValue, int row, int column) {
		String key = DaxploreProperties.clientSettings.get(row);
		settings.putSetting(key, aValue);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		String key = DaxploreProperties.clientSettings.get(rowIndex);
		if(columnIndex == 0) {
			return UITexts.get("setting." + key);
		} else {
			return settings.getObject(key);
		}
	}
	
}
