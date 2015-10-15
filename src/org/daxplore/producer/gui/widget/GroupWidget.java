/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui.widget;

import java.awt.BorderLayout;
import java.awt.Color;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.daxplore.producer.daxplorelib.metadata.MetaGroup;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;
import org.daxplore.producer.gui.GuiSettings;
import org.daxplore.producer.gui.event.DisplayLocaleSelectEvent;

import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

@SuppressWarnings("serial")
public class GroupWidget extends JPanel implements AbstractWidgetEditor<MetaGroup> {

	private JLabel textField;
	private JLabel idField;
	private MetaGroup metaGroup;
	private Locale locale;
	private String htmlFormat = "<html><b>{0}</b></html>";
	private String missingFormat = "<html><i>missing group name</i></html>";
	private String idFormat = "<html>(<b>{0}</b>)</html>";
	
	public GroupWidget(EventBus eventBus) {
		setLayout(new BorderLayout());
		eventBus.register(this);
		locale = GuiSettings.getCurrentDisplayLocale();
		textField = new JLabel();
		textField.setBorder(new EmptyBorder(5, 5, 5, 15));
		add(textField, BorderLayout.WEST);
		idField = new JLabel();
		add(idField, BorderLayout.CENTER);
	}
	
	@Override
	public MetaGroup getContent() {
		return metaGroup;
	}

	@Override
	public void setContent(MetaGroup value) {
		this.metaGroup = value;
		if(metaGroup.getTextRef().hasLocale(locale)) {
			textField.setText(MessageFormat.format(htmlFormat, getLabelText()));
		} else {
			textField.setText(missingFormat);
		}
		idField.setText(MessageFormat.format(idFormat, value.getTextRef().getRef()));
		idField.setForeground(Color.GRAY);
		
	}
	
	private String getLabelText() {
		TextReference textRef = metaGroup.getTextRef();
	
		if(locale == null) {
			return textRef.getRef();
		}
	
		String text = textRef.getText(locale);
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

}
