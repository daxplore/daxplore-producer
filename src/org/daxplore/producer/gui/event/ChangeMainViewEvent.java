/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui.event;

import org.daxplore.producer.gui.MainController.Views;

public class ChangeMainViewEvent {
	private Views view;
	private Object command;
	
	public ChangeMainViewEvent(Views view) {
		this(view, null);
	}
	
	public ChangeMainViewEvent(Views view, Object command) {
		this.view = view;
		this.command = command;
	}
	
	public Views getView() {
		return view;
	}
	
	public Object getCommand() {
		return command;
	}
}
