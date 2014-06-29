/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui.menu;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Locale;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;

import org.daxplore.producer.gui.menu.ToolbarController.ToolbarCommand;
import org.daxplore.producer.gui.utility.DisplayLocale;

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
		
		toolbar.add(actionManager.NEW);
		toolbar.add(actionManager.OPEN);
		toolbar.add(actionManager.SAVE);
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
		if(locales.size()>0) {
			localePicker.removeAllItems();
			for(Locale locale : locales) {
				localePicker.addItem(new DisplayLocale(locale));
			}
			localePicker.setSelectedIndex(0);
		}
	}
	
	public DisplayLocale getSelectedLocale() {
		return (DisplayLocale)localePicker.getSelectedItem();
	}
}
