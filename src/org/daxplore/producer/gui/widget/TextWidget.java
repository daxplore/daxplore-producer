package org.daxplore.producer.gui.widget;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;
import org.daxplore.producer.gui.MainController;
import org.daxplore.producer.gui.MainController.Views;

@SuppressWarnings("serial")
public class TextWidget extends AbstractWidgetEditor<TextReference>{

	private TextReference textRef;
	
	private JLabel label = new JLabel();

	private JButton gotoButton;
	
	public static MainController mainController; //TODO: fulhack
 	
	public TextWidget() {
		setLayout(new BorderLayout(0, 0));
		add(label, BorderLayout.WEST);
		gotoButton = new JButton("");
		gotoButton.setHideActionText(true);
		gotoButton.setIcon(new ImageIcon(QuestionWidget.class.getResource("/gui/resources/edit_go_32.png")));
		gotoButton.setOpaque(false);
		gotoButton.setContentAreaFilled(false);
		gotoButton.setBorderPainted(false);
		gotoButton.setEnabled(true);
		//gotoButton.setVisible(false);
		gotoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//mainController.switchTo(Views.EDITTEXTVIEW, metaQuestion.getFullTextRef());
				mainController.switchTo(Views.EDITTEXTVIEW, textRef);
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
	public TextReference getContent() throws org.daxplore.producer.gui.widget.AbstractWidgetEditor.InvalidContentException {
		return textRef;
	}

	@Override
	public void setContent(TextReference value) {
		this.textRef = value;
		label.setText(textRef.getRef());
		label.setHorizontalAlignment(SwingConstants.LEFT);
	}
}
