package org.daxplore.producer.gui;

import java.awt.event.ActionListener;

import javax.swing.JMenuBar;

public class MenuBarController {
	
	private MenuBarView view;
	public MenuBarController(ActionListener listener) {
		view = new MenuBarView(listener);
	}
	
	public JMenuBar getView() {
		return view;
	}
	
}
