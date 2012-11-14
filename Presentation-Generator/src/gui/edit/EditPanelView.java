package gui.edit;

import gui.GuiFile;
import gui.GuiMain;

import javax.swing.JPanel;
import javax.swing.JLabel;

public class EditPanelView extends JPanel {

	/**
	 * Create the panel.
	 * @param guiFile 
	 * @param guiMain 
	 */
	public EditPanelView(GuiMain guiMain) {
		
		JLabel EditPanelLabel = new JLabel("Edit Panel");
		add(EditPanelLabel);

	}

}
