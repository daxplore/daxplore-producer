package org.daxplore.producer.gui.widget;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;
import org.daxplore.producer.gui.MainController.Views;
import org.daxplore.producer.gui.event.ChangeMainViewEvent;

import com.google.common.eventbus.EventBus;

@SuppressWarnings("serial")
public class QuestionWidget extends AbstractWidgetEditor<MetaQuestion> {
	
	private MetaQuestion metaQuestion;
	
	private JPanel labelHolder = new JPanel();
	private JLabel label;

	private JButton gotoButton;
	
	public QuestionWidget(EventBus eventBus, MetaQuestion metaQuestion) {
		this(eventBus);
		setContent(metaQuestion);
	}
 	
	public QuestionWidget(final EventBus eventBus) {
		setLayout(new BorderLayout(0, 0));
		add(labelHolder, BorderLayout.WEST);
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
		label = new JLabel(metaQuestion.getId());
		label.setHorizontalAlignment(SwingConstants.LEFT);
		labelHolder.removeAll();
		labelHolder.add(label);
	}

	@Override
	public MetaQuestion getContent() {
		return metaQuestion;
	}
}
