package gui.widget;

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
	
	public MetaQuestion metaQuestion;
	
	private JLabel label;

	private JButton gotoButton;
	
	public QuestionWidget(MetaQuestion metaQuestion) {
		this.metaQuestion = metaQuestion;
		setLayout(new BorderLayout(0, 0));
		label = new JLabel(metaQuestion.getId());
		label.setHorizontalAlignment(SwingConstants.LEFT);
		add(label, BorderLayout.WEST);
		
		gotoButton = new JButton("edit");
		gotoButton.setVisible(false);
		gotoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		add(gotoButton, BorderLayout.EAST);
	}
	
	public void showEdit(boolean show) {
		gotoButton.setVisible(show);
	}
}
