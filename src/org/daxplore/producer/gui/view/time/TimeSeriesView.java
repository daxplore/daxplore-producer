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

import org.daxplore.producer.gui.view.time.TimeSeriesController.TimeSeriesCommand;

@SuppressWarnings("serial")
public class TimeSeriesView extends JPanel {

	private JScrollPane scrollPane;
	private JTextField timeSeriesTextField;
	private JScrollPane columnValueCountPane;

	/**
	 * Create the panel.
	 */
	public <T extends DocumentListener & ActionListener> TimeSeriesView(T listener) {
		setLayout(new BorderLayout(0, 0));
		
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
		setColumnButton.setActionCommand(TimeSeriesCommand.SET_COLUMN.toString());
		setColumnButton.addActionListener(listener);
		panel_2.add(setColumnButton);
		
		timeSeriesTextField.getDocument().addDocumentListener(listener);
		
		columnValueCountPane = new JScrollPane();
		panel_1.add(columnValueCountPane, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.SOUTH);
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JButton addButton = new JButton("Add");
		addButton.setActionCommand(TimeSeriesCommand.ADD.toString());
		addButton.addActionListener(listener);
		panel.add(addButton);
		
		JButton removeButton = new JButton("Remove");
		removeButton.setActionCommand(TimeSeriesCommand.REMOVE.toString());
		removeButton.addActionListener(listener);
		panel.add(removeButton);
		
		JButton upButton = new JButton("Up");
		upButton.setActionCommand(TimeSeriesCommand.UP.toString());
		upButton.addActionListener(listener);
		panel.add(upButton);
		
		JButton downButton = new JButton("Down");
		downButton.setActionCommand(TimeSeriesCommand.DOWN.toString());
		downButton.addActionListener(listener);
		panel.add(downButton);
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
