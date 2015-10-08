/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui.widget;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;
import org.daxplore.producer.gui.Dialogs;
import org.daxplore.producer.gui.GuiSettings;
import org.daxplore.producer.gui.event.DisplayLocaleSelectEvent;
import org.daxplore.producer.gui.resources.Colors;
import org.daxplore.producer.gui.resources.GuiTexts;

import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

@SuppressWarnings("serial")
public class TextWidget extends JPanel implements AbstractWidgetEditor<TextReference> {

	private TextReference textRef;
	private JLabel label = new JLabel();
	private JButton editButton;
	private boolean enabled = true;
	
	private Locale locale; 
	
	public TextWidget(final EventBus eventBus, final GuiTexts texts) {
		eventBus.register(this);
		locale = GuiSettings.getCurrentDisplayLocale();
		setLayout(new BorderLayout(0, 0));
		locale = GuiSettings.getCurrentDisplayLocale();
		add(label, BorderLayout.CENTER);
		label.setHorizontalAlignment(SwingConstants.LEFT);
		JPanel editPanel = new JPanel();
		editPanel.setBackground(Colors.transparent);
		editButton = new JButton(texts.get("general.button.edit"));
		editButton.setEnabled(false);
		editPanel.add(editButton);

		editButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean edited = Dialogs.editTextRefDialog(editButton, texts, textRef.getActiveLocales(), textRef);
				if (edited) {
					setContent(textRef);
				}
			}
		});
		add(editPanel, BorderLayout.EAST);
	}
	
	public void showEdit(boolean show) {
		editButton.setVisible(show);
	}
	
	@Override
	public TextReference getContent() {
		return textRef;
	}

	@Override
	public void setContent(TextReference textRef) {
		this.textRef = textRef;
		label.setText(getLabelText());
		editButton.setEnabled(enabled);
	}
	
	private String getLabelText() {
		if(locale == null) {
			return textRef.getRef();
		}
	
		String text = textRef.get(locale);
		if(Strings.isNullOrEmpty(text)) {
			return textRef.getRef();
		}

		return text;
	}
	
	@Subscribe
	public void on(DisplayLocaleSelectEvent e) {
		try {
			locale = e.getLocale();
		} catch (Exception ex) {
			Logger.getGlobal().log(Level.SEVERE, "Failed to handle event", ex);
		}
	}
	
	@Override
	public String getToolTipText() {
		return label.getText();
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		editButton.setEnabled(enabled);
		label.setEnabled(enabled);
	}
}
