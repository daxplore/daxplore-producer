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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.WindowConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.DaxploreFile;
import org.daxplore.producer.daxplorelib.metadata.MetaMean.Direction;
import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;
import org.daxplore.producer.daxplorelib.metadata.MetaScale;
import org.daxplore.producer.daxplorelib.metadata.MetaScale.Option;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;
import org.daxplore.producer.daxplorelib.raw.RawMetaQuestion;
import org.daxplore.producer.daxplorelib.raw.VariableOptionInfo;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;

public class VariableController implements TableModelListener, ActionListener {
	
	enum QuestionCommand {
		FREQ_ENABLE, FREQ_ADD, FREQ_REMOVE, FREQ_UP, FREQ_DOWN, FREQ_INVERT,
		LINE_ENABLE, MEAN_ENABLE, MEAN_DIRECTION, GLOBAL_MEAN_CHANGE, USE_GLOBAL_MEAN,
	}
	
	private VariableView view;
	private MetaQuestion mq;
	private RawVariableTableModel rawModel;
	private RawVariableTable rawTable;
	private MeanExcludedTableModel meanExcludedModel;
	private MeanExcludedTable meanExcludedTable;
	private VariableTableModel variableModel;
	private VariableTable variableTable;
	private List<VariableOptionInfo> rawVariableList;
	private RawMetaQuestion rawMetaQuestion;
	private DaxploreFile daxploreFile;
	
	private TabInfoPanel infoPanel;
	private TabFrequenciesPanel freqPanel;
	private TabMeanPanel meanPanel;
	private TabTextPanel textPanel;
	
	public VariableController(EventBus eventBus, DaxploreFile daxploreFile, MetaQuestion metaQuestion) throws DaxploreException {
		this.mq = metaQuestion;
		this.daxploreFile = daxploreFile;
		
		eventBus.register(this);
		
		rawMetaQuestion = daxploreFile.getRawMetaManager().getQuestion(metaQuestion.getColumn());
		rawVariableList = daxploreFile.getRawColumnInfo(rawMetaQuestion.getColumn());

		variableModel = new VariableTableModel(metaQuestion.getScale(), calculateAfter());
		variableTable = new VariableTable(eventBus, variableModel);
		variableModel.addTableModelListener(this);
		List<Integer> availebleToNumbers = variableModel.getAvailebleToNumbers();
		
		rawModel = new RawVariableTableModel(rawMetaQuestion, rawVariableList, metaQuestion.getScale(), availebleToNumbers);
		rawTable = new RawVariableTable(eventBus, rawModel);
		rawModel.addTableModelListener(this);
		
		meanExcludedModel = new MeanExcludedTableModel(rawMetaQuestion, rawVariableList, metaQuestion.getMetaMean());
		meanExcludedTable = new MeanExcludedTable(meanExcludedModel);
		meanExcludedModel.addTableModelListener(this);
		
		TimePointTableModel timePointTableModel = new TimePointTableModel(
				daxploreFile.getMetaTimepointShortManager(),
				daxploreFile.getRawDataManager(),
				daxploreFile.getAbout(),
				metaQuestion);
		
		TimePointTable timePointTable = new TimePointTable(eventBus, timePointTableModel);
		
		infoPanel = new TabInfoPanel(eventBus, metaQuestion, timePointTable);
		freqPanel = new TabFrequenciesPanel(this, metaQuestion, rawTable, variableTable);
		meanPanel = new TabMeanPanel(this, metaQuestion, meanExcludedTable);
		textPanel = new TabTextPanel();
		
		view = new VariableView(infoPanel, freqPanel, meanPanel, textPanel);
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
				if(option.containsValue(info.getValue())) {
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
		if (e.getActionCommand().startsWith("mean-option-")) {
			String key = e.getActionCommand().replaceFirst("mean-option-", "");
			JCheckBox box = (JCheckBox)e.getSource();
			boolean checked = box.isSelected();
			mq.getMetaMean().setExcludedValue(Double.parseDouble(key), checked);
		} else {
			switch(QuestionCommand.valueOf(e.getActionCommand())) {
			case FREQ_ENABLE:
				mq.setUseFrequencies(freqPanel.isFreqActivated());
				freqPanel.setEnabled(freqPanel.isFreqActivated());
				break;
			case FREQ_ADD:
				int refIndex = mq.getScale().getLowestUnusedTextrefIndex();
				try {
					TextReference textRef = daxploreFile.getTextReferenceManager().get(mq.getColumn() + "_option_" + refIndex);
					switch (mq.getType()) {
					case NUMERIC:
						MetaScale<Double> msDouble = (MetaScale<Double>) mq.getScale();
						msDouble.addOption(new Option<Double>(textRef, new HashSet<Double>(), true));
						break;
					case TEXT:
						MetaScale<String> msString = (MetaScale<String>) mq.getScale();
						msString.addOption(new Option<String>(textRef, new HashSet<String>(), true));
						break;
					}
					
					variableModel.fireTableStructureChanged();
					variableTable.packAll();
					rawTable.setAvailableNumbers(variableModel.getAvailebleToNumbers());
					rawModel.remapFromMetaScale();
					rawModel.fireTableStructureChanged();
					rawTable.packAll();
				} catch (DaxploreException de) {
					// TODO Auto-generated catch block
					de.printStackTrace();
				}
				break;
			case FREQ_REMOVE:
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
			case FREQ_UP:
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
			case FREQ_DOWN:
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
			case FREQ_INVERT:
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
			case LINE_ENABLE:
				boolean useLine = meanPanel.isLineActivated();
				mq.setUseLine(useLine);
				meanPanel.updateEnabled();
				break;
			case MEAN_ENABLE:
				boolean useMean = meanPanel.isMeanActivated();
				mq.setUseMean(useMean);
				meanPanel.updateEnabled();
				break;
			case MEAN_DIRECTION:
				Direction direction = meanPanel.getGoodDirection();
				mq.getMetaMean().setGoodDirection(direction);
				break;
			case USE_GLOBAL_MEAN:
				boolean useGlobalMean = meanPanel.isGlobalMeanUsed();
				mq.getMetaMean().setMeanReferenceValue(useGlobalMean);
				meanPanel.setUseGlobalMean(useGlobalMean);
				break;
			case GLOBAL_MEAN_CHANGE:
				double globalMean = meanPanel.getGlobalMean();
				if(!Double.isNaN(globalMean)) {
					mq.getMetaMean().setGlobalMean(globalMean);
				}
				break;
			default:
				throw new AssertionError("Undefined action command: " + e.getActionCommand());
			}
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
