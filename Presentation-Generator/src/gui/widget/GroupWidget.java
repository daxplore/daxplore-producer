package gui.widget;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import javax.swing.JLabel;
import javax.swing.JPanel;

import daxplorelib.metadata.MetaGroup;

@SuppressWarnings("serial")
public class GroupWidget extends JPanel {
	
	public MetaGroup metaGroup;
	public List<QuestionWidget> questions = new LinkedList<QuestionWidget>();
	
	public GroupWidget(MetaGroup mg) {
		this.metaGroup = mg;
		add(new JLabel(mg.getTextRef().get(new Locale("sv")))); //TODO: get universal locale
	}
	
	public int getQuestionCount() {
		return questions.size();
	}
	
}
