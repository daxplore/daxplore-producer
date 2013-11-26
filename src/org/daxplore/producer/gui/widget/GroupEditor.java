package org.daxplore.producer.gui.widget;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.daxplore.producer.daxplorelib.metadata.MetaGroup;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;
import org.daxplore.producer.gui.Settings;
import org.daxplore.producer.gui.event.DisplayLocaleSelectEvent;
import org.daxplore.producer.gui.event.EmptyEvents;
import org.daxplore.producer.gui.resources.GuiTexts;
import org.daxplore.producer.gui.view.build.GroupEditorPopupPanel;

import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

@SuppressWarnings("serial")
public class GroupEditor extends AbstractWidgetEditor<MetaGroup> implements ActionListener {

	private EventBus eventBus;
	private GuiTexts texts;
//	private JTextField textField;
	private JLabel textField;
	private MetaGroup metaGroup;
	private Locale locale;
	
	public GroupEditor(EventBus eventBus, GuiTexts texts) {
		this.eventBus = eventBus;
		this.texts = texts;
		setLayout(new BorderLayout());
		eventBus.register(this);
		locale = Settings.getCurrentDisplayLocale();
		textField = new JLabel();
		add(textField, BorderLayout.WEST);
		JButton editButton = new JButton("Edit"); //TODO use GuiTexts
		add(editButton, BorderLayout.EAST);
		editButton.addActionListener(this);
	}
	
	@Override
	public MetaGroup getContent() {
		return metaGroup;
	}

	@Override
	public void setContent(MetaGroup value) {
		this.metaGroup = value;
		textField.setText(getLabelText());
		//TODO how to handle change texts?
		eventBus.post(new EmptyEvents.RepaintWindowEvent());
//		textField.getDocument().addDocumentListener(new DocumentListener() {
//			@Override
//			public void removeUpdate(DocumentEvent e) {
//				if(!Strings.isNullOrEmpty(textField.getText()) && locale != null) {
//					metaGroup.getTextRef().put(textField.getText(), locale);
//				}
//			}
//			@Override
//			public void insertUpdate(DocumentEvent e) {
//				if(locale != null) {
//					metaGroup.getTextRef().put(textField.getText(), locale);
//				}
//			}
//			@Override
//			public void changedUpdate(DocumentEvent e) {
//				if(locale != null) {
//					metaGroup.getTextRef().put(textField.getText(), locale);
//				}
//
//			}
//		});
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
//		textField.setEditable(locale != null);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		GroupEditorPopupPanel editor = new GroupEditorPopupPanel(texts,
				metaGroup.getTextRef().getRef(),
				metaGroup.getTextRef().get(locale));
		int answer = JOptionPane.showConfirmDialog(this,
				editor,
				"Edit group",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if(answer == JOptionPane.OK_OPTION) {
			//TODO allow changing the textref id
			String technicalText = editor.getTechnicalText(); 
			
			String userText = editor.getUserText();
			metaGroup.getTextRef().put(userText, locale);
			
			setContent(metaGroup);
		}
		
	}

}
