package org.daxplore.producer.gui.importwizard;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

@SuppressWarnings("serial")
public class FinalImportPanel extends JPanel {
	protected JTable table = new JTable();

	public FinalImportPanel() {
		setLayout(new BorderLayout());		
	}
	
	public void showTable(TableModel model) {
			
		table = new JTable(model) {
			@Override
			public boolean isCellEditable(int rowIndex, int colIndex) {
				return false; //Disallow the editing of any cell
			}
		};
				
		TableColumn column = new TableColumn();
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setFillsViewportHeight(true);
		for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
			column = table.getColumnModel().getColumn(i);
			column.setPreferredWidth(50);
		}
		
		JScrollPane scrollPane = new JScrollPane(table);
		add(scrollPane, BorderLayout.CENTER);
		
	}
}
