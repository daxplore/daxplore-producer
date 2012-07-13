package gui.view;

import gui.GUIMain;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.opendatafoundation.data.FileFormatInfo;
import org.opendatafoundation.data.FileFormatInfo.ASCIIFormat;
import org.opendatafoundation.data.FileFormatInfo.Compatibility;
import org.opendatafoundation.data.spss.SPSSFile;
import org.opendatafoundation.data.spss.SPSSFileException;
import org.opendatafoundation.data.spss.SPSSVariable;

import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.BoxLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

public class OpenSPSSDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private JDialog dialog;

	private final JPanel contentPanel = new JPanel();
	private JTable table;

	private BufferedReader br;
	
	public JDialog getDialog() {
		return dialog;
	}

	public void setDialog(JDialog dialog) {
		this.dialog = dialog;
	}

	/**
	 * Create the dialog.
	 * 
	 * @param spssFile
	 * @param daxploreGUI
	 */
	public OpenSPSSDialog(final GUIMain daxploreGUI, SPSSFile spssFile) {
		setDialog(this);
		setTitle("Inspect SPSS file");
		setBounds(100, 100, 762, 622);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(daxploreGUI.guiMainFrame.getLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));
		{
			JScrollPane scrollPane = new JScrollPane();
			contentPanel.add(scrollPane);
			{
				SPSSTable(spssFile);
				scrollPane.setViewportView(table);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						getDialog().dispose();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						getDialog().dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
	
	/**
	 * Creates a temporary file on disc and imports SPSS file information as well as outputs it to a table display.
	 * @param sf
	 */
	public void SPSSTable(SPSSFile sf) {
		File temp;
		FileFormatInfo ffi = new FileFormatInfo();
		ffi.namesOnFirstLine = false;
		ffi.asciiFormat = ASCIIFormat.CSV;
		ffi.compatibility = Compatibility.GENERIC;
		try {
			sf.logFlag = false;
			sf.loadMetadata();
			temp = File.createTempFile("spsscsv", ".csv.tmp");
			sf.exportData(temp, ffi);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		} catch (SPSSFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		Dimension d = new Dimension(1000, 800);
		setSize(d);
		String[] columns = new String[sf.getVariableCount()];
		Object[][] data = new Object[sf.getRecordCount()][sf.getVariableCount()];
		for(int i = 0; i < sf.getVariableCount(); i++){
			SPSSVariable var = sf.getVariable(i);
			columns[i] = var.getShortName();
			System.out.print(sf.getVariable(i).getShortName() + ", ");
		}

		try {
			br = new BufferedReader(new FileReader(temp));
			String line;
			int l = 0;
			while((line = br.readLine()) != null){
				StringTokenizer st = new StringTokenizer(line, ",");
				int c = 0;
				while(st.hasMoreTokens()){
					data[l][c] = st.nextToken();
					c++;
				}
				l++;
			}
		} catch (FileNotFoundException e) {
			System.out.println("FileNotFoundException");
			e.printStackTrace();
			
		} catch (IOException e) {
			System.out.println("IOException");
			System.out.print(e.toString());
			e.printStackTrace();
		}
		
		// disallow editing of table fields through this model.
		TableModel model = new DefaultTableModel(data, columns) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int rowIndex, int mColIndex) {
		        return false;
		      }
		    };

		table = new JTable(model);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		TableColumn column = null;
		for (int i = 0; i < 5; i++) {
			column = table.getColumnModel().getColumn(i);
			column.setPreferredWidth(50);
		}
	}
}
