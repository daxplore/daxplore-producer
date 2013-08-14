package org.daxplore.producer.gui.widget;

import javax.swing.JLabel;

import org.daxplore.producer.tools.NumberlineCoverage;

@SuppressWarnings("serial")
public class NumberLineRenderer extends AbstractWidget<NumberlineCoverage> {

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
