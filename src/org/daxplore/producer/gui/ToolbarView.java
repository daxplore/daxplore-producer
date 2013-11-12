package org.daxplore.producer.gui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;

@SuppressWarnings("serial")
public class ToolbarView extends JPanel {

	private JToolBar toolbar;
	
	public ToolbarView(ActionManager actionManager) {
		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(5, 10, 5, 10));
		
		toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.setRollover(true);
		
		toolbar.add(actionManager.BACK);
		toolbar.add(actionManager.NEW);
		toolbar.add(actionManager.OPEN);
		toolbar.add(actionManager.SAVE);
		toolbar.add(actionManager.SAVE_AS);
		toolbar.add(actionManager.EXPORT_UPLOAD);
		
		add(toolbar, BorderLayout.WEST);
	}
}
