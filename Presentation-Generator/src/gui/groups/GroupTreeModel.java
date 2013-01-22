package gui.groups;

import gui.widget.GroupWidget;
import gui.widget.QuestionWidget;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import daxplorelib.DaxploreException;
import daxplorelib.metadata.MetaData;
import daxplorelib.metadata.MetaGroup;
import daxplorelib.metadata.MetaGroup.GroupType;
import daxplorelib.metadata.MetaQuestion;

class GroupTreeModel implements TreeModel {

	JPanel root = new JPanel();
	List<GroupWidget> groups = new LinkedList<GroupWidget>();
	
	private EventListenerList listeners = new EventListenerList();
	//private Vector<TreeModelListener> listeners = new Vector<TreeModelListener>(); // Declare the listeners vector
	
	public GroupTreeModel(MetaData md) throws DaxploreException {
		root.add(new JLabel("root object"));
		List<MetaGroup> allGroups = md.getAllGroups();
		for(MetaGroup mg: allGroups) {
			if(mg.getType() == GroupType.QUESTIONS) {
				GroupWidget gw = new GroupWidget(mg);
				for(MetaQuestion mq: mg.getQuestions()) {
					gw.questions.add(new QuestionWidget(mq));
				}
				groups.add(gw);
			}
		}
	}
	
	@Override
	public Object getRoot() {
		return root;
	}

	@Override
	public Object getChild(Object parent, int index) {
		if(parent == root) {
			if(index < groups.size()) {
				return groups.get(index);
			} else return null;
		} else if(parent instanceof GroupWidget) {
			GroupWidget p = (GroupWidget)parent;
			if(index < p.questions.size()) {
				return p.questions.get(index);
			} else return null;
		} else if (parent instanceof QuestionWidget) {
			return null;
		}
		return null;
	}
	
	public void addGroup(MetaGroup mg, int atIndex) throws Exception { //TODO: specialize exception
		if(atIndex >= 0 && atIndex <= groups.size()) {
			GroupWidget gw = new GroupWidget(mg);
			groups.add(atIndex, gw);
			mg.setIndex(atIndex);
			fireTreeNodesInserted(new TreeModelEvent(this, new Object[]{root}));
			return;
		}
		throw new Exception("Not allowed to place this at that");
	}
	
	public void addQuestion(MetaQuestion mq, GroupWidget parent, int atIndex) throws Exception { //TODO: specialize exception
		for(GroupWidget gw: groups) {
			for(QuestionWidget qw: gw.questions) {
				if(qw.metaQuestion.getId() == mq.getId()) {
					throw new Exception("Can't have duplicate of that");
				}
			}
		}
		if(groups.contains(parent) && atIndex >= 0 && atIndex <= getChildCount(parent)) {
			parent.questions.add(atIndex, new QuestionWidget(mq));
			List<MetaQuestion> qList = new LinkedList<MetaQuestion>();
			for(QuestionWidget qw: parent.questions) {
				qList.add(qw.metaQuestion);
			}
			parent.metaGroup.setQuestions(qList);
			fireTreeNodesInserted(new TreeModelEvent(this, new Object[]{root,parent}));
			return;
		}
		throw new Exception("Not allowed to place this at that");
	}
	
	public void moveChild(Object child, Object toParent, int atIndex) throws Exception { //TODO: specialize exception
		if(child instanceof GroupWidget && toParent == root && groups.contains(child)
				&& atIndex >= 0 && atIndex <= groups.size()) {
			int delta = groups.indexOf(child) < atIndex ? -1: 0;
			groups.remove(child);
			groups.add(atIndex + delta, (GroupWidget)child);
			for(int index = 0; index < groups.size(); index++) {
				groups.get(index).metaGroup.setIndex(index);
			}
			fireTreeNodesChanged(new TreeModelEvent(this, new Object[]{root}));
			return;
		} else if(child instanceof QuestionWidget && toParent instanceof GroupWidget && groups.contains(toParent)) {
			for(GroupWidget gw: groups) {
				if(gw.questions.contains(child)) {
					if(gw == toParent && atIndex >= 0 && atIndex <= gw.questions.size()) {
						int delta = gw.questions.indexOf(child) < atIndex ? -1: 0;
						gw.questions.remove(child);
						gw.questions.add(atIndex + delta, (QuestionWidget)child);
						List<MetaQuestion> qList = new LinkedList<MetaQuestion>();
						for(QuestionWidget qw: gw.questions) {
							qList.add(qw.metaQuestion);
						}
						gw.metaGroup.setQuestions(qList);
						fireTreeNodesChanged(new TreeModelEvent(this, new Object[]{root, gw}));
						return;
					} else if(atIndex >= 0 && atIndex <= gw.questions.size()){
						gw.questions.remove(child);
						GroupWidget parent = (GroupWidget)toParent;
						parent.questions.add(atIndex, (QuestionWidget)child);
						
						List<MetaQuestion> qList = new LinkedList<MetaQuestion>();
						for(QuestionWidget qw: gw.questions) {
							qList.add(qw.metaQuestion);
						}
						gw.metaGroup.setQuestions(qList);
						
						qList = new LinkedList<MetaQuestion>();
						for(QuestionWidget qw: parent.questions) {
							qList.add(qw.metaQuestion);
						}
						parent.metaGroup.setQuestions(qList);
						fireTreeNodesChanged(new TreeModelEvent(this, new Object[]{root}));
						return;
					}
				}
			}
		}
		throw new Exception("Couldn't move this to that");
	}
	
