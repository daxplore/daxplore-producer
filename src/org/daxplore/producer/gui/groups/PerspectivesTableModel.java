package org.daxplore.producer.gui.groups;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import org.daxplore.producer.daxplorelib.metadata.MetaGroup;
import org.daxplore.producer.daxplorelib.metadata.MetaGroup.GroupType;
import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;

@SuppressWarnings("serial")
public class PerspectivesTableModel extends DefaultTableModel {

	MetaGroup mg;
	
	public PerspectivesTableModel(MetaGroup mg) throws Exception {
		if(mg.getType() == GroupType.PERSPECTIVE) {
			this.mg = mg;
		} else {
			throw new Exception("Group not of perspective type");
		}
	}
	
	@Override
	public int getRowCount() {
		if(mg == null) return 0;
		return mg.getQuestions().size();
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
		return mg.getQuestion(rowIndex);
	}
	
	@Override
	public void removeRow(int row) {
		List<MetaQuestion> mqlist = mg.getQuestions();
		mqlist.remove(row);
		mg.setQuestions(mqlist);
		fireTableRowsDeleted(row, row);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void insertRow(int row, Vector rowData) {
		if(rowData.size() == 1 && rowData.get(0) instanceof MetaQuestion) {
			List<MetaQuestion> mqlist = mg.getQuestions();
			mqlist.add(row, (MetaQuestion)rowData.get(0));
			mg.setQuestions(mqlist);
			fireTableRowsInserted(row, row);
		}
	}
	
	@Override
	public void moveRow(int start, int end, int to) {
		List<MetaQuestion> mqlist = mg.getQuestions();
		List<MetaQuestion> moveList = new LinkedList<MetaQuestion>();
		
		for(int i = start; i <= end; i++) {
			moveList.add(mqlist.remove(start)); //TODO: check if it works with more rows than 1
		}
		int j = to;
		for(MetaQuestion mq: moveList) {
			mqlist.add(j, mq);
			j++;
		}
		mg.setQuestions(mqlist);
		
        int shift = to - start;
        int first, last;
        if (shift < 0) {
            first = to;
            last = end;
        }
        else {
            first = start;
            last = to + end - start;
        }
        
        fireTableRowsUpdated(first, last);
	}
}
