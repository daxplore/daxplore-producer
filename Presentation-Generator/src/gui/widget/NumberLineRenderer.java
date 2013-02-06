package gui.widget;

import javax.swing.JLabel;

import tools.NumberlineCoverage;

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
