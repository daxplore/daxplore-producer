package org.daxplore.producer.gui.view.build;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractCellEditor;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import org.daxplore.producer.daxplorelib.metadata.MetaGroup;
import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;
import org.daxplore.producer.gui.resources.Colors;
import org.daxplore.producer.gui.widget.AbstractWidgetEditor;
import org.daxplore.producer.gui.widget.AbstractWidgetEditor.InvalidContentException;
import org.daxplore.producer.gui.widget.GroupEditor;
import org.daxplore.producer.gui.widget.GroupRenderer;
import org.daxplore.producer.gui.widget.QuestionWidget;

import com.google.common.eventbus.EventBus;

@SuppressWarnings("serial")
public class GroupTree extends JTree {
	
	private Color listBackground = new Color(255,255,255);
	private Color listSelectionBackground = new Color(200, 200, 255);
    
    public GroupTree(EventBus eventBus, GroupTreeModel groupTreeModel) {
    	super(groupTreeModel);
    	setRootVisible(false);
    	GroupTreeCellRendererEditor groupTreeCellRendererEditor = new GroupTreeCellRendererEditor(eventBus);
    	setCellRenderer(groupTreeCellRendererEditor);
    	setCellEditor(groupTreeCellRendererEditor);
    	setUI(new GroupTreeUI());
    	setEditable(true);
    }
	
	class GroupTreeCellRendererEditor extends AbstractCellEditor implements TreeCellRenderer, TreeCellEditor {
		
		private EventBus eventBus;
		private QuestionWidget questionWidget;
		private GroupRenderer groupRenderer;
		private GroupEditor groupEditor;
		private AbstractWidgetEditor<?> editor;
		
		public GroupTreeCellRendererEditor(EventBus eventBus) {
			this.eventBus = eventBus;
			groupRenderer  = new GroupRenderer(eventBus);
			groupEditor = new GroupEditor(eventBus);
			questionWidget = new QuestionWidget(eventBus, true);
		}
		
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			Container container;
			if(value instanceof MetaQuestion) {
				MetaQuestion mq = (MetaQuestion)value;
				questionWidget.setContent(mq);
				container = questionWidget;
			} else if(value instanceof MetaGroup) {
				MetaGroup mg = (MetaGroup)value;
				groupRenderer.setContent(mg);
				container = groupRenderer;
			} else if(value instanceof JPanel){
				return (JPanel)value; //TODO why is it trying to display the root?
			} else {
				Logger.getGlobal().log(Level.SEVERE, "Tried showing " + value.getClass() + " in grouptree");
				throw new AssertionError("Tried showing " + value.getClass() + " in grouptree");
			}
			
			int mouseOver = -1; //TODO fix broken mouse over
			Color bgColor = Colors.getRowColor(selected, false, row%2==0);
		    container.setBackground(bgColor);
		    
		    Component[] children = container.getComponents();
	    	for (int ii = 0; (children != null) && (ii < children.length); ii++) {
	    		children[ii].setBackground(bgColor);
	    	}
	    	
			return container;
		}

		@Override
		public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
			if(value instanceof MetaQuestion) {
				questionWidget.setContent((MetaQuestion)value);
				editor = questionWidget;
			} else if(value instanceof MetaGroup) {
				groupEditor.setContent((MetaGroup)value);
				editor = groupEditor;
			} else {
				throw new AssertionError(); 
			}
			
			Color bgColor = null;
			if(isSelected) {
	            bgColor = listSelectionBackground;
	        } else {
	            bgColor = listBackground;
	        }
			
			editor.setBackground(bgColor);
		    
		    Component[] children = editor.getComponents();
	    	for (int ii = 0; (children != null) && (ii < children.length); ii++) {
	    		children[ii].setBackground(bgColor);
	    	}
			
			return editor;
		}
		
		@Override
		public Object getCellEditorValue() {
			try {
				return editor.getContent();
			} catch (InvalidContentException e) {
				return null;
			}
		}

	    @Override
	    public boolean isCellEditable(EventObject anEvent) {
	    	if (anEvent instanceof MouseEvent && anEvent.getSource() instanceof JTree) {
	    		MouseEvent e = (MouseEvent)anEvent;
	    		JTree tree = (JTree)anEvent.getSource();
	    		TreePath path = tree.getPathForLocation(e.getX(),e.getY());
	    		if(path.getLastPathComponent() instanceof MetaGroup) {
	    			return true;
	    		}
	    	}
	    	return false;
	    }
	    
	    @Override
	    public boolean shouldSelectCell(EventObject anEvent) {
	        return true;
	    }
		
	}
	
	class GroupTreeUI extends BasicTreeUI {
		
		@Override
		protected void paintRow( Graphics g, Rectangle clipBounds, Insets insets, Rectangle bounds, TreePath path, int row, boolean isExpanded, boolean hasBeenExpanded, boolean isLeaf) {
			Graphics gg = g.create();
			Color bgColor = Colors.getRowColor(tree.isRowSelected(row), false, row%2==0);
			gg.setColor(bgColor);
			gg.fillRect(0, bounds.y, tree.getWidth(), bounds.height);
			gg.dispose();

			if(path.getLastPathComponent() instanceof MetaGroup) {
				int arrowX = bounds.x - getRightChildIndent() + 1;
				int arrowY = bounds.y + (bounds.height / 2);
				Icon expandIcon;
				if (isExpanded) {
					expandIcon = getExpandedIcon();
				} else {
					expandIcon = getCollapsedIcon();
				}
				drawCentered(tree, g, expandIcon, arrowX, arrowY);
			}			
			super.paintRow(g, clipBounds, insets, bounds, path, row, isExpanded, hasBeenExpanded, isLeaf);
		}
		
	}
}
