package org.daxplore.producer.gui.widget;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;
import org.daxplore.producer.gui.Dialogs;
import org.daxplore.producer.gui.Settings;
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
	
	private Locale locale; 
	
	public TextWidget(final EventBus eventBus, final GuiTexts texts) {
		eventBus.register(this);
		locale = Settings.getCurrentDisplayLocale();
		setLayout(new BorderLayout(0, 0));
		locale = Settings.getCurrentDisplayLocale();
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
		editButton.setEnabled(true);
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
