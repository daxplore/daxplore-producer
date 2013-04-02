package gui.question;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.TreeMap;

import gui.MainController;
import gui.widget.ColumnTableModel;

import javax.swing.DefaultListModel;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import tools.Pair;

import daxplorelib.DaxploreException;
import daxplorelib.metadata.MetaQuestion;
import daxplorelib.metadata.MetaScale;
import daxplorelib.metadata.MetaScale.Option;

public class QuestionController implements TableModelListener {
	
	QuestionView view;
	private MainController mainController;
	private LinkedList<Pair<Double, Integer>> values;
	MetaQuestion mq;
	ColumnTableModel afterTableModel;
	
	public QuestionController(MainController mainController, QuestionView view) {
		this.view = view;
		this.mainController = mainController;
	}

	public void openMetaQuestion(MetaQuestion mq) {
		this.mq = mq;
		
		ScaleTableModel scaleTableModel = new ScaleTableModel(mq.getScale());
		scaleTableModel.addTableModelListener(this);
		ScaleTable scaleTable = new ScaleTable(scaleTableModel);
		view.getScaleScrollPane().setViewportView(scaleTable);
		
		try {
			values = mainController.getDaxploreFile().getImportedData().getRawData().getColumnValueCount(mq.getId());
			view.getBeforeScrollPane().setViewportView(new JTable(new ColumnTableModel(values)));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		updateCalculatedValues();
		
		view.getAfterScrollPane().setViewportView(new JTable(afterTableModel));
		
		try {
			TimePointTableModel timePointTableModel = new TimePointTableModel(
					mainController.getDaxploreFile().getMetaData().getMetaTimepointManager(),
					mainController.getDaxploreFile().getImportedData().getRawData(),
					mainController.getDaxploreFile().getAbout(),
					mq);
			
			TimePointTable timePointTable = new TimePointTable(timePointTableModel);
			view.getTimePointScrollPane().setViewportView(timePointTable);
			
		} catch (SQLException | DaxploreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	void updateCalculatedValues() {
		LinkedList<Pair<Double, Integer>> afterValues = calculateAfter();
		if(afterTableModel == null) {
			afterTableModel = new ColumnTableModel(afterValues);
		} else {
			afterTableModel.setValues(afterValues);
		}
	}
	
	LinkedList<Pair<Double, Integer>> calculateAfter() {
		TreeMap<Double, Integer> valueMap = new TreeMap<Double, Integer>();
		for(MetaScale.Option option: mq.getScale().getOptions()) {
			int total = 0;
			for(Pair<Double, Integer> value: values) {
				if(value.getKey() != null && option.getTransformation().contains(value.getKey())) {
					total += value.getValue();
				}
			}
			if(valueMap.containsKey(option.getValue())) {
				Integer p = valueMap.get(option.getValue());
				valueMap.put(option.getValue(), total + p);
			} else {
				valueMap.put(option.getValue(), total);
			}
		}
		
		LinkedList<Pair<Double, Integer>> list = new LinkedList<Pair<Double, Integer>>();
		for(Entry<Double, Integer> entry : valueMap.entrySet()) {
			list.add(new Pair<Double, Integer>(entry.getKey(), entry.getValue()));
		}
		
		Collections.sort(list, new Comparator<Pair<Double, Integer>>() {

			@Override
			public int compare(Pair<Double, Integer> o1, Pair<Double, Integer> o2) {
				return o1.getKey().compareTo(o2.getKey());
			}
		});
		
		return list;
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		updateCalculatedValues();
	}
	
}
