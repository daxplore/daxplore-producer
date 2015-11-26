package org.daxplore.producer.gui.view.variable;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;
import org.daxplore.producer.gui.resources.GuiTexts;
import org.daxplore.producer.gui.view.variable.VariableController.QuestionCommand;

public class TabMeanPanel extends JPanel {
	
	private JCheckBox enableCheckBox = new JCheckBox();
	private JCheckBox useGlobalMean = new JCheckBox();
	private JLabel globalMeanText = new JLabel();
	private JFormattedTextField globalMean = new JFormattedTextField(NumberFormat.getNumberInstance());
	
	public TabMeanPanel(GuiTexts texts, ActionListener actionListener, MetaQuestion metaQuestion) {
		setLayout(new BorderLayout());
		
		JPanel topPanel = new JPanel(new GridLayout(0, 1));
		
		
		enableCheckBox.setText(texts.get("question_edit.mean.enable"));
		enableCheckBox.setActionCommand(QuestionCommand.MEAN_ENABLE.name());
		enableCheckBox.addActionListener(actionListener);
		enableCheckBox.setSelected(metaQuestion.useMean());
		topPanel.add(enableCheckBox);
		
		topPanel.add(useGlobalMean);

		useGlobalMean.setText(texts.get("question_edit.mean.use_global_mean"));
		useGlobalMean.setActionCommand(QuestionCommand.USE_GLOBAL_MEAN.name());
		useGlobalMean.addActionListener(actionListener);
		useGlobalMean.setSelected(metaQuestion.getMetaMean().useGlobalMean());
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
        globalMeanText.setText(texts.get("question_edit.mean.global_mean"));
        labelPane.add(globalMeanText);

        JPanel fieldPane = new JPanel(new GridLayout(0,1));
        fieldPane.add(globalMean);

        textFieldPanel. add(labelPane, BorderLayout.CENTER);
        textFieldPanel.add(fieldPane, BorderLayout.EAST);
        
        add(textFieldPanel, BorderLayout.WEST);
        
        setUseGlobalMean(isGlobalMeanUsed());
        setEnabled(isMeanActivated());
	}
	
	public boolean isMeanActivated() {
		return enableCheckBox.isSelected();
	}
	
	public void setEnabled(boolean enabled) {
		useGlobalMean.setEnabled(enabled);
		globalMeanText.setEnabled(enabled && isGlobalMeanUsed());
		globalMean.setEnabled(enabled && isGlobalMeanUsed());
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
}

