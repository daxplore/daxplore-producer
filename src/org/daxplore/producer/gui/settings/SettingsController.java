package org.daxplore.producer.gui.settings;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.Settings;
import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;
import org.daxplore.producer.daxplorelib.metadata.MetaQuestion.MetaQuestionManager;
import org.daxplore.producer.daxplorelib.resources.DaxploreProperties;
import org.daxplore.producer.gui.event.DaxploreFileUpdateEvent;
import org.daxplore.producer.gui.resources.GuiTexts;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class SettingsController implements ActionListener{

	private MetaQuestionManager metaQuestionManager;
	private GuiTexts texts;
	private SettingsView settingsView;
	private Settings settings;
	
	public SettingsController(MetaQuestionManager metaQuestionManager, EventBus eventBus, GuiTexts texts) {
		this.metaQuestionManager = metaQuestionManager;
		this.texts = texts;
		eventBus.register(this);
		settingsView = new SettingsView(texts, this);
	}
	
	public Component getView() {
		return settingsView;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if(source instanceof JCheckBox) {
			JCheckBox box = (JCheckBox)source;
			String key = e.getActionCommand();
			boolean checked = box.isSelected();
			settings.putSetting(key, checked);
		} else if(source instanceof JButton) {
			try {
				String title = texts.get("setting.confirm.title");
				switch(e.getActionCommand()) {
				case "checkFreq":
					String text = texts.get("setting.confirm.checkfreq");
					if(JOptionPane.showConfirmDialog(null, text, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						for(MetaQuestion mq : metaQuestionManager.getAll()) {
							mq.setUseFrequencies(true);
						}
					}
					break;
				case "uncheckFreq":
					text = texts.get("setting.confirm.uncheckfreq");
					if(JOptionPane.showConfirmDialog(null, text, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						for(MetaQuestion mq : metaQuestionManager.getAll()) {
							mq.setUseFrequencies(false);
						}
					}
					break;
				case "checkMean":
					text = texts.get("setting.confirm.checkmean");
					if(JOptionPane.showConfirmDialog(null, text, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						for(MetaQuestion mq : metaQuestionManager.getAll()) {
							mq.setUseMean(true);
						}
					}
					break;
				case "uncheckMean":
					text = texts.get("setting.confirm.uncheckmean");
					if(JOptionPane.showConfirmDialog(null, text, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						for(MetaQuestion mq : metaQuestionManager.getAll()) {
							mq.setUseMean(false);
						}
					}
					break;
					
				}
			} catch (DaxploreException de) {
				de.printStackTrace();
				//TODO show exception to user
			}
		}
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
