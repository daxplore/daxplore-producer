package gui.edit;

import gui.MainController;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import daxplorelib.DaxploreException;
import daxplorelib.metadata.TextReference;
import daxplorelib.metadata.TextReference.TextReferenceManager;

@SuppressWarnings("serial")
public class EditPanelView extends JPanel {
	private JTextField textField;

	MainController mainController;
	private JComboBox<LocaleItem> localeCombo1;
	private JComboBox<LocaleItem> localeCombo2;
	private JScrollPane scrollPane;
	
	private JTable table;
	private TableRowSorter<TextsTableModel> sorter;
	private List<TextReference> textsList = new LinkedList<TextReference>();
	private Locale[] currentLocales = new Locale[2];

	private TextsTableModel model;
	
	protected class TextsTableModel extends DefaultTableModel implements TableModelListener {
		
		public String getColumnName(int col) {
			switch(col) {
			case 0:
				return "Reference";
			case 1:
				return currentLocales[0].getDisplayLanguage();
			case 2:
				return currentLocales[1].getDisplayLanguage();
			}
			throw new AssertionError();
		}

		public int getRowCount() {
			return textsList.size();
		}

		public int getColumnCount() {
			return 3;
		}

		public Object getValueAt(int row, int col) {
			switch (col) {
			case 0:
				return textsList.get(row).getRef();
			case 1:
				return textsList.get(row).get(currentLocales[0]);
			case 2:
				return textsList.get(row).get(currentLocales[1]);
			}
			throw new AssertionError();
		}

		public boolean isCellEditable(int row, int col) {
			return col > 0;
		}

		public void setValueAt(Object value, int row, int col) {
			textsList.get(row).put(value.toString(), currentLocales[col-1]);
		}

		@Override
		public void tableChanged(TableModelEvent e) {
			// TODO Auto-generated method stub
			
		}
	}
	
	protected class LocaleItem {
		Locale loc;
		public LocaleItem(Locale loc) {
			this.loc = loc;
		}
		public String toString() {
			return loc.getDisplayLanguage();
		}
	}
	
	/**
	 * Create the panel.
	 * @param guiFile 
	 * @param mainController 
	 */
	public EditPanelView(MainController mainController) {
		this.mainController = mainController;
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
				filter();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				filter();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				filter();
			}
		});
		
		
		
		localeCombo1 = new JComboBox<LocaleItem>();
		panel.add(localeCombo1);
		localeCombo1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentLocales[0] = ((LocaleItem)localeCombo1.getSelectedItem()).loc;
				doUpdate();
			}
		});
		localeCombo2 = new JComboBox<LocaleItem>();
		panel.add(localeCombo2);
		localeCombo2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentLocales[1] = ((LocaleItem)localeCombo2.getSelectedItem()).loc;
				doUpdate();
			}
		});
		
		scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);
		
		
		addComponentListener(new ComponentListener() {
			
			@Override
			public void componentShown(ComponentEvent e) {
				System.out.println("edit got shown");
				doUpdate();
			}
			
			@Override
			public void componentResized(ComponentEvent e) {}
			@Override
			public void componentMoved(ComponentEvent e) {}
			@Override
			public void componentHidden(ComponentEvent e) {}
		});
		
	}
	
	private void filter() {
        RowFilter<TextsTableModel, Object> rf = null;
        //If current expression doesn't parse, don't update.
        try {
        	String caseInsensitive = "(?i)";
            rf = RowFilter.regexFilter(caseInsensitive + textField.getText(), 0);
        } catch (java.util.regex.PatternSyntaxException e) {
            return;
        }
        sorter.setRowFilter(rf);
	}
	
	public void doUpdate() {
		if(table != null) {
			int a = scrollPane.getVerticalScrollBar().getValue();
			loadTable();
			scrollPane.getVerticalScrollBar().setValue(a);
		}
		//table.updateUI();
	}

	public void loadData() {
		System.out.println("EditPanelView.updateStuff()");
		if(mainController.isSet()) {
			System.out.print("adding locales... ");
			try {
				TextReferenceManager trm = mainController.getDaxploreFile().getMetaData().getTextsManager();
				List<Locale> localeList = trm.getAllLocales();
				System.out.print(localeList.size() + " locales");
				for(Locale l: localeList) {
					localeCombo1.addItem(new LocaleItem(l));
					localeCombo2.addItem(new LocaleItem(l));
				}
				textsList = trm.getAll();
			} catch (DaxploreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			loadTable();
		}
	}
	
	private void loadTable() {
		model = new TextsTableModel();
		sorter = new TableRowSorter<TextsTableModel>(model);
		table = new JTable(new TextsTableModel());
		table.setRowSorter(sorter);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        table.setFillsViewportHeight(true);
		scrollPane.setViewportView(table);
	}
	
	
}
