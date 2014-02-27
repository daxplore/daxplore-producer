/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui.view.build;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;
import org.daxplore.producer.gui.event.EditQuestionEvent;
import org.daxplore.producer.gui.event.EmptyEvents.ReloadTextsEvent;
import org.daxplore.producer.gui.resources.Colors;
import org.daxplore.producer.gui.widget.QuestionWidget;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

@SuppressWarnings("serial")
public class QuestionTable extends JTable {
	
	protected int mouseOver;
	private AbstractTableModel model;
	
	public QuestionTable(final EventBus eventBus, final AbstractTableModel model) {
		super(model);
		this.model = model;
		
		eventBus.register(this);
		setTableHeader(null);
		
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        mouseOver = -1;
        
        setDefaultRenderer(MetaQuestion.class, new QuestionCellRenderer(eventBus));
        //TODO should we have to create a QuestionWidget here?
        setRowHeight(new QuestionWidget(eventBus).getPreferredSize().height);
        
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
			public void mouseMoved(MouseEvent e) {
            	mouseOver = rowAtPoint(e.getPoint());
                repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
        	/**
        	 * Stop showing mouse over highlighting when mouse exits
        	 */
            @Override
			public void mouseExited(MouseEvent e) {
                mouseOver = -1;
                repaint();
            }
            
            /**
             * Edit a variable that is double clicked
             */
            @Override
            public void mousePressed(MouseEvent e) {
            	if(e.getClickCount() == 2) {
            		int row = rowAtPoint(e.getPoint());
            		Object clickedObject = model.getValueAt(row, 0);
            		if(clickedObject instanceof MetaQuestion) {
            			eventBus.post(new EditQuestionEvent((MetaQuestion)clickedObject));
            		}
            	}
            }
        });
	}
	
	@Subscribe
	public void on(ReloadTextsEvent e) {
		int[] selected = getSelectedRows();
		model.fireTableDataChanged();
		for(int i=0; i<selected.length; i++) {
			addRowSelectionInterval(selected[i], selected[i]);
		}
	}
	
	class QuestionCellRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {

		private QuestionWidget qwRenderer;
		private QuestionWidget qwEditor;
		private Border focusBorder = BorderFactory.createLineBorder(Colors.listFocusBorder, 1);
		private Border nonFocusBorder = new EmptyBorder(1, 1, 1, 1);
		
		public QuestionCellRenderer(EventBus eventBus) {
			qwRenderer = new QuestionWidget(eventBus);
			qwEditor = new QuestionWidget(eventBus);
		}
		
	    @Override
	    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		    if(value instanceof MetaQuestion) {
		    	qwRenderer.setContent((MetaQuestion)value);
		    	Color bgColor = Colors.getRowColor(isSelected, row == mouseOver, row%2 == 0);
			    qwRenderer.setBackground(bgColor);
			    if(hasFocus) {
			    	qwRenderer.setBorder(focusBorder);
			    } else {
			    	qwRenderer.setBorder(nonFocusBorder);
			    }
			    
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
			return qwEditor.getContent();
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
