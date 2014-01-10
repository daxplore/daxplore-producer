/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui.menu;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;

import org.daxplore.producer.daxplorelib.DaxploreFile;
import org.daxplore.producer.gui.event.DaxploreFileUpdateEvent;
import org.daxplore.producer.gui.event.DisplayLocaleSelectEvent;
import org.daxplore.producer.gui.event.EmptyEvents.LocaleAddedOrRemovedEvent;
import org.daxplore.producer.gui.event.EmptyEvents.RepaintWindowEvent;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class ToolbarController implements ActionListener {
	
	enum ToolbarCommand {
		SELECT_LOCALE
	}
	
	private EventBus eventBus;
	private DaxploreFile daxploreFile;
	private ToolbarView view;
	
	public ToolbarController(EventBus eventBus, ActionManager actionManager) {
		this.eventBus = eventBus;
		view = new ToolbarView(this, actionManager);
		eventBus.register(this);
	}
	
	public Component getView() {
		return view;
	}
	
	@Subscribe
	public void on(DaxploreFileUpdateEvent e) {
		daxploreFile = e.getDaxploreFile();
		view.setLocales(daxploreFile.getAbout().getLocales());
		updateSelectedLocale();
	}

	@Subscribe
	public void on(LocaleAddedOrRemovedEvent e) {
		view.setLocales(daxploreFile.getAbout().getLocales());
		updateSelectedLocale();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch(ToolbarCommand.valueOf(e.getActionCommand())) {
		case SELECT_LOCALE:
			if(view.getSelectedLocale() != null) {
				updateSelectedLocale();
			}
			break;
		default:
			throw new AssertionError("Unimplemented action command");
		}
	}
	
	public void updateSelectedLocale() {
		Locale locale = view.getSelectedLocale().locale;
		eventBus.post(new DisplayLocaleSelectEvent(locale));
		eventBus.post(new RepaintWindowEvent());
	}
}
