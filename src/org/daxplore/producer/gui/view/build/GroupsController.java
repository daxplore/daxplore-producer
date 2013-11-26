package org.daxplore.producer.gui.view.build;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Locale;

import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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
import org.daxplore.producer.gui.event.DisplayLocaleSelectEvent;
import org.daxplore.producer.gui.event.EditQuestionEvent;
import org.daxplore.producer.gui.event.EmptyEvents.RawImportEvent;
import org.daxplore.producer.gui.resources.GuiTexts;

import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class GroupsController implements ActionListener {

	enum GroupsCommand {
		EDIT_VARIABLE, GROUP_ADD, GROUP_UP, GROUP_DOWN, GROUP_REMOVE, GROUP_ADD_ITEM,
		PERSPECTIVE_UP, PERSPECTIVE_DOWN, PERSPECTIVE_REMOVE, PERSPECTIVE_ADD_ITEM
	}
	
	private EventBus eventBus;
	private Component parentComponent;
	
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
	
	public GroupsController(EventBus eventBus, GuiTexts texts, Component parentComponent) {
		this.eventBus = eventBus;
		this.parentComponent = parentComponent;
		
		eventBus.register(this);
		
		groupsView = new GroupsView(texts, this);
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
	
	@Subscribe
	public void on(DisplayLocaleSelectEvent e) {
		selectedLocale = e.getLocale();
		viewMetaQuestionInfo(selectedMetaQuestion);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object[] path;
		TreePath[] paths;
		int[] selectedRows;
		switch(GroupsCommand.valueOf(e.getActionCommand())) {
		case EDIT_VARIABLE:
			//TODO use a different event when the question view is replaced with modular window
			eventBus.post(new EditQuestionEvent(selectedMetaQuestion));
			break;
		case GROUP_ADD:
			String groupName = (String)JOptionPane.showInputDialog(parentComponent, "Name:", "Create new group", JOptionPane.PLAIN_MESSAGE, null, null, "");
			if(groupName != null && !Strings.isNullOrEmpty(groupName)) {
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
									Object newGroup = groupTreeModel.getChild(p.getPathComponent(0), groupIndex-1);
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
			} catch (Exception ex) { ex.printStackTrace(); }
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
									Object newGroup = groupTreeModel.getChild(p.getPathComponent(0), groupIndex+1);
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
			if(groupTree.getSelectionPath()==null) {
				break;
			}
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
			
			ArrayList<TreePath> pathslist = new ArrayList<>(questionJTable.getSelectedRowCount()); 
			
			try {
				for(int i : questionJTable.getSelectedRows()) {
					MetaQuestion mq = (MetaQuestion)questionJTable.getValueAt(i, 0);
					MetaGroup gr = groupTreeModel.getGroupFor(mq);
					if(gr == null) {
						TreePath treepath = groupTreeModel.addQuestion(mq, parent, atIndex);
						atIndex++;
						pathslist.add(treepath);
					} else {
						pathslist.add(new TreePath(new Object[]{groupTreeModel.getRoot(), gr, mq}));
					}
				}
				}catch (Exception e2) {
				// TODO: handle exception
				e2.printStackTrace();
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
						MessageFormat.format("Are you sure you want to remove the group \"{0}\"?", group.getTextRef().get(Settings.getCurrentDisplayLocale())),
								"Remove group?",
								JOptionPane.YES_NO_OPTION);
				if(removeOption==JOptionPane.YES_OPTION) {
					groupTree.setSelectionPath(null);
					groupTreeModel.removeChild(group);
				}
			} else if(paths[0].getLastPathComponent() instanceof MetaQuestion) {
				for(TreePath p: paths) {
					if(p.getLastPathComponent() instanceof MetaQuestion) {
						groupTree.setSelectionPath(null);
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
			int index = perspectivesTable.getSelectedRow() != -1? perspectivesTable.getSelectedRow(): perspectivesTable.getRowCount();
			for(int i : questionJTable.getSelectedRows()) {
				MetaQuestion mq = (MetaQuestion)questionJTable.getValueAt(i, 0);
				perspectivesTableModel.insertRow(index, new Object[]{mq});
				index++;
			}
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
				groupsView.setQuestionTree(groupTree);
				
				MetaGroup perspectives = daxploreFile.getMetaGroupManager().getPerspectiveGroup();
				
				perspectivesTableModel = new PerspectivesTableModel(perspectives);
				perspectivesTable = new QuestionTable(eventBus, perspectivesTableModel);
				groupsView.setPerspectiveList(perspectivesTable);
				
				questionJTable.getSelectionModel().setSelectionInterval(0, 0);
			} catch (DaxploreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void viewMetaQuestionInfo(MetaQuestion question) {
		selectedMetaQuestion = question;
		String id = question.getId();
		if(selectedLocale != null) {
			
			String shorttext = question.getShortTextRef().get(selectedLocale);
			String fulltext = question.getFullTextRef().get(selectedLocale);
			groupsView.setVariableInfo(id, shorttext, fulltext);
		} else {
			groupsView.setVariableInfo(id, "", "");
		}
	}

	public Component getView() {
		return groupsView;
	}
}
