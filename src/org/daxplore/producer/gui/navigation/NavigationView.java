package org.daxplore.producer.gui.navigation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.MatteBorder;

import org.daxplore.producer.gui.navigation.NavigationController.NavigationCommand;

@SuppressWarnings("serial")
public class NavigationView extends JPanel {

	private JButton backButton;
	private JPanel toolbarPanel;
	
	NavigationView(ActionListener listener) {
		setLayout(new BorderLayout(0, 0));
		
		backButton = new JButton("Back");
		backButton.setActionCommand(NavigationCommand.BACK.toString());
		backButton.addActionListener(listener);
		backButton.setEnabled(false);
		add(backButton, BorderLayout.WEST);
		
		JButton saveButton = new JButton("Save");
		saveButton.setActionCommand(NavigationCommand.SAVE.toString());
		saveButton.addActionListener(listener);
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

	void setToolbar(Component comp) {
		toolbarPanel.removeAll();
		if(comp != null) toolbarPanel.add(comp);
	}
}
