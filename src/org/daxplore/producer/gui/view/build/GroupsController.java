/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui.view.build;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.TreePath;

import org.daxplore.producer.daxplorelib.DaxploreFile;
import org.daxplore.producer.daxplorelib.metadata.MetaGroup;
import org.daxplore.producer.daxplorelib.metadata.MetaGroup.GroupType;
import org.daxplore.producer.daxplorelib.metadata.MetaGroup.MetaGroupManager;
import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReferenceManager;
import org.daxplore.producer.gui.Dialogs;
import org.daxplore.producer.gui.GuiSettings;
import org.daxplore.producer.gui.event.DaxploreFileUpdateEvent;
import org.daxplore.producer.gui.event.DisplayLocaleSelectEvent;
import org.daxplore.producer.gui.event.EditQuestionEvent;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class GroupsController implements ActionListener, MouseListener {

	enum GroupsCommand {
		EDIT_VARIABLE, GROUP_ADD, GROUP_UP, GROUP_DOWN, GROUP_REMOVE, GROUP_ADD_ITEM,
		PERSPECTIVE_UP, PERSPECTIVE_DOWN, PERSPECTIVE_REMOVE, PERSPECTIVE_ADD_ITEM, EDIT_TREE
	}
	
	private EventBus eventBus;
	
	private DaxploreFile daxploreFile;
	
	private GroupsView groupsView;
	
	private GroupTreeModel groupTreeModel;
	private GroupTree groupTree;
	private QuestionTable questionJTable;
	private QuestionTableModel questionTableModel;
	private PerspectivesTableModel perspectivesTableModel;
	private QuestionTable perspectivesTable;
	
	private MetaQuestion selectedMetaQuestion;
	private Locale selectedLocale;
	
	private Comparator<TreePath> pathComparator = new Comparator<TreePath>() {

		@Override
		public int compare(TreePath o1, TreePath o2) {
			if(o1.getLastPathComponent() instanceof MetaGroup || o2.getLastPathComponent() instanceof MetaGroup) {
				return groupTreeModel.getIndexOfChild(o1.getPathComponent(0), o1.getPathComponent(1)) - groupTreeModel.getIndexOfChild(o2.getPathComponent(0), o2.getPathComponent(1));
			} else if(o1.getLastPathComponent() instanceof MetaQuestion && o2.getLastPathComponent() instanceof MetaQuestion) {
				return groupTreeModel.getIndexOfChild(o1.getPathComponent(1), o1.getPathComponent(2)) - groupTreeModel.getIndexOfChild(o2.getPathComponent(1), o2.getPathComponent(2));
			}
			throw new AssertionError("Only metagroups and metaquestions in grouptree");
		}
	};
	
	public GroupsController(EventBus eventBus) {
		this.eventBus = eventBus;
		
		eventBus.register(this);
		
		groupsView = new GroupsView(this);
	}
	
	@Subscribe
	public void on(DaxploreFileUpdateEvent e) {
		this.daxploreFile = e.getDaxploreFile();
		try {
			questionTableModel = new QuestionTableModel(daxploreFile.getMetaQuestionManager());
			questionJTable = new QuestionTable(eventBus, questionTableModel);
			
			questionJTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
		        @Override
				public void valueChanged(ListSelectionEvent event) {
		        	if(!event.getValueIsAdjusting() && questionJTable.getSelectedRow() >= 0) {
		        		viewMetaQuestionInfo((MetaQuestion)questionJTable.getValueAt(questionJTable.getSelectedRow(), 0));
		        	}
		        }
		    });
			
			groupsView.setVariableList(questionJTable);
			
			//get groups and perspectives
			groupTreeModel = new GroupTreeModel(daxploreFile.getMetaGroupManager());
			groupTree = new GroupTree(eventBus, groupTreeModel);
			groupTree.addMouseListener(this);
			groupsView.setQuestionTree(groupTree);
			
			MetaGroup perspectives = daxploreFile.getMetaGroupManager().getPerspectiveGroup();
			
			perspectivesTableModel = new PerspectivesTableModel(perspectives);
			perspectivesTable = new QuestionTable(eventBus, perspectivesTableModel);
			groupsView.setPerspectiveList(perspectivesTable);
			
			questionJTable.getSelectionModel().setSelectionInterval(0, 0);
		} catch (Exception ex) {
			Logger.getGlobal().log(Level.SEVERE, "Failed to handle event", ex);
		}
	}
	
	@Subscribe
	public void on(DisplayLocaleSelectEvent e) {
		try {
			selectedLocale = e.getLocale();
			viewMetaQuestionInfo(selectedMetaQuestion);
		} catch (Exception ex) {
			Logger.getGlobal().log(Level.SEVERE, "Failed to handle event", ex);
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object[] path;
		TreePath[] paths;
		int[] selectedRows;
		switch(GroupsCommand.valueOf(e.getActionCommand())) {
		case EDIT_VARIABLE:
			eventBus.post(new EditQuestionEvent(selectedMetaQuestion));
			break;
		case EDIT_TREE:
			if(groupTree.getSelectionPath()==null) {
				break;
			}
			editNode(groupTree.getSelectionPath());
			break;
		case GROUP_ADD:
			TextReferenceManager textReferenceManager = daxploreFile.getTextReferenceManager();
			MetaGroupManager metaGroupManager = daxploreFile.getMetaGroupManager();
			MetaGroup mg = Dialogs.createGroupDialog(getView(), textReferenceManager, metaGroupManager, daxploreFile.getAbout().getLocales());
			if(mg != null) {
				TreePath treepath = groupTreeModel.addGroup(mg, groupTreeModel.getChildCount(groupTreeModel.getRoot()));
				groupTree.setSelectionPath(treepath);
			}
			break;
		case GROUP_UP:
			paths = groupTree.getSelectionPaths();
			if(paths == null) {
				break;
			}
			
			Arrays.sort(paths, pathComparator);
			
			try {
				groupTree.setSelectionPath(null);
				if(paths[0].getLastPathComponent() instanceof MetaGroup) {
					for(TreePath p: paths) {
						if(p.getLastPathComponent() instanceof MetaGroup) {
							int currentIndex = groupTreeModel.getIndexOfChild(p.getPathComponent(0), p.getPathComponent(1));
							if(currentIndex > 0) {
								groupTreeModel.moveChild(p.getPathComponent(1), p.getPathComponent(0), currentIndex-1);
							} else {
								break;
							}
						}
					}
				} else if(paths[0].getLastPathComponent() instanceof MetaQuestion) {
					int i = 0;
					for(TreePath p: paths) {
						if(p.getLastPathComponent() instanceof MetaQuestion) {
							int currentIndex = groupTreeModel.getIndexOfChild(p.getPathComponent(1), p.getPathComponent(2));
							if(currentIndex > 0) {
								groupTreeModel.moveChild(p.getPathComponent(2), p.getPathComponent(1), currentIndex-1);
							} else {
								int groupIndex = groupTreeModel.getIndexOfChild(p.getPathComponent(0), p.getPathComponent(1));
								if(groupIndex > 0) {
									Object newGroup = null;
									while(--groupIndex >= 0) {
										newGroup = groupTreeModel.getChild(p.getPathComponent(0), groupIndex);
										if(newGroup instanceof MetaGroup) {
											MetaGroup newmg = (MetaGroup)newGroup;
											if(newmg.getType() == GroupType.QUESTIONS) {
												break;
											}
										}
									}
									if(groupIndex < 0) {
										break;
									}
									groupTreeModel.moveChild(p.getPathComponent(2), newGroup, groupTreeModel.getChildCount(newGroup));
									paths[i] = new TreePath(new Object[]{p.getPathComponent(0), newGroup, p.getPathComponent(2)});
								} else {
									break;
								}
							}
						}
						i++;
					}
				}
				groupTree.setSelectionPaths(paths);
			} catch (IllegalArgumentException ex) { ex.printStackTrace(); }
			break;
		case GROUP_DOWN:
			paths = groupTree.getSelectionPaths();
			if(paths == null) {
				break;
			}
			
			Arrays.sort(paths, pathComparator);
			
			try {
				groupTree.setSelectionPath(null);
				if(paths[0].getLastPathComponent() instanceof MetaGroup) {
					for(int i = paths.length-1; i >= 0; i--) {
						TreePath p = paths[i];
						if(p.getLastPathComponent() instanceof MetaGroup) {
							int currentIndex = groupTreeModel.getIndexOfChild(p.getPathComponent(0), p.getPathComponent(1));
							int siblingCount = groupTreeModel.getChildCount(p.getPathComponent(0));
							if(currentIndex < siblingCount-1) {
								groupTreeModel.moveChild(p.getPathComponent(1), p.getPathComponent(0), currentIndex+1);
							} else {
								break;
							}
						}
					}
				} else if(paths[0].getLastPathComponent() instanceof MetaQuestion) {
					for(int i = paths.length-1; i >= 0; i--) {
						TreePath p = paths[i];
						if(p.getLastPathComponent() instanceof MetaQuestion) {
							int currentIndex = groupTreeModel.getIndexOfChild(p.getPathComponent(1), p.getPathComponent(2));
							int siblingCount = groupTreeModel.getChildCount(p.getPathComponent(1));
							if(currentIndex < siblingCount-1){
								groupTreeModel.moveChild(p.getPathComponent(2), p.getPathComponent(1), currentIndex+1);
							} else {
								int groupIndex = groupTreeModel.getIndexOfChild(p.getPathComponent(0), p.getPathComponent(1));
								int groupSiblingCount = groupTreeModel.getChildCount(p.getPathComponent(0));
								if(groupIndex < groupSiblingCount-1) {
									Object newGroup = null;
									while(++groupIndex < groupSiblingCount) {
										newGroup = groupTreeModel.getChild(p.getPathComponent(0), groupIndex);
										if(newGroup instanceof MetaGroup) {
											MetaGroup newmg = (MetaGroup)newGroup;
											if(newmg.getType() == GroupType.QUESTIONS) {
												break;
											}
										}
									}
									if(groupIndex >= groupSiblingCount) {
										break;
									}
									groupTreeModel.moveChild(p.getPathComponent(2), newGroup, 0);
									paths[i] = new TreePath(new Object[]{p.getPathComponent(0), newGroup, p.getPathComponent(2)});
								} else {
									break;
								}
							}
						}
					}
				}
				groupTree.setSelectionPaths(paths);
			} catch (Exception ex) { ex.printStackTrace(); }
			break;
		case GROUP_ADD_ITEM:
			MetaGroup parent = null;
			int atIndex = 0;
			if(groupTree.getSelectionPath()==null) {
				Object root = groupTreeModel.getRoot();
				// if there are no groups, create a new one
				if(groupTreeModel.getChildCount(root)==0) {
					actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, GroupsCommand.GROUP_ADD.name()));
					if(groupTreeModel.getChildCount(root)==0) {
						// break if the user didn't create a new group
						break;
					}
				}
				parent = (MetaGroup)groupTreeModel.getChild(root, groupTreeModel.getChildCount(root)-1);
				atIndex = parent.getQuestionCount();
			} else {
				path = groupTree.getSelectionPath().getPath();
				if(path.length == 2) {
					parent = (MetaGroup)path[1];
					atIndex = parent.getQuestionCount();
				} else if(path.length == 3) {
					parent = (MetaGroup)path[1];
					atIndex = groupTreeModel.getIndexOfChild(parent, path[2]) + 1;
				}
			}
			
			ArrayList<TreePath> pathslist = new ArrayList<>(questionJTable.getSelectedRowCount()); 
			
			for(int i : questionJTable.getSelectedRows()) {
				MetaQuestion question = (MetaQuestion)questionJTable.getValueAt(i, 0);
				MetaGroup group = groupTreeModel.getGroupFor(question);
				if(groupTreeModel.containsQuestion(question)) {
					pathslist.add(new TreePath(new Object[]{groupTreeModel.getRoot(), group, question}));
				} else {
					TreePath treepath = groupTreeModel.addQuestion(question, parent, atIndex);
					atIndex++;
					pathslist.add(treepath);
				}
			}
			groupTree.setSelectionPaths(pathslist.toArray(new TreePath[pathslist.size()]));
			break;
		case GROUP_REMOVE:
			paths = groupTree.getSelectionPaths();
			if(paths == null) {
				break;
			}
			
			if(paths[0].getLastPathComponent() instanceof MetaGroup) {
				MetaGroup group = (MetaGroup)paths[0].getLastPathComponent();
				 //TODO externalize 
				int removeOption = JOptionPane.showConfirmDialog(groupsView,
						MessageFormat.format("Are you sure you want to remove the group \"{0}\"?", group.getTextRef().getText(GuiSettings.getCurrentDisplayLocale())),
								"Remove group?",
								JOptionPane.YES_NO_OPTION);
				if(removeOption==JOptionPane.YES_OPTION) {
					groupTree.setSelectionPath(null);
					groupTreeModel.removeChild(group);
				}
			} else if(paths[0].getLastPathComponent() instanceof MetaQuestion) {
				groupTree.setSelectionPath(null);
				for(TreePath p: paths) {
					if(p.getLastPathComponent() instanceof MetaQuestion) {
						groupTreeModel.removeChild(p.getLastPathComponent());
					}
				}
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
			int index = perspectivesTable.getSelectedRow() != -1 ? perspectivesTable.getSelectedRow() : perspectivesTable.getRowCount();
			for(int i : questionJTable.getSelectedRows()) {
				MetaQuestion mq = (MetaQuestion)questionJTable.getValueAt(i, 0);
				if(!perspectivesTableModel.mg.contains(mq)) {
					perspectivesTableModel.insertRow(index, new Object[]{mq});
					index++;
				}
			}
			break;
		default:
			throw new AssertionError("Action command not implemented: '" + e.getActionCommand() + "'");
		}
		
	}
	
	private void viewMetaQuestionInfo(MetaQuestion question) {
		selectedMetaQuestion = question;
		String column = question.getColumn();
		if(selectedLocale != null) {
			
			String shorttext = question.getShortTextRef().getText(selectedLocale);
			String fulltext = question.getFullTextRef().getText(selectedLocale);
			groupsView.setVariableInfo(column, shorttext, fulltext);
		} else {
			groupsView.setVariableInfo(column, "", "");
		}
	}
	
	/**
	 * Edit the {@link MetaGroup} or {@link MetaGroup} described by the path.
	 * 
	 * @param path The path to the node to be edited
	 */
	private void editNode(TreePath path) {
		if (path != null) {
			Object[] pathObjects = path.getPath();
			if(pathObjects.length == 2 && pathObjects[1] instanceof MetaGroup) {
				TextReferenceManager textReferenceManager = daxploreFile.getTextReferenceManager();
				TreePath[] paths = groupTree.getSelectionPaths(); 
				MetaGroup metaGroup = (MetaGroup) pathObjects[1];
				boolean edited = Dialogs.editGroupDialog(getView(), textReferenceManager, daxploreFile.getAbout().getLocales(), metaGroup);
				if(edited) {
					groupTree.setSelectionPath(null);
					groupTree.setSelectionPaths(paths);
					groupTreeModel.valueForPathChanged(path, pathObjects[1]);
				}
			} else if (pathObjects.length == 3 && pathObjects[2] instanceof MetaQuestion) {
				eventBus.post(new EditQuestionEvent((MetaQuestion)pathObjects[2]));
				groupTreeModel.valueForPathChanged(path, pathObjects[2]);
			}
		}
	}
	
	public Component getView() {
		return groupsView;
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * Edit tree on double click
	 */
    @Override
    public void mousePressed(MouseEvent e) {
    	if(e.getClickCount() == 2) {
    		editNode(groupTree.getPathForLocation(e.getX(), e.getY()));
        }
    }

	@Override
	public void mouseReleased(MouseEvent e) {
	}
}
