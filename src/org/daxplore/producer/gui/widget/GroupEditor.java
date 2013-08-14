package org.daxplore.producer.gui.widget;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.daxplore.producer.daxplorelib.metadata.MetaGroup;
import org.daxplore.producer.gui.Settings;

@SuppressWarnings("serial")
public class GroupEditor extends AbstractWidgetEditor<MetaGroup> {

	JTextField textField;
	private MetaGroup metaGroup;
	
	public GroupEditor() {
		textField = new JTextField();
		add(textField);
	}
	
	@Override
	public MetaGroup getContent() throws InvalidContentException {
		return metaGroup;
	}

	@Override
	public void setContent(MetaGroup value) {
		this.metaGroup = value;
		textField.setText(metaGroup.getTextRef().get(Settings.getDefaultLocale()));
		textField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				if(!textField.getText().equals("")) {
					metaGroup.getTextRef().put(textField.getText(), Settings.getDefaultLocale());
				}
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				metaGroup.getTextRef().put(textField.getText(), Settings.getDefaultLocale());
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				metaGroup.getTextRef().put(textField.getText(), Settings.getDefaultLocale());
			}
		});
		
	}

}
