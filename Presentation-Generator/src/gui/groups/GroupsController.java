package gui.groups;

import gui.GuiMain;
import gui.widget.GroupWidget;
import gui.widget.QuestionWidget;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.Locale;

import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;

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
	
	private GuiMain guiMain;
	private GroupsView groupsView;
	
	private GroupTreeModel groupTreeModel;
	private QuestionListModel questionListModel;
	private JList<QuestionWidget> questionJList;
	private JTree groupJTree;
	
	public GroupsController(GroupsView groupView, GuiMain guiMain) {
		this.guiMain = guiMain;
		this.groupsView = groupView;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		switch(e.getActionCommand()) {
		case GROUPS_ADD_ACTION_COMMAND:
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
			break;
		case GROUPS_UP_ACTION_COMMAND:
			break;
		case GROUPS_DOWN_ACTION_COMMAND:
			break;
		case GROUPS_REMOVE_ACTION_COMMAND:
			break;
		case PERSPECTIVES_UP_ACTION_COMMAND:
			break;
		case PERSPECTIVES_DOWN_ACTION_COMMAND:
			break;
		case PERSPECTIVES_REMOVE_ACTION_COMMAND:
			break;
		case ADD_TO_GROUP_ACTION_COMMAND:
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
			break;
		case ADD_TO_PERSPECTIVES_ACTION_COMMAND:
			break;
		}
		
	}

	public void loadData() {
		if(guiMain.getGuiFile().isSet()) {
			try {
				MetaData md = guiMain.getGuiFile().getDaxploreFile().getMetaData();
				questionListModel = new QuestionListModel(md);
				questionJList = new JList<QuestionWidget>(questionListModel);
				questionJList.setCellRenderer(groupsView.new QuestionListCellRenderer());
				questionJList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				groupsView.getQuestionsScrollPane().setViewportView(questionJList);
				
				//get groups and perspectives
				groupTreeModel = new GroupTreeModel(md);
				groupJTree = new JTree(groupTreeModel);
				groupJTree.setRootVisible(false);
				groupJTree.setCellRenderer(groupsView.new GroupTreeCellRenderer());
				//tree.getPr
				groupsView.getGroupsScollPane().setViewportView(groupJTree);
				
			} catch (DaxploreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
}
