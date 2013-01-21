package gui.groups;

import gui.GuiMain;
import gui.widget.OurListWidget;
import gui.widget.QuestionWidget;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionListener;

import daxplorelib.DaxploreException;
import daxplorelib.metadata.MetaQuestion;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.BoxLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.SwingConstants;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.Rectangle;

@SuppressWarnings("serial")
public class GroupsPanelView extends JPanel {
	
	private GuiMain guiMain;
	private List<QuestionWidget> questionList = new LinkedList<QuestionWidget>();
	private JScrollPane questionsScrollPane = new JScrollPane();
	private JScrollPane groupsScollPane = new JScrollPane();
	private JScrollPane perspectiveScrollPane = new JScrollPane();
	
	private class QuestionListModel implements ListModel<QuestionWidget> {
		@Override
		public int getSize() {
			return questionList.size();
		}

		@Override
		public QuestionWidget getElementAt(int index) {
			return questionList.get(index);
		}

		@Override
		public void addListDataListener(ListDataListener l) {
			// do nothing
		}
		@Override
		public void removeListDataListener(ListDataListener l) {
			// do nothing
		}
	}
	
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
	
	private class GroupListModel implements ListModel<OurListWidget> {

		@Override
		public int getSize() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public QuestionWidget getElementAt(int index) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void addListDataListener(ListDataListener l) {
			// TODO Auto-generated method stub
		}

		@Override
		public void removeListDataListener(ListDataListener l) {
			// TODO Auto-generated method stub
		}
	}
	
	private class GroupListCellRenderer implements ListCellRenderer<OurListWidget> {

		@Override
		public Component getListCellRendererComponent(JList<? extends OurListWidget> list, OurListWidget value, int index, boolean isSelected, boolean cellHasFocus) {
			if(isSelected) {
				value.setBackground(new Color(255, 255, 200));
			} else {
				value.setBackground(new Color(255,255,255));
			}
			return value;
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
				List<MetaQuestion> mqList = guiMain.getGuiFile().getDaxploreFile().getMetaData().getAllQuestions();
				int i = 0;
				for(MetaQuestion mq: mqList) {
					questionList.add(new QuestionWidget(mq));
					i++;
				}
				System.out.println("Added "+ i + " questions");
				JList<QuestionWidget> list = new JList<QuestionWidget>(new QuestionListModel());
				list.setCellRenderer(new QuestionListCellRenderer());
				list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				questionsScrollPane.setViewportView(list);
			} catch (DaxploreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
}
