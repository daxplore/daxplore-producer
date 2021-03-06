/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui.view.build;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.metadata.MetaGroup;
import org.daxplore.producer.daxplorelib.metadata.MetaGroup.GroupType;
import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;

@SuppressWarnings("serial")
public class PerspectivesTableModel extends DefaultTableModel {

	MetaGroup mg;
	
	public PerspectivesTableModel(MetaGroup mg) throws DaxploreException {
		if(mg.getType() == GroupType.PERSPECTIVE) {
			this.mg = mg;
		} else {
			throw new DaxploreException("Group not of perspective type");
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
		List<MetaQuestion> moveList = new LinkedList<>();
		
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
