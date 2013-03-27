package gui.timeseries;

import gui.MainController;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JTable;

import tools.Pair;

import daxplorelib.DaxploreException;
import daxplorelib.metadata.MetaData;
import daxplorelib.metadata.MetaTimepointShort;
import daxplorelib.metadata.MetaTimepointShort.MetaTimepointShortManager;
import daxplorelib.metadata.textreference.TextReference;
import daxplorelib.metadata.textreference.TextReferenceManager;
import daxplorelib.raw.RawData;
import daxplorelib.raw.RawMeta;

public class TimeSeriesController implements ActionListener {
	public static final String TIMEPOINT_ADD_ACTION_COMMAND = "TimePointAddActionCommand";
	public static final String TIMEPOINT_UP_ACTION_COMMAND = "TimePointUpActionCommand";
	public static final String TIMEPOINT_DOWN_ACTION_COMMAND = "TimePointDownActionCommand";
	public static final String TIMEPOINT_REMOVE_ACTION_COMMAND = "TimePointRemoveActionCommand";
	public static final String TIMESERIES_SET_COLUMN_ACTION_COMMAND = "TimeSeriesSetColumnActionCommand";
	
	private MainController mainController;
	private TimeSeriesTableModel timeSeriesTableModel;
	private TimeSeriesTable timeSeriesTable;
	private TimeSeriesView view;
	
	public TimeSeriesController(MainController mainController, TimeSeriesView view) {
		this.mainController = mainController;
		this.view = view;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		Object[] path;
		int[] selectedRows;
		switch(arg0.getActionCommand()) {
		case TIMEPOINT_ADD_ACTION_COMMAND:
			try {
				MetaTimepointShortManager timeManager = mainController.getDaxploreFile().getMetaData().getMetaTimepointManager();
				List<MetaTimepointShort> timeList = timeManager.getAll();
				
				TextReferenceManager textManager = mainController.getDaxploreFile().getMetaData().getTextsManager();
				TextReference textref = textManager.get("tp"+(timeManager.getHighestId()+1));
				
				int timeindex = 0;
				if(timeList.size()>0) {
					timeindex = timeList.get(timeList.size()-1).getTimeindex()+1;
				}
				
				double value = 0;
				RawData rawData = mainController.getDaxploreFile().getImportedData().getRawData();
				String column = mainController.getDaxploreFile().getAbout().getTimeSeriesShortColumn();
				List<Pair<Double, Integer>> columnValueCounts= rawData.getColumnValueCount(column);
				L: for(Pair<Double, Integer> valuePair: columnValueCounts) {
					for(MetaTimepointShort tp: timeList) {
						if(valuePair.getKey().equals(tp.getValue())) {
							continue L;
						}
					}
					value = valuePair.getKey();
					break;
				}
			
				MetaTimepointShort timepoint = timeManager.create(textref, timeindex, value);
				timeSeriesTableModel.fireTableDataChanged();
			} catch (SQLException | DaxploreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case TIMEPOINT_REMOVE_ACTION_COMMAND:
			selectedRows = timeSeriesTable.getSelectedRows();
			if(selectedRows.length < 1) break;
			timeSeriesTable.clearSelection();
			timeSeriesTable.removeEditor();
			for(int i = selectedRows.length-1; i >= 0; i--) {
				timeSeriesTableModel.removeRow(selectedRows[i]);
			}
			break;
		case TIMEPOINT_UP_ACTION_COMMAND:
			selectedRows = timeSeriesTable.getSelectedRows();
			if(selectedRows.length < 1 || selectedRows[0] == 0) break;
			timeSeriesTable.clearSelection();
			timeSeriesTable.removeEditor();
			for(int i = 0; i < selectedRows.length; i++) {
				timeSeriesTableModel.moveRow(selectedRows[i], selectedRows[i], selectedRows[i]-1);
				timeSeriesTable.getSelectionModel().addSelectionInterval(selectedRows[i]-1, selectedRows[i]-1);
			}
			break;
		case TIMEPOINT_DOWN_ACTION_COMMAND:
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
		case TIMESERIES_SET_COLUMN_ACTION_COMMAND:
			String column = view.getTimeSeriesColumn();
			try {
				boolean hasColumn = mainController.getDaxploreFile().getRawMeta().hasColumn(column);
				if(hasColumn) {
					mainController.getDaxploreFile().getAbout().setTimeSeriesShortColumn(column);
				}
			} catch (SQLException | DaxploreException e) {
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
			try {
				MetaData md = mainController.getDaxploreFile().getMetaData();
				timeSeriesTableModel = new TimeSeriesTableModel(md.getMetaTimepointManager());
				timeSeriesTable = new TimeSeriesTable(timeSeriesTableModel);
				view.getTimeSeriesScrollPane().setViewportView(timeSeriesTable);
				view.setTimeSeriesColumn(mainController.getDaxploreFile().getAbout().getTimeSeriesShortColumn());
			} catch (DaxploreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void filter(String text) { //TODO rename method
		try {
			RawData rawData = mainController.getDaxploreFile().getImportedData().getRawData();
			RawMeta rawMeta = mainController.getDaxploreFile().getRawMeta();
			if(rawMeta.hasColumn(text)) {
				LinkedList<Pair<Double, Integer>> columnValueList = rawData.<Double>getColumnValueCount(text);
				ColumnTableModel model = new ColumnTableModel(columnValueList);
				JTable columnValueTable = new JTable(model);
				view.getColumnValueCountPane().setViewportView(columnValueTable);
			}
		} catch (SQLException | DaxploreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
