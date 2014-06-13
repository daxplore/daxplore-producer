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

public class ToolsController {

	private EventBus eventBus;
	private DaxploreFile daxploreFile;
	private ToolsView toolsView;
	
	public ToolsController(EventBus eventBus) {
		this.eventBus = eventBus;
		eventBus.register(this);
		
		toolsView = new ToolsView();
	}
	
	@Subscribe
	public void on(DaxploreFileUpdateEvent e) {
		this.daxploreFile = e.getDaxploreFile();
		loadData();
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
