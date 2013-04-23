/**
 * 
 */
package gui.tools;

import gui.MainController;
import gui.Settings;

import javax.swing.table.DefaultTableModel;

import daxplorelib.About;

/**
 * @author ladu5359
 *
 */
@SuppressWarnings("serial")
public class LocalesTableModel extends DefaultTableModel {

	About about;
	MainController mainController;
	
	public LocalesTableModel(About about, MainController mainController) {
		this.about = about;
		this.mainController = mainController;
	}
	
	public String getColumnName(int col) {
		switch(col) {
		case 0:
			return "Locale";
		case 1:
			return "Used";
		}
		throw new AssertionError();
	}

	public int getRowCount() {
		return Settings.availableLocales().size();
	}

	public int getColumnCount() {
		return 2;
	}

	public Object getValueAt(int row, int col) {
		switch (col) {
		case 0:
			return Settings.availableLocales().get(row).getDisplayLanguage();
		case 1:
			return about.getLocales().contains(Settings.availableLocales().get(row));
		}
		throw new AssertionError();
	}
	
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return String.class;
		case 1:
			return Boolean.class;
		}
		throw new AssertionError();
	}

	public boolean isCellEditable(int row, int col) {
		return col == 1;
	}

	public void setValueAt(Object value, int row, int col) {
		if(col == 1 && value instanceof Boolean) {
			Boolean set = (Boolean)value;
			if(set) {
				about.addLocale(Settings.availableLocales().get(row));
			} else {
				about.removeLocale(Settings.availableLocales().get(row));
			}
			mainController.updateStuff(); //TODO should use event system
		}
	}
}
