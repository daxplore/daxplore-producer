/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui.view.time;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;

import org.daxplore.producer.gui.SectionHeader;
import org.daxplore.producer.gui.resources.GuiTexts;
import org.daxplore.producer.gui.view.time.TimeSeriesController.TimeSeriesCommand;

@SuppressWarnings("serial")
public class TimeSeriesView extends JPanel {

	private GuiTexts texts;
	
	private JScrollPane columnValueCountPane;
	private JScrollPane scrollPane;
	private JTextField timeSeriesTextField;
	
	private DocumentListener documentListener;
	private ActionListener actionListener;
	
	/**
	 * Create the panel.
	 */
	public <T extends DocumentListener & ActionListener> TimeSeriesView(GuiTexts texts, T listener) {
		this.texts = texts;
		documentListener = listener;
		actionListener = listener;

		setLayout(new BorderLayout());
		
		add(new SectionHeader(texts, "timeseries"), BorderLayout.NORTH);
		add(buildTimepointSelectionPanel(), BorderLayout.CENTER);
		add(buildUtilityPanel(), BorderLayout.SOUTH);
	}
	
	private JPanel buildTimepointSelectionPanel() {
		JPanel selectionPanel = new JPanel(new BorderLayout(0, 0));
		
		scrollPane = new JScrollPane();
		selectionPanel.add(scrollPane, BorderLayout.CENTER);
		
		JPanel panel_1 = new JPanel();
		selectionPanel.add(panel_1, BorderLayout.WEST);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_2 = new JPanel();
		panel_1.add(panel_2, BorderLayout.NORTH);
		
		JLabel timeSeriesColumnLabel = new JLabel("Time Series Column:");
		panel_2.add(timeSeriesColumnLabel);
		
		timeSeriesTextField = new JTextField();
		panel_2.add(timeSeriesTextField);
		timeSeriesTextField.setColumns(10);
		
		JButton setColumnButton = new JButton("Set");
		setColumnButton.setActionCommand(TimeSeriesCommand.SET_COLUMN.toString());
		setColumnButton.addActionListener(actionListener);
		panel_2.add(setColumnButton);
		
		timeSeriesTextField.getDocument().addDocumentListener(documentListener);
		
		columnValueCountPane = new JScrollPane();
		panel_1.add(columnValueCountPane, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		selectionPanel.add(panel, BorderLayout.SOUTH);
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JButton addButton = new JButton("Add");
		addButton.setActionCommand(TimeSeriesCommand.ADD.toString());
		addButton.addActionListener(actionListener);
		panel.add(addButton);
		
		JButton removeButton = new JButton("Remove");
		removeButton.setActionCommand(TimeSeriesCommand.REMOVE.toString());
		removeButton.addActionListener(actionListener);
		panel.add(removeButton);
		
		JButton upButton = new JButton("Up");
		upButton.setActionCommand(TimeSeriesCommand.UP.toString());
		upButton.addActionListener(actionListener);
		panel.add(upButton);
		
		JButton downButton = new JButton("Down");
		downButton.setActionCommand(TimeSeriesCommand.DOWN.toString());
		downButton.addActionListener(actionListener);
		panel.add(downButton);
		
		return selectionPanel;
	}
	
	private JPanel buildUtilityPanel() {
		JPanel utilityPanel = new JPanel(new BorderLayout());
		
		utilityPanel.add(new SectionHeader(texts, "replace_timepoints"), BorderLayout.NORTH);
		
		JButton addTimepointsButton = new JButton("Replace timepoints");
		addTimepointsButton.setBounds(27, 189, 185, 47);
		addTimepointsButton.setActionCommand(TimeSeriesCommand.REPLACE_TIMEPOINTS.toString());
		addTimepointsButton.addActionListener(actionListener);
		
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		panel.add(addTimepointsButton);
		utilityPanel.add(panel, BorderLayout.CENTER);
		
		return utilityPanel;
	}
	
	/**
	 * @return the columnValueCountPane
	 */
	public JScrollPane getColumnValueCountPane() {
		return columnValueCountPane;
	}

	public JScrollPane getTimeSeriesScrollPane() {
		return scrollPane;
	}
	
	public String getTimeSeriesColumn() {
		return timeSeriesTextField.getText();
	}
	
	public void setTimeSeriesColumn(String column) {
		timeSeriesTextField.setText(column);
	}
	
	public String getTimeSeriesText() {
		return timeSeriesTextField.getText();
	}
}
