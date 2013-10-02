package org.daxplore.producer.gui.navigation;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.gui.MainController;

public class NavigationController implements ActionListener {

	private NavigationView navigationView;
	private MainController mainController;
	
	enum NavigationCommand {
		BACK, SAVE
	}
	
	public NavigationController(MainController mainController) {
		this.mainController = mainController;
		navigationView = new NavigationView(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch(NavigationCommand.valueOf(e.getActionCommand())) {
		case BACK:
			mainController.historyBack();
			break;
		case SAVE:
			try {
				mainController.getDaxploreFile().saveAll();
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
