package org.daxplore.producer.gui.edit;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentListener;

import org.daxplore.producer.gui.edit.EditTextController.EditTextCommand;

@SuppressWarnings("serial")
public class EditTextView extends JPanel {
	private JTextField textField;

	private JComboBox<LocaleItem> localeCombo1;
	private JComboBox<LocaleItem> localeCombo2;
	private JScrollPane scrollPane;
	
	protected static class LocaleItem {
		Locale locale;
		public LocaleItem(Locale loc) {
			this.locale = loc;
		}
		@Override
		public String toString() {
			return locale.getDisplayLanguage();
		}
		@Override
		public boolean equals(Object o) {
			return o!=null && o instanceof LocaleItem && locale != null && locale.equals(((LocaleItem)o).locale);
		}
		@Override
		public int hashCode() {
			return locale==null ? 7 : locale.hashCode();
		}
	}
	
	/**
	 * Create the panel.
	 * @param guiFile 
	 */
	public <T extends DocumentListener & ActionListener> EditTextView(T listener) {
		setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.NORTH);
		panel.setLayout(new GridLayout(0, 3, 0, 0));
		
		textField = new JTextField();
		textField.setHorizontalAlignment(SwingConstants.LEFT);
		panel.add(textField);
		textField.setColumns(15);
		textField.getDocument().addDocumentListener(listener);

		localeCombo1 = new JComboBox<>();
		panel.add(localeCombo1);
		localeCombo1.setActionCommand(EditTextCommand.UPDATE_COLUMN_1.toString());
		localeCombo1.addActionListener(listener);
		
		localeCombo2 = new JComboBox<>();
		panel.add(localeCombo2);
		localeCombo2.setActionCommand(EditTextCommand.UPDATE_COLUMN_2.toString());
		localeCombo2.addActionListener(listener);
		
		scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);
	}
	
	public void setTable(JTable table) {
		scrollPane.setViewportView(table);
	}
	
	public void setLocales(List<Locale> localeList) {
		LocaleItem item1 = (LocaleItem)localeCombo1.getSelectedItem();
		LocaleItem item2 = (LocaleItem)localeCombo2.getSelectedItem();
		localeCombo1.removeAllItems();
		localeCombo2.removeAllItems();
		List<LocaleItem> localeItemList = new LinkedList<>();
		for(Locale l: localeList) { 
			localeItemList.add(new LocaleItem(l));
		}
		if(localeList.size() == 0 ) {
			return;
		}
		for(LocaleItem l: localeItemList) {
			localeCombo1.addItem(l);
			localeCombo2.addItem(l);
		}
		if(localeItemList.contains(item1)) {
			//localeCombo1.setSelectedItem(item1);
			// stay same
		} else if(localeItemList.get(0).equals(item2) && localeItemList.size() > 1) {
			item1 = localeItemList.get(1);
		} else {
			item1 = localeItemList.get(0);
		}
		if(localeItemList.contains(item2) && !item1.equals(item2)) {
			// stay same
		} else if(localeItemList.size() > 1) {
			for(LocaleItem l: localeItemList) {
				if(!l.equals(item1)) {
					item2 = l;
					break;
				}
			}
		} else {
			item2 = localeItemList.get(0);
		}
		localeCombo1.setSelectedItem(item1);
		localeCombo2.setSelectedItem(item2);
	}
	
	public String getFilterText() {
		return textField.getText();
	}
	
	public void setSearchField(String text) {
		textField.setText(text);
	}
	
	public Locale getSelectedLocale1() {
		LocaleItem item = (LocaleItem)localeCombo1.getSelectedItem(); 
		if(item != null) {
			return item.locale;
		}
		return null;
	}

	public Locale getSelectedLocale2() {
		LocaleItem item = (LocaleItem)localeCombo2.getSelectedItem(); 
		if(item != null) {
			return item.locale;
		}
		return null;
	}
	
	public int getScrollbarPosition() {
		return scrollPane.getVerticalScrollBar().getValue();
	}
	
	public void setScrollbarPosition(int position) {
		scrollPane.getVerticalScrollBar().setValue(position);
	}
}
