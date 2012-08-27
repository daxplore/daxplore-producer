package gui.importwizard;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.BorderLayout;

public class FinalImportPanel extends JPanel {
	private JTable table;

	public FinalImportPanel() {
		setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);
		
		scrollPane.setViewportView(table);
		
	}
	
	public void showTable(DefaultTableModel model) {
		table = new JTable(model){
			public boolean isCellEditable(int rowIndex, int colIndex) {
				return false; //Disallow the editing of any cell
			}
		};
		
		TableColumn column = new TableColumn();
		for (int i = 0; i < 5; i++) {
			column = table.getColumnModel().getColumn(i);
			column.setPreferredWidth(50);
		}
	}
}
