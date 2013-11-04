package org.daxplore.producer.gui;

import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.daxplore.producer.gui.ProgramCommandListener.ProgramCommand;

@SuppressWarnings("serial")
public class MenuBarView extends JMenuBar {
	
	public MenuBarView(ActionListener listener) {
		
		// File menu
		JMenu fileMenu = new JMenu("File");
		
		JMenuItem newItem = new JMenuItem("New...");
		newItem.setActionCommand(ProgramCommand.NEW.toString());
		newItem.addActionListener(listener);
		fileMenu.add(newItem);
		
		JMenuItem openItem = new JMenuItem("Open...");
		openItem.setActionCommand(ProgramCommand.OPEN.toString());
		openItem.addActionListener(listener);
		fileMenu.add(openItem);
		
		JMenuItem saveItem = new JMenuItem("Save");
		saveItem.setActionCommand(ProgramCommand.SAVE.toString());
		saveItem.addActionListener(listener);
		fileMenu.add(saveItem);		
		
		JMenuItem saveAsItem = new JMenuItem("Save as...");
		saveAsItem.setActionCommand(ProgramCommand.SAVE_AS.toString());
		saveAsItem.addActionListener(listener);
		fileMenu.add(saveAsItem);
		
		JMenu importSubMenu = new JMenu("Import");
		JMenuItem importSpssItem = new JMenuItem("SPSS");
		importSpssItem.setActionCommand(ProgramCommand.IMPORT_SPSS.toString());
		importSpssItem.addActionListener(listener);
		importSubMenu.add(importSpssItem);

		JMenuItem importTextsItem = new JMenuItem("User written texts");
		importTextsItem.setActionCommand(ProgramCommand.IMPORT_TEXTS.toString());
		importTextsItem.addActionListener(listener);
		importSubMenu.add(importTextsItem);
		
		fileMenu.add(importSubMenu);
		
		
		JMenu exportSubMenu = new JMenu("Export");
		JMenuItem exportTextsItem = new JMenuItem("User written texts");
		exportTextsItem.setActionCommand(ProgramCommand.EXPORT_TEXTS.toString());
		exportTextsItem.addActionListener(listener);
		exportSubMenu.add(exportTextsItem);
		
		JMenuItem uploadFileItem = new JMenuItem("File for server upload");
		uploadFileItem.setActionCommand(ProgramCommand.EXPORT_UPLOAD_FILE.toString());
		uploadFileItem.addActionListener(listener);
		exportSubMenu.add(uploadFileItem);

		fileMenu.add(exportSubMenu);		
		
		JMenuItem discardItem = new JMenuItem("Discard all changes");
		discardItem.setActionCommand(ProgramCommand.DISCARD_CHANGES.toString());
		discardItem.addActionListener(listener);
		fileMenu.add(discardItem);
		
		JMenuItem quitItem = new JMenuItem("Quit");
		quitItem.setActionCommand(ProgramCommand.QUIT.toString());
		quitItem.addActionListener(listener);
		fileMenu.add(quitItem);
		
		add(fileMenu);
		
		
		// Edit menu
		// TODO add edit menu stuff
		
		// Something menu - TODO: rename this menu
		JMenu somethingMenu = new JMenu("Something");
		
		JMenuItem settingsItem = new JMenuItem("Program settings");
		settingsItem.setActionCommand(ProgramCommand.SETTINGS.toString());
		settingsItem.addActionListener(listener);
		somethingMenu.add(settingsItem);
		
		JMenuItem infoItem = new JMenuItem("File info");
		infoItem.setActionCommand(ProgramCommand.INFO.toString());
		infoItem.addActionListener(listener);
		somethingMenu.add(infoItem);
		
		add(somethingMenu);
		
		
		// Help menu 
		JMenu helpMenu = new JMenu("Help");
		
		JMenuItem helpItem = new JMenuItem("Online manual");
		helpItem.setActionCommand(ProgramCommand.HELP.toString());
		helpItem.addActionListener(listener);
		helpMenu.add(helpItem);
		
		JMenuItem aboutItem = new JMenuItem("About");
		aboutItem.setActionCommand(ProgramCommand.ABOUT.toString());
		aboutItem.addActionListener(listener);
		helpMenu.add(aboutItem);
		
		add(helpMenu);
	}
	
}
