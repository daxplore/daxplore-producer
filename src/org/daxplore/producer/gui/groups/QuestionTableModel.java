package org.daxplore.producer.gui.groups;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;
import org.daxplore.producer.daxplorelib.metadata.MetaQuestion.MetaQuestionManager;

@SuppressWarnings("serial") 
class QuestionTableModel extends DefaultTableModel {

	List<MetaQuestion> questionList;
	
	public QuestionTableModel(MetaQuestionManager metaQuestionManager) throws DaxploreException {
		questionList = metaQuestionManager.getAll();
		Collections.sort(questionList, new Comparator<MetaQuestion>() {
			@Override
			public int compare(MetaQuestion o1, MetaQuestion o2) {
				return o1.getId().compareTo(o2.getId());
			}
		});
	}
	
	@Override
	public int getRowCount() {
		if(questionList == null) return 0;
		return questionList.size();
	}

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public String getColumnName(int columnIndex) {
		return null;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return MetaQuestion.class;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}
	
	@Override
	public void setValueAt(Object aValue, int row, int column) {
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return questionList.get(rowIndex);
	}
}