	public void removeChild(Object child) throws Exception { //TODO: specialize exception
		if(child instanceof GroupWidget && groups.contains(child)) {
			groups.remove(child);
			fireTreeNodesRemoved(new TreeModelEvent(this, new Object[]{root}));
			return;
		} else if(child instanceof QuestionWidget) {
			for(GroupWidget gw: groups) {
				if(gw.questions.contains(child)) {
					gw.questions.remove(child);
					
					List<MetaQuestion> qList = new LinkedList<MetaQuestion>();
					for(QuestionWidget qw: gw.questions) {
						qList.add(qw.metaQuestion);
					}
					gw.metaGroup.setQuestions(qList);
					fireTreeNodesRemoved(new TreeModelEvent(this, new Object[]{root, gw}));
					return;
				}
			}
		}
		throw new Exception("Couldn't remove that");
	}

	@Override
	public int getChildCount(Object parent) {
		if(parent == root) {
			return groups.size();
		} else if(parent instanceof GroupWidget) {
			GroupWidget p = (GroupWidget)parent;
			return p.questions.size();
		} else if (parent instanceof QuestionWidget) {
			return 0;
		}
		return 0;
	}

	@Override
	public boolean isLeaf(Object node) {
		if(node == root || node instanceof GroupWidget) {
			return false;
		} else if(node instanceof QuestionWidget) {
			return true;
		}
		return true;
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		// TODO Should this do anything?!?
	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
		if(parent == root) {
			return groups.indexOf(child);
		} else if(parent instanceof GroupWidget) {
			GroupWidget p = (GroupWidget)parent;
			if(groups.contains(p)) {
				return p.questions.indexOf(child);				
			} else return -1;
		} else if (parent instanceof QuestionWidget) {
			return -1;
		}
		return -1;
	}

	@Override
	public void addTreeModelListener(TreeModelListener l) {
		if(l != null) {
			listeners.add(TreeModelListener.class, l);
		}
	}

	@Override
	public void removeTreeModelListener(TreeModelListener l) {
		if (l != null) {
	    	listeners.remove(TreeModelListener.class, l);
	    }
	}
	
	private void fireTreeNodesChanged(TreeModelEvent e) {
		/*for(TreeModelListener tml: listeners.getListeners(TreeModelListener.class)) {
			tml.treeNodesChanged(e);
		}*/
		fireTreeStructureChanged(e);
	}
	
	private void fireTreeNodesInserted(TreeModelEvent e) {
		/*for(TreeModelListener tml: listeners.getListeners(TreeModelListener.class)) {
			tml.treeNodesInserted(e);
		}*/
		fireTreeStructureChanged(e);
	}
	
	private void fireTreeNodesRemoved(TreeModelEvent e) {
		/*for(TreeModelListener tml: listeners.getListeners(TreeModelListener.class)) {
			tml.treeNodesRemoved(e);
		}*/
		fireTreeStructureChanged(e);
	}
	
	private void fireTreeStructureChanged(TreeModelEvent e) {
		TreeModelEvent e2 = new TreeModelEvent(this, new Object[]{root});
		for(TreeModelListener tml: listeners.getListeners(TreeModelListener.class)) {
			tml.treeStructureChanged(e2);
		}
	}
	
}