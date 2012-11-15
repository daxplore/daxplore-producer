package gui.edit;

import gui.GuiFile;
import gui.GuiMain;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

import javax.swing.ListSelectionModel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

import daxplorelib.DaxploreException;
import daxplorelib.metadata.TextReference;
import daxplorelib.metadata.TextReference.TextReferenceManager;

public class EditPanelView extends JPanel {
	private JTextField textField;
	private JTable table;

	GuiMain guiMain;
	private JComboBox<Locale> comboBox;
	private JComboBox<Locale> comboBox_1;
	private JScrollPane scrollPane;
	
	private List<TextReference> textsList;
	private Locale[] currentLocales = new Locale[2];
	
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
	
	/**
	 * Create the panel.
	 * @param guiFile 
	 * @param guiMain 
	 */
	public EditPanelView(GuiMain guiMain) {
		this.guiMain = guiMain;
		setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.NORTH);
		
		textField = new JTextField();
		panel.add(textField);
		textField.setColumns(10);
		
		comboBox = new JComboBox<Locale>();
		panel.add(comboBox);
		comboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentLocales[0] = (Locale)comboBox.getSelectedItem();
				doUpdate();
			}
		});
		
		comboBox_1 = new JComboBox<Locale>();
		panel.add(comboBox_1);
		comboBox_1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentLocales[1] = (Locale)comboBox_1.getSelectedItem();
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
	
	public void doUpdate() {
		//table.updateUI();
	}

	public void loadData() {
		System.out.println("EditPanelView.updateStuff()");
		if(guiMain.getGuiFile().isSet()) {
			System.out.print("adding locales... ");
			try {
				TextReferenceManager trm = guiMain.getGuiFile().getDaxploreFile().getMetaData().getTextsManager();
				List<Locale> localeList = trm.getAllLocales();
				System.out.print(localeList.size() + " locales");
				for(Locale l: localeList) {
					comboBox.addItem(l);
					comboBox_1.addItem(l);
				}
				textsList = trm.getAll();
			} catch (DaxploreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			table = new JTable(new TextsTableModel());
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	        table.setPreferredScrollableViewportSize(new Dimension(500, 70));
	        table.setFillsViewportHeight(true);
			scrollPane.setViewportView(table);
		}
	}
	
	
}
