package gui.view;

import gui.GUIFile;
import gui.GUIMain;
import gui.controller.ImportWizardController;
import gui.controller.ImportWizardDescriptor;
import gui.model.ImportWizardDescriptorNotFoundException;
import gui.model.ImportWizardModel;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.charset.Charset;
import java.util.SortedMap;

import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

/**
 * This class implements a basic wizard dialog, where the programmer can
 * insert one or more Components to act as panels. These panels can be navigated
 * through arbitrarily using the 'Next' or 'Back' buttons, or the dialog itself
 * can be closed using the 'Cancel' button. Note that even though the dialog
 * uses a CardLayout manager, the order of the panels is not linear. Each panel
 * determines at runtime what its next and previous panel will be.
 */
public class ImportWizardDialog extends JDialog implements PropertyChangeListener {

	private static final long serialVersionUID = 1L;
	private JDialog importWizardDialog;
	private ImportWizardModel importWizardModel;
	private ImportWizardController importWizardController;
	
	 // descriptor control flags.
    public static final int FINISH_RETURN_CODE = 0;
    public static final int CANCEL_RETURN_CODE = 1;
    public static final int ERROR_RETURN_CODE = 2;
        
    // field identifiers for action commands.
    public static final String NEXT_BUTTON_ACTION_COMMAND = "NextButtonActionCommand";
    public static final String BACK_BUTTON_ACTION_COMMAND = "BackButtonActionCommand";
    public static final String CANCEL_BUTTON_ACTION_COMMAND = "CancelButtonActionCommand"; 
    public static final String OPEN_SPSS_FILE_ACTION_COMMAND = "OpenSpssFileActionCommand";
	public static final String ENCODING_COMBO_BOX_ACTION = "EncodingComboBoxAction";
	public static final String IMPORT_SPSS_FILE_ACTION = "ImportSpssFileAction";
	public static final Object CANCEL_TEXT = null;
	public static final Object BACK_TEXT = null;
	public static final Object FINISH_TEXT = null;
	public static final Object NEXT_TEXT = null;
    
	private final JPanel contentPanel = new JPanel();
	private JScrollPane encodingListPanel;
	private CardLayout cardLayout = new CardLayout(0,0);
	private JButton openSpssFileButton;
	private JButton nextButton;
	private JButton backButton;
	private JButton cancelButton;
	private JTextPane spssFileInfoText;
	private JComboBox encodingComboBox;
	
	private int returnCode;
	
	/**
	 * Constructor.
	 * 
	 * @param spssFile
	 * @param guiMain
	 */
	public ImportWizardDialog(final GUIMain guiMain, GUIFile guiFile) {
		
		importWizardController = new ImportWizardController(guiMain, this, guiFile);
		importWizardModel = new ImportWizardModel();
		importWizardDialog = this;
		initcomponents(guiMain, guiFile);	// create the dialogue.
	}
	
	public String getSpssFileInfoText() {
		return spssFileInfoText.getText();
	}

	public void setSpssFileInfoText(String spssFileInfoText) {
		this.spssFileInfoText.setText(spssFileInfoText);
	}

	public JDialog getDialog() {
		return importWizardDialog;
	}

	public void setDialog(JDialog dialog) {
		this.importWizardDialog = dialog;
	}
	
	public Component getOnwer() {
		return this.importWizardDialog.getOwner();
	}

	public void setBackButtonEnabled(boolean b) {
	    backButton.setEnabled(b);
	}
	void setNextButtonEnabled(boolean b) {
	    nextButton.setEnabled(b);
	}
	
	public void setEncodingList(JList list) {
		encodingListPanel.getViewport().setView(list);
		encodingListPanel.validate();
	}
	
	/**
     * Add a Component as a panel for the wizard dialog by registering its
     * WizardPanelDescriptor object. Each panel is identified by a unique Object-based
     * identifier (often a String), which can be used by the setCurrentPanel()
     * method to display the panel at runtime.
     * @param id An Object-based identifier used to identify the WizardPanelDescriptor object.
     * @param panel ImportWizardDescriptor object which contains helpful information about the panel.
     */    
    public void registerWizardPanel(Object id, ImportWizardDescriptor panel) {
        
        //  Add the incoming panel to our JPanel display that is managed by
        //  the CardLayout layout manager.
        
        contentPanel.add(panel.getPanelComponent(), id);
        
        //  Set a callback to the current wizard.
        
        panel.setWizard(this);
        
        //  Place a reference to it in the model. 
        
        importWizardModel.registerPanel(id, panel);
    }  
    
    /**
     * Displays the panel identified by the object passed in. This is the same Object-based
     * identified used when registering the panel.
     * @param id The Object-based identifier of the panel to be displayed.
     * @throws ImportWizardDescriptorNotFoundException 
     */    
    public void setCurrentPanel(Object id) {

        //  Get the hashtable reference to the panel that should
        //  be displayed. If the identifier passed in is null, then close
        //  the dialog.
        
        if (id == null)
            close(ERROR_RETURN_CODE);
        
        ImportWizardDescriptor oldPanelDescriptor = importWizardModel.getCurrentPanelDescriptor();
        if (oldPanelDescriptor != null)
            oldPanelDescriptor.aboutToHidePanel();
        
        importWizardModel.setCurrentPanel(id);
        importWizardModel.getCurrentPanelDescriptor().aboutToDisplayPanel();
        
        //  Show the panel in the dialog.
        
        cardLayout.show(contentPanel, id.toString());
        importWizardModel.getCurrentPanelDescriptor().displayingPanel();        
        
        
    }
	
