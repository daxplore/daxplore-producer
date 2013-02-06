package gui.groups;

import gui.MainController;
import gui.widget.GroupRenderer;
import gui.widget.QuestionWidget;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.tree.TreeCellRenderer;

@SuppressWarnings("serial")
public class GroupsView extends JPanel {
	
	private MainController mainController;

	private JScrollPane questionsScrollPane = new JScrollPane();
	private JScrollPane groupsScollPane = new JScrollPane();
	private JScrollPane perspectiveScrollPane = new JScrollPane();
	private GroupsController groupsController;
	
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
	
	public GroupsView(final MainController mainController) {
		this.mainController = mainController;
		groupsController = new GroupsController(this, this.mainController);
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
		groupsAddNewButton.setActionCommand(GroupsController.GROUPS_ADD_ACTION_COMMAND);
		groupsAddNewButton.addActionListener(groupsController);
		groupsButtonPanel.add(groupsAddNewButton);
		
		JButton groupsUpButton = new JButton("Up");
		groupsUpButton.setActionCommand(GroupsController.GROUPS_UP_ACTION_COMMAND);
		groupsUpButton.addActionListener(groupsController);
		groupsButtonPanel.add(groupsUpButton);
		
		JButton groupsDownButton = new JButton("Down");
		groupsDownButton.setActionCommand(GroupsController.GROUPS_DOWN_ACTION_COMMAND);
		groupsDownButton.addActionListener(groupsController);
		groupsButtonPanel.add(groupsDownButton);
		
		JButton groupsRemoveButton = new JButton("X");
		groupsRemoveButton.setActionCommand(GroupsController.GROUPS_REMOVE_ACTION_COMMAND);
		groupsRemoveButton.addActionListener(groupsController);
		groupsButtonPanel.add(groupsRemoveButton);
		
		JPanel addToGroupsPanel = new JPanel();
		groupsPanel.add(addToGroupsPanel, BorderLayout.WEST);
		addToGroupsPanel.setLayout(new BoxLayout(addToGroupsPanel, BoxLayout.X_AXIS));
		
		addToGroupsPanel.add(Box.createVerticalGlue());
		JButton addToGroupsButton = new JButton("->");
		addToGroupsButton.setActionCommand(GroupsController.ADD_TO_GROUP_ACTION_COMMAND);
		addToGroupsButton.addActionListener(groupsController);
		addToGroupsPanel.add(addToGroupsButton);
		addToGroupsPanel.add(Box.createVerticalGlue());
		
		JPanel perspectivePanel = new JPanel();
		groupsAndPerspectivesPanel.add(perspectivePanel);
		perspectivePanel.setLayout(new BorderLayout(0, 0));
		perspectivePanel.add(perspectiveScrollPane);
		
		JPanel perspectivesButtonPanel = new JPanel();
		perspectivePanel.add(perspectivesButtonPanel, BorderLayout.SOUTH);
		
		JButton perspectivesUpButton = new JButton("Up");
		perspectivesUpButton.setActionCommand(GroupsController.PERSPECTIVES_UP_ACTION_COMMAND);
		perspectivesUpButton.addActionListener(groupsController);
		perspectivesButtonPanel.add(perspectivesUpButton);
		
		JButton perspectivesDownButton = new JButton("Down");
		perspectivesDownButton.setActionCommand(GroupsController.PERSPECTIVES_DOWN_ACTION_COMMAND);
		perspectivesDownButton.addActionListener(groupsController);
		perspectivesButtonPanel.add(perspectivesDownButton);
		
		JButton perspectivesRemoveButton = new JButton("X");
		perspectivesRemoveButton.setActionCommand(GroupsController.PERSPECTIVES_REMOVE_ACTION_COMMAND);
		perspectivesRemoveButton.addActionListener(groupsController);
		perspectivesButtonPanel.add(perspectivesRemoveButton);
		
		JPanel addToPerspectivesPanel = new JPanel();
		perspectivePanel.add(addToPerspectivesPanel, BorderLayout.WEST);
		
		JButton addToPerspectivesButton = new JButton("->");
		addToPerspectivesButton.setActionCommand(GroupsController.ADD_TO_PERSPECTIVES_ACTION_COMMAND);
		addToPerspectivesButton.addActionListener(groupsController);
		addToPerspectivesPanel.setLayout(new BoxLayout(addToPerspectivesPanel, BoxLayout.X_AXIS));
		addToPerspectivesPanel.add(Box.createVerticalGlue());
		addToPerspectivesPanel.add(addToPerspectivesButton);
		addToPerspectivesPanel.add(Box.createVerticalGlue());
		
	}
	
	public GroupsController getController() {
		return groupsController;
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
