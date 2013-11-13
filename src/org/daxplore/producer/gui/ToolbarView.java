package org.daxplore.producer.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Locale;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;

import org.daxplore.producer.gui.ToolbarController.ToolbarCommand;

@SuppressWarnings("serial")
public class ToolbarView extends JPanel {

	private JToolBar toolbar;
	private JComboBox<DisplayLocale> localePicker;
	
	public ToolbarView(ActionListener listener, ActionManager actionManager) {
		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(5, 10, 5, 10));
		
		toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.setRollover(true);
		toolbar.setBorderPainted(false);
		
		toolbar.add(actionManager.BACK);
		toolbar.add(actionManager.NEW);
		toolbar.add(actionManager.OPEN);
		toolbar.add(actionManager.SAVE);
		toolbar.add(actionManager.SAVE_AS);
		toolbar.add(actionManager.EXPORT_UPLOAD);
		
		add(toolbar, BorderLayout.WEST);
		
		JPanel pickerPanel = new JPanel(new BorderLayout());
		pickerPanel.add(new JLabel("Text display language"), BorderLayout.NORTH);
		localePicker = new JComboBox<>();
		localePicker.setActionCommand(ToolbarCommand.SELECT_LOCALE.toString());
		localePicker.addActionListener(listener);
		pickerPanel.add(localePicker, BorderLayout.SOUTH);
		add(pickerPanel, BorderLayout.EAST);
	}
	
	public void setLocales(List<Locale> locales) {
		localePicker.removeAll();
		localePicker.addItem(new DisplayLocale("Technical"));
		for(Locale locale : locales) {
			localePicker.addItem(new DisplayLocale(locale));
		}
	}
	
	public DisplayLocale getSelectedLocale() {
		return (DisplayLocale)localePicker.getSelectedItem();
	}
}
