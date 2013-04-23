package gui.edit;

import gui.MainController;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

@SuppressWarnings("serial")
public class EditTextView extends JPanel {
	private JTextField textField;

	MainController mainController;
	private JComboBox<LocaleItem> localeCombo1;
	private JComboBox<LocaleItem> localeCombo2;
	private JScrollPane scrollPane;
	private JTable table;
	private EditTextController editTextController;
	
	protected static class LocaleItem {
		Locale loc;
		public LocaleItem(Locale loc) {
			this.loc = loc;
		}
		public String toString() {
			return loc.getDisplayLanguage();
		}
		public boolean equals(Object o) {
			return o!=null && o instanceof LocaleItem && ((LocaleItem)o).loc.equals(loc);
		}
	}
	
	/**
	 * Create the panel.
	 * @param guiFile 
	 * @param mainController 
	 */
	public EditTextView(MainController mainController) {
		this.mainController = mainController;
		this.editTextController = new EditTextController(mainController, this);
		setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.NORTH);
		panel.setLayout(new GridLayout(0, 3, 0, 0));
		
		textField = new JTextField();
		textField.setHorizontalAlignment(SwingConstants.LEFT);
		panel.add(textField);
		textField.setColumns(15);
		textField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				editTextController.filter(textField.getText());
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				editTextController.filter(textField.getText());
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				editTextController.filter(textField.getText());
			}
		});
		
		
		
		localeCombo1 = new JComboBox<LocaleItem>();
		panel.add(localeCombo1);
		localeCombo1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(localeCombo1.getSelectedItem() != null) {
					LocaleItem locale = (LocaleItem)localeCombo1.getSelectedItem();
					editTextController.setCurrentLocale(locale.loc, 0);
					doUpdate();
				}
			}
		});
		localeCombo2 = new JComboBox<LocaleItem>();
		panel.add(localeCombo2);
		localeCombo2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(localeCombo2.getSelectedItem() != null) {
					LocaleItem locale = (LocaleItem)localeCombo2.getSelectedItem();
					editTextController.setCurrentLocale(locale.loc, 1);
					doUpdate();
				}
			}
		});
		
		scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);
		
		
		addComponentListener(new ComponentListener() {
			
			@Override
			public void componentShown(ComponentEvent e) {
				//doUpdate();
			}
			
			@Override
			public void componentResized(ComponentEvent e) {}
			@Override
			public void componentMoved(ComponentEvent e) {}
			@Override
			public void componentHidden(ComponentEvent e) {}
		});
		
	}
	
	public EditTextController getController() {
		return editTextController;
	}
	
	public void setTable(JTable table) {
		this.table = table;
		scrollPane.setViewportView(table);
	}
	
	public void setLocales(List<Locale> localeList) {
		LocaleItem item1 = (LocaleItem)localeCombo1.getSelectedItem();
		LocaleItem item2 = (LocaleItem)localeCombo2.getSelectedItem();
		localeCombo1.removeAllItems();
		localeCombo2.removeAllItems();
		List<LocaleItem> localeItemList = new LinkedList<LocaleItem>();
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

	public void doUpdate() {
		if(table != null) {
			int a = scrollPane.getVerticalScrollBar().getValue();
			editTextController.loadTable();
			scrollPane.getVerticalScrollBar().setValue(a);
		}
		//table.updateUI();
	}
	
	public void setSearchField(String text) {
		textField.setText(text);
	}
}
