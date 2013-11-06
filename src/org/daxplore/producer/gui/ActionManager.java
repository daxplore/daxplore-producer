package org.daxplore.producer.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.daxplore.producer.gui.event.EmptyEvents.HistoryGoBackEvent;
import org.daxplore.producer.gui.event.EmptyEvents.QuitProgramEvent;
import org.daxplore.producer.gui.event.EmptyEvents.SaveFileEvent;

import com.google.common.eventbus.EventBus;

public class ActionManager {
	public final Action ABOUT, BACK, DISCARD_CHANGES, EXPORT_TEXTS, EXPORT_UPLOAD_FILE, HELP, IMPORT_SPSS,
			IMPORT_TEXTS, INFO, NEW, OPEN, QUIT, SAVE, SAVE_AS, SETTINGS;
	
	@SuppressWarnings("serial")
	public ActionManager(final EventBus eventBus) {
		
		ABOUT = new AbstractAction("About") {
			@Override
			public void actionPerformed(ActionEvent e) {
				
			}
		};
		
		BACK = new AbstractAction("Back") {
			@Override
			public void actionPerformed(ActionEvent e) {
				eventBus.post(new HistoryGoBackEvent());
			}
		};
		
		
		DISCARD_CHANGES = new AbstractAction("Discard changes") {
			@Override
			public void actionPerformed(ActionEvent e) {
				
			}
		};
		
		EXPORT_TEXTS = new AbstractAction("Export texts") {
			@Override
			public void actionPerformed(ActionEvent e) {
				
			}
		};
		
		EXPORT_UPLOAD_FILE = new AbstractAction("Export upload file") {
			@Override
			public void actionPerformed(ActionEvent e) {
				
			}
		};
		
		HELP = new AbstractAction("Help") {
			@Override
			public void actionPerformed(ActionEvent e) {
				
			}
		};
		
		IMPORT_SPSS = new AbstractAction("Import from SPSS file") {
			@Override
			public void actionPerformed(ActionEvent e) {
				
			}
		};
		
		IMPORT_TEXTS = new AbstractAction("Import texts") {
			@Override
			public void actionPerformed(ActionEvent e) {
				
			}
		};
		
		INFO = new AbstractAction("Info about this project") {
			@Override
			public void actionPerformed(ActionEvent e) {
				
			}
		};
		
		NEW = new AbstractAction("New project") {
			@Override
			public void actionPerformed(ActionEvent e) {
				
			}
		};
		
		OPEN = new AbstractAction("Open project") {
			@Override
			public void actionPerformed(ActionEvent e) {
				
			}
		};
		
		QUIT = new AbstractAction("Quit") {
			@Override
			public void actionPerformed(ActionEvent e) {
				eventBus.post(new QuitProgramEvent());
			}
		};
		
		SAVE = new AbstractAction("Save") {
			@Override
			public void actionPerformed(ActionEvent e) {
				eventBus.post(new SaveFileEvent());
			}
		};
		
		SAVE_AS = new AbstractAction("Save project as...") {
			@Override
			public void actionPerformed(ActionEvent e) {
				
			}
		};
		
		SETTINGS = new AbstractAction("Program settings") {
			@Override
			public void actionPerformed(ActionEvent e) {
				
			}
		};
	}
}