    /**
     * Method used to listen for property change events from the model and update the
     * dialog's graphical components as necessary.
     * @param evt PropertyChangeEvent passed from the model to signal that one of its properties has changed value.
     */    
    public void propertyChange(PropertyChangeEvent evt) {
        
        if (evt.getPropertyName().equals(ImportWizardModel.CURRENT_PANEL_DESCRIPTOR_PROPERTY)) {
            importWizardController.resetButtonsToPanelRules(); 
        } else if (evt.getPropertyName().equals(ImportWizardModel.NEXT_FINISH_BUTTON_TEXT_PROPERTY)) {            
            nextButton.setText(evt.getNewValue().toString());
        } else if (evt.getPropertyName().equals(ImportWizardModel.BACK_BUTTON_TEXT_PROPERTY)) {            
            backButton.setText(evt.getNewValue().toString());
        } else if (evt.getPropertyName().equals(ImportWizardModel.CANCEL_BUTTON_TEXT_PROPERTY)) {            
            cancelButton.setText(evt.getNewValue().toString());
        } else if (evt.getPropertyName().equals(ImportWizardModel.NEXT_FINISH_BUTTON_ENABLED_PROPERTY)) {            
            nextButton.setEnabled(((Boolean)evt.getNewValue()).booleanValue());
        } else if (evt.getPropertyName().equals(ImportWizardModel.BACK_BUTTON_ENABLED_PROPERTY)) {            
            backButton.setEnabled(((Boolean)evt.getNewValue()).booleanValue());
        } else if (evt.getPropertyName().equals(ImportWizardModel.CANCEL_BUTTON_ENABLED_PROPERTY)) {            
            cancelButton.setEnabled(((Boolean)evt.getNewValue()).booleanValue());
        }
        
    }
    
    /**
     * Closes the dialog and sets the return code to the integer parameter.
     * @param code The return code.
     */    
    public void close(int code) {
        returnCode = code;
        importWizardDialog.dispose();
    }
    
    public ImportWizardModel getModel() {
		return importWizardModel;
	}
	
	private void initcomponents(final GUIMain guiMain, GUIFile guiFile) {
		setIconImage(Toolkit.getDefaultToolkit().getImage(ImportWizardDialog.class.getResource("/gui/resources/Arrow-Up-48.png")));
		setDialog(this);
		setTitle("SPSS File Wizard");
		setBounds(100, 100, 762, 622);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(guiMain.getGuiMainFrame().getLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(cardLayout);

		JPanel openFilePanel = new JPanel();
		contentPanel.add(openFilePanel, "openFilePanel");
		
		openSpssFileButton = new JButton("Open SPSS file...");
		openSpssFileButton.setActionCommand(OPEN_SPSS_FILE_ACTION_COMMAND);
		openSpssFileButton.addActionListener(importWizardController);
		
		openSpssFileButton.setPreferredSize(new Dimension(38, 27));
		
		JPanel fileInfoPanel = new JPanel();
		fileInfoPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GroupLayout gl_openFilePanel = new GroupLayout(openFilePanel);
		gl_openFilePanel.setHorizontalGroup(
			gl_openFilePanel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_openFilePanel.createSequentialGroup()
					.addGap(93)
					.addComponent(fileInfoPanel, GroupLayout.PREFERRED_SIZE, 536, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(107, Short.MAX_VALUE))
				.addGroup(gl_openFilePanel.createSequentialGroup()
					.addContainerGap(307, Short.MAX_VALUE)
					.addComponent(openSpssFileButton, GroupLayout.PREFERRED_SIZE, 166, GroupLayout.PREFERRED_SIZE)
					.addGap(277))
		);
		gl_openFilePanel.setVerticalGroup(
			gl_openFilePanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_openFilePanel.createSequentialGroup()
					.addGap(39)
					.addComponent(fileInfoPanel, GroupLayout.PREFERRED_SIZE, 317, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(openSpssFileButton, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(148, Short.MAX_VALUE))
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
		
		encodingComboBox = new JComboBox();
		encodingComboBox.setActionCommand(ENCODING_COMBO_BOX_ACTION);
		encodingComboBox.addActionListener(importWizardController);
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
		nextButton.addActionListener(importWizardController);
		
		nextButton.setActionCommand(NEXT_BUTTON_ACTION_COMMAND);
		buttonPanel.add(nextButton);
		getRootPane().setDefaultButton(nextButton);
		
		backButton = new JButton("Back");
		backButton.addActionListener(new ImportWizardController(guiMain, this, guiFile));
		
		backButton.setPreferredSize(new Dimension(80, 28));
		backButton.setActionCommand(BACK_BUTTON_ACTION_COMMAND);
		buttonPanel.add(backButton);
		
		buttonPanel.add(nextButton);
		getRootPane().setDefaultButton(nextButton);

		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(importWizardController);
		
		Component horizontalStrut = Box.createHorizontalStrut(20);
		horizontalStrut.setPreferredSize(new Dimension(50, 0));
		buttonPanel.add(horizontalStrut);
		
		cancelButton.setActionCommand(CANCEL_BUTTON_ACTION_COMMAND);
		buttonPanel.add(cancelButton);
	}
}
