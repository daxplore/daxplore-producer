package gui.groups;

import gui.MainController;
import gui.widget.GroupWidget;
import gui.widget.QuestionWidget;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.tree.TreePath;

import daxplorelib.DaxploreException;
import daxplorelib.metadata.MetaData;
import daxplorelib.metadata.MetaGroup;
import daxplorelib.metadata.MetaGroup.GroupType;
import daxplorelib.metadata.MetaGroup.MetaGroupManager;
import daxplorelib.metadata.MetaQuestion;
import daxplorelib.metadata.TextReference;
import daxplorelib.metadata.TextReference.TextReferenceManager;

public class GroupsController implements ActionListener {

	public static final String GROUPS_ADD_ACTION_COMMAND = "GroupsAddActionCommand";
	public static final String GROUPS_UP_ACTION_COMMAND = "GroupsUpActionCommand";
	public static final String GROUPS_DOWN_ACTION_COMMAND = "GroupsDownActionCommand";
	public static final String GROUPS_REMOVE_ACTION_COMMAND = "GroupsRemoveActionCommand";
	
	public static final String PERSPECTIVES_UP_ACTION_COMMAND = "PerspectivesUpActionCommand";
	public static final String PERSPECTIVES_DOWN_ACTION_COMMAND = "PerspectivesDownActionCommand";
	public static final String PERSPECTIVES_REMOVE_ACTION_COMMAND = "PerspectivesRemoveActionCommand";
	
	public static final String ADD_TO_GROUP_ACTION_COMMAND = "AddToGroupActionCommand";
	public static final String ADD_TO_PERSPECTIVES_ACTION_COMMAND = "AddToPerspectivesActionCommand";
	
	private MainController mainController;
	private GroupsView groupsView;
	
	private GroupTreeModel groupTreeModel;
	private GroupTree groupTree;
	private QuestionTable questionJTable;
	private QuestionTableModel questionTableModel;
	
