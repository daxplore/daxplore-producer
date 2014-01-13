/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui.utility;

import java.awt.Color;

import javax.swing.JTextPane;

/**
 * A simple text label with selectable text.
 */
@SuppressWarnings("serial")
public class JLabelSelectable extends JTextPane {
	
	public JLabelSelectable(String text) {
		this(text, true);
	}
	
	public JLabelSelectable(String text, boolean asHTML) {
		if(asHTML) {
			setContentType("text/html");
		}
		setText(text);
		setOpaque(false);
		setEditable(false);
		setBackground(new Color(0, 0, 0, 0));
		setBorder(null);
	}
}
