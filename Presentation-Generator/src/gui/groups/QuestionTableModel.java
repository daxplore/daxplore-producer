package gui.groups;

import gui.widget.QuestionWidget;

import java.util.List;

import javax.swing.table.DefaultTableModel;

import daxplorelib.DaxploreException;
import daxplorelib.metadata.MetaData;
import daxplorelib.metadata.MetaQuestion;

@SuppressWarnings("serial") 
class QuestionTableModel extends DefaultTableModel {

	List<MetaQuestion> questionList;
	
	public QuestionTableModel(MetaData md) throws DaxploreException {
		questionList = md.getAllQuestions();
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