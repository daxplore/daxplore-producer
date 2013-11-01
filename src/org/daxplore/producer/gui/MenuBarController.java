package org.daxplore.producer.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.daxplore.producer.gui.event.EmptyEvents.SaveFileEvent;
import org.daxplore.producer.gui.event.EmptyEvents.QuitProgramEvent;

import com.google.common.eventbus.EventBus;

public class MenuBarController implements ActionListener{
	
	public enum MenuBarCommand {
		NEW, OPEN, SAVE, SAVE_AS, IMPORT_SPSS, IMPORT_TEXTS, EXPORT_TEXTS, EXPORT_UPLOAD_FILE, DISCARD_CHANGES, QUIT,
		SETTINGS, INFO,
		HELP, ABOUT, 
	}
	
	private MenuBarView view;
	private EventBus eventBus;
	public MenuBarController(EventBus eventBus) {
		this.eventBus = eventBus;
		view = new MenuBarView(this);
	}
	
	public MenuBarView getView() {
		return view;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch(MenuBarCommand.valueOf(e.getActionCommand())) {
		case ABOUT:
			break;
		case DISCARD_CHANGES:
			break;
		case EXPORT_TEXTS:
			break;
		case EXPORT_UPLOAD_FILE:
			break;
		case HELP:
			break;
		case IMPORT_SPSS:
			break;
		case IMPORT_TEXTS:
			break;
		case INFO:
			break;
		case NEW:
			break;
		case OPEN:
			break;
		case QUIT:
			eventBus.post(new QuitProgramEvent());
			break;
		case SAVE:
			eventBus.post(new SaveFileEvent());
			break;
		case SAVE_AS:
			break;
		case SETTINGS:
			break;
		default:
			break;
		}
		
	}
	
}
