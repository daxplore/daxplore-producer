package org.daxplore.producer.gui.view.build;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;
import org.daxplore.producer.gui.resources.GuiTexts;

public class EditTextRefPanel extends JPanel {
	
	private JTextField technicalTextField;
	
	private Map<Locale, JTextField> userTextFields = new HashMap<>();
	
	public EditTextRefPanel(GuiTexts texts, List<Locale> localesToEdit, TextReference textRef) {
		setLayout(new BorderLayout(0, 10));
		add(new JLabel("You can edit some or all of the texts"), BorderLayout.NORTH);
		
		JPanel editPanel = new JPanel(new GridLayout(localesToEdit.size()+1, 2));
		
		int i = 0;
		editPanel.add(new Label("Techincal ID:"), i++);
		technicalTextField = new JTextField(textRef.getRef());
		editPanel.add(technicalTextField, i++);

		for(Locale l : localesToEdit) {
			//TODO use correct locale to display locale
			Label label = new Label(MessageFormat.format("{0} text:", l.getDisplayLanguage(Locale.ENGLISH))); 
			editPanel.add(label, i++);
			
			JTextField textField = new JTextField(textRef.get(l));
			userTextFields.put(l, textField);
			editPanel.add(textField, i++);
		}
		
		add(editPanel, BorderLayout.CENTER);
	}

	public String getNewTextRefId() {
		return technicalTextField.getText();
	}

	public Map<Locale, String> getNewTexts() {
		Map<Locale, String> resultingTexts = new HashMap<>();
		for(Locale l : userTextFields.keySet()) {
			resultingTexts.put(l, userTextFields.get(l).getText());
		}
		return resultingTexts;
	}

}
