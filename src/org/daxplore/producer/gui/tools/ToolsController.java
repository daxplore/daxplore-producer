package org.daxplore.producer.gui.tools;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.SQLException;
import java.util.Locale;

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.DaxploreFile;
import org.daxplore.producer.gui.event.DaxploreFileUpdateEvent;
import org.daxplore.producer.gui.event.EmptyEvents.RawImportEvent;

import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class ToolsController implements ActionListener {

	enum ToolsCommand {
		IMPORT, REPLACE_TIMEPOINTS, GENERATE_DATA
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
	public void onDaxploreFileUpdate(DaxploreFileUpdateEvent e) {
		this.daxploreFile = e.getDaxploreFile();
		loadData();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		switch (ToolsCommand.valueOf(e.getActionCommand())) {
		case IMPORT:
			String localeText = toolsView.getUserLocaleText();
			if(!Strings.isNullOrEmpty(localeText)) {
				Locale locale = new Locale(localeText);
				if(!locale.toLanguageTag().equals("und")) {
					try {
						daxploreFile.importFromRaw(locale);
						eventBus.post(new RawImportEvent());
					} catch (DaxploreException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
			break;
		case REPLACE_TIMEPOINTS:
			try {
				daxploreFile.replaceAllTimepointsInQuestions();
			} catch (DaxploreException|SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			break;
		case  GENERATE_DATA:
			File uploadFile = toolsView.showExportDialog();
			if(uploadFile == null) {
				return;
			}
			
			try {
				daxploreFile.writeUploadFile(uploadFile);
			} catch (DaxploreException e1) {
				//TODO communicate error to user
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
