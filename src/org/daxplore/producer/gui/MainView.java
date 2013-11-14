package org.daxplore.producer.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeListener;

import org.daxplore.producer.gui.MainController.Views;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class MainView {

	private JFrame mainWindow;
	private final JTabbedPane mainPanel = new JTabbedPane();
	private JPanel panel;
	
	private BiMap<Views, Component> viewsMap = HashBiMap.create();
	
	MainView(JFrame mainWindow) {
		this.mainWindow = mainWindow;
		mainWindow.setIconImage(Toolkit.getDefaultToolkit().getImage(MainController.class.getResource("/org/daxplore/producer/gui/resources/Colorful_Chart_Icon_vol2.png")));
		mainWindow.setTitle("Daxplore Producer Developer Version");
		mainWindow.setSize(1020, 900); //TODO: save size in preferences
		mainWindow.setLocationRelativeTo(null);
		mainWindow.getContentPane().setLayout(new BorderLayout(0, 0));
		
		panel = new JPanel();
		mainWindow.getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));
		panel.add(mainPanel, BorderLayout.CENTER);
		
		mainPanel.setRequestFocusEnabled(true);
		mainWindow.setVisible(true);
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
}
