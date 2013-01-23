package gui.groups;

import gui.widget.QuestionWidget;

import java.util.LinkedList;
import java.util.List;

import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

import daxplorelib.DaxploreException;
import daxplorelib.metadata.MetaData;
import daxplorelib.metadata.MetaQuestion;

class QuestionListModel implements ListModel<QuestionWidget> {

	List<QuestionWidget> questionList;

	public QuestionListModel(MetaData md) throws DaxploreException {
		questionList = new LinkedList<QuestionWidget>();
		List<MetaQuestion> mqList = md.getAllQuestions();
		int i = 0;
		for (MetaQuestion mq : mqList) {
			questionList.add(new QuestionWidget(mq));
			i++;
		}
		System.out.println("Added " + i + " questions");
	}

	@Override
	public int getSize() {
		return questionList.size();
	}

	@Override
	public QuestionWidget getElementAt(int index) {
		return questionList.get(index);
	}

	@Override
	public void addListDataListener(ListDataListener l) {
		// do nothing
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		// do nothing
	}
}