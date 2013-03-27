package gui.groups;

import gui.MainController;
import gui.Settings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.LinkedList;

import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

import daxplorelib.DaxploreException;
import daxplorelib.metadata.MetaData;
import daxplorelib.metadata.MetaGroup;
import daxplorelib.metadata.MetaGroup.GroupType;
import daxplorelib.metadata.MetaGroup.MetaGroupManager;
import daxplorelib.metadata.MetaQuestion;
import daxplorelib.metadata.textreference.TextReference;
import daxplorelib.metadata.textreference.TextReferenceManager;

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
	
	//debug thingy
	public static final String RELOADDATA = "reload";
	
	private MainController mainController;
	private GroupsView groupsView;
	private GroupsToolbar toolbar;
	
	private GroupTreeModel groupTreeModel;
	private GroupTree groupTree;
	private QuestionTable questionJTable;
	private QuestionTableModel questionTableModel;
	private PerspectivesTableModel perspectivesTableModel;
	private QuestionTable perspectivesTable;
	
	public GroupsController(GroupsView groupView, MainController mainController) {
		this.mainController = mainController;
		this.groupsView = groupView;
		this.toolbar = new GroupsToolbar(this);
	}
	
	public GroupsToolbar getToolbar() {
		return toolbar;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object[] path;
		int[] selectedRows;
		switch(e.getActionCommand()) {
		case GROUPS_ADD_ACTION_COMMAND:
			String groupName = (String)JOptionPane.showInputDialog(mainController.getMainFrame(), "Name:", "Create new group", JOptionPane.PLAIN_MESSAGE, null, null, "");
			if(groupName != null && !groupName.equals("")) {
				try {
					MetaGroupManager metaGroupManager = mainController.getDaxploreFile().getMetaData().getMetaGroupManager();
					int nextid = metaGroupManager.getHighestId() +1;
					TextReferenceManager textReferenceManager = mainController.getDaxploreFile().getMetaData().getTextsManager();
					TextReference tr = textReferenceManager.get("Group" + nextid);
					tr.put(groupName, Settings.getDefaultLocale());
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
						groupTree.setSelectionPath(null);
						groupTreeModel.moveChild(path[1], path[0], currentIndex-1);
						groupTree.setSelectionPath(new TreePath(path));
					}
				} else if(path.length == 3) {
					int currentIndex = groupTreeModel.getIndexOfChild(path[1], path[2]);
					if(currentIndex > 0) {
						groupTree.setSelectionPath(null);
						groupTreeModel.moveChild(path[2], path[1], currentIndex-1);
						groupTree.setSelectionPath(new TreePath(path));
					} else {
						int groupIndex = groupTreeModel.getIndexOfChild(path[0], path[1]);
						if(groupIndex > 0) {
							Object newGroup = groupTreeModel.getChild(path[0], groupIndex-1);
							groupTree.setSelectionPath(null);
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
						groupTree.setSelectionPath(null);
						groupTreeModel.moveChild(path[1], path[0], currentIndex+1);
						groupTree.setSelectionPath(new TreePath(path));
					}
				} else if(path.length == 3) {
					System.out.println("move question down");
					int currentIndex = groupTreeModel.getIndexOfChild(path[1], path[2]);
					int siblingCount = groupTreeModel.getChildCount(path[1]);
					if(currentIndex < siblingCount-1){
						groupTree.setSelectionPath(null);
						groupTreeModel.moveChild(path[2], path[1], currentIndex+1);
						groupTree.setSelectionPath(new TreePath(path));
					} else {
						int groupIndex = groupTreeModel.getIndexOfChild(path[0], path[1]);
						if(groupIndex < groupTreeModel.getChildCount(path[0])-1) {
							Object newGroup = groupTreeModel.getChild(path[0], groupIndex+1);
							groupTree.setSelectionPath(null);
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
				Object child = path[path.length-1];
				groupTree.setSelectionPath(null);
				groupTreeModel.removeChild(child); //the row that actually does work, rest just what the selection should be...
				/*
				Object parent = path[path.length-2];
				int index = groupTreeModel.getIndexOfChild(parent, child);
				int siblingCount = groupTreeModel.getChildCount(parent);
				List<Object> newPath = Arrays.asList(Arrays.copyOfRange(path, 0, path.length-2));
				if(siblingCount > 0 && siblingCount >= index) {
					newPath.add(groupTreeModel.getChild(parent, index));
				} else if(siblingCount > 0) {
					newPath.add(groupTreeModel.getChild(parent, index-1));
				}
				groupTree.setSelectionPath(new TreePath(newPath.toArray()));*/
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			break;
		case PERSPECTIVES_UP_ACTION_COMMAND:
			selectedRows = perspectivesTable.getSelectedRows();
			if(selectedRows.length < 1 || selectedRows[0] == 0) break;
			perspectivesTable.clearSelection();
			perspectivesTable.removeEditor();
			for(int i = 0; i < selectedRows.length; i++) {
				perspectivesTableModel.moveRow(selectedRows[i], selectedRows[i], selectedRows[i]-1);
				perspectivesTable.getSelectionModel().addSelectionInterval(selectedRows[i]-1, selectedRows[i]-1);
			}
			break;
		case PERSPECTIVES_DOWN_ACTION_COMMAND:
			selectedRows = perspectivesTable.getSelectedRows();
			if(selectedRows.length < 1 || selectedRows[selectedRows.length-1] == perspectivesTableModel.getRowCount() -1) break;
			perspectivesTable.clearSelection();
			perspectivesTable.removeEditor();
			for(int i = selectedRows.length-1; i >= 0; i--) {
				//perspectivesTable.changeSelection(selectedRows[i], 1, true, true);
				perspectivesTableModel.moveRow(selectedRows[i], selectedRows[i], selectedRows[i]+1);
				perspectivesTable.getSelectionModel().addSelectionInterval(selectedRows[i]+1, selectedRows[i]+1);
			}
			break;
		case PERSPECTIVES_REMOVE_ACTION_COMMAND:
			selectedRows = perspectivesTable.getSelectedRows();
			int delta = 0;
			for(int row: selectedRows) {
				perspectivesTableModel.removeRow(row - delta);
				delta++;
			}
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
			int index = perspectivesTable.getSelectedRow() != -1? perspectivesTable.getSelectedRow(): perspectivesTable.getRowCount();
			for(int i : questionJTable.getSelectedRows()) {
				MetaQuestion mq = (MetaQuestion)questionJTable.getValueAt(i, 0);
				perspectivesTableModel.insertRow(index, new Object[]{mq});
				index++;
			}
			break;
		case RELOADDATA:
			loadData();
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
				
				MetaGroup perspectives = null;
				for(MetaGroup mg : md.getMetaGroupManager().getAll()) {
					if(mg.getType() == GroupType.PERSPECTIVE) {
						perspectives = mg;
						break;
					}
				}
				if(perspectives == null) {
					System.out.println("Create perspectives group");
					TextReference textref = md.getTextsManager().get("PERSPECTIVESGROUP");
					perspectives = md.getMetaGroupManager().create(textref, 999, GroupType.PERSPECTIVE, new LinkedList<MetaQuestion>());
				}
				
				perspectivesTableModel = new PerspectivesTableModel(perspectives);
				perspectivesTable = new QuestionTable(perspectivesTableModel);
				groupsView.getPerspectiveScrollPane().setViewportView(perspectivesTable);
				
				
			} catch (DaxploreException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
}
