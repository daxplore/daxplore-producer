/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui.view.text;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentListener;

import org.daxplore.producer.gui.SectionHeader;
import org.daxplore.producer.gui.menu.ActionManager;
import org.daxplore.producer.gui.resources.GuiTexts;
import org.daxplore.producer.gui.utility.DisplayLocale;
import org.daxplore.producer.gui.view.text.EditTextController.EditTextCommand;

@SuppressWarnings("serial")
public class EditTextView extends JPanel {
	
	private GuiTexts texts;
	private ActionListener actionListener;
	private DocumentListener documentListener;
	private ActionManager actionManager;
	
	private JTextField textField;

	private JComboBox<DisplayLocale> localeCombo1;
	private JComboBox<DisplayLocale> localeCombo2;
	
	private JScrollPane textScrollPane;
	private JScrollPane localeScrollPane;
	private JTable localeTable;
	
	public <T extends DocumentListener & ActionListener>
	EditTextView(GuiTexts texts, ActionManager actionManager, T listener) {
		this.texts = texts;
		actionListener = listener;
		documentListener = listener;
		this.actionManager = actionManager;
		
		setLayout(new BorderLayout());
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		splitPane.setDividerLocation(650);
		splitPane.setTopComponent(buildEditArea());
		
		JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 20, 0));
		bottomPanel.add(buildLocaleSelector(), 0);
		bottomPanel.add(buildImportExportPanel(), 1);
		
		splitPane.setBottomComponent(bottomPanel);
		
		add(splitPane, BorderLayout.CENTER);
	}
	
	private JPanel buildEditArea() {
		JPanel editPanel = new JPanel(new BorderLayout());
		
		editPanel.add(new SectionHeader(texts, "texts"), BorderLayout.NORTH);
		
		JPanel textsPanel = new JPanel(new BorderLayout());
		JPanel searchComboPanel = new JPanel(new GridLayout(0, 3, 0, 0));
		
		textField = new JTextField();
		textField.setHorizontalAlignment(SwingConstants.LEFT);
		textField.setColumns(15);
		textField.getDocument().addDocumentListener(documentListener);
		searchComboPanel.add(textField);

		localeCombo1 = new JComboBox<>();
		localeCombo1.setActionCommand(EditTextCommand.UPDATE_COLUMN_1.toString());
		localeCombo1.addActionListener(actionListener);
		searchComboPanel.add(localeCombo1);
		
		localeCombo2 = new JComboBox<>();
		localeCombo2.setActionCommand(EditTextCommand.UPDATE_COLUMN_2.toString());
		localeCombo2.addActionListener(actionListener);
		searchComboPanel.add(localeCombo2);
		
		textsPanel.add(searchComboPanel, BorderLayout.NORTH);

		textScrollPane = new JScrollPane();
		textsPanel.add(textScrollPane, BorderLayout.CENTER);
		
		editPanel.add(textsPanel, BorderLayout.CENTER);
		
		return editPanel;
	}
	
	private JPanel buildLocaleSelector() {
		JPanel selectorPanel = new JPanel(new BorderLayout());
		
		selectorPanel.add(new SectionHeader(texts, "locale_picker"), BorderLayout.NORTH);
		
		localeScrollPane = new JScrollPane();
		selectorPanel.add(localeScrollPane, BorderLayout.CENTER);
		
		return selectorPanel;
	}
	
	private JPanel buildImportExportPanel() {
		JPanel sectionPanel = new JPanel(new BorderLayout());
		
		sectionPanel.add(new SectionHeader(texts, "import_export"), BorderLayout.NORTH);
		
		JPanel flowPanel = new JPanel(new FlowLayout());
		flowPanel.add(new JButton(actionManager.IMPORT_TEXTS));
		flowPanel.add(new JButton(actionManager.EXPORT_TEXTS));
		
		sectionPanel.add(flowPanel, BorderLayout.CENTER);
		
		return sectionPanel;
	}
	
	public void setTextTable(JTable table) {
		textScrollPane.setViewportView(table);
	}
	
	void setLocaleTable(SelectedLocalesTableModel tableModel) {
		localeTable = new JTable(tableModel);
		localeScrollPane.setViewportView(localeTable);
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
		return textScrollPane.getVerticalScrollBar().getValue();
	}
	
	public void setScrollbarPosition(int position) {
		textScrollPane.getVerticalScrollBar().setValue(position);
	}
}
