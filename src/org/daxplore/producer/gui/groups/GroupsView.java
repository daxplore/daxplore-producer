package org.daxplore.producer.gui.groups;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;

import org.daxplore.producer.gui.SectionHeader;
import org.daxplore.producer.gui.groups.GroupsController.GroupsCommand;
import org.daxplore.producer.gui.resources.GuiTexts;

import com.google.common.base.Strings;

@SuppressWarnings("serial")
public class GroupsView extends JPanel {
	
	private GuiTexts texts;
	private ActionListener actionListener;
	
	private JScrollPane questionsScrollPane = new JScrollPane();
	private JScrollPane groupsScollPane = new JScrollPane();
	private JScrollPane perspectiveScrollPane = new JScrollPane();
	
	private JLabel infoIdText = new JLabel();
	private JLabel infoShortText = new JLabel();
	private JLabel infoFullText = new JLabel();
	
	public GroupsView(GuiTexts texts, ActionListener actionListener) {
		this.texts = texts;
		this.actionListener = actionListener;
		
		setLayout(new BorderLayout());
		
		JSplitPane mainSplitPanel = new JSplitPane();
		
		JPanel leftSection = new JPanel(new BorderLayout());
		leftSection.add(buildListSection(), BorderLayout.CENTER);
		leftSection.add(buildVariableInfoSection(), BorderLayout.SOUTH);
		mainSplitPanel.setLeftComponent(leftSection);
		
		JSplitPane rightSection = new JSplitPane();
		rightSection.setResizeWeight(0.6);
		rightSection.setOrientation(JSplitPane.VERTICAL_SPLIT);
		rightSection.setTopComponent(buildQuestionTreeSection());
		rightSection.setBottomComponent(buildPerspectiveSection());

		mainSplitPanel.setRightComponent(rightSection);
		
		add(mainSplitPanel);
	}
	
	void setVariableInfo(String id, String shorttext, String fulltext) {
		infoIdText.setText(texts.format("variable_info.label.id", id));
		
		String shortDisplay = shorttext;
		if(Strings.isNullOrEmpty(shortDisplay)) {
			shortDisplay = "<i>missing</i>";
		}
		infoShortText.setText(texts.format("variable_info.label.shorttext", shortDisplay));
		
		String fullDisplay = fulltext;
		if(Strings.isNullOrEmpty(fullDisplay)) {
			fullDisplay = "<i>missing</i>";
		} 
		infoFullText.setText(texts.format("variable_info.label.fulltext", fullDisplay));
	}
	
	private JPanel buildListSection() {
		JPanel variableListPanel = new JPanel(new BorderLayout(0, 0));
		variableListPanel.add(new SectionHeader(texts, "variable_list"), BorderLayout.NORTH);
		variableListPanel.add(questionsScrollPane, BorderLayout.CENTER);
		return variableListPanel;
	}
	
	private JPanel buildVariableInfoSection() {
		final JPanel infoPanel = new VerticallyGrowingJPanel(new BorderLayout());
		infoPanel.add(new SectionHeader(texts, "variable_info"), BorderLayout.NORTH);
		
		JPanel textsPanel = new JPanel();
		BoxLayout layout = new BoxLayout(textsPanel, BoxLayout.Y_AXIS);
		textsPanel.setLayout(layout);
		textsPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
		
		textsPanel.add(infoIdText);
		textsPanel.add(infoShortText);
		textsPanel.add(infoFullText);
		
		infoPanel.add(textsPanel, BorderLayout.CENTER);
		
		JPanel bottomButtonPanel = new JPanel(new BorderLayout());
		JButton editVariableButton = new JButton(texts.get("variable_info.button.edit"));
		editVariableButton.setActionCommand(GroupsCommand.EDIT_VARIABLE.toString());
		editVariableButton.addActionListener(actionListener);
		bottomButtonPanel.add(editVariableButton, BorderLayout.EAST);
		
		infoPanel.add(bottomButtonPanel, BorderLayout.SOUTH);
		
		return infoPanel;
	}
	
	private JPanel buildQuestionTreeSection() {
		JPanel groupsPanel = new JPanel(new BorderLayout(0,0));
		
		JPanel listPanel = new JPanel(new BorderLayout());
		listPanel.add(new SectionHeader(texts, "question_tree"), BorderLayout.NORTH);
		listPanel.add(groupsScollPane, BorderLayout.CENTER);
		groupsPanel.add(listPanel, BorderLayout.CENTER);
		
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
	
	private JPanel buildPerspectiveSection() {
		JPanel perspectivePanel = new JPanel();
		perspectivePanel.setLayout(new BorderLayout());
		
		JPanel listPanel = new JPanel();
		listPanel.add(new SectionHeader(texts, "perspective_list"), BorderLayout.NORTH);
		listPanel.add(perspectiveScrollPane, BorderLayout.CENTER);
		perspectivePanel.add(listPanel, BorderLayout.CENTER);
		
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
	
	void setVariableList(Component component) {
		questionsScrollPane.setViewportView(component);
	}

	void setQuestionTree(Component component) {
		groupsScollPane.setViewportView(component);
	}

	void setPerspectiveList(Component component) {
		perspectiveScrollPane.setViewportView(component);
	}
	
}
