/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui.view.variable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.JDialog;
import javax.swing.WindowConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.DaxploreFile;
import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;
import org.daxplore.producer.daxplorelib.metadata.MetaScale;
import org.daxplore.producer.daxplorelib.metadata.MetaScale.Option;
import org.daxplore.producer.daxplorelib.raw.RawMeta.RawMetaQuestion;
import org.daxplore.producer.daxplorelib.raw.VariableOptionInfo;
import org.daxplore.producer.gui.resources.GuiTexts;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;

public class VariableController implements TableModelListener, ActionListener {
	
	enum QuestionCommand {
		ADD, REMOVE, UP, DOWN, INVERT
	}
	
	private VariableView view;
	private MetaQuestion mq;
	private RawVariableTableModel rawModel;
	private RawVariableTable rawTable;
	private VariableTableModel variableModel;
	private VariableTable variableTable;
	private List<VariableOptionInfo> rawVariableList;
	private RawMetaQuestion rawMetaQuestion;
	private EventBus eventBus;
	
	public VariableController(EventBus eventBus, GuiTexts texts, DaxploreFile daxploreFile, MetaQuestion metaQuestion) {
		this.mq = metaQuestion;
		this.eventBus = eventBus;
		eventBus.register(this);
		
		try {
			
			rawMetaQuestion = daxploreFile.getRawMeta().getQuestion(metaQuestion.getColumn());
			rawVariableList = daxploreFile.getRawColumnInfo(rawMetaQuestion.column);

			variableModel = new VariableTableModel(metaQuestion.getScale(), calculateAfter());
			variableTable = new VariableTable(eventBus, texts, variableModel);
			variableModel.addTableModelListener(this);
			List<Integer> availebleToNumbers = variableModel.getAvailebleToNumbers();
			
			rawModel = new RawVariableTableModel(rawMetaQuestion, rawVariableList, metaQuestion.getScale(), availebleToNumbers);
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
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	LinkedList<Integer> calculateAfter() {
		if(mq==null || mq.getScale()==null) {
			return new LinkedList<>();
		}
		TreeMap<Integer, Integer> valueMap = new TreeMap<>();
		int i = 0; //TODO: when metascales changes to explicit ordering, fix this too
		for(MetaScale.Option option: mq.getScale().getOptions()) {
			int total = 0;
			for(VariableOptionInfo info: rawVariableList) {
				//TODO: remove cast when adding string value support
				if(info.getValue() != null && option.containsValue((Double)info.getValue())) {
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
	@SuppressWarnings("unchecked")
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
			variableTable.clearSelection();
			variableTable.removeEditor();

			switch (mq.getType()) {
			case NUMERIC:
				MetaScale<Double> msDouble = (MetaScale<Double>) mq.getScale();
				List<Option<Double>> optionsDouble = msDouble.getOptions();
				for(int i = selectedRows.length-1; i>=0; i--) {
					optionsDouble.remove(selectedRows[i]);
				}
				msDouble.setOptions(optionsDouble);
				break;
			case TEXT:
				MetaScale<String> msString = (MetaScale<String>) mq.getScale();
				List<Option<String>> optionsString = msString.getOptions();
				for(int i = selectedRows.length-1; i>=0; i--) {
					optionsString.remove(selectedRows[i]);
				}
				msString.setOptions(optionsString);
				break;
			}
			
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
			switch (mq.getType()) {
			case NUMERIC:
				MetaScale<Double> msDouble = (MetaScale<Double>) mq.getScale();
				msDouble.setOptions(Lists.reverse(msDouble.getOptions()));
				break;
			case TEXT:
				MetaScale<Double> msString = (MetaScale<Double>) mq.getScale();
				msString.setOptions(Lists.reverse(msString.getOptions()));
				break;
			}
			
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
		dialog.setTitle(mq.getColumn());
		dialog.setContentPane(getView());
		dialog.setModal(true);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		return dialog;
	}
	
}
