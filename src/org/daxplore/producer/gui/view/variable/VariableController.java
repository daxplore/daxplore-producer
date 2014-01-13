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
import org.daxplore.producer.daxplorelib.raw.VariableOptionInfo;
import org.daxplore.producer.gui.resources.GuiTexts;
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
	private GuiTexts texts;
	private DaxploreFile daxploreFile;
	
	private VariableView view;
	private MetaQuestion mq;
	private RawVariableTableModel rawModel;
	private RawVariableTable rawTable;
	private VariableTableModel variableModel;
	private VariableTable variableTable;
	private List<VariableOptionInfo> rawVariableList;
	
	public VariableController(EventBus eventBus, GuiTexts texts, DaxploreFile daxploreFile, MetaQuestion metaQuestion) {
		this.eventBus = eventBus;
		this.texts = texts;
		this.mq = metaQuestion;
		this.daxploreFile = daxploreFile;
		eventBus.register(this);
		
		try {
			rawVariableList = daxploreFile.getRawColumnInfo(metaQuestion.getId());

			variableModel = new VariableTableModel(metaQuestion.getScale(), calculateAfter());
			variableTable = new VariableTable(eventBus, texts, variableModel);
			variableModel.addTableModelListener(this);
			List<Integer> availebleToNumbers = variableModel.getAvailebleToNumbers();
			
			rawModel = new RawVariableTableModel(rawVariableList, metaQuestion.getScale(), availebleToNumbers);
			rawTable = new RawVariableTable(eventBus, texts, rawModel);
			rawModel.addTableModelListener(this);
			
			TimePointTableModel timePointTableModel = new TimePointTableModel(
					daxploreFile.getMetaTimepointShortManager(),
					daxploreFile.getRawData(),
					daxploreFile.getAbout(),
					metaQuestion);
			
			TimePointTable timePointTable = new TimePointTable(eventBus, texts, timePointTableModel);
			
			view = new VariableView(eventBus, texts, this, metaQuestion, rawTable, variableTable, timePointTable);
			
		} catch (SQLException | DaxploreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	private void updateCalculatedValues() {
		LinkedList<Integer> afterValues = calculateAfter();
		if(variableModel != null) {
			variableModel.setAfterValues(afterValues);
		}
	}
	
	LinkedList<Integer> calculateAfter() {
		if(mq==null || mq.getScale()==null) {
			return new LinkedList<>();
		}
		TreeMap<Integer, Integer> valueMap = new TreeMap<>();
		int i = 0; //TODO: when metascales changes to explicit ordering, fix this too
		for(MetaScale.Option option: mq.getScale().getOptions()) {
			int total = 0;
			for(VariableOptionInfo info: rawVariableList) {
				if(info.getValue() != null && option.getTransformation().contains(info.getValue())) {
					total += info.getCount();
				}
			}
			if(valueMap.containsKey(i)) {
				Integer p = valueMap.get(i);
				valueMap.put(i, total + p);
			} else {
				valueMap.put(i, total);
			}
			i++;
		}
		
		LinkedList<Integer> list = new LinkedList<>();
		for(Entry<Integer, Integer> entry : valueMap.entrySet()) {
			list.add(entry.getValue());
		}
		
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
//		case ADD:
//			//TODO add even if there are no unused o.getTransformation().contains(p.getKey())
//			try {
//				if(mq.getScale()==null){
//					mq.setScale(daxploreFile.getMetaScaleManager().create(new LinkedList<Option>(), new NumberlineCoverage()));
//					scaleTableModel = new ScaleTableModel(mq.getScale());
//					scaleTable = new ScaleTable(eventBus, texts, scaleTableModel);
//					//view.getScaleScrollPane().setViewportView(scaleTable);
//					view.validate();
//				    view.repaint();
//					scaleTableModel.fireTableStructureChanged();
//				}
//				boolean added = false;
//				double pKey = 0;
//				AddLabel: for(Pair<Double, Integer> p : values) {
//					if(p.getKey()!=null) {
//						pKey = p.getKey();
//						for(Option o : mq.getScale().getOptions()) {
//							if(o.getTransformation().contains(p.getKey())){
//								continue AddLabel;
//							}
//						}
//						addOption(p.getKey());
//						added = true;
//						scaleTableModel.fireTableStructureChanged();
//						break AddLabel;
//					}
//				}
//				if(!added) {
//					addOption(pKey+1);
//					scaleTableModel.fireTableStructureChanged();
//				}
//			} catch (DaxploreException | SQLException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//			break;
		case REMOVE:
			int[] selectedRows = variableTable.getSelectedRows();
			List<Option> options = mq.getScale().getOptions();
			variableTable.clearSelection();
			variableTable.removeEditor();
			for(int i = selectedRows.length-1; i>=0; i--) {
				options.remove(selectedRows[i]);
			}
			mq.getScale().setOptions(options);
			rawTable.setAvailableNumbers(variableModel.getAvailebleToNumbers());
			variableModel.fireTableRowsDeleted(selectedRows[0], selectedRows[0]);
			variableTable.packAll();
			rawModel.remapFromMetaScale();
			rawModel.fireTableStructureChanged();
			rawTable.packAll();
			break;
		case UP:
			selectedRows = variableTable.getSelectedRows();
			if(selectedRows.length < 1 || selectedRows[0] == 0) break;
			variableTable.clearSelection();
			variableTable.removeEditor();
			for(int i = 0; i < selectedRows.length; i++) {
				variableModel.moveRow(selectedRows[i], selectedRows[i], selectedRows[i]-1);
				variableTable.getSelectionModel().addSelectionInterval(selectedRows[i]-1, selectedRows[i]-1);
			}
			variableTable.packAll();
			rawModel.remapFromMetaScale();
			rawModel.fireTableStructureChanged();
			rawTable.packAll();
			break;
		case DOWN:
			selectedRows = variableTable.getSelectedRows();
			if(selectedRows.length < 1 || selectedRows[selectedRows.length-1] == variableModel.getRowCount() -1) break;
			variableTable.clearSelection();
			variableTable.removeEditor();
			for(int i = selectedRows.length-1; i >= 0; i--) {
				//perspectivesTable.changeSelection(selectedRows[i], 1, true, true);
				variableModel.moveRow(selectedRows[i], selectedRows[i], selectedRows[i]+1);
				variableTable.getSelectionModel().addSelectionInterval(selectedRows[i]+1, selectedRows[i]+1);
			}
			variableTable.packAll();
			rawModel.remapFromMetaScale();
			rawModel.fireTableStructureChanged();
			rawTable.packAll();
			break;
		case INVERT:
			options = mq.getScale().getOptions();
			List<Option> invertedOptions = new LinkedList<>();
			for(Option option : options) {
				invertedOptions.add(0, option);
			}

			mq.getScale().setOptions(invertedOptions);
			variableModel.fireTableStructureChanged();
			variableTable.packAll();
			rawModel.remapFromMetaScale();
			rawModel.fireTableStructureChanged();
			rawTable.packAll();
			break;
		default:
			throw new AssertionError("Undefined action command: " + e.getActionCommand());
		}
		
	}
	
	/*private void addOption(double value) throws DaxploreException {
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
	}*/
	
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
