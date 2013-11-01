package org.daxplore.producer.gui;

import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.daxplore.producer.gui.MenuBarController.MenuBarCommand;

import com.google.common.eventbus.EventBus;

public class MenuBarView extends JMenuBar{
	
	private EventBus eventBus;
	
	public MenuBarView(ActionListener listener) {
		this.eventBus = eventBus;
		
		// File menu
		JMenu fileMenu = new JMenu("File");
		
		JMenuItem newItem = new JMenuItem("New...");
		newItem.setActionCommand(MenuBarCommand.NEW.toString());
		newItem.addActionListener(listener);
		fileMenu.add(newItem);
		
		JMenuItem openItem = new JMenuItem("Open...");
		openItem.setActionCommand(MenuBarCommand.OPEN.toString());
		openItem.addActionListener(listener);
		fileMenu.add(openItem);
		
		JMenuItem saveItem = new JMenuItem("Save");
		saveItem.setActionCommand(MenuBarCommand.SAVE.toString());
		saveItem.addActionListener(listener);
		fileMenu.add(saveItem);		
		
		JMenuItem saveAsItem = new JMenuItem("Save as...");
		saveAsItem.setActionCommand(MenuBarCommand.SAVE_AS.toString());
		saveAsItem.addActionListener(listener);
		fileMenu.add(saveAsItem);
		
		JMenu importSubMenu = new JMenu("Import");
		JMenuItem importSpssItem = new JMenuItem("SPSS");
		importSpssItem.setActionCommand(MenuBarCommand.IMPORT_SPSS.toString());
		importSpssItem.addActionListener(listener);
		importSubMenu.add(importSpssItem);

		JMenuItem importTextsItem = new JMenuItem("User written texts");
		importTextsItem.setActionCommand(MenuBarCommand.IMPORT_TEXTS.toString());
		importTextsItem.addActionListener(listener);
		importSubMenu.add(importTextsItem);
		
		fileMenu.add(importSubMenu);
		
		
		JMenu exportSubMenu = new JMenu("Export");
		JMenuItem exportTextsItem = new JMenuItem("User written texts");
		exportTextsItem.setActionCommand(MenuBarCommand.EXPORT_TEXTS.toString());
		exportTextsItem.addActionListener(listener);
		exportSubMenu.add(exportTextsItem);
		
		JMenuItem uploadFileItem = new JMenuItem("File for server upload");
		uploadFileItem.setActionCommand(MenuBarCommand.EXPORT_UPLOAD_FILE.toString());
		uploadFileItem.addActionListener(listener);
		exportSubMenu.add(uploadFileItem);

		fileMenu.add(exportSubMenu);		
		
		JMenuItem discardItem = new JMenuItem("Discard all changes");
		discardItem.setActionCommand(MenuBarCommand.DISCARD_CHANGES.toString());
		discardItem.addActionListener(listener);
		fileMenu.add(discardItem);
		
		JMenuItem quitItem = new JMenuItem("Quit");
		quitItem.setActionCommand(MenuBarCommand.QUIT.toString());
		quitItem.addActionListener(listener);
		fileMenu.add(quitItem);
		
		add(fileMenu);
		
		
		// Edit menu
		// TODO add edit menu stuff
		
		// Something menu - TODO: rename this menu
		JMenu somethingMenu = new JMenu("Something");
		
		JMenuItem settingsItem = new JMenuItem("Program settings");
		settingsItem.setActionCommand(MenuBarCommand.SETTINGS.toString());
		settingsItem.addActionListener(listener);
		somethingMenu.add(settingsItem);
		
		JMenuItem infoItem = new JMenuItem("File info");
		infoItem.setActionCommand(MenuBarCommand.INFO.toString());
		infoItem.addActionListener(listener);
		somethingMenu.add(infoItem);
		
		add(somethingMenu);
		
		
		// Help menu 
		JMenu helpMenu = new JMenu("Help");
		
		JMenuItem helpItem = new JMenuItem("Online manual");
		helpItem.setActionCommand(MenuBarCommand.HELP.toString());
		helpItem.addActionListener(listener);
		helpMenu.add(helpItem);
		
		JMenuItem aboutItem = new JMenuItem("About");
		aboutItem.setActionCommand(MenuBarCommand.ABOUT.toString());
		aboutItem.addActionListener(listener);
		helpMenu.add(aboutItem);
		
		add(helpMenu);
	}
	
}
