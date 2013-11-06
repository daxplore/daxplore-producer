package org.daxplore.producer.gui;

import java.awt.Component;

import com.google.common.eventbus.EventBus;

public class ToolbarController {
	
	private ToolbarView view;
	
	public ToolbarController(EventBus eventBus, ActionManager actionManager) {
		view = new ToolbarView(actionManager);
		eventBus.register(this);
	}
	
	public Component getView() {
		return view;
	}
}
