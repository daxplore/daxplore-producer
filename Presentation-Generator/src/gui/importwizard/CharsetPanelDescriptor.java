package gui.importwizard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.Charset;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import tools.SPSSTools;


public class CharsetPanelDescriptor extends ImportWizardDescriptor implements ActionListener {

	private static final String ENCODING_COMBO_BOX_ACTION = "ENCODING_COMBO_BOX_ACTION";
	public static final String IDENTIFIER = "CHARSET_SELECTION_PANEL";
	
	CharsetPanel charsetPanel;
    
	/**
	 * Constructor.
	 */
    public CharsetPanelDescriptor() {
        super(IDENTIFIER, new CharsetPanel());
        charsetPanel = (CharsetPanel) super.getPanelComponent();
        charsetPanel.addEncodingComboBoxAction(this);
        charsetPanel.encodingComboBox.setActionCommand(ENCODING_COMBO_BOX_ACTION);
    }
    
    public Object getNextPanelDescriptor() {
        return FinalImportPanelDescriptor.IDENTIFIER;
    }
    
    public Object getBackPanelDescriptor() {
        return OpenFilePanelDescriptor.IDENTIFIER;
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(CharsetPanelDescriptor.ENCODING_COMBO_BOX_ACTION))
			charsetComboBoxAction(e);
	}
	
	/**
	 * Method to handle the encoding charset combo box.
	 * @param e
	 */
	public void charsetComboBoxAction(ActionEvent e) {
		if(!(e.getSource() instanceof JComboBox)) {
			return;
		}
		JComboBox charsetSource = (JComboBox) e.getSource();
		
		String charsetType = (String) charsetSource.getSelectedItem();
		if (charsetType == charsetPanel.ENCODING_COMBO_BOX_LIST_LABEL)
			return;
		
		if(charsetType != null && !charsetType.equals("") && 
				getWizard().getGuiMain().getGuiFile().getSpssFile() != null) {
			Charset charset = Charset.forName(charsetType);
			DefaultComboBoxModel stringList = new DefaultComboBoxModel();
			try {
				Set<String> encodedStrings = SPSSTools.getNonAsciiStrings(
						getWizard().getGuiMain().getGuiFile().getSpssFile(), charset);
				
				for (String es: encodedStrings) {
					stringList.addElement(es);
				}
				
				JList encodedStringsList = new JList(stringList);
				
				getWizard().getModel().setCharsetName(charset.name());
				charsetPanel.setEncodingList(encodedStringsList);
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(this.getWizard(),
						"That encoding type is not supported.",
						"Encoding error", JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			}
		}
	}
}
