package org.daxplore.producer.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.daxplore.producer.gui.event.EmptyEvents.*;

import com.google.common.eventbus.EventBus;

public class ProgramCommandListener implements ActionListener {

	public enum ProgramCommand {
		NEW, OPEN, SAVE, SAVE_AS, IMPORT_SPSS, IMPORT_TEXTS, EXPORT_TEXTS, EXPORT_UPLOAD_FILE, DISCARD_CHANGES, QUIT,
		SETTINGS, INFO,
		HELP, ABOUT, BACK, 
	}


	private EventBus eventBus;


	public ProgramCommandListener(EventBus eventBus) {
		this.eventBus = eventBus;
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		switch(ProgramCommand.valueOf(e.getActionCommand())) {
		case ABOUT:
			break;
		case BACK:
			eventBus.post(new HistoryGoBackEvent());
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
