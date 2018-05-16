package org.daxplore.producer.gui.view.variable;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;
import org.daxplore.producer.gui.resources.UITexts;
import org.daxplore.producer.gui.view.variable.VariableController.QuestionCommand;

@SuppressWarnings("serial")
public class TabFrequenciesPanel extends JPanel {
	
	private MetaQuestion metaQuestion;
	private JScrollPane variableScrollPane = new JScrollPane();
	private JScrollPane rawScrollPane = new JScrollPane();
	private JCheckBox enableFreqCheckBox = new JCheckBox();
	private JCheckBox enableDichLineCheckBox = new JCheckBox();
	private JTable rawTable, variableTable;
	private JPanel scalePanel;
	private JButton invertButton, removeButton, downButton, upButton, addButton;
	
	public TabFrequenciesPanel(ActionListener actionListener, 
			MetaQuestion metaQuestion, JTable rawTable, JTable variableTable) {
		this.metaQuestion = metaQuestion;
		this.rawTable = rawTable;
		this.variableTable = variableTable;
		
		setLayout(new BorderLayout());
		
		JPanel topPanel = new JPanel(new BorderLayout());
		enableFreqCheckBox.setText(UITexts.get("question_edit.freq.enable"));
		enableFreqCheckBox.setActionCommand(QuestionCommand.FREQ_ENABLE.name());
		enableFreqCheckBox.setSelected(metaQuestion.useFrequencies());
		enableFreqCheckBox.addActionListener(actionListener);
		topPanel.add(enableFreqCheckBox, BorderLayout.NORTH);
		
		enableDichLineCheckBox.setText(UITexts.get("question_edit.dichline.enable"));
		enableDichLineCheckBox.setActionCommand(QuestionCommand.DICHLINE_ENABLE.name());
		enableDichLineCheckBox.setSelected(metaQuestion.useDichotomizedLine());
		enableDichLineCheckBox.addActionListener(actionListener);
		topPanel.add(enableDichLineCheckBox, BorderLayout.SOUTH);
		
		add(topPanel, BorderLayout.NORTH);
		
		rawScrollPane.setViewportView(rawTable);
		variableScrollPane.setViewportView(variableTable);
		
		scalePanel = new JPanel(new GridLayout(1, 2));
		add(scalePanel, BorderLayout.CENTER);
		scalePanel.add(rawScrollPane, 0);
		
		JPanel rightPanel = new JPanel();
		scalePanel.add(rightPanel, 1);
		rightPanel.setLayout(new BorderLayout(0, 0));
		
		rightPanel.add(variableScrollPane, BorderLayout.CENTER);
		
		JPanel actionButtonPanel = new JPanel();
		rightPanel.add(actionButtonPanel, BorderLayout.SOUTH);
		
		addButton = new JButton("Add");
		addButton.setActionCommand(QuestionCommand.FREQ_ADD.name());
		addButton.addActionListener(actionListener);
		actionButtonPanel.add(addButton);
		
		upButton = new JButton("Up");
		upButton.setActionCommand(QuestionCommand.FREQ_UP.name());
		upButton.addActionListener(actionListener);
		actionButtonPanel.add(upButton);
		
		downButton = new JButton("Down");
		downButton.setActionCommand(QuestionCommand.FREQ_DOWN.name());
		downButton.addActionListener(actionListener);
		actionButtonPanel.add(downButton);
		
		removeButton = new JButton("Remove");
		removeButton.setActionCommand(QuestionCommand.FREQ_REMOVE.name());
		removeButton.addActionListener(actionListener);
		actionButtonPanel.add(removeButton);
		
		invertButton = new JButton("Invert");
		invertButton.setActionCommand(QuestionCommand.FREQ_INVERT.name());
		invertButton.addActionListener(actionListener);
		actionButtonPanel.add(invertButton);
		
		updateEnabled();
	}

	public boolean isFreqActivated() {
		return enableFreqCheckBox.isSelected();
	}
	
	public boolean isDichotomizedLineActivated() {
		return enableDichLineCheckBox.isSelected();
	}
	
	public void updateEnabled() {
		boolean enabled = metaQuestion.useFrequencies() || metaQuestion.useDichotomizedLine();
		
		scalePanel.setEnabled(enabled);
		variableScrollPane.setEnabled(enabled);
		rawScrollPane.setEnabled(enabled);
		rawTable.setEnabled(enabled);
		variableTable.setEnabled(enabled);
		invertButton.setEnabled(enabled);
		removeButton.setEnabled(enabled);
		downButton.setEnabled(enabled);
		upButton.setEnabled(enabled);
		addButton.setEnabled(enabled);
	}
}
