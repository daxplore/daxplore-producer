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
import org.daxplore.producer.gui.MainController;
import org.daxplore.producer.gui.MainController.Views;

@SuppressWarnings("serial")
public class QuestionWidget extends AbstractWidgetEditor<MetaQuestion> {
	
	public MetaQuestion metaQuestion;
	
	private JPanel labelHolder = new JPanel();
	private JLabel label;

	private JButton gotoButton;
	
	public static MainController mainController; //TODO: ugly hack, replace with eventbus
	
	public QuestionWidget(MetaQuestion metaQuestion) {
		this();
		setContent(metaQuestion);
	}
 	
	public QuestionWidget() {
		setLayout(new BorderLayout(0, 0));
		add(labelHolder, BorderLayout.WEST);
		gotoButton = new JButton("");
		gotoButton.setHideActionText(true);
		gotoButton.setIcon(new ImageIcon(QuestionWidget.class.getResource("/org/daxplore/producer/gui/resources/edit_go_32.png")));
		gotoButton.setOpaque(false);
		gotoButton.setContentAreaFilled(false);
		gotoButton.setBorderPainted(false);
		gotoButton.setEnabled(true);
		//gotoButton.setVisible(false);
		gotoButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//mainController.switchTo(Views.EDITTEXTVIEW, metaQuestion.getFullTextRef());
				mainController.switchTo(Views.QUESTIONVIEW, metaQuestion);
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
