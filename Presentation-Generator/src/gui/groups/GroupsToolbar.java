package gui.groups;

import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class GroupsToolbar extends JPanel {
	JButton relodaData = new JButton("reload");
	
	public GroupsToolbar(ActionListener listener) {
		relodaData.setActionCommand("reload");
		relodaData.addActionListener(listener);
		add(relodaData);
	}
}
