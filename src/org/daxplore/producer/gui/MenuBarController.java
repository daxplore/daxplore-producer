package org.daxplore.producer.gui;

import javax.swing.JMenuBar;

public class MenuBarController {
	
	private MenuBarView view;
	public MenuBarController(ActionManager actionManager) {
		view = new MenuBarView(actionManager);
	}
	
	public JMenuBar getView() {
		return view;
	}
	
}
