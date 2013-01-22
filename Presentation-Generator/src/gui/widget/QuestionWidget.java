package gui.widget;

import javax.swing.JLabel;
import javax.swing.JPanel;

import daxplorelib.metadata.MetaQuestion;

@SuppressWarnings("serial")
public class QuestionWidget extends JPanel {
	
	public MetaQuestion metaQuestion;
	
	public QuestionWidget(MetaQuestion metaQuestion) {
		metaQuestion = metaQuestion;
		add(new JLabel(metaQuestion.getId()));
	}
}
