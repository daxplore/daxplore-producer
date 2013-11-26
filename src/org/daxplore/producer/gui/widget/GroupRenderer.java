package org.daxplore.producer.gui.widget;

import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.daxplore.producer.daxplorelib.metadata.MetaGroup;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;
import org.daxplore.producer.gui.Settings;
import org.daxplore.producer.gui.event.DisplayLocaleSelectEvent;

import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

@SuppressWarnings("serial")
public class GroupRenderer extends AbstractWidget<MetaGroup> {
	
	private MetaGroup metaGroup;
	private JLabel label;
	private Locale locale;
	
	public GroupRenderer(EventBus eventBus) {
		eventBus.register(this);
		locale = Settings.getCurrentDisplayLocale();
		label = new JLabel();
		label.setHorizontalAlignment(SwingConstants.LEFT);
		add(label);
	}
	

	@Override
	public void setContent(MetaGroup value) {
		this.metaGroup = value;
		label.setText(getLabelText());
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
