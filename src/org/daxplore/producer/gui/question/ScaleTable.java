package org.daxplore.producer.gui.question;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;
import org.daxplore.producer.gui.widget.AbstractWidget;
import org.daxplore.producer.gui.widget.AbstractWidgetEditor;
import org.daxplore.producer.gui.widget.AbstractWidgetEditor.InvalidContentException;
import org.daxplore.producer.gui.widget.NumberLineEditor;
import org.daxplore.producer.gui.widget.NumberLineRenderer;
import org.daxplore.producer.gui.widget.QuestionWidget;
import org.daxplore.producer.gui.widget.TextWidget;
import org.daxplore.producer.tools.NumberlineCoverage;

@SuppressWarnings("serial")
public class ScaleTable extends JTable {
	
    static Color listBackground, listSelectionBackground;
    static {
        listBackground = new Color(255,255,255);
        listSelectionBackground = new Color(200, 200, 255);
    }
	
	private int mouseOverRow;
	private int mouseOverColumn;

	public ScaleTable(ScaleTableModel model) {
		super(model);
		//this.setTableHeader(null);
		
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mouseOverRow = -1;
        mouseOverColumn = -1;
        
        ScaleCellRenderer cellRenderer = new ScaleCellRenderer();
        setDefaultRenderer(TextReference.class, cellRenderer);
        setDefaultEditor(TextReference.class, cellRenderer);
        setDefaultRenderer(NumberlineCoverage.class, cellRenderer);
        setDefaultEditor(NumberlineCoverage.class, cellRenderer);
        setRowHeight(new QuestionWidget().getPreferredSize().height);
        
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
            	mouseOverRow = rowAtPoint(e.getPoint());
            	mouseOverColumn = columnAtPoint(e.getPoint());
                repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            public void mouseExited(MouseEvent e) {
                mouseOverRow = -1;
                mouseOverColumn = -1;
                repaint();
            }
        });
	}
	
	class ScaleCellRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {
		
		private AbstractWidgetEditor<?> editor;
		private AbstractWidget<?> renderer;
		
		private TextWidget textRenderer = new TextWidget();
		private TextWidget textEditor = new TextWidget();
		
		private NumberLineRenderer numberRenderer = new NumberLineRenderer();
		private NumberLineEditor numberEditor = new NumberLineEditor();
		
	    @Override
	    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	    	if(value instanceof TextReference) {
	    		textRenderer.setContent((TextReference)value);
	    		renderer = textRenderer;
	    	} else if(value instanceof NumberlineCoverage) {
	    		numberRenderer.setContent((NumberlineCoverage)value);
	    		renderer = numberRenderer;
	    	} else {
	    		return null;
	    	}
	    	
	    	Color bgColor = null;
		    if (row == mouseOverRow && column == mouseOverColumn) {
		        if(isSelected) {
		        	bgColor = new Color(175,175,255);
		        } else {
		        	bgColor = new Color(255,255,220);
		        }
		    } else {
		        if(isSelected) {
		            bgColor = listSelectionBackground;
		        } else {
		            bgColor = listBackground;
		        }
		    }

		    renderer.setBackground(bgColor);
		    if (value instanceof Container) {
		    	Component[] children = ((Container) value).getComponents();
		    	for (int ii = 0; (children != null) && (ii > children.length); ii++) {
		    		children[ii].setBackground(bgColor);
		    	}
		    }
		    //table.setRowHeight(row, renderer.getPreferredSize().height);
		    return renderer;
	    }
		
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
	    	if(value instanceof TextReference) {
	    		textEditor.setContent((TextReference)value);
	    		editor = textEditor;
	    	} else if(value instanceof NumberlineCoverage) {
	    		numberEditor.setContent((NumberlineCoverage)value);
	    		editor = numberEditor;
	    	} else {
	    		return null;
	    	}
			
	    	Color bgColor = null;
		    if (row == mouseOverRow && column == mouseOverColumn) {
		        if(isSelected) {
		        	bgColor = new Color(175,175,255);
		        } else {
		        	bgColor = new Color(255,255,220);
		        }
		    } else {
		        if(isSelected) {
		            bgColor = listSelectionBackground;
		        } else {
		            bgColor = listBackground;
		        }
		    }
		    editor.setBackground(bgColor);
		    if (value instanceof Container) {
		    	Component[] children = ((Container) value).getComponents();
		    	for (int ii = 0; (children != null) && (ii > children.length); ii++) {
		    		children[ii].setBackground(bgColor);
		    	}
		    }
		    //table.setRowHeight(row, editor.getPreferredSize().height);
		    return editor;
		}
		
		@Override
		public Object getCellEditorValue() {
			try {
				return editor.getContent();
			} catch (InvalidContentException|NullPointerException e) {
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
