package gui.view;

import gui.GUIFile;
import gui.GUIMain;
import gui.opencontroller.CharsetComboBoxAction;
import gui.opencontroller.OpenSpssFileAction;

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
import java.nio.charset.Charset;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.awt.CardLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.border.LineBorder;
import java.awt.Color;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import java.awt.Component;
import javax.swing.Box;
import javax.swing.JList;

/**
 * Import SPSS dialog wizard.
 * 
 * @author hkfs89
 * 
 */
public class ImportSPSSWizardDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private JDialog dialog;

	private final JPanel contentPanel = new JPanel();
	private JScrollPane encodingListPanel;

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
	 * @param guiMain
	 */
	public ImportSPSSWizardDialog(final GUIMain guiMain, GUIFile guiFile) {
		setDialog(this);
		setTitle("Inspect SPSS file");
		setBounds(100, 100, 762, 622);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(guiMain.guiMainFrame.getLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new CardLayout(0, 0));

		JPanel openFilePanel = new JPanel();
		contentPanel.add(openFilePanel, "openFilePanel");
		openFilePanel.setLayout(new BorderLayout(0, 0));
		
		JButton openSpssFileButton = new JButton("Open SPSS file...");
		openSpssFileButton.addActionListener(new OpenSpssFileAction(this, guiFile));
		
		openSpssFileButton.setPreferredSize(new Dimension(38, 27));
		openFilePanel.add(openSpssFileButton, BorderLayout.NORTH);
		
		JPanel fileInfoPanel = new JPanel();
		fileInfoPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null,
				null));
		openFilePanel.add(fileInfoPanel, BorderLayout.CENTER);

		JPanel encodingPanel = new JPanel();
		contentPanel.add(encodingPanel, "encodingPanel");
		encodingPanel.setLayout(new BorderLayout(0, 0));

		JPanel specifyEncodingPanel = new JPanel();
		encodingPanel.add(specifyEncodingPanel, BorderLayout.NORTH);

		JLabel lblNewLabel = new JLabel("Specify encoding:");
		specifyEncodingPanel.add(lblNewLabel);
		
		JComboBox<String> encodingComboBox = new JComboBox<String>();
		encodingComboBox.addActionListener(new CharsetComboBoxAction(this, guiFile));
		specifyEncodingPanel.add(encodingComboBox);
		
		encodingListPanel = new JScrollPane();
		//encodingListPanel.setLayout(new BorderLayout(0, 0));
		//encodingPanel.add(encodingListPanel, BorderLayout.CENTER);
		
		
		JList list = new JList();
		//encodingListPanel.add(list, BorderLayout.CENTER);
		encodingListPanel.getViewport().setView(list);
		
		SortedMap<String, Charset> cset = Charset.availableCharsets();

		// populate the combobox with available charsets.
		for (String charname : cset.keySet()) {
			encodingComboBox.addItem(charname);
		}
		
		JPanel tablePanel = new JPanel();
		contentPanel.add(tablePanel, "tablePanel");

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);

		JButton nextButton = new JButton("Next");
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CardLayout cl = (CardLayout)(contentPanel.getLayout());
			    cl.next(contentPanel);
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
	
	public void setEncodingList(JList list) {
		encodingListPanel.getViewport().setView(list);
		encodingListPanel.validate();
	}
}
