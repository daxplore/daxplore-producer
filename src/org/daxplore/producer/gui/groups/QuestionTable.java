package org.daxplore.producer.gui.groups;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;
import org.daxplore.producer.gui.widget.QuestionWidget;

import com.google.common.eventbus.EventBus;

@SuppressWarnings("serial")
public class QuestionTable extends JTable {
	
	protected int mouseOver;
	
    private Color listBackground = new Color(255,255,255);
    private Color listSelectionBackground = new Color(200, 200, 255);
	
	public QuestionTable(EventBus eventBus, TableModel model) {
		super(model);
		setTableHeader(null);
		
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        mouseOver = -1;
        
        QuestionCellRenderer questionCellRenderer = new QuestionCellRenderer(eventBus);
        setDefaultRenderer(MetaQuestion.class, questionCellRenderer);
        setDefaultEditor(MetaQuestion.class, questionCellRenderer);
        //TODO should we have to create a QuestionWidget here?
        setRowHeight(new QuestionWidget(eventBus).getPreferredSize().height);
        
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
			public void mouseMoved(MouseEvent e) {
            	mouseOver = rowAtPoint(new Point(e.getX(), e.getY()));
                repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
			public void mouseExited(MouseEvent e) {
                mouseOver = -1;
                repaint();
            }
        });
	}
	
	class QuestionCellRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {

		private QuestionWidget qwRenderer;
		private QuestionWidget qwEditor;
		
		public QuestionCellRenderer(EventBus eventBus) {
			qwRenderer = new QuestionWidget(eventBus);
			qwEditor = new QuestionWidget(eventBus);
		}
		
	    @Override
	    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		    if(value instanceof MetaQuestion) {
		    	qwRenderer.setContent((MetaQuestion)value);
		    	Color bgColor = null;
			    if (row == mouseOver) {
			        qwRenderer.showEdit(true);
			        if(!isSelected) {
			        	bgColor = new Color(200,200,255);
			        } else {
			        	bgColor = new Color(175,175,255);
			        }
			    } else {
			    	qwRenderer.showEdit(false);
			        if(isSelected) {
			            bgColor = listSelectionBackground;
			        } else {
			            bgColor = listBackground;
			        }
			    }
			    qwRenderer.setBackground(bgColor);
			    
		    	Component[] children = qwRenderer.getComponents();
		    	for (int ii = 0; (children != null) && (ii < children.length); ii++) {
		    		children[ii].setBackground(bgColor);
		    	}
			    return qwRenderer;
		    }
	    	System.out.println("not renderer MetaQuestion");
	    	return null;
		}
	    
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		    if(value instanceof MetaQuestion) {
		    	qwEditor.setContent((MetaQuestion)value);
		    	Color bgColor = null;
			    if (row == mouseOver) {
			    	qwEditor.showEdit(true);
			        if(!isSelected) {
			        	bgColor = new Color(150,150,255);
			        } else {
			        	bgColor = new Color(175,175,255);
			        }
			    } else {
			    	qwEditor.showEdit(false);
			        if(isSelected) {
			            bgColor = listSelectionBackground;
			        } else {
			            bgColor = listBackground;
			        }
			    }
			    qwEditor.setBackground(bgColor);

			    Component[] children = qwEditor.getComponents();
		    	for (int ii = 0; (children != null) && (ii < children.length); ii++) {
		    		children[ii].setBackground(bgColor);
		    	}
			    return qwEditor;
		    }
	    	System.out.println("not editor MetaQuestion");
	    	return null;
		}
		

		@Override
		public Object getCellEditorValue() {
			return qwEditor.getContent();
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
