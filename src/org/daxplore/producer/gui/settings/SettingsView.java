package org.daxplore.producer.gui.settings;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.daxplore.producer.gui.SectionHeader;
import org.daxplore.producer.gui.resources.UITexts;

public class SettingsView extends JPanel {
	private ActionListener listener;
	
	public SettingsView(ActionListener listener) {
		this.listener = listener;
		
		setLayout(new BorderLayout());
		
		add(new SectionHeader("settings"), BorderLayout.NORTH);
	}
	
	void build(Map<String, Boolean> boolSettings, SettingsTableModel model) {
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		
		JPanel boolPanel = new JPanel();
		boolPanel.setLayout(new BoxLayout(boolPanel, BoxLayout.Y_AXIS));
		boolPanel.setAlignmentX(0);
		for(String key: boolSettings.keySet()) {
			Boolean checked = boolSettings.get(key);
			JCheckBox box = new JCheckBox(UITexts.get("setting." + key + ".checkbox"), checked);
			box.setActionCommand(key);
			box.addActionListener(listener);
			boolPanel.add(box, Component.LEFT_ALIGNMENT);
		}
		contentPanel.add(boolPanel, Component.LEFT_ALIGNMENT);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		buttonPanel.setAlignmentX(0);

		JButton checkFreq = new JButton(UITexts.get("setting.button.checkfreq"));
		checkFreq.setActionCommand("checkFreq");
		checkFreq.addActionListener(listener);
		buttonPanel.add(checkFreq);
		
		JButton uncheckFreq = new JButton(UITexts.get("setting.button.uncheckfreq"));
		uncheckFreq.setActionCommand("uncheckFreq");
		uncheckFreq.addActionListener(listener);
		buttonPanel.add(uncheckFreq);
		
		JButton checkMean = new JButton(UITexts.get("setting.button.checkmean"));
		checkMean.setActionCommand("checkMean");
		checkMean.addActionListener(listener);
		buttonPanel.add(checkMean);
		
		JButton uncheckMean = new JButton(UITexts.get("setting.button.uncheckmean"));
		uncheckMean.setActionCommand("uncheckMean");
		uncheckMean.addActionListener(listener);
		buttonPanel.add(uncheckMean);
		
		contentPanel.add(buttonPanel, Component.LEFT_ALIGNMENT);
		
		JScrollPane tableScroller = new JScrollPane();
		SettingsTable table = new SettingsTable(model);
		tableScroller.setViewportView(table);
		contentPanel.add(tableScroller, Component.LEFT_ALIGNMENT);
		
		add(contentPanel, BorderLayout.WEST);
	}
}
