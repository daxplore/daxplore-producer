package org.daxplore.producer.gui.tools;

import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.daxplore.producer.gui.Settings;
import org.daxplore.producer.gui.tools.ToolsController.ToolsCommand;

@SuppressWarnings("serial")
public class ToolsView extends JPanel {
	
	private JTextField textField;
	private JScrollPane scrollPane;
	private JTable table;
	
	ToolsView(ActionListener listener) {
		setLayout(null);
		
		textField = new JTextField();
		textField.setBounds(147, 10, 114, 19);
		add(textField);
		textField.setColumns(10);
		
		JButton addTimepointsButton = new JButton("Replace timepoints");
		addTimepointsButton.setBounds(27, 189, 185, 47);
		addTimepointsButton.setActionCommand(ToolsCommand.REPLACE_TIMEPOINTS.toString());
		addTimepointsButton.addActionListener(listener);
		add(addTimepointsButton);
		
		JButton generateDataButton = new JButton("Generate Data");
		generateDataButton.setBounds(247, 101, 191, 25);
		generateDataButton.setActionCommand(ToolsCommand.GENERATE_DATA.toString());
		generateDataButton.addActionListener(listener);
		add(generateDataButton);
		
		scrollPane = new JScrollPane();
		scrollPane.setBounds(301, 234, 294, 204);
		add(scrollPane);
	}
	
	File showExportDialog() {
		JFileChooser fc = new JFileChooser(Settings.getWorkingDirectory());
		
		FileFilter filter = new FileNameExtensionFilter("Zip files", "zip");
		fc.addChoosableFileFilter(filter);
		fc.setFileFilter(filter);
		
		int returnVal = fc.showSaveDialog(this);
		switch(returnVal) {
		case JFileChooser.APPROVE_OPTION:
			Settings.setWorkingDirectory(fc.getCurrentDirectory());
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
