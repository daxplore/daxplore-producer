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
