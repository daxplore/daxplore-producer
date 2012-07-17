package gui.opencontroller;

import gui.GUIFile;
import gui.view.ImportSPSSWizardDialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JList;

import tools.SPSSTools;

public final class CharsetComboBoxAction implements ActionListener {
	/**
	 * 
	 */
	private final ImportSPSSWizardDialog importWizardParent;
	private GUIFile guiFile;

	/**
	 * @param importSPSSWizardDialog
	 */
	public CharsetComboBoxAction(ImportSPSSWizardDialog importSPSSWizardDialog, GUIFile guiFile) {
		importWizardParent = importSPSSWizardDialog;
		this.guiFile = guiFile;
	}

	public void actionPerformed(ActionEvent e) {
		if(!(e.getSource() instanceof JComboBox)) {
			return;
		}
		JComboBox<String> charsetSource = (JComboBox<String>) e.getSource();
		
		String charsetName = (String) charsetSource.getSelectedItem();
		
		if(charsetName != null && !charsetName.equals("") && guiFile.getSpssFile() != null) {
			Charset charset = Charset.forName(charsetName);
			DefaultComboBoxModel<String> stringList = new DefaultComboBoxModel<String>();
			try {
				Set<String> encodedStrings = SPSSTools.getNonAsciiStrings(guiFile.getSpssFile(), charset);
				
				for (String es: encodedStrings) {
					stringList.addElement(es);
				}
				
				JList<String> encodedStringsList = new JList<String>(stringList);
				
				importWizardParent.setEncodingList(encodedStringsList);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
}