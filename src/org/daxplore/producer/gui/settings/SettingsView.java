package org.daxplore.producer.gui.settings;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.daxplore.producer.gui.SectionHeader;
import org.daxplore.producer.gui.resources.GuiTexts;

public class SettingsView extends JPanel {
	
	private GuiTexts texts;
	private ActionListener listener;
	
	public SettingsView(GuiTexts texts, ActionListener listener) {
		this.texts = texts;
		this.listener = listener;
		
		setLayout(new BorderLayout());
		
		add(new SectionHeader(texts, "settings"), BorderLayout.NORTH);
		
	}
	
	void build(Map<String, Boolean> boolSettings) {
		JPanel boolPanel = new JPanel();
		boolPanel.setLayout(new BoxLayout(boolPanel, BoxLayout.Y_AXIS));
		for(String key: boolSettings.keySet()) {
			Boolean checked = boolSettings.get(key);
			JCheckBox box = new JCheckBox(texts.get("setting." + key + ".checkbox"), checked);
			box.setActionCommand(key);
			box.addActionListener(listener);
			boolPanel.add(box);
		}
		add(boolPanel, BorderLayout.CENTER);
	}
}
