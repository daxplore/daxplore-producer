package org.daxplore.producer.gui.widget;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;
import org.daxplore.producer.gui.MainController.Views;
import org.daxplore.producer.gui.event.ChangeMainViewEvent;
import org.daxplore.producer.gui.event.DisplayLocaleSelectEvent;
import org.daxplore.producer.gui.event.EmptyEvents.RepaintWindowEvent;

import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

@SuppressWarnings("serial")
public class TextWidget extends AbstractWidgetEditor<TextReference>{

	private TextReference textRef;
	private JLabel label = new JLabel();
	private JButton gotoButton;
	
	private Locale locale; 
	
	public TextWidget(final EventBus eventBus) {
		eventBus.register(this);
		setLayout(new BorderLayout(0, 0));
		add(label, BorderLayout.WEST);
		label.setHorizontalAlignment(SwingConstants.LEFT);
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
				eventBus.post(new ChangeMainViewEvent(Views.EDITTEXTVIEW, textRef));
			}
		});
		add(gotoButton, BorderLayout.EAST);
	}

	public void showEdit(boolean show) {
		gotoButton.setVisible(show);
		gotoButton.setEnabled(show);
	}
	
	public void setIndependetView(boolean indie) {
		if(indie) {
			setBorder(new LineBorder(new Color(0,0,0), 1));
			setBackground(new Color(255,255,255));
		} else {
			setBorder(null);
		}
	}

	@Override
	public TextReference getContent() {
		return textRef;
	}

	@Override
	public void setContent(TextReference value) {
		this.textRef = value;
		label.setText(getLabelText());
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
		locale = e.getLocale();
	}
}
