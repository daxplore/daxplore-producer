package gui.groups;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.junit.internal.matchers.IsCollectionContaining;

import tools.MyTools;
import daxplorelib.DaxploreException;
import daxplorelib.metadata.MetaData;
import daxplorelib.metadata.MetaGroup;
import daxplorelib.metadata.MetaGroup.GroupType;
import daxplorelib.metadata.MetaQuestion;

class GroupTreeModel implements TreeModel {

	JPanel root = new JPanel();
	//List<GroupWidget> groups = new LinkedList<GroupWidget>();
	List<MetaGroup> groups = new LinkedList<MetaGroup>();
	
	private EventListenerList listeners = new EventListenerList();
	//private Vector<TreeModelListener> listeners = new Vector<TreeModelListener>(); // Declare the listeners vector
	
	public GroupTreeModel(MetaData md) throws DaxploreException {
		root.add(new JLabel("root object"));
		List<MetaGroup> allGroups = md.getAllGroups();
		for(MetaGroup mg: allGroups) {
			if(mg.getType() == GroupType.QUESTIONS) {
				groups.add(mg);
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
		} else if(parent instanceof MetaGroup) {
			MetaGroup p = (MetaGroup)parent;
			if(index < p.getQuestionCount()) {
				return p.getQuestion(index);
			} else return null;
		} else if (parent instanceof MetaQuestion) {
			return null;
		}
		return null;
	}
	/**
	 * Add a new group to the tree
	 * @param The MetaGroup to add
	 * @param The index at witch to insert the new group
	 * @return The TreePath to the new group
	 * @throws Exception
	 */
	public TreePath addGroup(MetaGroup mg, int atIndex) throws Exception { //TODO: specialize exception
		if(atIndex >= 0 && atIndex <= groups.size()) {
			groups.add(atIndex, mg);
			mg.setIndex(atIndex);
			fireTreeNodesInserted(new TreeModelEvent(this, 
					new Object[]{root},
					MyTools.range(0, groups.size() -1),
					groups.toArray()));
			return new TreePath(new Object[]{root, mg});
		}
		throw new Exception("Not allowed to place this at that");
	}
	/**
	 * Add a new question to the tree
	 * @param The MetaQuestion to add
	 * @param The group to which the question will be added
	 * @param The index at which the question will be added
	 * @return The TreePath to the added question
	 * @throws Exception
	 */
	public TreePath addQuestion(MetaQuestion mq, MetaGroup parent, int atIndex) throws Exception { //TODO: specialize exception
		for(MetaGroup mg: groups){
			if(mg.getQuestions().contains(mq)) {
				throw new Exception("Can't have duplicate of that");
			}
		}
		if(groups.contains(parent) && atIndex >= 0 && atIndex <= getChildCount(parent)) {
			parent.addQuestion(mq, atIndex);
			fireTreeNodesInserted(new TreeModelEvent(this, 
					new Object[]{root,parent},
					new int[]{atIndex},
					new Object[]{mq}));
			return new TreePath(new Object[]{root, parent, mq});
		}
		throw new Exception("Not allowed to place this at that");
	}
	
	public void moveChild(Object child, Object toParent, int atIndex) throws Exception { //TODO: specialize exception
		if(child instanceof MetaGroup && toParent == root && groups.contains(child)
				&& atIndex >= 0 && atIndex <= groups.size()) {
			int delta = groups.indexOf(child) < atIndex ? -1: 0;
			groups.remove(child);
			groups.add(atIndex + delta, (MetaGroup)child);
			for(int index = 0; index < groups.size(); index++) {
				groups.get(index).setIndex(index);
			}
			for(int i = 0; i < groups.size(); i++) {
				groups.get(i).setIndex(i);
			}
			fireTreeNodesChanged(new TreeModelEvent(this, new Object[]{root}));
			return;
		} else if(child instanceof MetaQuestion && toParent instanceof MetaGroup && groups.contains(toParent)) {
			MetaQuestion mq = (MetaQuestion)child;
			for(MetaGroup gw: groups) {
				if(gw.getQuestions().contains(mq)) {
					if(gw == toParent && atIndex >= 0 && atIndex <= gw.getQuestionCount()) {
						int delta = gw.getQuestions().indexOf(mq) < atIndex ? -1: 0;
						int oldIndex = gw.getQuestions().indexOf(mq);
						int indexLow = oldIndex < atIndex? oldIndex: atIndex;
						int indexHigh = oldIndex > atIndex? oldIndex: atIndex;
						
						fireTreeNodesRemoved(
								new TreeModelEvent(this,
										new Object[]{root, gw},
										new int[]{oldIndex},
										new Object[]{mq}));
						gw.removeQuestion(mq);

						fireTreeNodesInserted(new TreeModelEvent(this,
								new Object[]{root, gw},
								new int[]{atIndex},
								new Object[]{mq}));
						gw.addQuestion(mq, atIndex);

						/*fireTreeNodesChanged(
								new TreeModelEvent(this, 
										new Object[]{root, gw}, 
										MyTools.range(indexLow, indexHigh),
										gw.getQuestions().subList(indexLow, indexHigh).toArray()));*/
						return;
					} else if(atIndex >= 0 && atIndex <= gw.getQuestionCount()){
						gw.removeQuestion(mq);
						MetaGroup parent = (MetaGroup)toParent;
						if(atIndex == gw.getQuestionCount()) {
							parent.addQuestion(mq);
						} else {
							parent.addQuestion(mq, atIndex);
						}
						
						fireTreeNodesChanged(new TreeModelEvent(this, 
								new Object[]{root}, 
								MyTools.range(0, groups.size()-1), 
								groups.toArray()));
						return;
					}
				}
			}
		}
		throw new Exception("Couldn't move this to that");
	}
	
	public void removeChild(Object child) throws Exception { //TODO: specialize exception
		if(child instanceof MetaGroup && groups.contains(child)) {
			groups.remove(child);
			fireTreeNodesRemoved(new TreeModelEvent(this, new Object[]{root}));
			return;
		} else if(child instanceof MetaQuestion) {
			MetaQuestion mq = (MetaQuestion)child;
			for(MetaGroup gw: groups) {
				if(gw.getQuestions().contains(mq)) {
					int index = gw.getQuestions().indexOf(mq);
					gw.removeQuestion(mq);
					
					fireTreeNodesRemoved(new TreeModelEvent(this, 
							new Object[]{root, gw},
							new int[]{index},
							new Object[]{child}));
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
		} else if(parent instanceof MetaGroup) {
			MetaGroup p = (MetaGroup)parent;
			return p.getQuestionCount();
		} else if (parent instanceof MetaQuestion) {
			return 0;
		}
		return 0;
	}
	
	@Override
	public int getIndexOfChild(Object parent, Object child) {
		if(parent == root) {
			return groups.indexOf(child);
		} else if(parent instanceof MetaGroup) {
			MetaGroup p = (MetaGroup)parent;
			if(groups.contains(p)) {
				return p.getQuestions().indexOf(child);				
			} else return -1;
		} else if (parent instanceof MetaQuestion) {
			return -1;
		}
		return -1;
	}

	@Override
	public boolean isLeaf(Object node) {
		if(node == root || node instanceof MetaGroup) {
			return false;
		} else if(node instanceof MetaQuestion) {
			return true;
		}
		return true;
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		// TODO Should this do anything?!?
	}

	@Override
	public void addTreeModelListener(TreeModelListener l) {
		System.out.println("TreeModelListener added");
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
		for(TreeModelListener tml: listeners.getListeners(TreeModelListener.class)) {
			tml.treeNodesChanged(e);
		}
		//fireTreeStructureChanged(e);
	}
	
	private void fireTreeNodesInserted(TreeModelEvent e) {
		for(TreeModelListener tml: listeners.getListeners(TreeModelListener.class)) {
			tml.treeNodesInserted(e);
		}
	}
	
	private void fireTreeNodesRemoved(TreeModelEvent e) {
		for(TreeModelListener tml: listeners.getListeners(TreeModelListener.class)) {
			tml.treeNodesRemoved(e);
		}
	}
	
	@SuppressWarnings("unused")
	private void fireTreeStructureChanged(TreeModelEvent e) {
		TreeModelEvent e2 = new TreeModelEvent(this, new Object[]{root});
		for(TreeModelListener tml: listeners.getListeners(TreeModelListener.class)) {
			tml.treeStructureChanged(e2);
		}
	}
	
	public String prettyPrint(Locale locale) {
		StringBuilder sb = new StringBuilder();
		for(MetaGroup mg: groups) {
			sb.append(mg.getTextRef().get(locale) + "\n");
			for(MetaQuestion mq: mg.getQuestions()) {
				sb.append("\t " + mq.getFullTextRef().get(locale) + "\n");
			}
		}
		return sb.toString();
	}
}