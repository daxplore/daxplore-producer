package org.daxplore.producer.gui.navigation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.MatteBorder;

import org.daxplore.producer.gui.MainController;

@SuppressWarnings("serial")
public class NavigationView extends JPanel {
	
	MainController mainController;
	NavigationController navigationController;

	private JButton backButton;
	private JPanel toolbarPanel;
	
	public NavigationView(final MainController mainController) {
		this.mainController = mainController;
		navigationController = new NavigationController(this, mainController);
		setLayout(new BorderLayout(0, 0));
		setBorder(new MatteBorder(0, 1, 0, 0, Color.GRAY));
		
		backButton = new JButton("Back");
		backButton.setActionCommand("BACK");
		backButton.addActionListener(navigationController);
		backButton.setEnabled(false);
		add(backButton, BorderLayout.WEST);
		
		JButton saveButton = new JButton("Save");
		saveButton.setActionCommand("SAVE");
		saveButton.addActionListener(navigationController);
		add(saveButton, BorderLayout.EAST);
		
		JPanel centerPanel = new JPanel();
		add(centerPanel, BorderLayout.CENTER);
		centerPanel.setLayout(new BorderLayout(0, 0));
		
		JSeparator westSeperator = new JSeparator();
		westSeperator.setOrientation(SwingConstants.VERTICAL);
		centerPanel.add(westSeperator, BorderLayout.WEST);
		
		JSeparator eastSeperator = new JSeparator();
		eastSeperator.setOrientation(SwingConstants.VERTICAL);
		centerPanel.add(eastSeperator, BorderLayout.EAST);
		
		toolbarPanel = new JPanel();
		centerPanel.add(toolbarPanel, BorderLayout.CENTER);
	}
	
	void setHistoryAvailble(boolean availible) {
		backButton.setEnabled(availible);
	}

	public NavigationController getController() {
		return navigationController;
	}
	
	public void setToolbar(Component comp) {
		toolbarPanel.removeAll();
		if(comp != null) toolbarPanel.add(comp);
	}


}
