/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui.view.tools;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.DaxploreFile;
import org.daxplore.producer.gui.event.DaxploreFileUpdateEvent;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class ToolsController implements ActionListener {

	enum ToolsCommand {
		REPLACE_TIMEPOINTS
	}
	
	private EventBus eventBus;
	private DaxploreFile daxploreFile;
	private ToolsView toolsView;
	
	public ToolsController(EventBus eventBus) {
		this.eventBus = eventBus;
		eventBus.register(this);
		
		toolsView = new ToolsView(this);
	}
	
	@Subscribe
	public void on(DaxploreFileUpdateEvent e) {
		this.daxploreFile = e.getDaxploreFile();
		loadData();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		switch (ToolsCommand.valueOf(e.getActionCommand())) {
		case REPLACE_TIMEPOINTS:
			try {
				daxploreFile.replaceAllTimepointsInQuestions();
			} catch (DaxploreException|SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			break;
		default:
			throw new AssertionError("Not a defined command: '" + e.getActionCommand() + "'");
		}
	}
	
	public void loadData() {
		if(daxploreFile != null) {
			LocalesTableModel localeTableModel = new LocalesTableModel(eventBus, daxploreFile.getAbout());
			toolsView.setLocaleTable(localeTableModel);
		}
	}

	public Component getView() {
		return toolsView;
	}
}
