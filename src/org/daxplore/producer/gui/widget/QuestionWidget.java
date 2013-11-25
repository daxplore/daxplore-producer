package org.daxplore.producer.gui.widget;

import java.awt.BorderLayout;
import java.awt.Color;
import java.text.MessageFormat;
import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;
import org.daxplore.producer.gui.event.DisplayLocaleSelectEvent;

import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

@SuppressWarnings("serial")
public class QuestionWidget extends AbstractWidgetEditor<MetaQuestion> {
	
	private MetaQuestion metaQuestion;

	private String idFormat = "<html><b>{0}</b></html>";
	private JLabel idLabel = new JLabel("placeholder");
	
	private final String shortTextFormat = "<html>{0}</html>";
	private JLabel shortTextLabel = new JLabel("placeholder");

	private final String longTextFormat = "{0}";
	private JLabel longTextLabel = new JLabel("placeholder");
	
	private Locale locale;
	boolean compact = false;
	
	public QuestionWidget(EventBus eventBus) {
		this(eventBus, false);
	}
 	
	public QuestionWidget(final EventBus eventBus, boolean compact) {
		this.compact = compact;
		eventBus.register(this);
		
		JPanel topRowPanel = new JPanel(new BorderLayout());
		if(compact) {
			idFormat = "<html>(<b>{0}</b>)</html>";
			idLabel.setForeground(Color.GRAY);
			shortTextLabel.setBorder(new EmptyBorder(0, 0, 0, 15));
			topRowPanel.add(shortTextLabel, BorderLayout.WEST);
			topRowPanel.add(idLabel, BorderLayout.EAST);
		} else {
			shortTextLabel.setForeground(Color.GRAY);
			topRowPanel.add(idLabel, BorderLayout.WEST);
			topRowPanel.add(shortTextLabel, BorderLayout.EAST);
			
			longTextLabel.setForeground(Color.GRAY);
			setLayout(new BorderLayout(0, 7));
			setBorder(new EmptyBorder(9, 7, 8, 7));
			add(longTextLabel, BorderLayout.CENTER);
		}
		add(topRowPanel, BorderLayout.NORTH);
	}
	
	@Override
	public void setContent(MetaQuestion value) {
		this.metaQuestion = value;
		
		idLabel.setText(MessageFormat.format(idFormat, metaQuestion.getId()));
		
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
		locale = e.getLocale();
	}
}
