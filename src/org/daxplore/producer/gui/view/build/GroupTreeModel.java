/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui.view.build;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.swing.JPanel;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.metadata.MetaGroup;
import org.daxplore.producer.daxplorelib.metadata.MetaGroup.GroupType;
import org.daxplore.producer.daxplorelib.metadata.MetaGroup.MetaGroupManager;
import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;

class GroupTreeModel implements TreeModel {

	private JPanel root = new JPanel();
	private List<MetaGroup> groups = new LinkedList<>();
	private MetaGroupManager manager;
	
	private EventListenerList listeners = new EventListenerList();
	
	public GroupTreeModel(MetaGroupManager metaGroupManager) throws DaxploreException {
		manager = metaGroupManager;
		List<MetaGroup> allGroups = metaGroupManager.getAll();
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
			}
		} else if(parent instanceof MetaGroup) {
			MetaGroup p = (MetaGroup)parent;
			if(index < p.getQuestionCount()) {
				return p.getQuestion(index);
			}
		}
		return null;
	}
	/**
	 * Add a new group to the tree
	 * @param The MetaGroup to add
	 * @param The index at witch to insert the new group
	 * @return The TreePath to the new group
	 */
	public TreePath addGroup(MetaGroup mg, int atIndex) throws IndexOutOfBoundsException {
		if(atIndex >= 0 && atIndex <= groups.size()) {
			groups.add(atIndex, mg);
			mg.setIndex(atIndex);
			fireTreeStructureChanged(null);
			return new TreePath(new Object[]{root, mg});
		}
		throw new IndexOutOfBoundsException("Tried to add a group with an invalid index");
	}
	/**
	 * Add a new question to the tree
	 * @param The MetaQuestion to add
	 * @param The group to which the question will be added
	 * @param The index at which the question will be added
	 * @return The TreePath to the added question
	 */
	public TreePath addQuestion(MetaQuestion mq, MetaGroup parent, int atIndex) throws IllegalArgumentException, IndexOutOfBoundsException {
		if(containsQuestion(mq)) {
			throw new IllegalArgumentException("Can't add duplicate of MetaQuestion: '" + mq.getId() + "'");
		}
		
		if(groups.contains(parent) && atIndex >= 0 && atIndex <= getChildCount(parent)) {
			parent.addQuestion(mq, atIndex);
			fireTreeNodesInserted(new TreeModelEvent(this, 
					new Object[]{root,parent},
					new int[]{atIndex},
					new Object[]{mq}));
			return new TreePath(new Object[]{root, parent, mq});
		}
		throw new IndexOutOfBoundsException("Index out of bounds: '" + atIndex);
	}
	
	public boolean containsQuestion(MetaQuestion mq) {
		for(MetaGroup mg : groups){
			if(mg.getQuestions().contains(mq)) {
				return true;
			}
		}
		return false;
	}
	
	public MetaGroup getGroupFor(MetaQuestion metaQuestion) {
		for(MetaGroup mg : groups){
			if(mg.getQuestions().contains(metaQuestion)) {
				return mg;
			}
		}
		return null;
	}
	
	public void moveChild(Object child, Object toParent, int atIndex) throws IllegalArgumentException {
		if(child instanceof MetaGroup && toParent == root && groups.contains(child)
				&& atIndex >= 0 && atIndex < groups.size()) {
			groups.remove(child);
			groups.add(atIndex, (MetaGroup)child);
			for(int index = 0; index < groups.size(); index++) {
				groups.get(index).setIndex(index);
			}
			for(int i = 0; i < groups.size(); i++) {
				groups.get(i).setIndex(i);
			}
			fireTreeStructureChanged(null);
			return;
		} else if(child instanceof MetaQuestion && toParent instanceof MetaGroup && groups.contains(toParent)) {
			MetaQuestion mq = (MetaQuestion)child;

			removeChild(mq);
			addQuestion(mq, (MetaGroup)toParent, atIndex);
			return;

		}
		throw new IllegalArgumentException("Invalid move instruction");
	}
	
	public boolean removeChild(Object child) {
		if(child instanceof MetaGroup && groups.contains(child)) {
			MetaGroup mg = (MetaGroup)child;
			manager.remove(mg.getId());
			groups.remove(child);
			fireTreeStructureChanged(new TreeModelEvent(this, new Object[]{root}));
			//fireTreeNodesRemoved(new TreeModelEvent(this, new Object[]{root})); //TODO: get tree update function working properly
			return true;
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
					return true;
				}
			}
		}
		return false;
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
			}
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
		fireTreeNodesChanged(new TreeModelEvent(this, path));
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
		for(TreeModelListener tml: listeners.getListeners(TreeModelListener.class)) {
			tml.treeNodesChanged(e);
		}
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
	
	private void fireTreeStructureChanged(TreeModelEvent e) {
		TreeModelEvent e2 = new TreeModelEvent(this, new Object[]{root});
		for(TreeModelListener tml: listeners.getListeners(TreeModelListener.class)) {
			tml.treeStructureChanged(e2);
		}
	}
	
	public String prettyPrint(Locale locale) {
		StringBuilder sb = new StringBuilder();
		for(MetaGroup mg: groups) {
			sb.append(mg.getTextRef().getText(locale) + "\n");
			for(MetaQuestion mq: mg.getQuestions()) {
				sb.append("\t " + mq.getFullTextRef().getText(locale) + "\n");
			}
		}
		return sb.toString();
	}
}
