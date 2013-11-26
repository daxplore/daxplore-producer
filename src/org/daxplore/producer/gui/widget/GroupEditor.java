package org.daxplore.producer.gui.widget;

import java.util.Locale;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.daxplore.producer.daxplorelib.metadata.MetaGroup;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;
import org.daxplore.producer.gui.Settings;
import org.daxplore.producer.gui.event.DisplayLocaleSelectEvent;

import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

@SuppressWarnings("serial")
public class GroupEditor extends AbstractWidgetEditor<MetaGroup> {

	private JTextField textField;
	private MetaGroup metaGroup;
	private Locale locale;
	
	public GroupEditor(EventBus eventBus) {
		eventBus.register(this);
		locale = Settings.getCurrentDisplayLocale();
		textField = new JTextField();
		add(textField);
	}
	
	@Override
	public MetaGroup getContent() {
		return metaGroup;
	}

	@Override
	public void setContent(MetaGroup value) {
		this.metaGroup = value;
		textField.setText(getLabelText());
		textField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				if(!Strings.isNullOrEmpty(textField.getText()) && locale != null) {
					metaGroup.getTextRef().put(textField.getText(), locale);
				}
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				if(locale != null) {
					metaGroup.getTextRef().put(textField.getText(), locale);
				}
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				if(locale != null) {
					metaGroup.getTextRef().put(textField.getText(), locale);
				}

			}
		});
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
		textField.setEditable(locale != null);
	}

}
