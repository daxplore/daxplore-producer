package gui.timeseries;

import gui.MainController;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.LinkedList;

import javax.swing.JTable;

import tools.Pair;

import daxplorelib.DaxploreException;
import daxplorelib.metadata.MetaData;
import daxplorelib.raw.RawData;

public class TimeSeriesController implements ActionListener {
	public static final String TIMEPOINT_ADD_ACTION_COMMAND = "TimePointAddActionCommand";
	public static final String TIMEPOINT_UP_ACTION_COMMAND = "TimePointUpActionCommand";
	public static final String TIMEPOINT_DOWN_ACTION_COMMAND = "TimePointDownActionCommand";
	public static final String TIMEPOINT_REMOVE_ACTION_COMMAND = "TimePointRemoveActionCommand";
	
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
		switch(arg0.getActionCommand()) {
		case TIMEPOINT_ADD_ACTION_COMMAND:
			break;
		case TIMEPOINT_REMOVE_ACTION_COMMAND:
			break;
		case TIMEPOINT_UP_ACTION_COMMAND:
			break;
		case TIMEPOINT_DOWN_ACTION_COMMAND:
			break;
		default:
			throw new AssertionError("Unknown action command");
		}
	}
	
	public void loadData() {
		if(mainController.fileIsSet()) {
			MetaData md;
			try {
				md = mainController.getDaxploreFile().getMetaData();
				timeSeriesTableModel = new TimeSeriesTableModel(md.getMetaTimepointManager().getAll());
				timeSeriesTable = new TimeSeriesTable(timeSeriesTableModel);
				view.getTimeSeriesScrollPane().setViewportView(timeSeriesTable);
			} catch (DaxploreException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void filter(String text) { //TODO rename method
		RawData rawData = mainController.getDaxploreFile().getImportedData().getRawData();
		try {
			if(rawData.hasColumn(text)) {
				LinkedList<Pair<Double, Integer>> columnValueList = rawData.<Double>getColumnValueCount(text);
				ColumnTableModel model = new ColumnTableModel(columnValueList);
				JTable columnValueTable = new JTable(model);
				view.getColumnValueCountPane().setViewportView(columnValueTable);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
