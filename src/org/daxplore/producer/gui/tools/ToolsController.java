package org.daxplore.producer.gui.tools;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.SQLException;
import java.util.Locale;

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.gui.MainController;

import com.beust.jcommander.Strings;

public class ToolsController implements ActionListener {

	enum ToolsCommand {
		IMPORT, REPLACE_TIMEPOINTS, GENERATE_DATA
	}
	
	private MainController mainController;
	private ToolsView toolsView;
	
	public ToolsController(MainController mainController) {
		this.mainController = mainController;
		toolsView = new ToolsView(this);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		switch (ToolsCommand.valueOf(e.getActionCommand())) {
		case IMPORT:
			String localeText = toolsView.getUserLocaleText();
			if(Strings.isStringEmpty(localeText)) {
				Locale locale = new Locale(localeText);
				if(!locale.toLanguageTag().equals("und")) {
					try {
						mainController.getDaxploreFile().importFromRaw(locale);
					} catch (DaxploreException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
			break;
		case REPLACE_TIMEPOINTS:
			try {
				mainController.getDaxploreFile().replaceAllTimepointsInQuestions();
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
				mainController.getDaxploreFile().writeUploadFile(uploadFile);
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
		if(mainController.fileIsSet()) {
			LocalesTableModel localeTableModel = new LocalesTableModel(mainController.getDaxploreFile().getAbout(), mainController);
			toolsView.setLocaleTable(localeTableModel);
		}
	}

	public Component getView() {
		return toolsView;
	}
}
