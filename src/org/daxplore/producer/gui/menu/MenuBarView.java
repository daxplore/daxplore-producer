package org.daxplore.producer.gui.menu;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

@SuppressWarnings("serial")
public class MenuBarView extends JMenuBar {
	
	public MenuBarView(ActionManager actionManager) {
		
		// File menu
		JMenu fileMenu = new JMenu("File");
		
		fileMenu.add(actionManager.NEW);
		fileMenu.add(actionManager.OPEN);
		fileMenu.add(actionManager.SAVE);
		fileMenu.add(actionManager.SAVE_AS);

		JMenu importSubMenu = new JMenu("Import");
		importSubMenu.add(actionManager.IMPORT_SPSS);
		importSubMenu.add(actionManager.IMPORT_TEXTS);
		fileMenu.add(importSubMenu);
		
		JMenu exportSubMenu = new JMenu("Export");
		exportSubMenu.add(actionManager.EXPORT_UPLOAD);
		exportSubMenu.add(actionManager.EXPORT_TEXTS);
		fileMenu.add(exportSubMenu);
		
		fileMenu.add(actionManager.DISCARD_CHANGES);
		fileMenu.add(actionManager.QUIT);
		
		add(fileMenu);

		
		// Edit menu
		// TODO add edit menu (copy, paste, cut, search)
		
		// Something menu - TODO: rename this menu
		JMenu somethingMenu = new JMenu("Something");
		somethingMenu.add(actionManager.SETTINGS);
		somethingMenu.add(actionManager.INFO);
		add(somethingMenu);
		
		// Help menu 
		JMenu helpMenu = new JMenu("Help");
		helpMenu.add(actionManager.HELP);
		helpMenu.add(actionManager.ABOUT);
		add(helpMenu);
	}
	
}
