package gui.groups;

import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import gui.GuiMain;
import gui.GuiFile;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import java.awt.BorderLayout;
import javax.swing.BoxLayout;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.border.BevelBorder;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JList;
import javax.swing.JButton;

public class GroupsPanelView extends JPanel {
	
	private JLabel lblGroupsPanel = new JLabel();
	private JTable table;
	private JTable table_1;
	private JTable table_2;
	
	public GroupsPanelView(GuiMain guiMain) {
		
		JPanel questionsPanel = new JPanel();
		questionsPanel.setBounds(6, 6, 326, 729);
		questionsPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		
		JPanel groupsPanel = new JPanel();
		groupsPanel.setBounds(344, 6, 354, 321);
		groupsPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		
		JPanel editPanel = new JPanel();
		editPanel.setBounds(344, 339, 354, 396);
		editPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		
		JButton btnNewButton = new JButton("Edit");
		btnNewButton.setBounds(725, 6, 49, 28);
		
		JLabel lblGroupFlow = new JLabel("No file loaded");
		editPanel.add(lblGroupFlow);
		
		JLabel lblEditField = new JLabel("No file loaded");
		groupsPanel.add(lblEditField);
		setLayout(null);
		add(btnNewButton);
		add(questionsPanel);
		
		JLabel lblNewLabel = new JLabel("No file loaded");
		questionsPanel.add(lblNewLabel);
		
		table = new JTable();
		questionsPanel.add(table);
		add(editPanel);
		
		table_2 = new JTable();
		editPanel.add(table_2);
		add(groupsPanel);
		
		table_1 = new JTable();
		groupsPanel.add(table_1);
	}
}
