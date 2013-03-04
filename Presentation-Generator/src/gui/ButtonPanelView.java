package gui;


import gui.MainController.Views;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;

/**
 * Left button panel 
 * @author jorgenrosen
 *
 */
@SuppressWarnings("serial")
public class ButtonPanelView extends JPanel {
	
	// data fields.
	private MainController mainController;
	private JRadioButton openButton;
	private JRadioButton groupsButton;
	private JRadioButton editButton;
	private JRadioButton toolsButton;
	private ButtonGroup buttonGroup;
	
	
	// getters and setters.
	public JRadioButton getOpenButton() {
		return openButton;
	}

	public void setOpenButton(JRadioButton openButton) {
		this.openButton = openButton;
	}

	public JRadioButton getGroupsButton() {
		return groupsButton;
	}

	public void setGroupsButton(JRadioButton groupsButton) {
		this.groupsButton = groupsButton;
	}

	public JRadioButton getEditButton() {
		return editButton;
	}

	public void setEditButton(JRadioButton editButton) {
		this.editButton = editButton;
	}

	public JRadioButton getToolsButton() {
		return toolsButton;
	}

	public void setToolsButton(JRadioButton toolsButton) {
		this.toolsButton = toolsButton;
	}

	public ButtonPanelView(final MainController mainController) {
		
		buttonGroup = new ButtonGroup();
		this.mainController = mainController;
		
		// create the button panel
		setBorder(new MatteBorder(0, 0, 0, 1, (Color) Color.LIGHT_GRAY));
		setLayout(new GridLayout(0, 1, 0, 0));
		
		openButton = new JRadioButton("");
		openButton.setSelected(true);
		openButton.setToolTipText("Manage file(s)");
		openButton.setActionCommand(Views.OPENFILEVIEW.toString());
		openButton.addActionListener(mainController);

		openButton.setRolloverEnabled(false);
		openButton.setSelectedIcon(new ImageIcon(MainController.class.getResource("/gui/resources/8_selected.png")));
		openButton.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		buttonGroup.add(openButton);
		openButton.setIcon(new ImageIcon(MainController.class.getResource("/gui/resources/8.png")));
		add(openButton);

		groupsButton = new JRadioButton("");
		groupsButton.setToolTipText("Edit groups and questions");
		groupsButton.setActionCommand(Views.GROUPSVIEW.toString());
		groupsButton.addActionListener(mainController);
		
		groupsButton.setSelectedIcon(new ImageIcon(ButtonPanelView.class.getResource("/gui/resources/29_selected.png")));
		groupsButton.setRolloverEnabled(false);
		groupsButton.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		buttonGroup.add(groupsButton);
		groupsButton.setIcon(new ImageIcon(ButtonPanelView.class.getResource("/gui/resources/29.png")));
		add(groupsButton);
	
		editButton = new JRadioButton("");
		editButton.setToolTipText("Edit questions");
		editButton.setActionCommand(Views.EDITTEXTVIEW.toString());
		editButton.addActionListener(mainController);
		
		editButton.setSelectedIcon(new ImageIcon(MainController.class.getResource("/gui/resources/21_selected.png")));
		editButton.setRolloverEnabled(false);
		editButton.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		buttonGroup.add(editButton);
		editButton.setIcon(new ImageIcon(MainController.class.getResource("/gui/resources/21.png")));
		add(editButton);
		
		toolsButton = new JRadioButton("");
		toolsButton.setToolTipText("Tools section");
		toolsButton.setActionCommand(Views.TOOLSVIEW.toString());
		toolsButton.addActionListener(mainController);
				
		toolsButton.setSelectedIcon(new ImageIcon(ButtonPanelView.class.getResource("/gui/resources/24_selected.png")));
		toolsButton.setRolloverEnabled(false);
		toolsButton.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		buttonGroup.add(toolsButton);
		toolsButton.setIcon(new ImageIcon(ButtonPanelView.class.getResource("/gui/resources/24.png")));
		add(toolsButton);
		
		updateButtonPanel();
	}

	public void updateButtonPanel() {
		// disable buttons if no file is loaded.
		if (!mainController.fileIsSet()) {
			groupsButton.setEnabled(false);
			editButton.setEnabled(false);
			toolsButton.setEnabled(false);
		}
		else {
			groupsButton.setEnabled(true);
			editButton.setEnabled(true);
			toolsButton.setEnabled(true);
		}
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
		case TOOLSVIEW:
			toolsButton.setSelected(true);
			break;
		default:
			buttonGroup.clearSelection();
		}
	}
}