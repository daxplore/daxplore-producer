package gui.widget;

import java.awt.BorderLayout;
import java.awt.TextField;
import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import daxplorelib.metadata.MetaGroup;

@SuppressWarnings("serial")
public class GroupWidget extends JPanel {
	
	public MetaGroup metaGroup;
	
	private JPanel labelHolder = new JPanel();
	private JLabel label;
	
	public GroupWidget(MetaGroup mg) {
		this();
		setMetaGroup(mg, false);
	}
	
	public GroupWidget() {
		setLayout(new BorderLayout(0, 0));
		add(labelHolder, BorderLayout.WEST);
	}
	
	public MetaGroup getMetaGroup() {
		return metaGroup;
	}
	
	public void setMetaGroup(MetaGroup metaGroup) {
		setMetaGroup(metaGroup, false);
	}

	public void setMetaGroup(final MetaGroup metaGroup, boolean asEditor) {
		this.metaGroup = metaGroup;
		if(asEditor) {
			final JTextField textField = new JTextField(metaGroup.getTextRef().get(new Locale("sv")));
			textField.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void removeUpdate(DocumentEvent e) {
					if(!textField.getText().equals("")) {
						metaGroup.getTextRef().put(textField.getText(), new Locale("sv"));
					}
				}
				@Override
				public void insertUpdate(DocumentEvent e) {
					metaGroup.getTextRef().put(textField.getText(), new Locale("sv"));
				}
				@Override
				public void changedUpdate(DocumentEvent e) {
					metaGroup.getTextRef().put(textField.getText(), new Locale("sv"));
				}
			});
			labelHolder.removeAll();
			labelHolder.add(textField);
		} else {
			label = new JLabel(metaGroup.getTextRef().get(new Locale("sv")));
			label.setHorizontalAlignment(SwingConstants.LEFT);
			labelHolder.removeAll();
			labelHolder.add(label);
		}
	}

	public int getQuestionCount() {
		return metaGroup.getQuestionCount();
	}
	
}
