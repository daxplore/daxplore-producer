package org.daxplore.producer.gui.timeseries;

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

import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;
import org.daxplore.producer.gui.widget.TextWidget;

import com.google.common.eventbus.EventBus;

@SuppressWarnings("serial")
public class TimeSeriesTable extends JTable {
	private EventBus eventBus;
	private int mouseOver;

    private Color listBackground = new Color(255,255,255);
    private Color listSelectionBackground = new Color(200, 200, 255);
	
	public TimeSeriesTable(EventBus eventBus, TableModel model) {
		super(model);
		this.eventBus = eventBus;
		this.setTableHeader(null);
		
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mouseOver = -1;
        
        TimePointCellRenderer timePointCellRenderer = new TimePointCellRenderer();
        setDefaultRenderer(TextReference.class, timePointCellRenderer);
        setDefaultEditor(TextReference.class, timePointCellRenderer);
      //TODO should we have to create a TextWidget here?
        setRowHeight(new TextWidget(eventBus).getPreferredSize().height);
        
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
	
	class TimePointCellRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {

		private TextWidget textRefRenderer;
		private TextWidget textRefEditor;
		
		public TimePointCellRenderer() {
			this.textRefRenderer = new TextWidget(eventBus);
			this.textRefEditor = new TextWidget(eventBus);
		}
		
	    @Override
	    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		    if(value instanceof TextReference) {
		    	textRefRenderer.setContent((TextReference)value);
		    	Color bgColor = null;
			    if (row == mouseOver) {
			    	textRefRenderer.showEdit(true);
			        if(!isSelected) {
			        	bgColor = new Color(200,200,255);
			        } else {
			        	bgColor = new Color(175,175,255);
			        }
			    } else {
			    	textRefRenderer.showEdit(false);
			        if(isSelected) {
			            bgColor = listSelectionBackground;
			        } else {
			            bgColor = listBackground;
			        }
			    }
			    textRefRenderer.setBackground(bgColor);
			    
		    	Component[] children = textRefRenderer.getComponents();
		    	for (int ii = 0; (children != null) && (ii < children.length); ii++) {
		    		children[ii].setBackground(bgColor);
		    	}
			    return textRefRenderer;
		    }
		    
		    return null; // TODO ?
		}
	    
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		    if(value instanceof TextReference) {
		    	textRefEditor.setContent((TextReference)value);
		    	Color bgColor = null;
			    if (row == mouseOver) {
			    	textRefEditor.showEdit(true);
			        if(!isSelected) {
			        	bgColor = new Color(200,200,255);
			        } else {
			        	bgColor = new Color(175,175,255);
			        }
			    } else {
			    	textRefEditor.showEdit(false);
			        if(isSelected) {
			            bgColor = listSelectionBackground;
			        } else {
			            bgColor = listBackground;
			        }
			    }
			    textRefEditor.setBackground(bgColor);
			    
		    	Component[] children = textRefEditor.getComponents();
		    	for (int ii = 0; (children != null) && (ii < children.length); ii++) {
		    		children[ii].setBackground(bgColor);
		    	}
			    return textRefEditor;
		    }
		    
	    	return null; //TODO ?
		}
		

		@Override
		public Object getCellEditorValue() {
			return textRefRenderer.getContent();
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
