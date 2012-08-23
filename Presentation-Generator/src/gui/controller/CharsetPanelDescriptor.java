package gui.controller;

import gui.view.CharsetPanel;

public class CharsetPanelDescriptor extends ImportWizardDescriptor {

	public static final String IDENTIFIER = "CHARSET_SELECTION_PANEL";
    
    public CharsetPanelDescriptor() {
        super(IDENTIFIER, new CharsetPanel());
    }
    
    public Object getNextPanelDescriptor() {
        return FinalImportPanelDescriptor.IDENTIFIER;
    }
    
    public Object getBackPanelDescriptor() {
        return ImportFilePanelDescriptor.IDENTIFIER;
    }  
}
