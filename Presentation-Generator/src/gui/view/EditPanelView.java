package gui.view;

import gui.GUIFile;
import gui.GUIMain;

import javax.swing.JPanel;
import javax.swing.JLabel;

public class EditPanelView extends JPanel {

	/**
	 * Create the panel.
	 * @param guiFile 
	 * @param guiMain 
	 */
	public EditPanelView(GUIMain guiMain, GUIFile guiFile) {
		
		JLabel EditPanelLabel = new JLabel("Edit Panel");
		add(EditPanelLabel);

	}

}
