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

	private final String idFormat = "<html><b>{0}</b></html>";
	private JLabel idLabel = new JLabel("placeholder");
	
	private final String shortTextFormat = "<html>{0}</html>";
	private JLabel shortTextLabel = new JLabel("placeholder");

	private final String longTextFormat = "{0}";
	private JLabel longTextLabel = new JLabel("placeholder");
	
	private Locale locale;
	
	public QuestionWidget(EventBus eventBus, MetaQuestion metaQuestion) {
		this(eventBus);
		setContent(metaQuestion);
	}
 	
	public QuestionWidget(final EventBus eventBus) {
		eventBus.register(this);
		setLayout(new BorderLayout(0, 7));
		setBorder(new EmptyBorder(9, 7, 8, 7));
		
		shortTextLabel.setForeground(Color.GRAY);
		longTextLabel.setForeground(Color.GRAY);
		
		JPanel topRowPanel = new JPanel(new BorderLayout());
		topRowPanel.add(idLabel, BorderLayout.WEST);
		topRowPanel.add(shortTextLabel, BorderLayout.EAST);
		add(topRowPanel, BorderLayout.NORTH);
		add(longTextLabel, BorderLayout.CENTER);
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
