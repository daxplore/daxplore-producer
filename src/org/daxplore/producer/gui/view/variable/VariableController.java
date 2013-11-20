package org.daxplore.producer.gui.view.variable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.DaxploreFile;
import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;
import org.daxplore.producer.daxplorelib.metadata.MetaScale;
import org.daxplore.producer.daxplorelib.metadata.MetaScale.Option;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;
import org.daxplore.producer.gui.view.question.ScaleTable;
import org.daxplore.producer.gui.view.question.ScaleTableModel;
import org.daxplore.producer.gui.view.question.TimePointTable;
import org.daxplore.producer.gui.view.question.TimePointTableModel;
import org.daxplore.producer.gui.widget.ColumnTableModel;
import org.daxplore.producer.tools.NumberlineCoverage;
import org.daxplore.producer.tools.Pair;

import com.google.common.eventbus.EventBus;

public class VariableController implements TableModelListener, ActionListener {
	
	enum QuestionCommand {
		ADD, REMOVE, UP, DOWN, INVERT
	}
	
	private EventBus eventBus;
	private DaxploreFile daxploreFile;
	
	private VariableView view;
	private LinkedList<Pair<Double, Integer>> values;
	private MetaQuestion mq;
	private ColumnTableModel afterTableModel;
	private JTable afterTable;
	private ScaleTable scaleTable;
	private ScaleTableModel scaleTableModel;
	private JTable beforeTable;
	
	public VariableController(EventBus eventBus, DaxploreFile daxploreFile, MetaQuestion metaQuestion) {
		this.eventBus = eventBus;
		this.mq = metaQuestion;
		this.daxploreFile = daxploreFile;
		eventBus.register(this);
		
		
		scaleTableModel = new ScaleTableModel(metaQuestion.getScale());
		scaleTableModel.addTableModelListener(this);
		scaleTable = new ScaleTable(eventBus, scaleTableModel);
		
		try {
			values = daxploreFile.getRawData().getColumnValueCount(metaQuestion.getId());
			beforeTable = new JTable(new ColumnTableModel(values));
		
			updateCalculatedValues();
			afterTable = new JTable(afterTableModel);

			TimePointTableModel timePointTableModel = new TimePointTableModel(
					daxploreFile.getMetaTimepointShortManager(),
					daxploreFile.getRawData(),
					daxploreFile.getAbout(),
					metaQuestion);
			
			TimePointTable timePointTable = new TimePointTable(eventBus, timePointTableModel);
			
			view = new VariableView(eventBus, this, metaQuestion, scaleTable, beforeTable, afterTable, timePointTable);
			
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
		if(mq==null || mq.getScale()==null) {
			return new LinkedList<>();
		}
		TreeMap<Double, Integer> valueMap = new TreeMap<>();
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
		
		LinkedList<Pair<Double, Integer>> list = new LinkedList<>();
		for(Entry<Double, Integer> entry : valueMap.entrySet()) {
			list.add(new Pair<>(entry.getKey(), entry.getValue()));
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
		switch(QuestionCommand.valueOf(e.getActionCommand())) {
		case ADD:
			//TODO add even if there are no unused o.getTransformation().contains(p.getKey())
			try {
				if(mq.getScale()==null){
					mq.setScale(daxploreFile.getMetaScaleManager().create(new LinkedList<Option>(), new NumberlineCoverage()));
					scaleTableModel = new ScaleTableModel(mq.getScale());
					scaleTable = new ScaleTable(eventBus, scaleTableModel);
					//view.getScaleScrollPane().setViewportView(scaleTable);
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
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			break;
		case REMOVE:
			int[] selectedRows = scaleTable.getSelectedRows();
			List<Option> options = mq.getScale().getOptions();
			for(int i = selectedRows.length-1; i>=0; i--) {
				options.remove(selectedRows[i]);
			}
			mq.getScale().setOptions(options);
			scaleTableModel.fireTableStructureChanged();
			break;
		case UP:
			selectedRows = scaleTable.getSelectedRows();
			if(selectedRows.length < 1 || selectedRows[0] == 0) break;
			scaleTable.clearSelection();
			scaleTable.removeEditor();
			for(int i = 0; i < selectedRows.length; i++) {
				scaleTableModel.moveRow(selectedRows[i], selectedRows[i], selectedRows[i]-1);
				scaleTable.getSelectionModel().addSelectionInterval(selectedRows[i]-1, selectedRows[i]-1);
			}
			break;
		case DOWN:
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
		case INVERT:
			options = mq.getScale().getOptions();
			List<Option> invertedOptions = new LinkedList<>();
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
	
	private void addOption(double value) throws SQLException {
		int i = -1;
		for(Option optionText : mq.getScale().getOptions()){
			String ref = optionText.getTextRef().getRef();
			int j = Integer.parseInt(ref.substring(ref.lastIndexOf('_')+1));
			i = i<j ? j : i;
		}
		TextReference textRef;
		textRef = daxploreFile.getTextReferenceManager().get(mq.getId() + "_option_" + (i+1));
		
		NumberlineCoverage transformation = new NumberlineCoverage(value);
		boolean setNew = true;
		List<Option> scale = mq.getScale().getOptions();
		scale.add(new Option(textRef, value, transformation, setNew));
		mq.getScale().setOptions(scale);
		scaleTableModel.fireTableStructureChanged();
	}
	
	public VariableView getView() {
		return view;
	}
	
	/**
	 * Return a dialog that can be showed modal relative a window with setLocationRelativeTo(Window) and show()
	 * @return JDialog containing a VariableView
	 */
	public JDialog getDialog() {
		JDialog dialog = new JDialog();
		dialog.setTitle(mq.getId());
		dialog.setContentPane(getView());
		dialog.setModal(true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		return dialog;
	}
	
}
