package org.daxplore.producer.gui.textview;

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

import org.daxplore.producer.gui.SectionHeader;
import org.daxplore.producer.gui.resources.GuiTexts;
import org.daxplore.producer.gui.textview.EditTextController.EditTextCommand;
import org.daxplore.producer.gui.utility.DisplayLocale;

@SuppressWarnings("serial")
public class EditTextView extends JPanel {
	private JTextField textField;

	private JComboBox<DisplayLocale> localeCombo1;
	private JComboBox<DisplayLocale> localeCombo2;
	private JScrollPane scrollPane;
	
	public <T extends DocumentListener & ActionListener> EditTextView(GuiTexts texts, T listener) {
		setLayout(new BorderLayout(0, 0));
		
		add(new SectionHeader(texts, "texts"), BorderLayout.NORTH);
		
		JPanel textsPanel = new JPanel(new BorderLayout());
		JPanel searchComboPanel = new JPanel(new GridLayout(0, 3, 0, 0));
		
		textField = new JTextField();
		textField.setHorizontalAlignment(SwingConstants.LEFT);
		textField.setColumns(15);
		textField.getDocument().addDocumentListener(listener);
		searchComboPanel.add(textField);

		localeCombo1 = new JComboBox<>();
		localeCombo1.setActionCommand(EditTextCommand.UPDATE_COLUMN_1.toString());
		localeCombo1.addActionListener(listener);
		searchComboPanel.add(localeCombo1);
		
		localeCombo2 = new JComboBox<>();
		localeCombo2.setActionCommand(EditTextCommand.UPDATE_COLUMN_2.toString());
		localeCombo2.addActionListener(listener);
		searchComboPanel.add(localeCombo2);
		
		textsPanel.add(searchComboPanel, BorderLayout.NORTH);

		scrollPane = new JScrollPane();
		textsPanel.add(scrollPane, BorderLayout.CENTER);
		
		add(textsPanel, BorderLayout.CENTER);
	}
	
	public void setTable(JTable table) {
		scrollPane.setViewportView(table);
	}
	
	public void setLocales(List<Locale> localeList) {
		DisplayLocale item1 = (DisplayLocale)localeCombo1.getSelectedItem();
		DisplayLocale item2 = (DisplayLocale)localeCombo2.getSelectedItem();
		localeCombo1.removeAllItems();
		localeCombo2.removeAllItems();
		List<DisplayLocale> localeItemList = new LinkedList<>();
		for(Locale l: localeList) { 
			localeItemList.add(new DisplayLocale(l));
		}
		if(localeList.size() == 0 ) {
			return;
		}
		for(DisplayLocale l: localeItemList) {
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
			for(DisplayLocale l: localeItemList) {
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
		DisplayLocale item = (DisplayLocale)localeCombo1.getSelectedItem(); 
		if(item != null) {
			return item.locale;
		}
		return null;
	}

	public Locale getSelectedLocale2() {
		DisplayLocale item = (DisplayLocale)localeCombo2.getSelectedItem(); 
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
