package gui.navigation;

import gui.MainController;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import daxplorelib.DaxploreException;

public class NavigationController implements ActionListener {

	private NavigationView navigationView;
	private MainController mainController;
	
	public NavigationController(NavigationView navigationView, MainController mainController) {
		this.navigationView = navigationView;
		this.mainController = mainController;
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch(e.getActionCommand()) {
		case "BACK":
			mainController.historyBack();
			break;
		case "SAVE":
			try {
				mainController.getDaxploreFile().getMetaData().save();
			} catch (DaxploreException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			break;
		}
	}
	
	public void setHistoryAvailible(boolean availible) {
		navigationView.setHistoryAvailble(availible);
	}

}
