/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.daxplore.producer.daxplorelib.DaxploreFile;
import org.daxplore.producer.daxplorelib.metadata.MetaGroup.GroupType;
import org.daxplore.producer.daxplorelib.metadata.MetaGroup.MetaGroupManager;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;
import org.daxplore.producer.gui.resources.GuiTexts;

@SuppressWarnings("serial")
public class DialogsPanels extends JPanel implements DocumentListener {
	
	private GuiTexts texts;
	private JButton okButton;
	private JTextField textRefIdField;
	private JLabel textRefIdLabel;
	private Map<Locale, JTextField> localeTextFields = new HashMap<>();
	private JLabel badTextRefIdLabel;
	private JCheckBox headerCheckbox;
	
	public static DialogsPanels createGroupPanel(MetaGroupManager mgm, GuiTexts texts, JButton okButton, List<Locale> localesToEdit) {
		DialogsPanels panel = new DialogsPanels(texts, okButton, localesToEdit, true, true);
		String suggestedId = mgm.getSuggestedRefName(GroupType.QUESTIONS);
		panel.textRefIdField.setText(suggestedId);
		panel.headerCheckbox.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if(panel.headerCheckbox.isSelected()) {
					panel.textRefIdField.setText(mgm.getSuggestedRefName(GroupType.HEADER));
				} else {
					panel.textRefIdField.setText(mgm.getSuggestedRefName(GroupType.QUESTIONS));
				}
			}
		});
		return panel;
	}
	
	public static DialogsPanels editGroupPanel(GuiTexts texts, JButton okButton, List<Locale> localesToEdit, TextReference textRef) {
		DialogsPanels panel = new DialogsPanels(texts, okButton, localesToEdit, true, false);
		panel.textRefIdField.setText(textRef.getRef());
		for(Locale l : localesToEdit) {
			panel.localeTextFields.get(l).setText(textRef.getText(l));
		}
		return panel;
	}
	
	public static DialogsPanels textRefEditPanel(GuiTexts texts, JButton okButton, List<Locale> localesToEdit,
			TextReference textRef, boolean allowRefstringEdit) {
		
		DialogsPanels panel = new DialogsPanels(texts, okButton, localesToEdit, allowRefstringEdit, false);
		if(allowRefstringEdit) {
			panel.textRefIdField.setText(textRef.getRef());
		} else {
			panel.textRefIdLabel.setText(textRef.getRef());
		}
		for(Locale l : localesToEdit) {
			panel.localeTextFields.get(l).setText(textRef.getText(l));
		}
		return panel;
	}
		
	private DialogsPanels(GuiTexts texts, JButton okButton, List<Locale> localesToEdit, boolean allowRefstringEdit, boolean createHeaderCheckbox) {
		this.texts = texts;
		this.okButton = okButton;
		
		setLayout(new BorderLayout(0, 10));
		add(new JLabel(texts.get("edit.texts.header")), BorderLayout.NORTH);
		
		JPanel editPanel = new JPanel(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;

		c.weightx = 1;
		Insets insetTop = new Insets(10, 0, 0, 0);
		Insets insetNone = new Insets(0, 0, 0, 0);

		c.insets = insetTop;
		Label label = new Label(texts.get("edit.texts.textrefid"));
		editPanel.add(label, c);
		
		c.weightx = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		if(allowRefstringEdit) {
			textRefIdField = new JTextField();
			editPanel.add(textRefIdField, c);
			textRefIdField.getDocument().addDocumentListener(this);
		} else {
			textRefIdLabel = new JLabel();
			editPanel.add(textRefIdLabel, c);
		}
		
		c.insets = insetNone;
		c.weightx = 0;
		
		for(Locale l : localesToEdit) {
			c.insets = insetTop;
			label = new Label(MessageFormat.format(texts.get("edit.texts.forlang"), l.getDisplayLanguage(GuiSettings.getProgramLocale()))); 
			editPanel.add(label, c);
			c.insets = insetNone;

			c.gridwidth = GridBagConstraints.REMAINDER;
			JTextField textField = new JTextField();
			localeTextFields.put(l, textField);
			editPanel.add(textField, c);
		}
		

		if(createHeaderCheckbox) {
			c.insets = new Insets(20, 0, 0, 0);
			c.gridwidth = GridBagConstraints.REMAINDER;
			headerCheckbox = new JCheckBox(texts.get("edit.texts.asheader"));
			editPanel.add(headerCheckbox, c);
		}

		add(editPanel, BorderLayout.CENTER);
		
		badTextRefIdLabel = new JLabel();
		badTextRefIdLabel.setForeground(Color.RED);
		add(badTextRefIdLabel, BorderLayout.SOUTH);
		if(allowRefstringEdit) {
			updateValidTextRefWarning();
		}
		
		JTextField defaultTextField = localeTextFields.get(GuiSettings.getCurrentDisplayLocale());
		
		defaultTextField.addAncestorListener(new AncestorListener() {
			@Override
			public void ancestorRemoved(AncestorEvent event) {}
			@Override
			public void ancestorMoved(AncestorEvent event) {}
			@Override
			public void ancestorAdded(AncestorEvent event) {
				event.getComponent().requestFocusInWindow();
			}
		});
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
	
	public boolean isHeaderGroup() {
		return headerCheckbox != null && headerCheckbox.isSelected();
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
		if(DaxploreFile.isValidColumnName(textRefId)) {
			badTextRefIdLabel.setText(" ");
			okButton.setEnabled(true);
		} else {
			badTextRefIdLabel.setText(texts.get("edit.texts.badtextref"));
			okButton.setEnabled(false);
		}
	}
}
