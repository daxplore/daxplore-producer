package gui.groups;

import gui.GuiFile;
import gui.GuiMain;
import gui.widget.GroupWidget;
import gui.widget.OurListWidget;
import gui.widget.QuestionWidget;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import daxplorelib.DaxploreException;
import daxplorelib.metadata.MetaData;
import daxplorelib.metadata.MetaGroup;
import daxplorelib.metadata.MetaGroup.GroupType;
import daxplorelib.metadata.MetaGroup.MetaGroupManager;
import daxplorelib.metadata.MetaQuestion;
import daxplorelib.metadata.TextReference;
import daxplorelib.metadata.TextReference.TextReferenceManager;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

@SuppressWarnings("serial")
public class GroupsPanelView extends JPanel {
	
	private GuiMain guiMain;
	private JScrollPane questionsScrollPane = new JScrollPane();
	private JScrollPane groupsScollPane = new JScrollPane();
	private JScrollPane perspectiveScrollPane = new JScrollPane();
	private GroupTreeModel groupTreeModel;
	private QuestionListModel questionListModel;
	private JList<QuestionWidget> questionJList;
	private JTree groupJTree;
	
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
			if(selected) {
				if(value instanceof QuestionWidget) {
					((QuestionWidget)value).setBackground(new Color(255, 255, 200));
				} else if (value instanceof GroupWidget) {
					((GroupWidget)value).setBackground(new Color(200, 200, 255));
				}
			} else {
				if(value instanceof QuestionWidget) {
					((QuestionWidget)value).setBackground(new Color(255, 255, 255));
				} else if (value instanceof GroupWidget) {
					((GroupWidget)value).setBackground(new Color(255, 255, 255));
				}
			}
			return (Component)value;
		}
		
	}
	
	public GroupsPanelView(final GuiMain guiMain) {
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
		groupsAddNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String groupName = (String)JOptionPane.showInputDialog(guiMain.getGuiMainFrame(), "Name:", "Create new group", JOptionPane.PLAIN_MESSAGE, null, null, "");
				if(groupName != null && !groupName.equals("")) {
					try {
						MetaGroupManager metaGroupManager = guiMain.getGuiFile().getDaxploreFile().getMetaData().getMetaGroupManager();
						int nextid = metaGroupManager.getHighestId() +1;
						TextReferenceManager textReferenceManager = guiMain.getGuiFile().getDaxploreFile().getMetaData().getTextsManager();
						TextReference tr = textReferenceManager.get("Group" + nextid);
						tr.put(groupName, new Locale("sv")); //TODO: fix global locale
						MetaGroup mg = metaGroupManager.create(tr, Integer.MAX_VALUE, GroupType.QUESTIONS, new LinkedList<MetaQuestion>());
						groupTreeModel.addGroup(mg, groupTreeModel.getChildCount(groupTreeModel.getRoot()));
					} catch (Exception e1) { //TODO: fix proper exception handling
						JOptionPane.showMessageDialog(guiMain.getGuiMainFrame(),
							    "Something went wrong while creating new group",
							    "Group creation error",
							    JOptionPane.ERROR_MESSAGE);
						e1.printStackTrace();
					}
				} else {
					JOptionPane.showMessageDialog(guiMain.getGuiMainFrame(),
						    "Group has to have a name",
						    "Group creation error",
						    JOptionPane.ERROR_MESSAGE);
				}
			}
		});
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
		addToGroupsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object[] path = groupJTree.getSelectionPath().getPath();
				GroupWidget parent;
				int atIndex = 0;
				if(path.length == 1) { 
					return; 
				} else if(path.length == 2) {
					parent = (GroupWidget)path[1];
					atIndex = parent.getQuestionCount();
				} else if(path.length == 3) {
					parent = (GroupWidget)path[1];
					atIndex = groupTreeModel.getIndexOfChild(parent, path[2]) + 1;
				} else {return;}
				
				for(QuestionWidget qw: questionJList.getSelectedValuesList()) {
					try {
						groupTreeModel.addQuestion(qw.metaQuestion, parent, atIndex);
						atIndex++;
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				return;
			}
		});
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
				questionListModel = new QuestionListModel(md);
				questionJList = new JList<QuestionWidget>(questionListModel);
				questionJList.setCellRenderer(new QuestionListCellRenderer());
				questionJList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				questionsScrollPane.setViewportView(questionJList);
				
				//get groups and perspectives
				groupTreeModel = new GroupTreeModel(md);
				groupJTree = new JTree(groupTreeModel);
				groupJTree.setRootVisible(false);
				groupJTree.setCellRenderer(new GroupTreeCellRenderer());
				//tree.getPr
				groupsScollPane.setViewportView(groupJTree);
				
			} catch (DaxploreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
}
