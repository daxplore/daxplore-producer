/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui.view.time;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.daxplore.producer.daxplorelib.About;
import org.daxplore.producer.daxplorelib.About.TimeSeriesType;
import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.DaxploreFile;
import org.daxplore.producer.daxplorelib.metadata.MetaTimepointShort;
import org.daxplore.producer.daxplorelib.metadata.MetaTimepointShort.MetaTimepointShortManager;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReferenceManager;
import org.daxplore.producer.daxplorelib.raw.RawData.RawDataManager;
import org.daxplore.producer.gui.event.DaxploreFileUpdateEvent;
import org.daxplore.producer.gui.resources.UITexts;
import org.daxplore.producer.gui.widget.ColumnTableModel;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class TimeSeriesController implements ActionListener, DocumentListener {
	
	enum TimeSeriesCommand {
		TIME_ENABLE, ADD, UP, DOWN, REMOVE, SET_COLUMN, REPLACE_TIMEPOINTS
	}
	
	private About about;
	private EventBus eventBus;
	private DaxploreFile daxploreFile;
	
	private TimeSeriesTableModel timeSeriesTableModel;
	private TimeSeriesTable timeSeriesTable;
	private TimeSeriesView timeSeriesView;
	
	public TimeSeriesController(About about, EventBus eventBus) {
		this.about = about;
		this.eventBus = eventBus; 
		eventBus.register(this);
		timeSeriesView = new TimeSeriesView(about, this);
	}
	
	@Subscribe
	public void on(DaxploreFileUpdateEvent e) {
		try {
			this.daxploreFile = e.getDaxploreFile();
			loadData();
		} catch (Exception ex) {
			Logger.getGlobal().log(Level.SEVERE, "Failed to handle event", ex);
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		int[] selectedRows;
		switch(TimeSeriesCommand.valueOf(event.getActionCommand())) {
		case TIME_ENABLE:
			if (timeSeriesView.isTimeSeriesEnabled()) {
				about.setTimeSeriesType(TimeSeriesType.SHORT);
			} else {
				about.setTimeSeriesType(TimeSeriesType.NONE);
			}
			break;
		case ADD:
			try {
				MetaTimepointShortManager timeManager = daxploreFile.getMetaTimepointShortManager();
				List<MetaTimepointShort> timeList = timeManager.getAll();
				
				TextReferenceManager textManager = daxploreFile.getTextReferenceManager();
				TextReference textref = textManager.get("tp"+(timeManager.getHighestId()+1));
				
				int timeindex = 0;
				if(timeList.size()>0) {
					timeindex = timeList.get(timeList.size()-1).getTimeindex()+1;
				}
				
				Double value = 0.0;
				RawDataManager rawDataManager = daxploreFile.getRawDataManager();
				String column = daxploreFile.getAbout().getTimeSeriesShortColumn();
				SortedMap<Object, Integer> columnValueCounts= rawDataManager.getColumnValueCount(column);
				L: for(Map.Entry<Object, Integer> valuePair: columnValueCounts.entrySet()) {
					if(valuePair.getKey() == null) {
						continue L;
					}
					for(MetaTimepointShort tp: timeList) {

						if(valuePair.getKey().equals(tp.getValue())) {
							continue L;
						}
					}
					//TODO: fix cast when string values are supported
					value = (Double)valuePair.getKey();
					break;
				}
				
				timeManager.create(textref, timeindex, value);
				timeSeriesTableModel.fireTableDataChanged();
			} catch (SQLException | DaxploreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case REMOVE:
			selectedRows = timeSeriesTable.getSelectedRows();
			if(selectedRows.length < 1) break;
			timeSeriesTable.clearSelection();
			timeSeriesTable.removeEditor();
			for(int i = selectedRows.length-1; i >= 0; i--) {
				timeSeriesTableModel.removeRow(selectedRows[i]);
			}
			break;
		case UP:
			selectedRows = timeSeriesTable.getSelectedRows();
			if(selectedRows.length < 1 || selectedRows[0] == 0) break;
			timeSeriesTable.clearSelection();
			timeSeriesTable.removeEditor();
			for(int i = 0; i < selectedRows.length; i++) {
				timeSeriesTableModel.moveRow(selectedRows[i], selectedRows[i], selectedRows[i]-1);
				timeSeriesTable.getSelectionModel().addSelectionInterval(selectedRows[i]-1, selectedRows[i]-1);
			}
			break;
		case DOWN:
			selectedRows = timeSeriesTable.getSelectedRows();
			if(selectedRows.length < 1 || selectedRows[selectedRows.length-1] == timeSeriesTableModel.getRowCount() -1) break;
			timeSeriesTable.clearSelection();
			timeSeriesTable.removeEditor();
			for(int i = selectedRows.length-1; i >= 0; i--) {
				//perspectivesTable.changeSelection(selectedRows[i], 1, true, true);
				timeSeriesTableModel.moveRow(selectedRows[i], selectedRows[i], selectedRows[i]+1);
				timeSeriesTable.getSelectionModel().addSelectionInterval(selectedRows[i]+1, selectedRows[i]+1);
			}
			break;
		case SET_COLUMN:
			String column = timeSeriesView.getTimeSeriesColumn();
			boolean hasColumn = daxploreFile.getRawMetaManager().hasColumn(column);
			if(hasColumn) {
				daxploreFile.getAbout().setTimeSeriesShortColumn(column);
			}
			break;
		case REPLACE_TIMEPOINTS:
			try {
				daxploreFile.replaceAllTimepointsInQuestions();
				JOptionPane.showMessageDialog(timeSeriesView,
						UITexts.get("dialog.time_replace.text"),
						UITexts.get("dialog.time_replace.title"),
						JOptionPane.INFORMATION_MESSAGE);
			} catch (DaxploreException|SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			break;
			
//		case PERSPECTIVES_UP_ACTION_COMMAND:
//
//			break;
//		case PERSPECTIVES_DOWN_ACTION_COMMAND:
//			selectedRows = perspectivesTable.getSelectedRows();
//			if(selectedRows.length < 1 || selectedRows[selectedRows.length-1] == perspectivesTableModel.getRowCount() -1) break;
//			perspectivesTable.clearSelection();
//			perspectivesTable.removeEditor();
//			for(int i = selectedRows.length-1; i >= 0; i--) {
//				//perspectivesTable.changeSelection(selectedRows[i], 1, true, true);
//				perspectivesTableModel.moveRow(selectedRows[i], selectedRows[i], selectedRows[i]+1);
//				perspectivesTable.getSelectionModel().addSelectionInterval(selectedRows[i]+1, selectedRows[i]+1);
//			}
//			break;
//		case PERSPECTIVES_REMOVE_ACTION_COMMAND:
//			selectedRows = perspectivesTable.getSelectedRows();
//			int delta = 0;
//			for(int row: selectedRows) {
//				perspectivesTableModel.removeRow(row - delta);
//				delta++;
//			}
//			break;
			
		default:
			throw new AssertionError("Unknown action command");
		}
	}
	
	public void loadData() {
		if(daxploreFile != null) {
			timeSeriesTableModel = new TimeSeriesTableModel(daxploreFile.getMetaTimepointShortManager());
			timeSeriesTable = new TimeSeriesTable(eventBus, timeSeriesTableModel);
			timeSeriesView.getTimeSeriesScrollPane().setViewportView(timeSeriesTable);
			if(daxploreFile.getAbout().getTimeSeriesType() == TimeSeriesType.SHORT) {
				timeSeriesView.setTimeSeriesColumn(daxploreFile.getAbout().getTimeSeriesShortColumn());
			}
		}
	}

	public void filter(String text) { //TODO rename method
		try {
			RawDataManager rawDataManager = daxploreFile.getRawDataManager();
			if(daxploreFile.getRawMetaManager().hasColumn(text)) {
				SortedMap<Object, Integer> columnValueList = rawDataManager.getColumnValueCount(text);
				ColumnTableModel model = new ColumnTableModel(columnValueList);
				JTable columnValueTable = new JTable(model);
				timeSeriesView.getColumnValueCountPane().setViewportView(columnValueTable);
			}
		} catch (DaxploreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Component getView() {
		return timeSeriesView;
	}
	
	// Document listener methods	
	@Override
	public void removeUpdate(DocumentEvent e) {
		filter(timeSeriesView.getTimeSeriesText());
	}
	
	@Override
	public void insertUpdate(DocumentEvent e) {
		filter(timeSeriesView.getTimeSeriesText());
	}
	
	@Override
	public void changedUpdate(DocumentEvent e) {
		filter(timeSeriesView.getTimeSeriesText());
	}
}
