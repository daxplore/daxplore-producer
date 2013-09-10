package org.daxplore.producer.gui.groups;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.util.EventObject;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractCellEditor;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;

import org.daxplore.producer.daxplorelib.metadata.MetaGroup;
import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;
import org.daxplore.producer.gui.widget.AbstractWidgetEditor;
import org.daxplore.producer.gui.widget.AbstractWidgetEditor.InvalidContentException;
import org.daxplore.producer.gui.widget.GroupEditor;
import org.daxplore.producer.gui.widget.GroupRenderer;
import org.daxplore.producer.gui.widget.QuestionWidget;

@SuppressWarnings("serial")
public class GroupTree extends JTree {
	
    static Color listBackground, listSelectionBackground;
    static {
        listBackground = new Color(255,255,255);
        listSelectionBackground = new Color(200, 200, 255);
    }
    
    public GroupTree(GroupTreeModel groupTreeModel) {
    	super(groupTreeModel);
    	//System.out.println("Pretty print group tree");
    	//System.out.println(groupTreeModel.prettyPrint(new Locale("sv")));
    	setRootVisible(false);
    	GroupTreeCellRendererEditor groupTreeCellRendererEditor = new GroupTreeCellRendererEditor();
    	setCellRenderer(groupTreeCellRendererEditor);
    	setCellEditor(groupTreeCellRendererEditor);
    	setEditable(true);
    }
	
	class GroupTreeCellRendererEditor extends AbstractCellEditor implements TreeCellRenderer, TreeCellEditor {
		
		private QuestionWidget questionRenderer = new QuestionWidget();
		private GroupRenderer groupRenderer = new GroupRenderer();
		private AbstractWidgetEditor<?> editor;
		
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			Container container;
			if(value instanceof MetaQuestion) {
				MetaQuestion mq = (MetaQuestion)value;
				questionRenderer.setContent(mq);
				container = questionRenderer;
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
			Color bgColor = null;
		    if (row == mouseOver) {
		        if(!selected) {
		        	bgColor = new Color(255,255,220);
		        } else {
		        	bgColor = new Color(175,175,255);
		        }
		    } else {
		        if(selected) {
		            bgColor = listSelectionBackground;
		        } else {
		            bgColor = listBackground;
		        }
		    }
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
				QuestionWidget qEdit = new QuestionWidget((MetaQuestion)value);
				editor = qEdit;
			} else if(value instanceof MetaGroup) {
				GroupEditor gEdit = new GroupEditor();
				gEdit.setContent((MetaGroup)value);
				editor = gEdit;
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
	        return true;
	    }
	    
	    @Override
	    public boolean shouldSelectCell(EventObject anEvent) {
	        return true;
	    }
		
	}
}
