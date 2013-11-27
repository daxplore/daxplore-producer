package org.daxplore.producer.gui.widget;

import java.awt.BorderLayout;
import java.awt.Color;
import java.text.MessageFormat;
import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import org.daxplore.producer.daxplorelib.metadata.MetaGroup;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;
import org.daxplore.producer.gui.Settings;
import org.daxplore.producer.gui.event.DisplayLocaleSelectEvent;
import org.daxplore.producer.gui.event.EmptyEvents;

import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

@SuppressWarnings("serial")
public class GroupWidget extends AbstractWidgetEditor<MetaGroup> {

	private EventBus eventBus;
	private JLabel textField;
	private MetaGroup metaGroup;
	private Locale locale;
	private String htmlFormat = "<html><b>{0}</b></html>";
	
	public GroupWidget(EventBus eventBus) {
		this.eventBus = eventBus;
		setLayout(new BorderLayout());
		eventBus.register(this);
		locale = Settings.getCurrentDisplayLocale();
		textField = new JLabel();
		textField.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(textField, BorderLayout.WEST);
	}
	
	@Override
	public MetaGroup getContent() {
		return metaGroup;
	}

	@Override
	public void setContent(MetaGroup value) {
		this.metaGroup = value;
		textField.setText(MessageFormat.format(htmlFormat, getLabelText()));
		textField.setForeground(Color.BLACK);
		
		eventBus.post(new EmptyEvents.RepaintWindowEvent());
	}
	
	private String getLabelText() {
		TextReference textRef = metaGroup.getTextRef();
	
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
		locale = e.getLocale();
	}

}
