/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui.view.variable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;
import org.daxplore.producer.gui.resources.GuiTexts;
import org.daxplore.producer.gui.view.variable.VariableController.QuestionCommand;
import org.daxplore.producer.gui.widget.TextWidget;

import com.google.common.eventbus.EventBus;

@SuppressWarnings("serial")
public class VariableView extends JPanel {
	
	private JScrollPane variableScrollPane = new JScrollPane();
	private JScrollPane rawScrollPane = new JScrollPane();
	private JScrollPane timePointScrollPane = new JScrollPane();
	
	private JLabel column = new JLabel();
	private JPanel fullTextRefHolder;
	private JPanel shortTextRefHolder;
	private TextWidget fullText;
	private TextWidget shortText;

	public VariableView(EventBus eventBus, GuiTexts texts, ActionListener actionListener, 
			MetaQuestion metaQuestion, JTable rawTable, JTable variableTable, JTable timePointTable) {
		
		rawScrollPane.setViewportView(rawTable);
		variableScrollPane.setViewportView(variableTable);
		timePointScrollPane.setViewportView(timePointTable);
		
		fullText = new TextWidget(eventBus, texts);
		shortText = new TextWidget(eventBus, texts);
		
		column.setText(metaQuestion.getColumn());
		fullText.setContent(metaQuestion.getFullTextRef());
		shortText.setContent(metaQuestion.getShortTextRef());
		
		setLayout(new BorderLayout(0, 0));
		
		JPanel topPanel = new JPanel(new BorderLayout());
		
		shortTextRefHolder = new JPanel();
		shortTextRefHolder.setBorder(new TitledBorder(new LineBorder(new Color(0,0,75)), "Short text"));
		shortTextRefHolder.add(shortText);
		
		fullTextRefHolder = new JPanel();
		fullTextRefHolder.setBorder(new TitledBorder(new LineBorder(new Color(0,0,75)), "Full text"));
		fullTextRefHolder.add(fullText);
		
		topPanel.add(shortTextRefHolder, BorderLayout.WEST);
		topPanel.add(fullTextRefHolder, BorderLayout.EAST);
		
		add(topPanel, BorderLayout.NORTH);
		
		JPanel scalePanel = new JPanel(new GridLayout(1, 2));
		add(scalePanel, BorderLayout.CENTER);
		scalePanel.add(rawScrollPane, 0);
		
		JPanel rightPanel = new JPanel();
		scalePanel.add(rightPanel, 1);
		rightPanel.setLayout(new BorderLayout(0, 0));
		
		rightPanel.add(variableScrollPane, BorderLayout.CENTER);
		
		JPanel avcionButtonPanel = new JPanel();
		rightPanel.add(avcionButtonPanel, BorderLayout.SOUTH);
		
		JButton addButton = new JButton("Add");
		addButton.setActionCommand(QuestionCommand.ADD.toString());
		addButton.addActionListener(actionListener);
		avcionButtonPanel.add(addButton);
		
		JButton upButton = new JButton("Up");
		upButton.setActionCommand(QuestionCommand.UP.toString());
		upButton.addActionListener(actionListener);
		avcionButtonPanel.add(upButton);
		
		JButton downButton = new JButton("Down");
		downButton.setActionCommand(QuestionCommand.DOWN.toString());
		downButton.addActionListener(actionListener);
		avcionButtonPanel.add(downButton);
		
		JButton removeButton = new JButton("Remove");
		removeButton.setActionCommand(QuestionCommand.REMOVE.toString());
		removeButton.addActionListener(actionListener);
		avcionButtonPanel.add(removeButton);
		
		JButton invertButton = new JButton("Invert");
		invertButton.setActionCommand(QuestionCommand.INVERT.toString());
		invertButton.addActionListener(actionListener);
		avcionButtonPanel.add(invertButton);
		
		add(scalePanel, BorderLayout.CENTER);
		
		JPanel timepointPanel = new JPanel(new BorderLayout());
		timePointScrollPane.setPreferredSize(new Dimension(100, 150)); //TODO: fix size issues, to big if left to it's own devices
		timepointPanel.add(timePointScrollPane, BorderLayout.CENTER);
		add(timepointPanel, BorderLayout.SOUTH);
	}
	
}
