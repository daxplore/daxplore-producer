/**
 * 
 */
package org.daxplore.producer.gui.view.tools;

import javax.swing.table.DefaultTableModel;

import org.daxplore.producer.daxplorelib.About;
import org.daxplore.producer.gui.Settings;
import org.daxplore.producer.gui.event.EmptyEvents.LocaleAddedOrRemovedEvent;

import com.google.common.eventbus.EventBus;

@SuppressWarnings("serial")
public class LocalesTableModel extends DefaultTableModel {

	private EventBus eventBus;
	private About about;
	
	public LocalesTableModel(EventBus eventBus, About about) {
		this.eventBus = eventBus;
		this.about = about;
	}
	
	@Override
	public String getColumnName(int col) {
		switch(col) {
		case 0:
			return "Locale";
		case 1:
			return "Used";
		default:
			throw new IndexOutOfBoundsException("Column index out of bounds: '" + col + "'");
		}
	}

	@Override
	public int getRowCount() {
		return Settings.availableLocales().size();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public Object getValueAt(int row, int col) {
		switch (col) {
		case 0:
			return Settings.availableLocales().get(row).getDisplayLanguage();
		case 1:
			return about.getLocales().contains(Settings.availableLocales().get(row));
		default:
			throw new IndexOutOfBoundsException("Column index out of bounds: '" + col + "'");
		}
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return String.class;
		case 1:
			return Boolean.class;
		default:
			throw new IndexOutOfBoundsException("Column index out of bounds: '" + columnIndex + "'");
		}
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return col == 1;
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		if(col == 1 && value instanceof Boolean) {
			Boolean set = (Boolean)value;
			if(set) {
				about.addLocale(Settings.availableLocales().get(row));
			} else {
				about.removeLocale(Settings.availableLocales().get(row));
			}
			eventBus.post(new LocaleAddedOrRemovedEvent());
		}
	}
}
