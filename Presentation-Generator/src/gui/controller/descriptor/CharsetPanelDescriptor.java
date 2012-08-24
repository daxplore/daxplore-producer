package gui.controller.descriptor;

import gui.view.panel.CharsetPanel;

public class CharsetPanelDescriptor extends ImportWizardDescriptor {

	public static final String IDENTIFIER = "CHARSET_SELECTION_PANEL";
    
    public CharsetPanelDescriptor() {
        super(IDENTIFIER, new CharsetPanel());
    }
    
    public Object getNextPanelDescriptor() {
        return FinalImportPanelDescriptor.IDENTIFIER;
    }
    
    public Object getBackPanelDescriptor() {
        return OpenFilePanelDescriptor.IDENTIFIER;
    }  
}
