package gui.question;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import gui.MainController;
import gui.widget.ColumnTableModel;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import tools.NumberlineCoverage;
import tools.Pair;

import daxplorelib.DaxploreException;
import daxplorelib.metadata.MetaQuestion;
import daxplorelib.metadata.MetaScale;
import daxplorelib.metadata.MetaScale.Option;
import daxplorelib.metadata.textreference.TextReference;

public class QuestionController implements TableModelListener, ActionListener  {
	
	static class Command {
		static final String ADD = "ADD";
		static final String REMOVE = "REMOVE";
		static final String UP = "UP"; 
		static final String DOWN = "DOWN"; 
		static final String INVERT = "INVERT"; 
	}
	
	QuestionView view;
	private MainController mainController;
	private LinkedList<Pair<Double, Integer>> values;
	MetaQuestion mq;
	ColumnTableModel afterTableModel;
	private ScaleTable scaleTable;
	private ScaleTableModel scaleTableModel;
	
	public QuestionController(MainController mainController, QuestionView view) {
		this.view = view;
		this.mainController = mainController;
	}

	//TODO handle null scales properly
	public void openMetaQuestion(MetaQuestion mq) {
		this.mq = mq;
		
		scaleTableModel = new ScaleTableModel(mq.getScale());
		scaleTableModel.addTableModelListener(this);
		scaleTable = new ScaleTable(scaleTableModel);
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
		if(mq.getScale()==null) {
			return new LinkedList<Pair<Double, Integer>>();
		}
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
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		switch(e.getActionCommand()) {
		case Command.ADD:
			//TODO add even if there are no unused o.getTransformation().contains(p.getKey())
			try {
				if(mq.getScale()==null){
					mq.setScale(mainController.getDaxploreFile().getMetaData().getMetaScaleManager().create(new LinkedList<Option>(), new NumberlineCoverage()));
					scaleTableModel = new ScaleTableModel(mq.getScale());
					scaleTable = new ScaleTable(scaleTableModel);
					view.getScaleScrollPane().setViewportView(scaleTable);
					view.validate();
				    view.repaint();
					scaleTableModel.fireTableStructureChanged();
				}
				boolean added = false;
				double pKey = 0;
				AddLabel: for(Pair<Double, Integer> p : values) {
					if(p.getKey()!=null) {
						pKey = p.getKey();
						for(Option o : mq.getScale().getOptions()) {
							if(o.getTransformation().contains(p.getKey())){
								continue AddLabel;
							}
						}
						addOption(p.getKey());
						added = true;
						scaleTableModel.fireTableStructureChanged();
						break AddLabel;
					}
				}
				if(!added) {
					addOption(pKey+1);
					scaleTableModel.fireTableStructureChanged();
				}
			} catch (SQLException | DaxploreException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			break;
		case Command.REMOVE:
			int[] selectedRows = scaleTable.getSelectedRows();
			List<Option> options = mq.getScale().getOptions();
			for(int i = selectedRows.length-1; i>=0; i--) {
				options.remove(selectedRows[i]);
			}
			mq.getScale().setOptions(options);
			scaleTableModel.fireTableStructureChanged();
			break;
		case Command.UP:
			selectedRows = scaleTable.getSelectedRows();
			if(selectedRows.length < 1 || selectedRows[0] == 0) break;
			scaleTable.clearSelection();
			scaleTable.removeEditor();
			for(int i = 0; i < selectedRows.length; i++) {
				scaleTableModel.moveRow(selectedRows[i], selectedRows[i], selectedRows[i]-1);
				scaleTable.getSelectionModel().addSelectionInterval(selectedRows[i]-1, selectedRows[i]-1);
			}
			break;
		case Command.DOWN:
			selectedRows = scaleTable.getSelectedRows();
			if(selectedRows.length < 1 || selectedRows[selectedRows.length-1] == scaleTableModel.getRowCount() -1) break;
			scaleTable.clearSelection();
			scaleTable.removeEditor();
			for(int i = selectedRows.length-1; i >= 0; i--) {
				//perspectivesTable.changeSelection(selectedRows[i], 1, true, true);
				scaleTableModel.moveRow(selectedRows[i], selectedRows[i], selectedRows[i]+1);
				scaleTable.getSelectionModel().addSelectionInterval(selectedRows[i]+1, selectedRows[i]+1);
			}
			break;
		case Command.INVERT:
			options = mq.getScale().getOptions();
			List<Option> invertedOptions = new LinkedList<Option>();
			for(Option option : options) {
				invertedOptions.add(0, option);
			}
			mq.getScale().setOptions(invertedOptions);
			scaleTableModel.fireTableStructureChanged();
			break;
		default:
			throw new AssertionError("Undefined action command: " + e.getActionCommand());
		}
		
	}
	
	private void addOption(double value) throws SQLException, DaxploreException {
		int i = 0;
		for(Option optionText : mq.getScale().getOptions()){
			String ref = optionText.getTextRef().getRef();
			int j = Integer.parseInt(ref.substring(ref.lastIndexOf('_')+1));
			i = i<j ? j : i;
		}
		TextReference textRef;
		textRef = mainController.getDaxploreFile().getMetaData().getTextsManager().get(mq.getId() + "_option_" + (i+1));
		
		NumberlineCoverage transformation = new NumberlineCoverage(value);
		boolean setNew = true;
		List<Option> scale = mq.getScale().getOptions();
		scale.add(new Option(textRef, value, transformation, setNew));
		mq.getScale().setOptions(scale);
		scaleTableModel.fireTableStructureChanged();
	}
	
}
