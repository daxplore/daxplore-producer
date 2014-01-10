/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
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
