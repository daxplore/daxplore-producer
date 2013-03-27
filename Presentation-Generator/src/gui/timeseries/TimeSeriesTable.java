package gui.timeseries;

import gui.widget.AbstractWidgetEditor.InvalidContentException;
import gui.widget.TextWidget;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
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

import daxplorelib.DaxploreException;
import daxplorelib.metadata.textreference.TextReference;

@SuppressWarnings("serial")
public class TimeSeriesTable extends JTable {
protected int mouseOver;
	
    static Color listBackground, listSelectionBackground;
    static {
        listBackground = new Color(255,255,255);
        listSelectionBackground = new Color(200, 200, 255);
    }
	
	public TimeSeriesTable(TableModel model) throws DaxploreException {
		super(model);
		this.setTableHeader(null);
		
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mouseOver = -1;
        
        TimePointCellRenderer timePointCellRenderer = new TimePointCellRenderer();
        setDefaultRenderer(TextReference.class, timePointCellRenderer);
        setDefaultEditor(TextReference.class, timePointCellRenderer);
        setRowHeight(new TextWidget().getPreferredSize().height);
        
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
            	mouseOver = rowAtPoint(new Point(e.getX(), e.getY()));
                repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            public void mouseExited(MouseEvent e) {
                mouseOver = -1;
                repaint();
            }
        });
	}
	
	class TimePointCellRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {

		private TextWidget textRefRenderer = new TextWidget();
		private TextWidget textRefEditor = new TextWidget();
		
	    @Override
	    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		    if(value instanceof TextReference) {
		    	textRefRenderer.setContent((TextReference)value);
		    	Color bgColor = null;
			    if (row == mouseOver) {
			    	textRefRenderer.showEdit(true);
			        if(!isSelected) {
			        	bgColor = new Color(255,255,220);
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
			    if (value instanceof Container) {
			    	Component[] children = ((Container) value).getComponents();
			    	for (int ii = 0; (children != null) && (ii > children.length); ii++) {
			    		children[ii].setBackground(bgColor);
			    	}
			    }
			    return textRefRenderer;
		    } else {
		    	return null; // TODO ?
		    }
		}
	    
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		    if(value instanceof TextReference) {
		    	textRefEditor.setContent((TextReference)value);
		    	Color bgColor = null;
			    if (row == mouseOver) {
			    	textRefEditor.showEdit(true);
			        if(!isSelected) {
			        	bgColor = new Color(255,255,220);
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
			    if (value instanceof Container) {
			    	Component[] children = ((Container) value).getComponents();
			    	for (int ii = 0; (children != null) && (ii > children.length); ii++) {
			    		children[ii].setBackground(bgColor);
			    	}
			    }
			    return textRefEditor;
		    } else {
		    	return null; //TODO ?
		    }
		}
		

		@Override
		public Object getCellEditorValue() {
			try {
				return textRefRenderer.getContent();
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
