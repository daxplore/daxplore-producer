package gui.widget;

import javax.swing.JTextField;

import tools.NumberlineCoverage;
import tools.NumberlineCoverage.NumberlineCoverageException;

@SuppressWarnings("serial")
public class NumberLineEditor extends AbstractWidgetEditor<NumberlineCoverage> {
	
	JTextField textField;
	NumberlineCoverage numberline;
	
	public NumberLineEditor() {
		textField = new JTextField();
		add(textField);
	}

	@Override
	public NumberlineCoverage getContent() throws InvalidContentException {
		try {
			return new NumberlineCoverage(textField.getText());
		} catch (NumberlineCoverageException e) {
			throw new InvalidContentException(e);
		}
	}

	@Override
	public void setContent(NumberlineCoverage value) {
		this.numberline = value;
		textField.setText(numberline.toString());
	}
	
}
