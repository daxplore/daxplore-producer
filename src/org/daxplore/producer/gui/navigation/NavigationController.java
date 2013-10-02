package org.daxplore.producer.gui.navigation;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.DaxploreFile;
import org.daxplore.producer.gui.event.DaxploreFileUpdateEvent;
import org.daxplore.producer.gui.event.HistoryGoBackEvent;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class NavigationController implements ActionListener {

	private EventBus eventBus;
	private NavigationView navigationView;
	
	private DaxploreFile daxploreFile;
	
	enum NavigationCommand {
		BACK, SAVE
	}
	
	public NavigationController(EventBus eventBus) {
		this.eventBus = eventBus;
		eventBus.register(this);
		navigationView = new NavigationView(this);
	}
	
	@Subscribe
	public void daxploreFileUpdate(DaxploreFileUpdateEvent e) {
		this.daxploreFile = e.getDaxploreFile();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch(NavigationCommand.valueOf(e.getActionCommand())) {
		case BACK:
			eventBus.post(new HistoryGoBackEvent());
			break;
		case SAVE:
			try {
				if(daxploreFile != null) {
					daxploreFile.saveAll();
				}
			} catch (DaxploreException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
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
