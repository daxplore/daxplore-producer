/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui.widget;

import javax.swing.JPanel;
import javax.swing.JTextField;

import org.daxplore.producer.tools.NumberlineCoverage;
import org.daxplore.producer.tools.NumberlineCoverage.NumberlineCoverageException;

@SuppressWarnings("serial")
public class NumberLineEditor extends JPanel implements AbstractWidgetEditor<NumberlineCoverage> {
	
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
