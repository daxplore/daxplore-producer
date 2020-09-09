/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui.view.build;

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
import org.daxplore.producer.gui.resources.Colors;
import org.daxplore.producer.gui.resources.UITexts;
import org.daxplore.producer.gui.utility.VerticallyGrowingJPanel;
import org.daxplore.producer.gui.view.build.GroupsController.GroupsCommand;

import com.google.common.base.Strings;

@SuppressWarnings("serial")
public class GroupsView extends JPanel {
	private ActionListener actionListener;
	
	private JScrollPane questionsScrollPane = new JScrollPane();
	private JScrollPane groupsScollPane = new JScrollPane();
	private JScrollPane perspectiveScrollPane = new JScrollPane();
	
	private JLabel infoIdText = new JLabel();
	private JLabel infoShortText = new JLabel();
	private JLabel infoFullText = new JLabel();
	
	public GroupsView(ActionListener actionListener) {
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
	
	void setVariableInfo(String column, String shorttext, String fulltext) {
		infoIdText.setText(UITexts.format("variable_info.label.column", column));
		
		String shortDisplay = shorttext;
		if(Strings.isNullOrEmpty(shortDisplay)) {
			shortDisplay = "<i>missing</i>";
		}
		infoShortText.setText(UITexts.format("variable_info.label.shorttext", shortDisplay));
		
		String fullDisplay = fulltext;
		if(Strings.isNullOrEmpty(fullDisplay)) {
			fullDisplay = "<i>missing</i>";
		} 
		infoFullText.setText(UITexts.format("variable_info.label.fulltext", fullDisplay));
	}
	
	private JPanel buildListSection() {
		JPanel variableListPanel = new JPanel(new BorderLayout(0, 0));
		questionsScrollPane.getViewport().setBackground(Colors.listBackgroundEven);
		variableListPanel.add(new SectionHeader("variable_list"), BorderLayout.NORTH);
		variableListPanel.add(questionsScrollPane, BorderLayout.CENTER);
		return variableListPanel;
	}
	
	private JPanel buildVariableInfoSection() {
		final JPanel infoPanel = new VerticallyGrowingJPanel(new BorderLayout());
		infoPanel.add(new SectionHeader("variable_info"), BorderLayout.NORTH);
		
		JPanel textsPanel = new JPanel();
		BoxLayout layout = new BoxLayout(textsPanel, BoxLayout.Y_AXIS);
		textsPanel.setLayout(layout);
		textsPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
		
		textsPanel.add(infoIdText);
		textsPanel.add(infoShortText);
		textsPanel.add(infoFullText);
		
		infoPanel.add(textsPanel, BorderLayout.CENTER);
		
		JPanel bottomButtonPanel = new JPanel(new BorderLayout());
		JButton editVariableButton = new JButton(UITexts.get("variable_info.button.edit"));
		editVariableButton.setActionCommand(GroupsCommand.EDIT_VARIABLE.toString());
		editVariableButton.addActionListener(actionListener);
		bottomButtonPanel.add(editVariableButton, BorderLayout.EAST);
		
		infoPanel.add(bottomButtonPanel, BorderLayout.SOUTH);
		
		return infoPanel;
	}
	
	private JPanel buildQuestionTreeSection() {
		JPanel groupsPanel = new JPanel(new BorderLayout(0,0));
		
		JPanel listPanel = new JPanel(new BorderLayout());
		listPanel.add(new SectionHeader("question_tree"), BorderLayout.NORTH);
		listPanel.add(groupsScollPane, BorderLayout.CENTER);
		groupsPanel.add(listPanel, BorderLayout.CENTER);
		
		JPanel groupsButtonsContainer = new JPanel(new BorderLayout());
		groupsPanel.add(groupsButtonsContainer, BorderLayout.SOUTH);
		
		JPanel groupsButtonPanel = new JPanel();
		groupsButtonsContainer.add(groupsButtonPanel, BorderLayout.CENTER);
		
		JButton groupsAddNewButton = new JButton("Add group");
		groupsAddNewButton.setActionCommand(GroupsCommand.GROUP_ADD.toString());
		groupsAddNewButton.addActionListener(actionListener);
		groupsButtonPanel.add(groupsAddNewButton);
		
		JButton groupsUpButton = new JButton("Move up");
		groupsUpButton.setActionCommand(GroupsCommand.GROUP_UP.toString());
		groupsUpButton.addActionListener(actionListener);
		groupsButtonPanel.add(groupsUpButton);
		
		JButton groupsDownButton = new JButton("Move down");
		groupsDownButton.setActionCommand(GroupsCommand.GROUP_DOWN.toString());
		groupsDownButton.addActionListener(actionListener);
		groupsButtonPanel.add(groupsDownButton);
		
		JButton groupsRemoveButton = new JButton("Remove");
		groupsRemoveButton.setActionCommand(GroupsCommand.GROUP_REMOVE.toString());
		groupsRemoveButton.addActionListener(actionListener);
		groupsButtonPanel.add(groupsRemoveButton);
		
		JPanel editButtonPanel = new JPanel();
		JButton editButton = new JButton("Edit");
		editButton.setActionCommand(GroupsCommand.EDIT_TREE.toString());
		editButton.addActionListener(actionListener);
		editButtonPanel.add(editButton);
		groupsButtonsContainer.add(editButtonPanel, BorderLayout.EAST);
		
		JPanel addToGroupsPanel = new JPanel();
		groupsPanel.add(addToGroupsPanel, BorderLayout.WEST);
		addToGroupsPanel.setLayout(new BoxLayout(addToGroupsPanel, BoxLayout.X_AXIS));
		
		addToGroupsPanel.add(Box.createVerticalGlue());
		JButton addToGroupsButton = new JButton("Add ->");
		addToGroupsButton.setActionCommand(GroupsCommand.GROUP_ADD_ITEM.toString());
		addToGroupsButton.addActionListener(actionListener);
		addToGroupsPanel.add(addToGroupsButton);
		addToGroupsPanel.add(Box.createVerticalGlue());
		
		return groupsPanel;
	}
	
	private JPanel buildPerspectiveSection() {
		JPanel perspectivePanel = new JPanel(new BorderLayout());
		perspectiveScrollPane.getViewport().setBackground(Colors.listBackgroundEven);
		
		JPanel listPanel = new JPanel(new BorderLayout());
		listPanel.add(new SectionHeader("perspective_list"), BorderLayout.NORTH);
		listPanel.add(perspectiveScrollPane, BorderLayout.CENTER);
		perspectivePanel.add(listPanel, BorderLayout.CENTER);
		
		JPanel perspectivesButtonPanel = new JPanel();
		perspectivePanel.add(perspectivesButtonPanel, BorderLayout.SOUTH);
		
		JButton perspectivesUpButton = new JButton("Move up");
		perspectivesUpButton.setActionCommand(GroupsCommand.PERSPECTIVE_UP.toString());
		perspectivesUpButton.addActionListener(actionListener);
		perspectivesButtonPanel.add(perspectivesUpButton);
		
		JButton perspectivesDownButton = new JButton("Move down");
		perspectivesDownButton.setActionCommand(GroupsCommand.PERSPECTIVE_DOWN.toString());
		perspectivesDownButton.addActionListener(actionListener);
		perspectivesButtonPanel.add(perspectivesDownButton);
		
		JButton perspectivesRemoveButton = new JButton("Remove");
		perspectivesRemoveButton.setActionCommand(GroupsCommand.PERSPECTIVE_REMOVE.toString());
		perspectivesRemoveButton.addActionListener(actionListener);
		perspectivesButtonPanel.add(perspectivesRemoveButton);
		
		JButton perspectivesSecondaryToggle = new JButton("Toggle secondary perspective");
		perspectivesSecondaryToggle.setActionCommand(GroupsCommand.PERSPECTIVE_SECONDARY_TOGGLE.toString());
		perspectivesSecondaryToggle.addActionListener(actionListener);
		perspectivesButtonPanel.add(perspectivesSecondaryToggle);
		
		JPanel addToPerspectivesPanel = new JPanel();
		perspectivePanel.add(addToPerspectivesPanel, BorderLayout.WEST);
		
		JButton addToPerspectivesButton = new JButton("Add ->");
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
