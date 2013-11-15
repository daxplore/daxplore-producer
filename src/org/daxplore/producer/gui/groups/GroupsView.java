package org.daxplore.producer.gui.groups;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import org.daxplore.producer.gui.SectionHeader;
import org.daxplore.producer.gui.groups.GroupsController.GroupsCommand;
import org.daxplore.producer.gui.resources.GuiTexts;

@SuppressWarnings("serial")
public class GroupsView extends JPanel {
	
	private JScrollPane questionsScrollPane = new JScrollPane();
	private JScrollPane groupsScollPane = new JScrollPane();
	private JScrollPane perspectiveScrollPane = new JScrollPane();
	
	public GroupsView(GuiTexts texts, ActionListener actionListener) {
		setLayout(new BorderLayout());
		
		JSplitPane mainSplitPanel = new JSplitPane();
		
		JPanel leftSection = new JPanel(new BorderLayout());
		leftSection.add(buildListSection(texts), BorderLayout.CENTER);
		leftSection.add(buildVariableInfoSection(texts), BorderLayout.SOUTH);
		mainSplitPanel.setLeftComponent(leftSection);
		
		JSplitPane rightSection = new JSplitPane();
		rightSection.setResizeWeight(0.6);
		rightSection.setOrientation(JSplitPane.VERTICAL_SPLIT);
		rightSection.setTopComponent(buildQuestionTreeSection(texts, actionListener));
		rightSection.setBottomComponent(buildPerspectiveSection(texts, actionListener));

		mainSplitPanel.setRightComponent(rightSection);
		
		add(mainSplitPanel);
	}
	
	private JPanel buildListSection(GuiTexts texts) {
		JPanel variableListPanel = new JPanel(new BorderLayout(0, 0));
		variableListPanel.add(new SectionHeader(texts, "variable_list"), BorderLayout.NORTH);
		variableListPanel.add(questionsScrollPane, BorderLayout.CENTER);
		return variableListPanel;
	}
	
	private JPanel buildVariableInfoSection(GuiTexts texts) {
		JPanel variableInfoPanel = new JPanel(new BorderLayout());
		variableInfoPanel.add(new SectionHeader(texts, "variable_info"), BorderLayout.NORTH);
		return variableInfoPanel;
	}
	
	private JPanel buildQuestionTreeSection(GuiTexts texts, ActionListener actionListener) {
		JPanel groupsPanel = new JPanel(new BorderLayout(0,0));
		
		groupsPanel.add(new SectionHeader(texts, "question_tree"), BorderLayout.NORTH);
		groupsPanel.add(groupsScollPane, BorderLayout.CENTER);
		
		JPanel groupsButtonPanel = new JPanel();
		groupsPanel.add(groupsButtonPanel, BorderLayout.SOUTH);
		
		JButton groupsAddNewButton = new JButton("+");
		groupsAddNewButton.setActionCommand(GroupsCommand.GROUP_ADD.toString());
		groupsAddNewButton.addActionListener(actionListener);
		groupsButtonPanel.add(groupsAddNewButton);
		
		JButton groupsUpButton = new JButton("Up");
		groupsUpButton.setActionCommand(GroupsCommand.GROUP_UP.toString());
		groupsUpButton.addActionListener(actionListener);
		groupsButtonPanel.add(groupsUpButton);
		
		JButton groupsDownButton = new JButton("Down");
		groupsDownButton.setActionCommand(GroupsCommand.GROUP_DOWN.toString());
		groupsDownButton.addActionListener(actionListener);
		groupsButtonPanel.add(groupsDownButton);
		
		JButton groupsRemoveButton = new JButton("X");
		groupsRemoveButton.setActionCommand(GroupsCommand.GROUP_REMOVE.toString());
		groupsRemoveButton.addActionListener(actionListener);
		groupsButtonPanel.add(groupsRemoveButton);
		
		JPanel addToGroupsPanel = new JPanel();
		groupsPanel.add(addToGroupsPanel, BorderLayout.WEST);
		addToGroupsPanel.setLayout(new BoxLayout(addToGroupsPanel, BoxLayout.X_AXIS));
		
		addToGroupsPanel.add(Box.createVerticalGlue());
		JButton addToGroupsButton = new JButton("->");
		addToGroupsButton.setActionCommand(GroupsCommand.GROUP_ADD_ITEM.toString());
		addToGroupsButton.addActionListener(actionListener);
		addToGroupsPanel.add(addToGroupsButton);
		addToGroupsPanel.add(Box.createVerticalGlue());
		
		return groupsPanel;
	}
	
	private JPanel buildPerspectiveSection(GuiTexts texts, ActionListener actionListener) {
		JPanel perspectivePanel = new JPanel();
		perspectivePanel.setLayout(new BorderLayout(0, 0));
		perspectivePanel.add(new SectionHeader(texts, "perspective_list"), BorderLayout.NORTH);
		perspectivePanel.add(perspectiveScrollPane, BorderLayout.CENTER);
		
		JPanel perspectivesButtonPanel = new JPanel();
		perspectivePanel.add(perspectivesButtonPanel, BorderLayout.SOUTH);
		
		JButton perspectivesUpButton = new JButton("Up");
		perspectivesUpButton.setActionCommand(GroupsCommand.PERSPECTIVE_UP.toString());
		perspectivesUpButton.addActionListener(actionListener);
		perspectivesButtonPanel.add(perspectivesUpButton);
		
		JButton perspectivesDownButton = new JButton("Down");
		perspectivesDownButton.setActionCommand(GroupsCommand.PERSPECTIVE_DOWN.toString());
		perspectivesDownButton.addActionListener(actionListener);
		perspectivesButtonPanel.add(perspectivesDownButton);
		
		JButton perspectivesRemoveButton = new JButton("X");
		perspectivesRemoveButton.setActionCommand(GroupsCommand.PERSPECTIVE_REMOVE.toString());
		perspectivesRemoveButton.addActionListener(actionListener);
		perspectivesButtonPanel.add(perspectivesRemoveButton);
		
		JPanel addToPerspectivesPanel = new JPanel();
		perspectivePanel.add(addToPerspectivesPanel, BorderLayout.WEST);
		
		JButton addToPerspectivesButton = new JButton("->");
		addToPerspectivesButton.setActionCommand(GroupsCommand.PERSPECTIVE_ADD_ITEM.toString());
		addToPerspectivesButton.addActionListener(actionListener);
		addToPerspectivesPanel.setLayout(new BoxLayout(addToPerspectivesPanel, BoxLayout.X_AXIS));
		addToPerspectivesPanel.add(Box.createVerticalGlue());
		addToPerspectivesPanel.add(addToPerspectivesButton);
		addToPerspectivesPanel.add(Box.createVerticalGlue());
		
		return perspectivePanel;
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
