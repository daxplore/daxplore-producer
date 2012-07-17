package gui.opencontroller;

import java.awt.Component;

import javax.swing.JPanel;

public class ImportWizardPanelController extends JPanel {

	private Component targetPanel;
	private Object panelIdentifier;

	public final Component getPanelComponent() {
	    return targetPanel;
	}

	public final void setPanelComponent(Component panel) {
	    targetPanel = panel;
	}

	public final Object getPanelDescriptorIdentifier() {
	    return panelIdentifier;
	}

	public final void setPanelDescriptorIdentifier(Object id) {
	    panelIdentifier = id;
	}
	
	public void aboutToDisplayPanel() {

	    // Place code here that will be executed before the
	    // panel is displayed.

	}

	public void displayingPanel() {

	    // Place code here that will be executed when the
	    // panel is displayed.

	}

	public void aboutToHidePanel() {

	    // Place code here that will be executed when the 
	    // panel is hidden.

	}

	public static void setCurrentPanel(Object id) {
		// TODO Auto-generated method stub
		
	}

	public static Object getCurrentPanelDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}
}
