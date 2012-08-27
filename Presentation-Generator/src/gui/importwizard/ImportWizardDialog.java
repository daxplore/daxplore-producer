package gui.importwizard;

import gui.GUIFile;
import gui.GUIMain;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
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
public class ImportWizardDialog extends JDialog implements PropertyChangeListener, WindowListener {


	private static final long serialVersionUID = 1L;
	
	// store instances of classes that will be used.
	private GUIMain guiMain;
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
	
    // no resource file is used so the strings for button text is stored here.
	public static final String CANCEL_TEXT = "Cancel";
	public static final String BACK_TEXT = "Back";
	public static final String FINISH_TEXT = "Finish";
	public static final String NEXT_TEXT = "Next";
    
	// elements specific for the import wizard components.
	private final JPanel contentPanel = new JPanel();
	private CardLayout cardLayout = new CardLayout(0,0);
	private JButton nextButton;
	private JButton backButton;
	private JButton cancelButton;
	
	private int returnCode;
	
	/**
	 * Constructor.
	 * 
	 * @param guiMain
	 */
	public ImportWizardDialog(final GUIMain guiMain) {
		
		importWizardController = new ImportWizardController(this);
		importWizardModel = new ImportWizardModel();
		importWizardDialog = this;
		this.guiMain = guiMain;
		initcomponents();	// create the dialogue.
	}
	
	// getters and setters be here.
	public GUIMain getGuiMain() {
		return guiMain;
	}

	public void setGuiMain(GUIMain guiMain) {
		this.guiMain = guiMain;
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
	  
    public void registerWizardPanel(Object id, ImportWizardDescriptor panel) {
        
        //  Add the incoming panel to our JPanel display that is managed by
        //  the CardLayout layout manager.
        
        contentPanel.add(panel.getPanelComponent(), id);
        
        //  Set a callback to the current wizard.
        
        panel.setWizard(this);
        
        //  Place a reference to it in the model. 
        
        importWizardModel.registerPanel(id, panel);
    }  
       
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
	
    // used to update button properties as events are triggered.
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
     * Mirrors the WizardModel method of the same name.
     * @return A boolean indicating if the button is enabled.
     */  
    public boolean getBackButtonEnabled() {
        return importWizardModel.getBackButtonEnabled().booleanValue();
    }

   /**
     * Mirrors the WizardModel method of the same name.
     * @param boolean newValue The new enabled status of the button.
     */ 
    public void setBackButtonEnabled(boolean newValue) {
    	importWizardModel.setBackButtonEnabled(new Boolean(newValue));
    }

   /**
     * Mirrors the WizardModel method of the same name.
     * @return A boolean indicating if the button is enabled.
     */ 
    public boolean getNextFinishButtonEnabled() {
        return importWizardModel.getNextFinishButtonEnabled().booleanValue();
    }

   /**
     * Mirrors the WizardModel method of the same name.
     * @param boolean newValue The new enabled status of the button.
     */ 
    public void setNextFinishButtonEnabled(boolean newValue) {
    	importWizardModel.setNextFinishButtonEnabled(new Boolean(newValue));
    }
 
   /**
     * Mirrors the WizardModel method of the same name.
     * @return A boolean indicating if the button is enabled.
     */ 
    public boolean getCancelButtonEnabled() {
        return importWizardModel.getCancelButtonEnabled().booleanValue();
    }

    /**
     * Mirrors the WizardModel method of the same name.
     * @param boolean newValue The new enabled status of the button.
     */ 
    public void setCancelButtonEnabled(boolean newValue) {
    	importWizardModel.setCancelButtonEnabled(new Boolean(newValue));
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
	
    /**
     * Code relevant to the creation of the dialog, basic layout, next, back and cancel buttons
     * goes here. Descriptors are used as controllers and panel views for display in separate files.
     */
	private void initcomponents() {
		
		importWizardModel.addPropertyChangeListener(this);       
        importWizardController = new ImportWizardController(this);       
		
		setIconImage(Toolkit.getDefaultToolkit().getImage(ImportWizardDialog.class.getResource("/gui/resources/Arrow-Up-48.png")));
		setDialog(this);
		setTitle("SPSS File Wizard");
		setBounds(100, 100, 762, 622);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(guiMain.getGuiMainFrame().getLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(cardLayout);
		addWindowListener(this);
		
		// TODO: Remove code below and sort it out in panel files.

		// JPanel encodingPanel = new JPanel();
		// contentPanel.add(encodingPanel, "encodingPanel");
		// encodingPanel.setLayout(new BorderLayout(0, 0));

		// JPanel specifyEncodingPanel = new JPanel();
		// encodingPanel.add(specifyEncodingPanel, BorderLayout.NORTH);

		// JLabel lblNewLabel = new JLabel("Specify encoding:");
		// specifyEncodingPanel.add(lblNewLabel);
		
		// encodingComboBox = new JComboBox();
		// encodingComboBox.setActionCommand(ENCODING_COMBO_BOX_ACTION);
		// encodingComboBox.addActionListener(importWizardController);
		// specifyEncodingPanel.add(encodingComboBox);
		
		// encodingListPanel = new JScrollPane();
		// encodingPanel.add(encodingListPanel, BorderLayout.CENTER);
		
		// SortedMap<String, Charset> cset = Charset.availableCharsets();

		// populate the combobox with available charsets.
		// for (String charname : cset.keySet()) {
		//	encodingComboBox.addItem(charname);
		// }
		
		// JPanel tablePanel = new JPanel();
		// contentPanel.add(tablePanel, "tablePanel");

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		backButton = new JButton("Back");
		backButton.addActionListener(importWizardController);
		backButton.setPreferredSize(new Dimension(80, 28));
		backButton.setActionCommand(BACK_BUTTON_ACTION_COMMAND);
		buttonPanel.add(backButton);
		
		nextButton = new JButton("Next");
		nextButton.setPreferredSize(new Dimension(80, 28));
		nextButton.addActionListener(importWizardController);
		nextButton.setActionCommand(NEXT_BUTTON_ACTION_COMMAND);
		buttonPanel.add(nextButton);
		getRootPane().setDefaultButton(nextButton);
		
		Component horizontalStrut = Box.createHorizontalStrut(20);
		horizontalStrut.setPreferredSize(new Dimension(50, 0));
		buttonPanel.add(horizontalStrut);
		
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(importWizardController);
		cancelButton.setActionCommand(CANCEL_BUTTON_ACTION_COMMAND);
		buttonPanel.add(cancelButton);
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
}
