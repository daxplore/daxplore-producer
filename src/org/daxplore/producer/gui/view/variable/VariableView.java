/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui.view.variable;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.daxplore.producer.gui.resources.UITexts;

public class VariableView extends JPanel {
	
	JTabbedPane tabPane = new JTabbedPane();
	
	public VariableView(TabInfoPanel infoPanel, TabFrequenciesPanel freqPanel, TabMeanPanel meanPanel, TabTextPanel textPanel) {
		setLayout(new BorderLayout());
	
		tabPane.addTab(UITexts.get("question_edit.tab.info"), infoPanel);
		tabPane.addTab(UITexts.get("question_edit.tab.freq"), freqPanel);
		tabPane.addTab(UITexts.get("question_edit.tab.mean"), meanPanel);
		tabPane.addTab(UITexts.get("question_edit.tab.text"), textPanel);
	
		this.add(tabPane, BorderLayout.CENTER);
	}
	
}
