package org.daxplore.producer.gui.view.variable;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;
import org.daxplore.producer.gui.resources.GuiTexts;
import org.daxplore.producer.gui.view.variable.VariableController.QuestionCommand;

public class TabMeanPanel extends JPanel {
	
	private JCheckBox enableCheckBox = new JCheckBox();
	
	public TabMeanPanel(GuiTexts texts, ActionListener actionListener, MetaQuestion metaQuestion) {
		setLayout(new BorderLayout());
		
		JPanel topPanel = new JPanel(new BorderLayout());
		enableCheckBox.setText(texts.get("question_edit.mean.enable"));
		enableCheckBox.setActionCommand(QuestionCommand.MEAN_ENABLE.name());
		enableCheckBox.setSelected(metaQuestion.useMean());
		enableCheckBox.addActionListener(actionListener);
		topPanel.add(enableCheckBox);
		add(topPanel, BorderLayout.NORTH);
	}
	
	public boolean isMeanActivated() {
		return enableCheckBox.isSelected();
	}
	
	public void setEnabled(boolean enabled) {
	}
}
