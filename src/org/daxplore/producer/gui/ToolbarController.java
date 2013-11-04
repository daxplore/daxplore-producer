package org.daxplore.producer.gui;

import java.awt.Component;
import java.awt.event.ActionListener;

import org.daxplore.producer.gui.event.HistoryAvailableEvent;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class ToolbarController {
	
	private ToolbarView view;
	private EventBus eventBus;
	
	public ToolbarController(EventBus eventBus, ActionListener listener) {
		this.eventBus = eventBus;
		view = new ToolbarView(listener);
		eventBus.register(this);
	}
	
	public Component getView() {
		return view;
	}
	
	@Subscribe
	public void on(HistoryAvailableEvent e) {
		
	}
}
