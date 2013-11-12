package org.daxplore.producer.gui.open;

import java.awt.Component;
import java.text.SimpleDateFormat;

import org.daxplore.producer.daxplorelib.DaxploreFile;
import org.daxplore.producer.gui.event.DaxploreFileUpdateEvent;

import com.google.common.eventbus.Subscribe;

/**
 * Daxplore file creation controller. Controls all action logic in the open panel view.
 */
public final class OpenFileController {

	//TODO remove direct main controller
	private DaxploreFile daxploreFile;
	
	private final OpenFileView view;

	public OpenFileController() {
		view = new OpenFileView();
	}
	
	@Subscribe
	public void on(DaxploreFileUpdateEvent e) {
		daxploreFile = e.getDaxploreFile();
		updateTextFields();
	}

	/**
	 * Updates text field for the SPSS file information in the open panel dialog.
	 * @param mainController
	 */
	public void updateSpssFileInfoText() {
		//TODO communicate properly? Remove stuff?
		view.setSpssFileInfoText("SPSS file possibly imported!\n");
	}

	/**
	 * Updates text fields in the open panel dialog to display daxplore file information.
	 * @param mainController
	 */
	public void updateTextFields() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		
		// set the text fields if we have a daxplore file loaded.
		if (daxploreFile != null) {
			// update text fields with appropriate data.
			view.getFileNameField().setText(daxploreFile.getFile().getName());
			
			// check if it's a newly created file, if so, it doesn't contain certain fields.
			String importFilename = daxploreFile.getAbout().getImportFilename();
			if (importFilename != null && !"".equals(importFilename)) {
				view.getLastImportFileNameField().setText(daxploreFile.getAbout().getImportFilename());
				// date must first be converted to the appropriate format before returned as string.
				if (daxploreFile.getAbout().getImportDate() != null) {
				view.getImportDateField().setText(formatter.format(daxploreFile.getAbout().getImportDate()));
				} else {
					view.getImportDateField().setText("");
				}
			} else {
				view.getLastImportFileNameField().setText("");
				view.getImportDateField().setText("");
			}
			
			view.getCreationDateField().setText(
			formatter.format(daxploreFile.getAbout().getCreationDate()));
		}
	}

	public Component getView() {
		return view;
	}
}
