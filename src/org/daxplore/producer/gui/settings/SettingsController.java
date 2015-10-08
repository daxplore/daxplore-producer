package org.daxplore.producer.gui.settings;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JCheckBox;

import org.daxplore.producer.daxplorelib.DaxploreProperties;
import org.daxplore.producer.daxplorelib.Settings;
import org.daxplore.producer.gui.event.DaxploreFileUpdateEvent;
import org.daxplore.producer.gui.resources.GuiTexts;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class SettingsController implements ActionListener{

	private SettingsView settingsView;
	private Settings settings;
	private GuiTexts texts;
	
	public SettingsController(EventBus eventBus, GuiTexts texts) {
		this.texts = texts;
		eventBus.register(this);
		settingsView = new SettingsView(texts, this);
	}
	
	public Component getView() {
		return settingsView;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String key = e.getActionCommand();
		JCheckBox box = (JCheckBox)e.getSource();
		boolean checked = box.isSelected();
		settings.putSetting(key, checked);
	}
	
	@Subscribe
	public void on(DaxploreFileUpdateEvent e) {
		settings = e.getDaxploreFile().getSettings();
		Map<String, Boolean> boolSettings = new LinkedHashMap<>();
		for(String key: DaxploreProperties.clientBoolSettings) {
			boolSettings.put(key, settings.getBool(key));
		}
		settingsView.build(boolSettings);
	}
}
