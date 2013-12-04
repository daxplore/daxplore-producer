package org.daxplore.producer.gui.widget;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.daxplore.producer.tools.NumberlineCoverage;

@SuppressWarnings("serial")
public class NumberLineRenderer extends JPanel implements AbstractWidget<NumberlineCoverage> {

	private JLabel text;
	
	public NumberLineRenderer() {
		text = new JLabel();
		add(text);
	}

	@Override
	public void setContent(NumberlineCoverage value) {
		text.setText(value.toString());
	}
	
}
