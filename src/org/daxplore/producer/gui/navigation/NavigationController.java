package org.daxplore.producer.gui.navigation;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.daxplore.producer.gui.event.EmptyEvents.HistoryGoBackEvent;
import org.daxplore.producer.gui.event.EmptyEvents.SaveFileEvent;

import com.google.common.eventbus.EventBus;

public class NavigationController implements ActionListener {

	private EventBus eventBus;
	private NavigationView navigationView;
	
	enum NavigationCommand {
		BACK, SAVE
	}
	
	public NavigationController(EventBus eventBus) {
		this.eventBus = eventBus;
		eventBus.register(this);
		navigationView = new NavigationView(this);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		switch(NavigationCommand.valueOf(e.getActionCommand())) {
		case BACK:
			eventBus.post(new HistoryGoBackEvent());
			break;
		case SAVE:
			eventBus.post(new SaveFileEvent());
			break;
		default:
			throw new AssertionError("Not a defined command: '" + e.getActionCommand() + "'");
		}
	}
	
	public void setHistoryAvailible(boolean availible) {
		navigationView.setHistoryAvailble(availible);
	}

	public NavigationView getView() {
		return navigationView;
	}
	
	public void setToolbar(Component comp) {
		navigationView.setToolbar(comp);
	}
}