	public GroupsController(GroupsView groupView, MainController mainController) {
		this.mainController = mainController;
		this.groupsView = groupView;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object[] path;
		switch(e.getActionCommand()) {
		case GROUPS_ADD_ACTION_COMMAND:
			String groupName = (String)JOptionPane.showInputDialog(mainController.getMainFrame(), "Name:", "Create new group", JOptionPane.PLAIN_MESSAGE, null, null, "");
			if(groupName != null && !groupName.equals("")) {
				try {
					MetaGroupManager metaGroupManager = mainController.getDaxploreFile().getMetaData().getMetaGroupManager();
					int nextid = metaGroupManager.getHighestId() +1;
					TextReferenceManager textReferenceManager = mainController.getDaxploreFile().getMetaData().getTextsManager();
					TextReference tr = textReferenceManager.get("Group" + nextid);
					tr.put(groupName, new Locale("sv")); //TODO: fix global locale
					MetaGroup mg = metaGroupManager.create(tr, Integer.MAX_VALUE, GroupType.QUESTIONS, new LinkedList<MetaQuestion>());
					TreePath treepath = groupTreeModel.addGroup(mg, groupTreeModel.getChildCount(groupTreeModel.getRoot()));
					groupTree.setSelectionPath(treepath);
				} catch (Exception e1) { //TODO: fix proper exception handling
					JOptionPane.showMessageDialog(mainController.getMainFrame(),
						    "Something went wrong while creating new group",
						    "Group creation error",
						    JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				}
			} else {
				JOptionPane.showMessageDialog(mainController.getMainFrame(),
					    "Group has to have a name",
					    "Group creation error",
					    JOptionPane.ERROR_MESSAGE);
			}
			break;
		case GROUPS_UP_ACTION_COMMAND:
			System.out.println("GROUPS_UP_ACTION_COMMAND");
			path = groupTree.getSelectionPath().getPath();
			try {
				if(path.length == 2) {
					int currentIndex = groupTreeModel.getIndexOfChild(path[0], path[1]);
					if(currentIndex > 0) {
						groupTreeModel.moveChild(path[1], path[0], currentIndex-1);
						groupTree.setSelectionPath(new TreePath(path));
					}
				} else if(path.length == 3) {
					int currentIndex = groupTreeModel.getIndexOfChild(path[1], path[2]);
					if(currentIndex > 0) {
						groupTreeModel.moveChild(path[2], path[1], currentIndex-1);
						groupTree.setSelectionPath(new TreePath(path));
					} else {
						int groupIndex = groupTreeModel.getIndexOfChild(path[0], path[1]);
						if(groupIndex > 0) {
							Object newGroup = groupTreeModel.getChild(path[0], groupIndex-1);
							groupTreeModel.moveChild(path[2], newGroup, groupTreeModel.getChildCount(newGroup));
							groupTree.setSelectionPath(new TreePath(new Object[]{path[0], newGroup, path[2]}));
						}
					}
				}
			} catch (Exception ex) { ex.printStackTrace(); }
			break;
		case GROUPS_DOWN_ACTION_COMMAND:
			System.out.println("GROUPS_DOWN_ACTION_COMMAND");
			path = groupTree.getSelectionPath().getPath();
			try {
				if(path.length == 2) {
					int currentIndex = groupTreeModel.getIndexOfChild(path[0], path[1]);
					int siblingCount = groupTreeModel.getChildCount(path[0]);
					if(currentIndex < siblingCount-1) {
						groupTreeModel.moveChild(path[1], path[0], currentIndex+1);
						groupTree.setSelectionPath(new TreePath(path));
					}
				} else if(path.length == 3) {
					System.out.println("move question down");
					int currentIndex = groupTreeModel.getIndexOfChild(path[1], path[2]);
					int siblingCount = groupTreeModel.getChildCount(path[1]);
					if(currentIndex < siblingCount-1){
						groupTreeModel.moveChild(path[2], path[1], currentIndex+1);
						groupTree.setSelectionPath(new TreePath(path));
					} else {
						int groupIndex = groupTreeModel.getIndexOfChild(path[0], path[1]);
						if(groupIndex < groupTreeModel.getChildCount(path[0])-1) {
							Object newGroup = groupTreeModel.getChild(path[0], groupIndex+1);
							groupTreeModel.moveChild(path[2], newGroup, 0);
							groupTree.setSelectionPath(new TreePath(new Object[]{path[0], newGroup, path[2]}));
						}
					}
				}
			} catch (Exception ex) { ex.printStackTrace(); }
			break;
		case GROUPS_REMOVE_ACTION_COMMAND:
			try {
				path = groupTree.getSelectionPath().getPath();
				Object parent = path[path.length-2];
				Object child = path[path.length-1];
				int index = groupTreeModel.getIndexOfChild(parent, child);
				groupTreeModel.removeChild(child); //the row that actually does work, rest just what the selection should be...
				int siblingCount = groupTreeModel.getChildCount(parent);
				List<Object> newPath = Arrays.asList(Arrays.copyOfRange(path, 0, path.length-2));
				if(siblingCount > 0 && siblingCount >= index) {
					newPath.add(groupTreeModel.getChild(parent, index));
				} else if(siblingCount > 0) {
					newPath.add(groupTreeModel.getChild(parent, index-1));
				}
				groupTree.setSelectionPath(new TreePath(newPath.toArray()));
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			break;
		case PERSPECTIVES_UP_ACTION_COMMAND:
			break;
		case PERSPECTIVES_DOWN_ACTION_COMMAND:
			break;
		case PERSPECTIVES_REMOVE_ACTION_COMMAND:
			break;
		case ADD_TO_GROUP_ACTION_COMMAND:
			path = groupTree.getSelectionPath().getPath();
			MetaGroup parent;
			int atIndex = 0;
			if(path.length == 1) { 
				return; 
			} else if(path.length == 2) {
				parent = (MetaGroup)path[1];
				atIndex = parent.getQuestionCount();
			} else if(path.length == 3) {
				parent = (MetaGroup)path[1];
				atIndex = groupTreeModel.getIndexOfChild(parent, path[2]) + 1;
			} else {return;}
			
			try {
				for(int i : questionJTable.getSelectedRows()) {
					MetaQuestion mq = (MetaQuestion)questionJTable.getValueAt(i, 0);
					TreePath treepath = groupTreeModel.addQuestion(mq, parent, atIndex);
					atIndex++;
					groupTree.setSelectionPath(treepath);
				}
			}catch (Exception e2) {
				// TODO: handle exception
			}
			break;
		case ADD_TO_PERSPECTIVES_ACTION_COMMAND:
			break;
		}
		
	}

	public void loadData() {
		if(mainController.fileIsSet()) {
			try {
				MetaData md = mainController.getDaxploreFile().getMetaData();
				
				questionTableModel = new QuestionTableModel(md);
				questionJTable = new QuestionTable(questionTableModel);
				groupsView.getQuestionsScrollPane().setViewportView(questionJTable);
				
				//get groups and perspectives
				groupTreeModel = new GroupTreeModel(md);
				groupTree = new GroupTree(groupTreeModel);
				groupsView.getGroupsScollPane().setViewportView(groupTree);
				
			} catch (DaxploreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
}
