/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui.view.question;

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
		
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
		
		public QuestionCellRenderer(EventBus eventBus) {
			qwRenderer = new QuestionWidget(eventBus);
		}
		
	    @Override
	    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		    if(value instanceof MetaQuestion) {
		    	qwRenderer.setContent((MetaQuestion)value);
		    	Color bgColor = null;
			    if (row == mouseOver) {
			        if(!isSelected) {
			        	bgColor = new Color(200,200,255);
			        } else {
			        	bgColor = new Color(175,175,255);
			        }
			    } else {
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
	    	return null;
		}
		

		@Override
		public Object getCellEditorValue() {
			return qwRenderer.getContent();
		}

	    @Override
	    public boolean isCellEditable(EventObject anEvent) {
	        return false;
	    }
	    
	    @Override
	    public boolean shouldSelectCell(EventObject anEvent) {
	        return true;
	    }
	}
}
