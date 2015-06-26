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
import java.awt.GridLayout;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;
import org.daxplore.producer.gui.Settings;
import org.daxplore.producer.gui.event.DisplayLocaleSelectEvent;
import org.daxplore.producer.gui.resources.Colors;

import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

@SuppressWarnings("serial")
public class QuestionWidget extends JPanel implements AbstractWidgetEditor<MetaQuestion> {
	
	private MetaQuestion metaQuestion;

	private String idFormat = "<html><b>{0}</b></html>";
	private JLabel idLabel = new JLabel(MessageFormat.format(idFormat, "placeholder"));
	
	private final String shortTextFormat = "<html>{0}</html>";
	private JLabel shortTextLabel = new JLabel(MessageFormat.format(shortTextFormat, "placeholder"));

	private final String longTextFormat = "{0}";
	private JLabel longTextLabel = new JLabel(MessageFormat.format(longTextFormat, "placeholder"));
	
	private Locale locale;
	boolean compact = false;
	
	public QuestionWidget(EventBus eventBus) {
		this(eventBus, false);
	}
 	
	public QuestionWidget(final EventBus eventBus, boolean compact) {
		this.compact = compact;
		eventBus.register(this);
		locale = Settings.getCurrentDisplayLocale();
		
		JPanel topRowPanel = new JPanel(new BorderLayout());
		if(compact) {
			idFormat = "<html>(<b>{0}</b>)</html>";
			idLabel.setForeground(Color.GRAY);
			shortTextLabel.setBorder(new EmptyBorder(0, 0, 0, 15));
			topRowPanel.add(shortTextLabel, BorderLayout.WEST);
			topRowPanel.add(idLabel, BorderLayout.CENTER);
			add(topRowPanel, BorderLayout.WEST);
		} else {
			setLayout(new GridLayout(1, 1));
			JPanel containerPanel = new JPanel(new BorderLayout(0, 4));
			containerPanel.setBorder(new EmptyBorder(4, 4, 4, 4));
			shortTextLabel.setForeground(Color.GRAY);
			topRowPanel.add(idLabel, BorderLayout.WEST);
			topRowPanel.add(shortTextLabel, BorderLayout.EAST);
			topRowPanel.setBackground(Colors.transparent);
			containerPanel.add(topRowPanel, BorderLayout.NORTH);
			longTextLabel.setForeground(Color.GRAY);
			containerPanel.add(longTextLabel, BorderLayout.SOUTH);
			add(containerPanel, 0);
		}
	}
	
	@Override
	public void setContent(MetaQuestion value) {
		this.metaQuestion = value;
		
		idLabel.setText(MessageFormat.format(idFormat, metaQuestion.getColumn()));
		
		String shortText = metaQuestion.getShortTextRef().get(locale);
		if(Strings.isNullOrEmpty(shortText)) {
			shortText = "<i>missing short text</i>";
		}
		shortTextLabel.setText(MessageFormat.format(shortTextFormat, shortText));

		String longText = metaQuestion.getFullTextRef().get(locale);
		if(Strings.isNullOrEmpty(longText)) {
			longText = "";
		} else {
			setToolTipText(longText);
		}
		
		longTextLabel.setText(MessageFormat.format(longTextFormat, longText));
	}

	@Override
	public MetaQuestion getContent() {
		return metaQuestion;
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
