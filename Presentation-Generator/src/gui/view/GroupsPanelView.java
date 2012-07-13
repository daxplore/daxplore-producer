package gui.view;

import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import gui.GUIMain;
import gui.GUIFile;

public class GroupsPanelView extends JPanel {
	
	private JLabel lblGroupsPanel = new JLabel();
	
	public GroupsPanelView(GUIMain guiMain, GUIFile guiFile) {
		
		setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JLabel lblGroupPanel = new JLabel("Group Panel");
		add(lblGroupPanel);
	}
}
