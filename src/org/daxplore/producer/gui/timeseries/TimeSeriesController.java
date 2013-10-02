package org.daxplore.producer.gui.timeseries;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.metadata.MetaTimepointShort;
import org.daxplore.producer.daxplorelib.metadata.MetaTimepointShort.MetaTimepointShortManager;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReferenceManager;
import org.daxplore.producer.daxplorelib.raw.RawData;
import org.daxplore.producer.daxplorelib.raw.RawMeta;
import org.daxplore.producer.gui.MainController;
import org.daxplore.producer.gui.widget.ColumnTableModel;
import org.daxplore.producer.tools.Pair;

public class TimeSeriesController implements ActionListener, DocumentListener {
	
	enum TimeSeriesCommand {
		ADD, UP, DOWN, REMOVE, SET_COLUMN
	}
	
	private MainController mainController;
	private TimeSeriesTableModel timeSeriesTableModel;
	private TimeSeriesTable timeSeriesTable;
	private TimeSeriesView timeSeriesView;
	
	public TimeSeriesController(MainController mainController) {
		this.mainController = mainController;
		timeSeriesView = new TimeSeriesView(this);
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		int[] selectedRows;
		switch(TimeSeriesCommand.valueOf(event.getActionCommand())) {
		case ADD:
			try {
				MetaTimepointShortManager timeManager = mainController.getDaxploreFile().getMetaTimepointShortManager();
				List<MetaTimepointShort> timeList = timeManager.getAll();
				
				TextReferenceManager textManager = mainController.getDaxploreFile().getTextReferenceManager();
				TextReference textref = textManager.get("tp"+(timeManager.getHighestId()+1));
				
				int timeindex = 0;
				if(timeList.size()>0) {
					timeindex = timeList.get(timeList.size()-1).getTimeindex()+1;
				}
				
				Double value = 0.0;
				RawData rawData = mainController.getDaxploreFile().getRawData();
				String column = mainController.getDaxploreFile().getAbout().getTimeSeriesShortColumn();
				List<Pair<Double, Integer>> columnValueCounts= rawData.getColumnValueCount(column);
				L: for(Pair<Double, Integer> valuePair: columnValueCounts) {
					if(valuePair.getKey() == null) {
						continue L;
					}
					for(MetaTimepointShort tp: timeList) {

						if(valuePair.getKey().equals(tp.getValue())) {
							continue L;
						}
					}
					value = valuePair.getKey();
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
			try {
				boolean hasColumn = mainController.getDaxploreFile().getRawMeta().hasColumn(column);
				if(hasColumn) {
					mainController.getDaxploreFile().getAbout().setTimeSeriesShortColumn(column);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
		if(mainController.fileIsSet()) {
			timeSeriesTableModel = new TimeSeriesTableModel(mainController.getDaxploreFile().getMetaTimepointShortManager());
			timeSeriesTable = new TimeSeriesTable(timeSeriesTableModel);
			timeSeriesView.getTimeSeriesScrollPane().setViewportView(timeSeriesTable);
			timeSeriesView.setTimeSeriesColumn(mainController.getDaxploreFile().getAbout().getTimeSeriesShortColumn());
		}
	}

	public void filter(String text) { //TODO rename method
		try {
			RawData rawData = mainController.getDaxploreFile().getRawData();
			RawMeta rawMeta = mainController.getDaxploreFile().getRawMeta();
			if(rawMeta.hasColumn(text)) {
				LinkedList<Pair<Double, Integer>> columnValueList = rawData.getColumnValueCount(text);
				ColumnTableModel model = new ColumnTableModel(columnValueList);
				JTable columnValueTable = new JTable(model);
				timeSeriesView.getColumnValueCountPane().setViewportView(columnValueTable);
			}
		} catch (SQLException e) {
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
