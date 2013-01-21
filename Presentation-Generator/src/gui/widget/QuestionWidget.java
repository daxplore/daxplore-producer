package gui.widget;

import javax.swing.JLabel;

import daxplorelib.metadata.MetaQuestion;

@SuppressWarnings("serial")
public class QuestionWidget extends OurListWidget {
	
	public QuestionWidget(MetaQuestion metaQuestion) {
		add(new JLabel(metaQuestion.getId()));
	}
}
