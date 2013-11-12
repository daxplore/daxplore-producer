package org.daxplore.producer.gui.groups;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.LinkedList;

import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.DaxploreFile;
import org.daxplore.producer.daxplorelib.metadata.MetaGroup;
import org.daxplore.producer.daxplorelib.metadata.MetaGroup.GroupType;
import org.daxplore.producer.daxplorelib.metadata.MetaGroup.MetaGroupManager;
import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReferenceManager;
import org.daxplore.producer.gui.Settings;
import org.daxplore.producer.gui.event.DaxploreFileUpdateEvent;
import org.daxplore.producer.gui.event.EmptyEvents.RawImportEvent;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class GroupsController implements ActionListener {

	enum GroupsCommand {
		GROUP_ADD, GROUP_UP, GROUP_DOWN, GROUP_REMOVE, GROUP_ADD_ITEM,
		PERSPECTIVE_UP, PERSPECTIVE_DOWN, PERSPECTIVE_REMOVE, PERSPECTIVE_ADD_ITEM,
		RELOAD_DATA //TODO remove debug thingy
	}
	
	private EventBus eventBus;
	private Component parentComponent;
	
	private DaxploreFile daxploreFile;
	
	private GroupsView groupsView;
	private GroupsToolbar toolbar;
	
	private GroupTreeModel groupTreeModel;
	private GroupTree groupTree;
	private QuestionTable questionJTable;
	private QuestionTableModel questionTableModel;
	private PerspectivesTableModel perspectivesTableModel;
	private QuestionTable perspectivesTable;
	
	public GroupsController(EventBus eventBus, Component parentComponent) {
		this.eventBus = eventBus;
		this.parentComponent = parentComponent;
		
		eventBus.register(this);
		
		groupsView = new GroupsView(this);
		toolbar = new GroupsToolbar(this);
	}
	
	public GroupsToolbar getToolbar() {
		return toolbar;
	}
	
	@Subscribe
	public void on(DaxploreFileUpdateEvent e) {
		this.daxploreFile = e.getDaxploreFile();
		loadData();
	}
	
	@Subscribe
	public void on(RawImportEvent e) {
		loadData();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object[] path;
		int[] selectedRows;
		switch(GroupsCommand.valueOf(e.getActionCommand())) {
		case GROUP_ADD:
			String groupName = (String)JOptionPane.showInputDialog(parentComponent, "Name:", "Create new group", JOptionPane.PLAIN_MESSAGE, null, null, "");
			if(groupName != null && !groupName.equals("")) {
				try {
					MetaGroupManager metaGroupManager = daxploreFile.getMetaGroupManager();
					int nextid = metaGroupManager.getHighestId(); // Assumes perspective group is at index 0, standard groups are 1-indexed
					TextReferenceManager textReferenceManager = daxploreFile.getTextReferenceManager();
					TextReference tr = textReferenceManager.get("group_" + nextid);
					tr.put(groupName, Settings.getDefaultLocale());
					MetaGroup mg = metaGroupManager.create(tr, Integer.MAX_VALUE, GroupType.QUESTIONS, new LinkedList<MetaQuestion>());
					TreePath treepath = groupTreeModel.addGroup(mg, groupTreeModel.getChildCount(groupTreeModel.getRoot()));
					groupTree.setSelectionPath(treepath);
				} catch (Exception e1) { //TODO: fix proper exception handling
					JOptionPane.showMessageDialog(parentComponent,
						    "Something went wrong while creating new group",
						    "Group creation error",
						    JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				}
			} else {
				JOptionPane.showMessageDialog(parentComponent,
					    "Group has to have a name",
					    "Group creation error",
					    JOptionPane.ERROR_MESSAGE);
			}
			break;
		case GROUP_UP:
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
		case GROUP_DOWN:
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
		case GROUP_ADD_ITEM:
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
		case GROUP_REMOVE:
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
		case PERSPECTIVE_UP:
			selectedRows = perspectivesTable.getSelectedRows();
			if(selectedRows.length < 1 || selectedRows[0] == 0) break;
			perspectivesTable.clearSelection();
			perspectivesTable.removeEditor();
			for(int i = 0; i < selectedRows.length; i++) {
				perspectivesTableModel.moveRow(selectedRows[i], selectedRows[i], selectedRows[i]-1);
				perspectivesTable.getSelectionModel().addSelectionInterval(selectedRows[i]-1, selectedRows[i]-1);
			}
			break;
		case PERSPECTIVE_DOWN:
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
		case PERSPECTIVE_REMOVE:
			selectedRows = perspectivesTable.getSelectedRows();
			int delta = 0;
			for(int row: selectedRows) {
				perspectivesTableModel.removeRow(row - delta);
				delta++;
			}
			break;
		case PERSPECTIVE_ADD_ITEM:
			int index = perspectivesTable.getSelectedRow() != -1? perspectivesTable.getSelectedRow(): perspectivesTable.getRowCount();
			for(int i : questionJTable.getSelectedRows()) {
				MetaQuestion mq = (MetaQuestion)questionJTable.getValueAt(i, 0);
				perspectivesTableModel.insertRow(index, new Object[]{mq});
				index++;
			}
			break;
		case RELOAD_DATA:
			loadData();
			break;
		default:
			throw new AssertionError("Action command not implemented: '" + e.getActionCommand() + "'");
		}
		
	}

	public void loadData() {
		if(daxploreFile != null) {
			try {
				questionTableModel = new QuestionTableModel(daxploreFile.getMetaQuestionManager());
				questionJTable = new QuestionTable(eventBus, questionTableModel);
				groupsView.getQuestionsScrollPane().setViewportView(questionJTable);
				
				//get groups and perspectives
				groupTreeModel = new GroupTreeModel(daxploreFile.getMetaGroupManager());
				groupTree = new GroupTree(eventBus, groupTreeModel);
				groupsView.getGroupsScollPane().setViewportView(groupTree);
				
				MetaGroup perspectives = daxploreFile.getMetaGroupManager().getPerspectiveGroup();
				
				perspectivesTableModel = new PerspectivesTableModel(perspectives);
				perspectivesTable = new QuestionTable(eventBus, perspectivesTableModel);
				groupsView.getPerspectiveScrollPane().setViewportView(perspectivesTable);
			} catch (DaxploreException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public Component getView() {
		return groupsView;
	}
}
