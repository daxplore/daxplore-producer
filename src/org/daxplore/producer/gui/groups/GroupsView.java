package org.daxplore.producer.gui.groups;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import org.daxplore.producer.gui.widget.GroupRenderer;
import org.daxplore.producer.gui.widget.QuestionWidget;

@SuppressWarnings("serial")
public class GroupsView extends JPanel {
	
	private JScrollPane questionsScrollPane = new JScrollPane();
	private JScrollPane groupsScollPane = new JScrollPane();
	private JScrollPane perspectiveScrollPane = new JScrollPane();
	
	class QuestionListCellRenderer implements ListCellRenderer<QuestionWidget> {
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
	
	class GroupTreeCellRenderer implements TreeCellRenderer {
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			if(selected) {
				if(value instanceof QuestionWidget) {
					((QuestionWidget)value).setBackground(new Color(255, 255, 200));
				} else if (value instanceof GroupRenderer) {
					((GroupRenderer)value).setBackground(new Color(200, 200, 255));
				}
			} else {
				if(value instanceof QuestionWidget) {
					((QuestionWidget)value).setBackground(new Color(255, 255, 255));
				} else if (value instanceof GroupRenderer) {
					((GroupRenderer)value).setBackground(new Color(255, 255, 255));
				}
			}
			return (Component)value;
		}
	}
	
	public GroupsView(ActionListener actionListener) {
		setLayout(new GridLayout(0, 2, 0, 0));
		
		JPanel questionsPanel = new JPanel();
		add(questionsPanel);
		questionsPanel.setLayout(new BorderLayout(0, 0));
		
		questionsPanel.add(questionsScrollPane);
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.6);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		add(splitPane);
		
		JPanel groupsPanel = new JPanel();
		splitPane.setLeftComponent(groupsPanel);
		groupsPanel.setLayout(new BorderLayout(0,0));
		groupsPanel.add(groupsScollPane);
		
		JPanel groupsButtonPanel = new JPanel();
		groupsPanel.add(groupsButtonPanel, BorderLayout.SOUTH);
		
		JButton groupsAddNewButton = new JButton("+");
		groupsAddNewButton.setActionCommand(GroupsController.GROUPS_ADD_ACTION_COMMAND);
		groupsAddNewButton.addActionListener(actionListener);
		groupsButtonPanel.add(groupsAddNewButton);
		
		JButton groupsUpButton = new JButton("Up");
		groupsUpButton.setActionCommand(GroupsController.GROUPS_UP_ACTION_COMMAND);
		groupsUpButton.addActionListener(actionListener);
		groupsButtonPanel.add(groupsUpButton);
		
		JButton groupsDownButton = new JButton("Down");
		groupsDownButton.setActionCommand(GroupsController.GROUPS_DOWN_ACTION_COMMAND);
		groupsDownButton.addActionListener(actionListener);
		groupsButtonPanel.add(groupsDownButton);
		
		JButton groupsRemoveButton = new JButton("X");
		groupsRemoveButton.setActionCommand(GroupsController.GROUPS_REMOVE_ACTION_COMMAND);
		groupsRemoveButton.addActionListener(actionListener);
		groupsButtonPanel.add(groupsRemoveButton);
		
		JPanel addToGroupsPanel = new JPanel();
		groupsPanel.add(addToGroupsPanel, BorderLayout.WEST);
		addToGroupsPanel.setLayout(new BoxLayout(addToGroupsPanel, BoxLayout.X_AXIS));
		
		addToGroupsPanel.add(Box.createVerticalGlue());
		JButton addToGroupsButton = new JButton("->");
		addToGroupsButton.setActionCommand(GroupsController.ADD_TO_GROUP_ACTION_COMMAND);
		addToGroupsButton.addActionListener(actionListener);
		addToGroupsPanel.add(addToGroupsButton);
		addToGroupsPanel.add(Box.createVerticalGlue());
		
		JPanel perspectivePanel = new JPanel();
		splitPane.setRightComponent(perspectivePanel);
		perspectivePanel.setLayout(new BorderLayout(0, 0));
		perspectivePanel.add(perspectiveScrollPane);
		
		JPanel perspectivesButtonPanel = new JPanel();
		perspectivePanel.add(perspectivesButtonPanel, BorderLayout.SOUTH);
		
		JButton perspectivesUpButton = new JButton("Up");
		perspectivesUpButton.setActionCommand(GroupsController.PERSPECTIVES_UP_ACTION_COMMAND);
		perspectivesUpButton.addActionListener(actionListener);
		perspectivesButtonPanel.add(perspectivesUpButton);
		
		JButton perspectivesDownButton = new JButton("Down");
		perspectivesDownButton.setActionCommand(GroupsController.PERSPECTIVES_DOWN_ACTION_COMMAND);
		perspectivesDownButton.addActionListener(actionListener);
		perspectivesButtonPanel.add(perspectivesDownButton);
		
		JButton perspectivesRemoveButton = new JButton("X");
		perspectivesRemoveButton.setActionCommand(GroupsController.PERSPECTIVES_REMOVE_ACTION_COMMAND);
		perspectivesRemoveButton.addActionListener(actionListener);
		perspectivesButtonPanel.add(perspectivesRemoveButton);
		
		JPanel addToPerspectivesPanel = new JPanel();
		perspectivePanel.add(addToPerspectivesPanel, BorderLayout.WEST);
		
		JButton addToPerspectivesButton = new JButton("->");
		addToPerspectivesButton.setActionCommand(GroupsController.ADD_TO_PERSPECTIVES_ACTION_COMMAND);
		addToPerspectivesButton.addActionListener(actionListener);
		addToPerspectivesPanel.setLayout(new BoxLayout(addToPerspectivesPanel, BoxLayout.X_AXIS));
		addToPerspectivesPanel.add(Box.createVerticalGlue());
		addToPerspectivesPanel.add(addToPerspectivesButton);
		addToPerspectivesPanel.add(Box.createVerticalGlue());
		splitPane.setDividerLocation(0.6);
	}
	
	JScrollPane getQuestionsScrollPane() {
		return questionsScrollPane;
	}

	JScrollPane getGroupsScollPane() {
		return groupsScollPane;
	}

	JScrollPane getPerspectiveScrollPane() {
		return perspectiveScrollPane;
	}
	
}
