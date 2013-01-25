package gui.groups;

import gui.widget.GroupWidget;
import gui.widget.OurListWidget;
import gui.widget.QuestionWidget;

import java.awt.Color;
import java.awt.Component;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.event.CellEditorListener;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;

import daxplorelib.metadata.MetaGroup;
import daxplorelib.metadata.MetaQuestion;

@SuppressWarnings("serial")
public class GroupTree extends JTree {
	
    static Color listBackground, listSelectionBackground;
    static {
        UIDefaults uid = UIManager.getLookAndFeel().getDefaults();
        listBackground = new Color(255,255,255);
        listSelectionBackground = new Color(200, 200, 255);
    }
    
    public GroupTree(GroupTreeModel groupTreeModel) {
    	super(groupTreeModel);
    	setRootVisible(false);
    	GroupTreeCellRendererEditor groupTreeCellRendererEditor = new GroupTreeCellRendererEditor();
    	setCellRenderer(groupTreeCellRendererEditor);
    	setCellEditor(groupTreeCellRendererEditor);
    	setEditable(true);
    }
	
	class GroupTreeCellRendererEditor extends AbstractCellEditor implements TreeCellRenderer, TreeCellEditor {
		
		private QuestionWidget questionRenderer = new QuestionWidget();
		private GroupWidget groupRenderer = new GroupWidget();
		private JPanel editor;
		
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			Component comp;
			if(value instanceof MetaQuestion) {
				MetaQuestion mq = (MetaQuestion)value;
				questionRenderer.setMetaQuestion(mq);
				comp = questionRenderer;
			} else if(value instanceof MetaGroup) {
				MetaGroup mg = (MetaGroup)value;
				groupRenderer.setMetaGroup(mg);
				comp = groupRenderer;
			} else return new Component() {};
			
			int mouseOver = -1;
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
		    comp.setBackground(bgColor);
			return comp;
		}

		@Override
		public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
			if(value instanceof MetaQuestion) {
				QuestionWidget qEdit = new QuestionWidget((MetaQuestion)value);
				editor = qEdit;
			} else if(value instanceof MetaGroup) {
				GroupWidget gEdit = new GroupWidget();
				gEdit.setMetaGroup((MetaGroup)value, true);
				editor = gEdit;
			} else {
				editor = null;
			}
			return editor;
		}
		
		@Override
		public Object getCellEditorValue() {
			if(editor instanceof QuestionWidget) {
				return ((QuestionWidget)editor).getMetaQuestion();
			} else if(editor instanceof GroupWidget) {
				return ((GroupWidget)editor).getMetaGroup();
			}
			return null;
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
