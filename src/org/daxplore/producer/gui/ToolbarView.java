package org.daxplore.producer.gui;

import javax.swing.JPanel;
import javax.swing.JToolBar;

@SuppressWarnings("serial")
public class ToolbarView extends JPanel {

	private JToolBar toolbar;
	
	public ToolbarView(ActionManager actionManager) {
		toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.setRollover(true);
		
		toolbar.add(actionManager.BACK);
		toolbar.add(actionManager.NEW);
		toolbar.add(actionManager.OPEN);
		toolbar.add(actionManager.SAVE);
		toolbar.add(actionManager.SAVE_AS);
		toolbar.add(actionManager.EXPORT_UPLOAD_FILE);
		
		add(toolbar);
	}
}
