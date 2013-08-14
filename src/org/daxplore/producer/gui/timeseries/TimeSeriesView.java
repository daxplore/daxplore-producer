package org.daxplore.producer.gui.timeseries;

import javax.swing.JPanel;

import java.awt.BorderLayout;

import javax.swing.JScrollPane;

import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.daxplore.producer.gui.MainController;

@SuppressWarnings("serial")
public class TimeSeriesView extends JPanel {

	private JScrollPane scrollPane;
	private TimeSeriesController controller;
	private JTextField timeSeriesTextField;
	private JScrollPane columnValueCountPane;


	/**
	 * @return the columnValueCountPane
	 */
	public JScrollPane getColumnValueCountPane() {
		return columnValueCountPane;
	}

	/**
	 * Create the panel.
	 */
	public TimeSeriesView(MainController mainController) {
		setLayout(new BorderLayout(0, 0));
		
		controller = new TimeSeriesController(mainController, this);
		
		scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);
		
		JPanel panel_1 = new JPanel();
		add(panel_1, BorderLayout.WEST);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_2 = new JPanel();
		panel_1.add(panel_2, BorderLayout.NORTH);
		
		JLabel timeSeriesColumnLabel = new JLabel("Time Series Column:");
		panel_2.add(timeSeriesColumnLabel);
		
		timeSeriesTextField = new JTextField();
		panel_2.add(timeSeriesTextField);
		timeSeriesTextField.setColumns(10);
		
		JButton setColumnButton = new JButton("Set");
		setColumnButton.setActionCommand(TimeSeriesController.TIMESERIES_SET_COLUMN_ACTION_COMMAND);
		setColumnButton.addActionListener(controller);
		panel_2.add(setColumnButton);
		
		
		timeSeriesTextField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				controller.filter(timeSeriesTextField.getText());
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				controller.filter(timeSeriesTextField.getText());
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				controller.filter(timeSeriesTextField.getText());
			}
		});
		
		
		columnValueCountPane = new JScrollPane();
		panel_1.add(columnValueCountPane, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.SOUTH);
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JButton addButton = new JButton("Add");
		addButton.setActionCommand(TimeSeriesController.TIMEPOINT_ADD_ACTION_COMMAND);
		addButton.addActionListener(controller);
		panel.add(addButton);
		
		JButton removeButton = new JButton("Remove");
		removeButton.setActionCommand(TimeSeriesController.TIMEPOINT_REMOVE_ACTION_COMMAND);
		removeButton.addActionListener(controller);
		panel.add(removeButton);
		
		JButton upButton = new JButton("Up");
		upButton.setActionCommand(TimeSeriesController.TIMEPOINT_UP_ACTION_COMMAND);
		upButton.addActionListener(controller);
		panel.add(upButton);
		
		JButton downButton = new JButton("Down");
		downButton.setActionCommand(TimeSeriesController.TIMEPOINT_DOWN_ACTION_COMMAND);
		downButton.addActionListener(controller);
		panel.add(downButton);
		
	}

	public JScrollPane getTimeSeriesScrollPane() {
		return scrollPane;
	}

	public TimeSeriesController getController() {
		return controller;
	}
	
	public String getTimeSeriesColumn() {
		return timeSeriesTextField.getText();
	}
	
	public void setTimeSeriesColumn(String column) {
		timeSeriesTextField.setText(column);
	}
}
