/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui.menu;

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
