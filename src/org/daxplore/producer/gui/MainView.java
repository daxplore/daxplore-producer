/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeListener;

import org.daxplore.producer.gui.MainController.Views;
import org.daxplore.producer.gui.resources.IconResources;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class MainView {

	private JFrame mainWindow;
	private final JTabbedPane mainPanel = new JTabbedPane();
	private JPanel panel;
	
	private BiMap<Views, Component> viewsMap = HashBiMap.create();
	
	MainView(JFrame mainWindow) {
		this.mainWindow = mainWindow;
		mainWindow.setIconImage(IconResources.getImage("window-icon.png"));
		mainWindow.setSize(1020, 900); //TODO: save size in preferences
		mainWindow.setLocationRelativeTo(null);
		mainWindow.getContentPane().setLayout(new BorderLayout(0, 0));
		
		panel = new JPanel();
		mainWindow.getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));
		panel.add(mainPanel, BorderLayout.CENTER);
		
		mainPanel.setRequestFocusEnabled(true);
	}
	
	
	void switchTo(Views view) {
		mainPanel.setSelectedComponent(viewsMap.get(view));
	}
	
	JFrame getMainFrame() {
		return mainWindow;
	}

	
	void addTab(String tabTitle, Component component, Views view) {
		viewsMap.put(view, component);
		mainPanel.addTab(tabTitle, component);
	}
	
	void setToolbar(Component component) {
		mainWindow.getContentPane().add(component, BorderLayout.NORTH);
	}
	
	void setMenuBar(JMenuBar menuBar) {
		mainWindow.setJMenuBar(menuBar);
	}

	void addChangeListener(ChangeListener listener) {
		mainPanel.addChangeListener(listener);
	}
	
	Views getSelectedView() {
		return viewsMap.inverse().get(mainPanel.getSelectedComponent());
	}
	
	void setVisibe(boolean visible) {
		mainWindow.setVisible(visible);
	}
}
