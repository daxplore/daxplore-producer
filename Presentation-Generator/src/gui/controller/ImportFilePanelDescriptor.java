package gui.controller;

import gui.view.ImportFilePanel;

public class ImportFilePanelDescriptor extends ImportWizardDescriptor {

	public static final String IDENTIFIER = "FILE_IMPORT_PANEL";
    
    public ImportFilePanelDescriptor() {
        super(IDENTIFIER, new ImportFilePanel());
    }
    
    public Object getNextPanelDescriptor() {
        return CharsetPanelDescriptor.IDENTIFIER;
    }
    
    public Object getBackPanelDescriptor() {
        return null;
    }
}
