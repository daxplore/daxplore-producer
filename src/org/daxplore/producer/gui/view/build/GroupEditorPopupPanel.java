package org.daxplore.producer.gui.view.build;

import java.awt.GridLayout;
import java.awt.Label;

import javax.swing.JPanel;
import javax.swing.JTextField;

import org.daxplore.producer.gui.resources.GuiTexts;

@SuppressWarnings("serial")
public class GroupEditorPopupPanel extends JPanel {
	
	private JTextField technicalTextField;
	private JTextField userTextField;
	
	public GroupEditorPopupPanel(GuiTexts texts, String currentTechicalText, String currentUserText) {
		setLayout(new GridLayout(2, 2));
		
		int i = 0;
		add(new Label("Techincal text:"), i++);
		technicalTextField = new JTextField(currentTechicalText);
		add(technicalTextField, i++);

		add(new Label("Display text:"), i++);
		userTextField = new JTextField(currentUserText);
		add(userTextField, i++);
	}

	public String getTechnicalText() {
		return technicalTextField.getText();
	}

	public String getUserText() {
		return userTextField.getText();
	}

}
