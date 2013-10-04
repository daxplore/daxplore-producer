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
