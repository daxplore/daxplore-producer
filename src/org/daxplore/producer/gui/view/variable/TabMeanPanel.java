package org.daxplore.producer.gui.view.variable;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.daxplore.producer.daxplorelib.metadata.MetaMean.Direction;
import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;
import org.daxplore.producer.gui.resources.UITexts;
import org.daxplore.producer.gui.view.variable.VariableController.QuestionCommand;

public class TabMeanPanel extends JPanel {
	
	private JCheckBox meanCheckBox = new JCheckBox();
	private JLabel goodDirectionText = new JLabel();
	private JComboBox<Direction> goodDirection = new JComboBox<>(Direction.values());
	private JCheckBox useGlobalMean = new JCheckBox();
	private JLabel globalMeanText = new JLabel();
	private JFormattedTextField globalMean = new JFormattedTextField(NumberFormat.getNumberInstance());
	private JLabel excludedHeader = new JLabel();
	private MeanExcludedTable meanExcludedTable;
	
	public TabMeanPanel(ActionListener actionListener, MetaQuestion metaQuestion, MeanExcludedTable meanExcludedTable) {
		this.meanExcludedTable = meanExcludedTable;
		
		setLayout(new BorderLayout());
		JPanel topPanel = new JPanel(new GridLayout(0, 1));

		meanCheckBox.setText(UITexts.get("question_edit.mean.enable"));
		meanCheckBox.setActionCommand(QuestionCommand.MEAN_ENABLE.name());
		meanCheckBox.addActionListener(actionListener);
		meanCheckBox.setSelected(metaQuestion.useMean());
		topPanel.add(meanCheckBox);
		
		goodDirectionText.setText(UITexts.get("question_edit.mean.good_direction"));
		topPanel.add(goodDirectionText);
		
		goodDirection.setSelectedItem(metaQuestion.getMetaMean().getGoodDirection());
		goodDirection.setActionCommand(QuestionCommand.MEAN_DIRECTION.name());
		goodDirection.addActionListener(actionListener);
		topPanel.add(goodDirection);

		useGlobalMean.setText(UITexts.get("question_edit.mean.use_global_mean"));
		useGlobalMean.setActionCommand(QuestionCommand.USE_GLOBAL_MEAN.name());
		useGlobalMean.addActionListener(actionListener);
		useGlobalMean.setSelected(metaQuestion.getMetaMean().useMeanReferenceValue());
		topPanel.add(useGlobalMean);
		
		add(topPanel, BorderLayout.NORTH);
		
		globalMean.setActionCommand(QuestionCommand.GLOBAL_MEAN_CHANGE.name());
		globalMean.addActionListener(actionListener);
		globalMean.setColumns(10);
		double globalMeanValue = metaQuestion.getMetaMean().getGlobalMean();
		if(!Double.isNaN(globalMeanValue)){
			globalMean.setValue(globalMeanValue);
		}
		
		JPanel textFieldPanel = new JPanel();
		textFieldPanel.setLayout(new FlowLayout());
        JPanel labelPane = new JPanel(new GridLayout(0,1));
        globalMeanText.setText(UITexts.get("question_edit.mean.global_mean"));
        labelPane.add(globalMeanText);

        JPanel fieldPane = new JPanel(new GridLayout(0,1));
        fieldPane.add(globalMean);

        textFieldPanel. add(labelPane, BorderLayout.CENTER);
        textFieldPanel.add(fieldPane, BorderLayout.EAST);
        
        add(textFieldPanel, BorderLayout.WEST);
        
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(meanExcludedTable);
        
        JPanel excludedSection = new JPanel(new BorderLayout());
        excludedHeader = new JLabel(UITexts.get("question_edit.mean.excluded_header"));
        excludedSection.add(excludedHeader, BorderLayout.WEST);
        excludedSection.add(scrollPane, BorderLayout.SOUTH);
        add(excludedSection, BorderLayout.SOUTH);

        setUseGlobalMean(isGlobalMeanUsed());
        setEnabled(isMeanActivated());
        updateEnabled();
	}
	
	public boolean isMeanActivated() {
		return meanCheckBox.isSelected();
	}
	
	public void updateEnabled() {
		boolean enabled = isMeanActivated();
		goodDirectionText.setEnabled(enabled);
		goodDirection.setEnabled(enabled);
		useGlobalMean.setEnabled(enabled);
		globalMeanText.setEnabled(enabled && isGlobalMeanUsed());
		globalMean.setEnabled(enabled && isGlobalMeanUsed());
		excludedHeader.setEnabled(enabled );
		meanExcludedTable.setEnabled(enabled);
	}
	
	public boolean isGlobalMeanUsed() {
		return useGlobalMean.isSelected();
	}

	public void setUseGlobalMean(boolean useGlobalMean) {
		globalMean.setEnabled(useGlobalMean);
		globalMeanText.setEnabled(useGlobalMean);
	}
	
	public double getGlobalMean() {
		Object value = globalMean.getValue();
		if(value instanceof Double) {
			return (Double)value;
		}
		if (value instanceof Long) {
			return (Long)value;
		}
		if (value instanceof String) {
			try {
				return Double.parseDouble((String)(value));
			} catch(NumberFormatException e) {
				return Double.NaN;
			}
		}
		return (Double)value;
	}

	public Direction getGoodDirection() {
		return (Direction)goodDirection.getSelectedItem();
	}
}

