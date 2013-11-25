package org.daxplore.producer.gui.menu;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.daxplore.producer.daxplorelib.DaxploreFile;
import org.daxplore.producer.gui.event.DaxploreFileUpdateEvent;
import org.daxplore.producer.gui.event.EmptyEvents.DiscardChangesEvent;
import org.daxplore.producer.gui.event.EmptyEvents.ExportTextsEvent;
import org.daxplore.producer.gui.event.EmptyEvents.ExportUploadEvent;
import org.daxplore.producer.gui.event.EmptyEvents.HistoryGoBackEvent;
import org.daxplore.producer.gui.event.EmptyEvents.ImportTextsEvent;
import org.daxplore.producer.gui.event.EmptyEvents.QuitProgramEvent;
import org.daxplore.producer.gui.event.EmptyEvents.SaveFileEvent;
import org.daxplore.producer.gui.event.ErrorMessageEvent;
import org.daxplore.producer.gui.resources.GuiTexts;
import org.daxplore.producer.gui.resources.IconResources;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class ActionManager {
	public final Action ABOUT, BACK, DISCARD_CHANGES, EXPORT_TEXTS, EXPORT_UPLOAD, HELP, IMPORT_SPSS,
			IMPORT_TEXTS, INFO, NEW, OPEN, QUIT, SAVE, SAVE_AS, SETTINGS;
	private DaxploreFile daxploreFile;
	
	/**
	 * An {@link Action} class that automatically adds texts and icons
	 * loaded by {@link GuiTexts} and {@link IconResources}, if available.
	 * 
	 * <p>Texts are expected to have they key <b>action.&lt;systemName&gt;.name</b> in {@link GuiTexts}.
	 * An action may also have the optional key <b>action.&lt;systemName&gt;.tooltip</b>.</p>
	 * 
	 * <p>Both icon images are optional. They are expected to be be .png files named
	 * <b>&lt;systemName&gt;-small.png</b> and <b>&lt;systemName&gt;-large.png</b>.</p>
	 */
	@SuppressWarnings("serial")
	private abstract static class ResourcedAction extends AbstractAction {
		public ResourcedAction(GuiTexts texts, String systemName) {
			super(texts.get("action." + systemName + ".name"));
			
			String tooltipKey = "action." + systemName + ".tooltip";
			if(texts.contains(tooltipKey)) {
				String tooltip = texts.get(tooltipKey);
				if(!tooltip.isEmpty()) {
					putValue(Action.SHORT_DESCRIPTION, tooltip);
				}
			}
			
			putValue(Action.SMALL_ICON, IconResources.getIcon(systemName + "-small.png"));
			putValue(Action.LARGE_ICON_KEY, IconResources.getIcon(systemName + "-large.png"));
		}
	}
	
	@SuppressWarnings("serial")
	public ActionManager(final EventBus eventBus, final GuiTexts texts) {
		eventBus.register(this);
		
		ABOUT = new ResourcedAction(texts, "about") {
			@Override
			public void actionPerformed(ActionEvent e) {
				
			}
		};
		
		BACK = new ResourcedAction(texts, "back") {
			@Override
			public void actionPerformed(ActionEvent e) {
				eventBus.post(new HistoryGoBackEvent());
			}
		};
		
		
		DISCARD_CHANGES = new ResourcedAction(texts, "discard_changes") {
			@Override
			public void actionPerformed(ActionEvent e) {
				eventBus.post(new DiscardChangesEvent());
			}
		};
		
		EXPORT_TEXTS = new ResourcedAction(texts, "export_texts") {
			@Override
			public void actionPerformed(ActionEvent e) {
				eventBus.post(new ExportTextsEvent());
			}
		};
		
		EXPORT_UPLOAD = new ResourcedAction(texts, "export_upload") {
			@Override
			public void actionPerformed(ActionEvent e) {
				eventBus.post(new ExportUploadEvent());
			}
		};
		
		HELP = new ResourcedAction(texts, "help") {
			@Override
			public void actionPerformed(ActionEvent e) {
				String helpUrlString = texts.get("action.help.url");
				try {
					URL helpSite = new URL(helpUrlString);
					Desktop.getDesktop().browse(helpSite.toURI());
				} catch (UnsupportedOperationException | IOException | URISyntaxException e1) {
					String errorMessage = texts.format("action.help.open_error", helpUrlString);
					eventBus.post(new ErrorMessageEvent(errorMessage, e1));
				}
			}
		};
		HELP.putValue(Action.SHORT_DESCRIPTION, texts.format("action.help.tooltip", texts.get("action.help.url")));
		
		IMPORT_SPSS = new ResourcedAction(texts, "import_spss") {
			@Override
			public void actionPerformed(ActionEvent e) {
				
			}
		};
		
		IMPORT_TEXTS = new ResourcedAction(texts, "import_texts") {
			@Override
			public void actionPerformed(ActionEvent e) {
				eventBus.post(new ImportTextsEvent());
			}
		};
		
		INFO = new ResourcedAction(texts, "info") {
			@Override
			public void actionPerformed(ActionEvent e) {
				
			}
		};
		
		NEW = new ResourcedAction(texts, "new") {
			@Override
			public void actionPerformed(ActionEvent e) {
				
			}
		};
		
		OPEN = new ResourcedAction(texts, "open") {
			@Override
			public void actionPerformed(ActionEvent e) {
				
			}
		};
		
		QUIT = new ResourcedAction(texts, "quit") {
			@Override
			public void actionPerformed(ActionEvent e) {
				eventBus.post(new QuitProgramEvent());
			}
		};
		
		SAVE = new ResourcedAction(texts, "save") {
			@Override
			public void actionPerformed(ActionEvent e) {
				eventBus.post(new SaveFileEvent());
			}
		};
		
		SAVE_AS = new ResourcedAction(texts, "save_as") {
			@Override
			public void actionPerformed(ActionEvent e) {
				//TODO: temporary hack
				eventBus.post(new DaxploreFileUpdateEvent(daxploreFile));
			}
		};
		
		SETTINGS = new ResourcedAction(texts, "settings") {
			@Override
			public void actionPerformed(ActionEvent e) {
				
			}
		};
	}
	
	@Subscribe
	public void on(DaxploreFileUpdateEvent e) {
		this.daxploreFile = e.getDaxploreFile();
	}
}
