package gui.groups;

import gui.GuiMain;
import gui.widget.OurListWidget;
import gui.widget.QuestionWidget;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.tree.TreeCellRenderer;

import daxplorelib.DaxploreException;
import daxplorelib.metadata.MetaData;
import daxplorelib.metadata.MetaGroup;

@SuppressWarnings("serial")
public class GroupsPanelView extends JPanel {
	
	private GuiMain guiMain;
	private JScrollPane questionsScrollPane = new JScrollPane();
	private JScrollPane groupsScollPane = new JScrollPane();
	private JScrollPane perspectiveScrollPane = new JScrollPane();
	
	private class QuestionListCellRenderer implements ListCellRenderer<QuestionWidget> {

		@Override
		public Component getListCellRendererComponent(JList<? extends QuestionWidget> list, QuestionWidget value, int index, boolean isSelected, boolean cellHasFocus) {
			if(isSelected) {
				value.setBackground(new Color(255, 255, 200));
			} else {
				value.setBackground(new Color(255,255,255));
			}
			return value;
		}
	}
	
	private class GroupTreeCellRenderer implements TreeCellRenderer {

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	public GroupsPanelView(GuiMain guiMain) {
		this.guiMain = guiMain;
		setLayout(new GridLayout(0, 2, 0, 0));
		
		JPanel questionsPanel = new JPanel();
		add(questionsPanel);
		questionsPanel.setLayout(new BorderLayout(0, 0));
		
		questionsPanel.add(questionsScrollPane);
		
		JPanel groupsAndPerspectivesPanel = new JPanel();
		add(groupsAndPerspectivesPanel);
		groupsAndPerspectivesPanel.setLayout(new GridLayout(2, 1, 0, 0));
		
		JPanel groupsPanel = new JPanel();
		groupsAndPerspectivesPanel.add(groupsPanel);
		groupsPanel.setLayout(new BorderLayout(0,0));
		groupsPanel.add(groupsScollPane);
		
		JPanel groupsButtonPanel = new JPanel();
		groupsPanel.add(groupsButtonPanel, BorderLayout.SOUTH);
		
		JButton groupsAddNewButton = new JButton("+");
		groupsButtonPanel.add(groupsAddNewButton);
		
		JButton groupsUpButton = new JButton("Up");
		groupsButtonPanel.add(groupsUpButton);
		
		JButton groupsDownButton = new JButton("Down");
		groupsButtonPanel.add(groupsDownButton);
		
		JButton groupsRemoveButton = new JButton("X");
		groupsButtonPanel.add(groupsRemoveButton);
		
		JPanel addToGroupsPanel = new JPanel();
		groupsPanel.add(addToGroupsPanel, BorderLayout.WEST);
		addToGroupsPanel.setLayout(new BoxLayout(addToGroupsPanel, BoxLayout.X_AXIS));
		
		addToGroupsPanel.add(Box.createVerticalGlue());
		JButton addToGroupsButton = new JButton("->");
		addToGroupsPanel.add(addToGroupsButton);
		addToGroupsPanel.add(Box.createVerticalGlue());
		
		JPanel perspectivePanel = new JPanel();
		groupsAndPerspectivesPanel.add(perspectivePanel);
		perspectivePanel.setLayout(new BorderLayout(0, 0));
		perspectivePanel.add(perspectiveScrollPane);
		
		JPanel perspectivesButtonPanel = new JPanel();
		perspectivePanel.add(perspectivesButtonPanel, BorderLayout.SOUTH);
		
		JButton perspectivesUpButton = new JButton("Up");
		perspectivesButtonPanel.add(perspectivesUpButton);
		
		JButton perspectivesDownButton = new JButton("Down");
		perspectivesButtonPanel.add(perspectivesDownButton);
		
		JButton perspectivesRemoveButton = new JButton("X");
		perspectivesButtonPanel.add(perspectivesRemoveButton);
		
		JPanel addToPerspectivesPanel = new JPanel();
		perspectivePanel.add(addToPerspectivesPanel, BorderLayout.WEST);
		
		JButton addToPerspectivesButton = new JButton("->");
		addToPerspectivesPanel.setLayout(new BoxLayout(addToPerspectivesPanel, BoxLayout.X_AXIS));
		addToPerspectivesPanel.add(Box.createVerticalGlue());
		addToPerspectivesPanel.add(addToPerspectivesButton);
		addToPerspectivesPanel.add(Box.createVerticalGlue());
		
	}
	
	public void loadData() {
		if(guiMain.getGuiFile().isSet()) {
			try {
				MetaData md = guiMain.getGuiFile().getDaxploreFile().getMetaData();
				//get metaquestions
				QuestionListModel qlm = new QuestionListModel(md);
				JList<QuestionWidget> list = new JList<QuestionWidget>(qlm);
				list.setCellRenderer(new QuestionListCellRenderer());
				list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				questionsScrollPane.setViewportView(list);
				
				//get groups and perspectives
				List<MetaGroup> mgList = md.getAllGroups();
				GroupTreeModel gtm = new GroupTreeModel(md);
				JTree tree = new JTree(gtm);
				tree.setRootVisible(false);
				groupsScollPane.setViewportView(tree);
				
				
				
			} catch (DaxploreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
}
