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

import com.google.appengine.repackaged.com.google.common.collect.Table;

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
import java.awt.CardLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;

/**
 * Import SPSS dialog wizard.
 * @author hkfs89
 *
 */
public class ImportSPSSWizardDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private JDialog dialog;

	private final JPanel contentPanel = new JPanel();
	
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
	public ImportSPSSWizardDialog(final GUIMain daxploreGUI, SPSSFile spssFile) {
		setDialog(this);
		setTitle("Inspect SPSS file");
		setBounds(100, 100, 762, 622);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(daxploreGUI.guiMainFrame.getLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new CardLayout(0, 0));
	
		JPanel openFilePanel = new JPanel();
		contentPanel.add(openFilePanel, "openFilePanel");


		JPanel encodingPanel = new JPanel();
		contentPanel.add(encodingPanel, "encodingPanel");
		encodingPanel.setLayout(new BoxLayout(encodingPanel, BoxLayout.X_AXIS));
	
		JPanel specifyEncodingPanel = new JPanel();
		encodingPanel.add(specifyEncodingPanel);

		JLabel lblNewLabel = new JLabel("Specify encoding:");
		specifyEncodingPanel.add(lblNewLabel);

		JComboBox encodingComboBox = new JComboBox();
		specifyEncodingPanel.add(encodingComboBox);

		JPanel tablePanel = new JPanel();
		contentPanel.add(tablePanel, "tablePanel");

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);

		JButton nextButton = new JButton("Next");
		nextButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
				getDialog().dispose();
				}
			});
		nextButton.setActionCommand("OK");
		buttonPane.add(nextButton);
		getRootPane().setDefaultButton(nextButton);
				
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
