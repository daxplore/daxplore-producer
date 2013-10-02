package org.daxplore.producer.gui.groups;

import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.daxplore.producer.gui.groups.GroupsController.GroupsCommand;

@SuppressWarnings("serial")
public class GroupsToolbar extends JPanel {
	JButton relodaData = new JButton("reload");
	
	public GroupsToolbar(ActionListener listener) {
		relodaData.setActionCommand(GroupsCommand.RELOAD_DATA.toString());
		relodaData.addActionListener(listener);
		add(relodaData);
	}
}
