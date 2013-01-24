package gui.widget;

import gui.MainController;
import gui.MainController.Views;

import javax.swing.JLabel;
import javax.swing.JPanel;

import daxplorelib.metadata.MetaQuestion;
import javax.swing.SwingConstants;
import java.awt.Component;
import javax.swing.Box;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.BorderLayout;

@SuppressWarnings("serial")
public class QuestionWidget extends JPanel {
	
	public final MetaQuestion metaQuestion;
	
	private JLabel label;

	private JButton gotoButton;
	
	public static MainController mainController; //TODO: fulhack
	
	public QuestionWidget(final MetaQuestion metaQuestion) {
		this.metaQuestion = metaQuestion;
		setLayout(new BorderLayout(0, 0));
		label = new JLabel(metaQuestion.getId());
		label.setHorizontalAlignment(SwingConstants.LEFT);
		add(label, BorderLayout.WEST);
		
		gotoButton = new JButton("edit");
		gotoButton.setVisible(false);
		gotoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Goto " + metaQuestion.getId());
				mainController.switchTo(Views.EDITTEXTVIEW, metaQuestion);
			}
		});
		add(gotoButton, BorderLayout.EAST);
	}
	
	public void showEdit(boolean show) {
		gotoButton.setVisible(show);
		gotoButton.setEnabled(show);
	}
}
