package generator;

import java.awt.Dimension;
import java.awt.Label;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.xml.transform.TransformerException;

import org.opendatafoundation.data.spss.SPSSFile;
import org.opendatafoundation.data.spss.SPSSFileException;
import org.opendatafoundation.data.spss.SPSSVariable;

public class SPSSTableTest extends JFrame {
	private JTable table;
	private SPSSFile sf;
	
	public SPSSTableTest(File file){
		try {
			sf = new SPSSFile(file);
			sf.loadMetadata();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SPSSFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Dimension d = new Dimension(1000, 800);
		setSize(d);
		String[] columns = new String[sf.getVariableCount()];
		Object[][] data = new Object[1][sf.getVariableCount()];
		for(int i = 0; i < sf.getVariableCount(); i++){
			SPSSVariable var = sf.getVariable(i);
			columns[i] = var.getShortName();
			System.out.print(sf.getVariable(i).getShortName() + ", ");
			data[0][i] = new String("" + var.getLength());
			
		}
		
		table = new JTable(data, columns);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		TableColumn column = null;
		for (int i = 0; i < 5; i++) {
			column = table.getColumnModel().getColumn(i);
			column.setPreferredWidth(50);
		}

		JScrollPane scrollPane = new JScrollPane(table,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		//scrollPane.
		table.setFillsViewportHeight(true);
		add(scrollPane);
		
		/*try {
			sf.dumpDDI3();
		} catch (SPSSFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

		
		setVisible(true);
	}
}
