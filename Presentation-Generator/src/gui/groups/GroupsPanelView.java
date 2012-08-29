package gui.groups;

import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import gui.GuiMain;
import gui.GuiFile;

public class GroupsPanelView extends JPanel {
	
	private JLabel lblGroupsPanel = new JLabel();
	
	public GroupsPanelView(GuiMain guiMain) {
		
		setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JLabel lblGroupPanel = new JLabel("Group Panel");
		add(lblGroupPanel);
	}
}
