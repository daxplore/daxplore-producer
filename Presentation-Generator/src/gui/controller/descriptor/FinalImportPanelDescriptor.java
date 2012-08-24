package gui.controller.descriptor;

import gui.view.panel.FinalImportPanel;

public class FinalImportPanelDescriptor extends ImportWizardDescriptor {

	public static final String IDENTIFIER = "FINAL_IMPORT_PANEL";
	
	public FinalImportPanelDescriptor() {
        super(IDENTIFIER, new FinalImportPanel());
    }
    
    public Object getNextPanelDescriptor() {
        return FINISH;
    }
    
    public Object getBackPanelDescriptor() {
        return CharsetPanelDescriptor.IDENTIFIER;
    }  
}
