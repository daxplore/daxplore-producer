package org.daxplore.producer.gui;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.daxplore.producer.gui.resources.GuiTexts;

@SuppressWarnings("serial")
public class SectionHeader extends JPanel {
	public SectionHeader(GuiTexts texts, String name) {
		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(5, 5, 5, 5));
		String header = "<html><b>" + texts.get("header." + name + ".title") + "</b></html>";
		String explanation = texts.get("header." + name + ".explanation"); 
		add(new JLabel(header), BorderLayout.NORTH);
		add(new JLabel(explanation), BorderLayout.CENTER);
	}
}
