/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui.view.tools;

import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.daxplore.producer.gui.DaxplorePreferences;
import org.daxplore.producer.gui.view.tools.ToolsController.ToolsCommand;

@SuppressWarnings("serial")
public class ToolsView extends JPanel {
	
	private JTextField textField;
	private JScrollPane scrollPane;
	private JTable table;
	
	ToolsView(ActionListener listener) {
		setLayout(null);
		
		JButton addTimepointsButton = new JButton("Replace timepoints");
		addTimepointsButton.setBounds(27, 189, 185, 47);
		addTimepointsButton.setActionCommand(ToolsCommand.REPLACE_TIMEPOINTS.toString());
		addTimepointsButton.addActionListener(listener);
		add(addTimepointsButton);

		scrollPane = new JScrollPane();
		scrollPane.setBounds(301, 234, 294, 204);
		add(scrollPane);
	}
	
	File showExportDialog(DaxplorePreferences preferences) {
		JFileChooser fc = new JFileChooser(preferences.getWorkingDirectory());
		
		FileFilter filter = new FileNameExtensionFilter("Zip files", "zip");
		fc.addChoosableFileFilter(filter);
		fc.setFileFilter(filter);
		
		int returnVal = fc.showSaveDialog(this);
		switch(returnVal) {
		case JFileChooser.APPROVE_OPTION:
			preferences.setWorkingDirectory(fc.getCurrentDirectory());
			return fc.getSelectedFile();
		default:
			return null;
		}
	}

	void setLocaleTable(LocalesTableModel tableModel) {
		table = new JTable(tableModel);
		scrollPane.setViewportView(table);
	}

	String getUserLocaleText() {
		return textField.getText();
	}
	
}
