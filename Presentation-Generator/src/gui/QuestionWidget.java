package gui;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import daxplorelib.metadata.MetaQuestion;

@SuppressWarnings("serial")
public class QuestionWidget extends JPanel {
	
	public QuestionWidget(MetaQuestion metaQuestion) {
		add(new JLabel(metaQuestion.getId()));
	}
}
