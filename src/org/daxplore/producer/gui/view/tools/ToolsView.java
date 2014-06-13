/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui.view.tools;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

@SuppressWarnings("serial")
public class ToolsView extends JPanel {
	
	private JScrollPane scrollPane;
	private JTable table;
	
	ToolsView() {
		setLayout(null);
		
		scrollPane = new JScrollPane();
		scrollPane.setBounds(301, 234, 294, 204);
		add(scrollPane);
	}
	
	void setLocaleTable(LocalesTableModel tableModel) {
		table = new JTable(tableModel);
		scrollPane.setViewportView(table);
	}

}
