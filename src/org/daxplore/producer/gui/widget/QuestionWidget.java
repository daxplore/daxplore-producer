package org.daxplore.producer.gui.widget;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;

import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;
import org.daxplore.producer.gui.MainController.Views;
import org.daxplore.producer.gui.event.ChangeMainViewEvent;
import org.daxplore.producer.gui.event.DisplayLocaleSelectEvent;

import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

@SuppressWarnings("serial")
public class QuestionWidget extends AbstractWidgetEditor<MetaQuestion> {
	
	private MetaQuestion metaQuestion;
	
	private JLabel label = new JLabel();

	private JButton gotoButton;
	
	private Locale locale;
	
	public QuestionWidget(EventBus eventBus, MetaQuestion metaQuestion) {
		this(eventBus);
		setContent(metaQuestion);
	}
 	
	public QuestionWidget(final EventBus eventBus) {
		eventBus.register(this);
		setLayout(new BorderLayout(0, 0));
		add(label, BorderLayout.WEST);
		gotoButton = new JButton("");
		gotoButton.setHideActionText(true);
		gotoButton.setIcon(new ImageIcon(QuestionWidget.class.getResource("/org/daxplore/producer/gui/resources/edit_go_32.png")));
		gotoButton.setOpaque(false);
		gotoButton.setContentAreaFilled(false);
		gotoButton.setBorderPainted(false);
		gotoButton.setEnabled(true);
		
		gotoButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				eventBus.post(new ChangeMainViewEvent(Views.QUESTIONVIEW, metaQuestion));
			}
		});
		add(gotoButton, BorderLayout.EAST);
	}
	
	public void showEdit(boolean show) {
		gotoButton.setVisible(show);
		gotoButton.setEnabled(show);
	}

	@Override
	public void setContent(MetaQuestion value) {
		this.metaQuestion = value;
		String text = getLabelText();
		label.setText(text);
	}
	
	private String getLabelText() {
		if(locale == null) {
			return metaQuestion.getId();
		}
	
		String text = metaQuestion.getShortTextRef().get(locale);
		if(Strings.isNullOrEmpty(text)) {
			return metaQuestion.getId();
		}

		return text;
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
