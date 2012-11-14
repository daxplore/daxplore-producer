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
import javax.swing.SwingConstants;

public class GroupsPanelView extends JPanel {
	
	private JLabel lblGroupsPanel = new JLabel();
	private JTable questionsTable;
	private JTable perspectiveTable;
	private JTable editTable;
	
	public GroupsPanelView(GuiMain guiMain) {
		
		JPanel questionsPanel = new JPanel();
		questionsPanel.setBounds(6, 43, 334, 692);
		questionsPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		
		JPanel editPanel = new JPanel();
		editPanel.setBounds(388, 43, 310, 284);
		editPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		
		JButton saveButton = new JButton("Save");
		saveButton.setBounds(710, 43, 90, 28);
		setLayout(null);
		add(saveButton);
		
		JButton editButton = new JButton("Edit");
		editButton.setBounds(710, 83, 90, 28);
		add(editButton);
		
		JButton languageButton = new JButton("Language");
		languageButton.setBounds(710, 123, 90, 28);
		add(languageButton);
		
		JButton addPerspectiveButton = new JButton("+");
		addPerspectiveButton.setBounds(604, 339, 46, 33);
		add(addPerspectiveButton);
		
		JButton removeButton = new JButton("-");
		removeButton.setBounds(651, 339, 46, 33);
		add(removeButton);
		
		JButton importQuestionButton = new JButton("->");
		importQuestionButton.setBounds(341, 149, 46, 50);
		add(importQuestionButton);
		
		JLabel editQuestionsLabel = new JLabel("Edit questions");
		editQuestionsLabel.setHorizontalAlignment(SwingConstants.LEFT);
		editQuestionsLabel.setBounds(503, 15, 96, 16);
		add(editQuestionsLabel);
		
		JLabel perspectiveLabel = new JLabel("Perspective");
		perspectiveLabel.setBounds(503, 346, 78, 16);
		add(perspectiveLabel);
		
		JLabel questionsLabel = new JLabel("Questions");
		questionsLabel.setHorizontalAlignment(SwingConstants.CENTER);
		questionsLabel.setBounds(138, 15, 72, 16);
		add(questionsLabel);
		add(questionsPanel);
		questionsPanel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane questionsScrollPane = new JScrollPane();
		questionsPanel.add(questionsScrollPane);
		
		questionsTable = new JTable();
		questionsScrollPane.setViewportView(questionsTable);
		add(editPanel);
		editPanel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane editScrollPane = new JScrollPane();
		
		editTable = new JTable();
		editScrollPane.setViewportView(editTable);
		editPanel.add(editScrollPane);
		
		JPanel perspectivePanel = new JPanel();
		perspectivePanel.setBounds(388, 376, 310, 359);
		perspectivePanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		add(perspectivePanel);
		perspectivePanel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane perspectiveScrollPane = new JScrollPane();
		perspectivePanel.add(perspectiveScrollPane);
		
		perspectiveTable = new JTable();
		perspectiveScrollPane.setViewportView(perspectiveTable);
	}
}
