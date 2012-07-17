package gui.view;

import gui.GUIFile;
import gui.GUIMain;
import gui.opencontroller.CharsetComboBoxAction;
import gui.opencontroller.ImportWizardPanelController;
import gui.opencontroller.OpenSpssFileAction;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.Charset;
import java.util.SortedMap;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.BoxLayout;
import net.miginfocom.swing.MigLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JTextPane;
import java.awt.Toolkit;

/**
 * Import SPSS dialog wizard.
 * 
 * @author hkfs89
 * 
 */
public class ImportSPSSWizardDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private JDialog dialog;
	private ImportWizardPanelController wizardController = new ImportWizardPanelController();

	private final JPanel contentPanel = new JPanel();
	private JScrollPane encodingListPanel;
	private CardLayout cardLayout = new CardLayout(0,0);
	private JButton openSpssFileButton;
	private JButton nextButton;
	private JButton backButton;
	private JButton cancelButton;
	private JTextPane spssFileInfoText;

	public String getSpssFileInfoText() {
		return spssFileInfoText.getText();
	}

	public void setSpssFileInfoText(String spssFileInfoText) {
		this.spssFileInfoText.setText(spssFileInfoText);
	}

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
		
		setIconImage(Toolkit.getDefaultToolkit().getImage(ImportSPSSWizardDialog.class.getResource("/gui/resources/Arrow-Up-48.png")));
		setDialog(this);
		setTitle("SPSS File Wizard");
		setBounds(100, 100, 762, 622);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(guiMain.guiMainFrame.getLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(cardLayout);

		JPanel openFilePanel = new JPanel();
		contentPanel.add(openFilePanel, "openFilePanel");
		
		openSpssFileButton = new JButton("Open SPSS file...");
		openSpssFileButton.addActionListener(new OpenSpssFileAction(this, guiFile));
		
		openSpssFileButton.setPreferredSize(new Dimension(38, 27));
		
		JPanel fileInfoPanel = new JPanel();
		fileInfoPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GroupLayout gl_openFilePanel = new GroupLayout(openFilePanel);
		gl_openFilePanel.setHorizontalGroup(
			gl_openFilePanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_openFilePanel.createSequentialGroup()
					.addGap(93)
					.addComponent(fileInfoPanel, GroupLayout.PREFERRED_SIZE, 536, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(105, Short.MAX_VALUE))
				.addGroup(Alignment.TRAILING, gl_openFilePanel.createSequentialGroup()
					.addContainerGap(309, Short.MAX_VALUE)
					.addComponent(openSpssFileButton, GroupLayout.PREFERRED_SIZE, 121, GroupLayout.PREFERRED_SIZE)
					.addGap(304))
		);
		gl_openFilePanel.setVerticalGroup(
			gl_openFilePanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_openFilePanel.createSequentialGroup()
					.addGap(39)
					.addComponent(fileInfoPanel, GroupLayout.PREFERRED_SIZE, 317, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(openSpssFileButton, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(133, Short.MAX_VALUE))
		);
		fileInfoPanel.setLayout(new BorderLayout(0, 0));
		
		spssFileInfoText = new JTextPane();
		fileInfoPanel.add(spssFileInfoText, BorderLayout.CENTER);
		openFilePanel.setLayout(gl_openFilePanel);

		JPanel encodingPanel = new JPanel();
		contentPanel.add(encodingPanel, "encodingPanel");
		encodingPanel.setLayout(new BorderLayout(0, 0));

		JPanel specifyEncodingPanel = new JPanel();
		encodingPanel.add(specifyEncodingPanel, BorderLayout.NORTH);

		JLabel lblNewLabel = new JLabel("Specify encoding:");
		specifyEncodingPanel.add(lblNewLabel);
		
		// combo box operations
		JComboBox<String> encodingComboBox = new JComboBox<String>();
		encodingComboBox.addActionListener(new CharsetComboBoxAction(this, guiFile));
		specifyEncodingPanel.add(encodingComboBox);
		
		encodingListPanel = new JScrollPane();
		encodingPanel.add(encodingListPanel, BorderLayout.CENTER);
		
		SortedMap<String, Charset> cset = Charset.availableCharsets();

		// populate the combobox with available charsets.
		for (String charname : cset.keySet()) {
			encodingComboBox.addItem(charname);
		}
		
		JPanel tablePanel = new JPanel();
		contentPanel.add(tablePanel, "tablePanel");

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		nextButton = new JButton("Next");
		nextButton.setPreferredSize(new Dimension(80, 28));
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CardLayout cl = (CardLayout)(contentPanel.getLayout());
			    cl.next(contentPanel);
			}
		});
		
		backButton = new JButton("Back");
		backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				CardLayout cl = (CardLayout)(contentPanel.getLayout());
			    cl.previous(contentPanel);
			}
		});
		backButton.setPreferredSize(new Dimension(80, 28));
		buttonPanel.add(backButton);
		nextButton.setActionCommand("Next");
		buttonPanel.add(nextButton);
		getRootPane().setDefaultButton(nextButton);

		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getDialog().dispose();
			}
		});
		
		cancelButton.setActionCommand("Cancel");
		buttonPanel.add(cancelButton);
		
		Component horizontalStrut = Box.createHorizontalStrut(20);
		horizontalStrut.setPreferredSize(new Dimension(60, 0));
		buttonPanel.add(horizontalStrut);
		

	}
	
	void setBackButtonEnabled(boolean b) {
	    backButton.setEnabled(b);
	}
	void setNextButtonEnabled(boolean b) {
	    nextButton.setEnabled(b);
	}
	
	public void setEncodingList(JList<String> list) {
		encodingListPanel.getViewport().setView(list);
		encodingListPanel.validate();
	}

	public void registerWizardPanel(Object id, ImportWizardPanelController panel) {
		contentPanel.add(panel.getPanelComponent(), id);
		ImportSPSSWizardDialog.registerPanel(id, panel);
	}

	private static void registerPanel(Object id, ImportWizardPanelController panel) {
		// TODO Auto-generated method stub
		
	}

	public ImportWizardPanelController getWizardController() {
		return wizardController;
	}
}
