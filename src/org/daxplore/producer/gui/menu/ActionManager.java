/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui.menu;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.daxplore.producer.daxplorelib.DaxploreFile;
import org.daxplore.producer.gui.event.DaxploreFileUpdateEvent;
import org.daxplore.producer.gui.event.EmptyEvents.DiscardChangesEvent;
import org.daxplore.producer.gui.event.EmptyEvents.ExportTextsEvent;
import org.daxplore.producer.gui.event.EmptyEvents.ExportUploadEvent;
import org.daxplore.producer.gui.event.EmptyEvents.HistoryGoBackEvent;
import org.daxplore.producer.gui.event.EmptyEvents.ImportSpssEvent;
import org.daxplore.producer.gui.event.EmptyEvents.ImportTextsEvent;
import org.daxplore.producer.gui.event.EmptyEvents.QuitProgramEvent;
import org.daxplore.producer.gui.event.EmptyEvents.SaveFileEvent;
import org.daxplore.producer.gui.event.ErrorMessageEvent;
import org.daxplore.producer.gui.resources.IconResources;
import org.daxplore.producer.gui.resources.UITexts;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class ActionManager {
	
	public final Action ABOUT, BACK, DISCARD_CHANGES, EXPORT_TEXTS, EXPORT_UPLOAD, HELP, IMPORT_SPSS,
			IMPORT_TEXTS, INFO, NEW, OPEN, QUIT, SAVE, SAVE_AS, SETTINGS;
	private DaxploreFile daxploreFile;
	
	/**
	 * An {@link Action} class that automatically adds texts and icons
	 * loaded by {@link UITexts} and {@link IconResources}, if available.
	 * 
	 * <p>Texts are expected to have they key <b>action.&lt;systemName&gt;.name</b> in {@link UITexts}.
	 * An action may also have the optional key <b>action.&lt;systemName&gt;.tooltip</b>.</p>
	 * 
	 * <p>Both icon images are optional. They are expected to be be .png files named
	 * <b>&lt;systemName&gt;-small.png</b> and <b>&lt;systemName&gt;-large.png</b>.</p>
	 */
	@SuppressWarnings("serial")
	private abstract static class ResourcedAction extends AbstractAction {
		public ResourcedAction(String systemName) {
			super(UITexts.get("action." + systemName + ".name"));
			
			String tooltipKey = "action." + systemName + ".tooltip";
			if(UITexts.contains(tooltipKey)) {
				String tooltip = UITexts.get(tooltipKey);
				if(!tooltip.isEmpty()) {
					putValue(Action.SHORT_DESCRIPTION, tooltip);
				}
			}
			
			putValue(Action.SMALL_ICON, IconResources.getIcon(systemName + "-small.png"));
			putValue(Action.LARGE_ICON_KEY, IconResources.getIcon(systemName + "-large.png"));
		}
	}
	
	@SuppressWarnings("serial")
	public ActionManager(final EventBus eventBus) {
		eventBus.register(this);
		
		ABOUT = new ResourcedAction("about") {
			@Override
			public void actionPerformed(ActionEvent e) {
				
			}
		};
		
		BACK = new ResourcedAction("back") {
			@Override
			public void actionPerformed(ActionEvent e) {
				eventBus.post(new HistoryGoBackEvent());
			}
		};
		
		
		DISCARD_CHANGES = new ResourcedAction("discard_changes") {
			@Override
			public void actionPerformed(ActionEvent e) {
				eventBus.post(new DiscardChangesEvent());
			}
		};
		
		EXPORT_TEXTS = new ResourcedAction("export_texts") {
			@Override
			public void actionPerformed(ActionEvent e) {
				eventBus.post(new ExportTextsEvent());
			}
		};
		
		EXPORT_UPLOAD = new ResourcedAction("export_upload") {
			@Override
			public void actionPerformed(ActionEvent e) {
				eventBus.post(new ExportUploadEvent());
			}
		};
		
		HELP = new ResourcedAction("help") {
			@Override
			public void actionPerformed(ActionEvent e) {
				String helpUrlString = UITexts.get("action.help.url");
				try {
					URL helpSite = new URL(helpUrlString);
					Desktop.getDesktop().browse(helpSite.toURI());
				} catch (UnsupportedOperationException | IOException | URISyntaxException e1) {
					String errorMessage = UITexts.format("action.help.open_error", helpUrlString);
					eventBus.post(new ErrorMessageEvent(errorMessage, e1));
				}
			}
		};
		HELP.putValue(Action.SHORT_DESCRIPTION, UITexts.format("action.help.tooltip", UITexts.get("action.help.url")));
		
		IMPORT_SPSS = new ResourcedAction("import_spss") {
			@Override
			public void actionPerformed(ActionEvent e) {
				eventBus.post(new ImportSpssEvent());
			}
		};
		
		IMPORT_TEXTS = new ResourcedAction("import_texts") {
			@Override
			public void actionPerformed(ActionEvent e) {
				eventBus.post(new ImportTextsEvent());
			}
		};
		
		INFO = new ResourcedAction("info") {
			@Override
			public void actionPerformed(ActionEvent e) {
				
			}
		};
		
		NEW = new ResourcedAction("new") {
			@Override
			public void actionPerformed(ActionEvent e) {
				
			}
		};
		
		OPEN = new ResourcedAction("open") {
			@Override
			public void actionPerformed(ActionEvent e) {
				
			}
		};
		
		QUIT = new ResourcedAction("quit") {
			@Override
			public void actionPerformed(ActionEvent e) {
				eventBus.post(new QuitProgramEvent());
			}
		};
		
		SAVE = new ResourcedAction("save") {
			@Override
			public void actionPerformed(ActionEvent e) {
				eventBus.post(new SaveFileEvent());
			}
		};
		
		SAVE_AS = new ResourcedAction("save_as") {
			@Override
			public void actionPerformed(ActionEvent e) {
				//TODO: temporary hack
				eventBus.post(new DaxploreFileUpdateEvent(daxploreFile));
			}
		};
		
		SETTINGS = new ResourcedAction("settings") {
			@Override
			public void actionPerformed(ActionEvent e) {
				
			}
		};
	}
	
	@Subscribe
	public void on(DaxploreFileUpdateEvent e) {
		try {
			this.daxploreFile = e.getDaxploreFile();
		} catch (Exception ex) {
			Logger.getGlobal().log(Level.SEVERE, "Failed to handle event", ex);
		}
	}
}
