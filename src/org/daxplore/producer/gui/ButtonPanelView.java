package org.daxplore.producer.gui;


import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;

import org.daxplore.producer.gui.MainController.Views;

/**
 * Left button panel 
 */
@SuppressWarnings("serial")
public class ButtonPanelView extends JPanel {
	
	// data fields.
	private JRadioButton openButton;
	private JRadioButton groupsButton;
	private JRadioButton editButton;
	private JRadioButton timeSeriesButton;
	private JRadioButton toolsButton;
	private ButtonGroup buttonGroup;
	
	public ButtonPanelView(ActionListener mainController) {
		
		buttonGroup = new ButtonGroup();
		
		// create the button panel
		setBorder(new MatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));
		setLayout(new GridLayout(1, 0, 0, 0));
		
		openButton = new JRadioButton("");
		openButton.setSelected(true);
		openButton.setToolTipText("Manage file(s)");
		openButton.setActionCommand(Views.OPENFILEVIEW.toString());
		openButton.addActionListener(mainController);

		openButton.setRolloverEnabled(false);
		openButton.setSelectedIcon(new ImageIcon(MainView.class.getResource("/org/daxplore/producer/gui/resources/8_selected.png")));
		openButton.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		buttonGroup.add(openButton);
		openButton.setIcon(new ImageIcon(MainController.class.getResource("/org/daxplore/producer/gui/resources/8.png")));
		add(openButton);

		groupsButton = new JRadioButton("");
		groupsButton.setToolTipText("Edit groups and questions");
		groupsButton.setActionCommand(Views.GROUPSVIEW.toString());
		groupsButton.addActionListener(mainController);
		
		groupsButton.setSelectedIcon(new ImageIcon(ButtonPanelView.class.getResource("/org/daxplore/producer/gui/resources/29_selected.png")));
		groupsButton.setRolloverEnabled(false);
		groupsButton.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		buttonGroup.add(groupsButton);
		groupsButton.setIcon(new ImageIcon(ButtonPanelView.class.getResource("/org/daxplore/producer/gui/resources/29.png")));
		add(groupsButton);
	
		editButton = new JRadioButton("");
		editButton.setToolTipText("Edit questions");
		editButton.setActionCommand(Views.EDITTEXTVIEW.toString());
		editButton.addActionListener(mainController);
		
		editButton.setSelectedIcon(new ImageIcon(MainController.class.getResource("/org/daxplore/producer/gui/resources/21_selected.png")));
		editButton.setRolloverEnabled(false);
		editButton.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		buttonGroup.add(editButton);
		editButton.setIcon(new ImageIcon(MainController.class.getResource("/org/daxplore/producer/gui/resources/21.png")));
		add(editButton);

		timeSeriesButton = new JRadioButton("");
		timeSeriesButton.setToolTipText("Time series");
		timeSeriesButton.setActionCommand(Views.TIMESERIESVIEW.toString());
		timeSeriesButton.addActionListener(mainController);
		
		timeSeriesButton.setSelectedIcon(new ImageIcon(MainController.class.getResource("/org/daxplore/producer/gui/resources/2_selected.png")));
		timeSeriesButton.setRolloverEnabled(false);
		timeSeriesButton.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		buttonGroup.add(timeSeriesButton);
		timeSeriesButton.setIcon(new ImageIcon(MainController.class.getResource("/org/daxplore/producer/gui/resources/2.png")));
		add(timeSeriesButton);
		
		toolsButton = new JRadioButton("");
		toolsButton.setToolTipText("Tools section");
		toolsButton.setActionCommand(Views.TOOLSVIEW.toString());
		toolsButton.addActionListener(mainController);
				
		toolsButton.setSelectedIcon(new ImageIcon(ButtonPanelView.class.getResource("/org/daxplore/producer/gui/resources/24_selected.png")));
		toolsButton.setRolloverEnabled(false);
		toolsButton.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		buttonGroup.add(toolsButton);
		toolsButton.setIcon(new ImageIcon(ButtonPanelView.class.getResource("/org/daxplore/producer/gui/resources/24.png")));
		add(toolsButton);
		
		setActive(false);
	}

	public void setActive(boolean active) {
		// disable buttons if no file is loaded.
		groupsButton.setEnabled(active);
		editButton.setEnabled(active);
		timeSeriesButton.setEnabled(active);
		toolsButton.setEnabled(active);
	}
	
	public void setActiveButton(Views view) {
		switch(view) {
		case OPENFILEVIEW:
			openButton.setSelected(true);
			break;
		case GROUPSVIEW:
			groupsButton.setSelected(true);
			break;
		case EDITTEXTVIEW:
			editButton.setSelected(true);
			break;
		case TIMESERIESVIEW:
			timeSeriesButton.setSelected(true);
			break;
		case TOOLSVIEW:
			toolsButton.setSelected(true);
			break;
		case QUESTIONVIEW: // TODO unused enum
		default:
			buttonGroup.clearSelection();
		}
	}
}