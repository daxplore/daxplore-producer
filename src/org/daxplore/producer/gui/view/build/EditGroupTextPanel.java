package org.daxplore.producer.gui.view.build;

import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;
import org.daxplore.producer.gui.resources.GuiTexts;

import com.google.common.base.Strings;

@SuppressWarnings("serial")
public class EditGroupTextPanel extends JPanel implements DocumentListener {
	
	private GuiTexts texts;
	private JTextField textRefIdField;
	private Map<Locale, JTextField> localeTextFields = new HashMap<>();
	private JLabel badTextRefIdLabel;
	
	public EditGroupTextPanel(GuiTexts texts, List<Locale> localesToEdit, TextReference textRef) {
		this.texts = texts;
		setLayout(new BorderLayout(0, 10));
		add(new JLabel(texts.get("edit.grouptexts.header")), BorderLayout.NORTH);
		
		JPanel editPanel = new JPanel(new GridLayout(localesToEdit.size()+1, 2));
		
		int i = 0;
		editPanel.add(new Label(texts.get("edit.grouptexts.textrefid")), i++);
		textRefIdField = new JTextField(textRef.getRef());
		editPanel.add(textRefIdField, i++);
		textRefIdField.getDocument().addDocumentListener(this);

		for(Locale l : localesToEdit) {
			//TODO use correct locale to display locale
			Label label = new Label(MessageFormat.format(texts.get("edit.grouptexts.forlang"), l.getDisplayLanguage(Locale.ENGLISH))); 
			editPanel.add(label, i++);
			
			JTextField textField = new JTextField(textRef.get(l));
			localeTextFields.put(l, textField);
			editPanel.add(textField, i++);
		}
		
		add(editPanel, BorderLayout.CENTER);
		
		badTextRefIdLabel = new JLabel();
		badTextRefIdLabel.setForeground(Color.RED);
		add(badTextRefIdLabel, BorderLayout.SOUTH);
		updateValidTextRefWarning();
	}

	public String getNewTextRefId() {
		return textRefIdField.getText();
	}

	public Map<Locale, String> getNewTexts() {
		Map<Locale, String> resultingTexts = new HashMap<>();
		for(Locale l : localeTextFields.keySet()) {
			resultingTexts.put(l, localeTextFields.get(l).getText());
		}
		return resultingTexts;
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		updateValidTextRefWarning();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		updateValidTextRefWarning();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		updateValidTextRefWarning();
	}
	
	private void updateValidTextRefWarning() {
		String textRefId = textRefIdField.getText();
		boolean valid = !Strings.isNullOrEmpty(textRefId) && textRefId.matches("^[a-zA-Z0-9_]*$"); 
		if(valid) {
			badTextRefIdLabel.setText(" ");
		} else {
			badTextRefIdLabel.setText(texts.get("edit.grouptexts.badtextref"));
		}
	}
}
