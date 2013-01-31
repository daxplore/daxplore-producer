package gui.question;

import gui.widget.QuestionWidget;
import gui.widget.ScaleEditorWidget;
import gui.widget.ScaleRendererWidget;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import daxplorelib.metadata.MetaQuestion;
import daxplorelib.metadata.MetaScale;

@SuppressWarnings("serial")
public class ScaleTable extends JTable {
	
    static Color listBackground, listSelectionBackground;
    static {
        UIDefaults uid = UIManager.getLookAndFeel().getDefaults();
        listBackground = new Color(255,255,255);
        listSelectionBackground = new Color(200, 200, 255);
    }
	
	private int mouseOver;

	public ScaleTable(ScaleTableModel model) {
		super(model);
		this.setTableHeader(null);
		
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mouseOver = -1;
        
        ScaleCellRenderer questionCellRenderer = new ScaleCellRenderer();
        setDefaultRenderer(MetaScale.Option.class, questionCellRenderer);
        setDefaultEditor(MetaScale.Option.class, questionCellRenderer);
        setRowHeight(new QuestionWidget().getPreferredSize().height);
        
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
	
	/*
    @Override
    public boolean editCellAt(int row, int column, EventObject e) {
        boolean result = super.editCellAt(row, column, e);
        if (result) {
            // adjust cell bounds of the editing component
            Rectangle bounds = getEditorComponent().getBounds();
            bounds.height = getEditorComponent().getPreferredSize().height;
            getEditorComponent().setBounds(bounds);
        }
        return result;
    }

    @Override
    public void removeEditor() {
        int editingColumn = getEditingColumn();
        super.removeEditor();
        if (editingColumn >= 0) {
            // cleanup
            repaint();
        }
    }
    */
	
	class ScaleCellRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {
		
		private ScaleRendererWidget renderer = new ScaleRendererWidget();
		private ScaleEditorWidget editor = new ScaleEditorWidget();
		
		private List<List<Integer>> rowColHeight = new ArrayList<List<Integer>>();
		
	    @Override
	    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	    	if(value instanceof MetaScale.Option) {
		    	renderer.setOption((MetaScale.Option)value);
		    	Color bgColor = null;
			    if (row == mouseOver) {
			        if(!isSelected) {
			        	bgColor = new Color(255,255,220);
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
			    renderer.setBackground(bgColor);
			    if (value instanceof Container) {
			    	Component[] children = ((Container) value).getComponents();
			    	for (int ii = 0; (children != null) && (ii > children.length); ii++) {
			    		children[ii].setBackground(bgColor);
			    	}
			    }
			    table.setRowHeight(row, renderer.getPreferredSize().height);
			    return renderer;
		    } else {
		    	System.out.println("not renderer MetaQuestion");
		    	return null;
		    }
	    }
		
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		    if(value instanceof MetaScale.Option) {
		    	editor.setOption((MetaScale.Option)value);
		    	Color bgColor = null;
			    if (row == mouseOver) {
			        if(!isSelected) {
			        	bgColor = new Color(255,255,220);
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
			    editor.setBackground(bgColor);
			    if (value instanceof Container) {
			    	Component[] children = ((Container) value).getComponents();
			    	for (int ii = 0; (children != null) && (ii > children.length); ii++) {
			    		children[ii].setBackground(bgColor);
			    	}
			    }
			    table.setRowHeight(row, editor.getPreferredSize().height);
			    return editor;
		    } else {
		    	System.out.println("not editor MetaQuestion");
		    	return null;
		    }
		}
	    
	    /**
	     * Calculate the new preferred height for a given row, and sets the height on the table.
	     */
	    private void adjustRowHeight(JTable table, int row, int column) {
	      //The trick to get this to work properly is to set the width of the column to the 
	      //textarea. The reason for this is that getPreferredSize(), without a width tries 
	      //to place all the text in one line. By setting the size with the with of the column, 
	      //getPreferredSize() returnes the proper height which the row should have in
	      //order to make room for the text.
	      int cWidth = 300; //table.getTableHeader().getColumnModel().getColumn(column).getWidth();
	      setSize(new Dimension(cWidth, 1000));
	      int prefH = getPreferredSize().height;
	      while (rowColHeight.size() <= row) {
	        rowColHeight.add(new ArrayList<Integer>(column));
	      }
	      List<Integer> colHeights = rowColHeight.get(row);
	      while (colHeights.size() <= column) {
	        colHeights.add(0);
	      }
	      colHeights.set(column, prefH);
	      int maxH = prefH;
	      for (Integer colHeight : colHeights) {
	        if (colHeight > maxH) {
	          maxH = colHeight;
	        }
	      }
	      if (table.getRowHeight(row) != maxH) {
	        table.setRowHeight(row, maxH);
	      }
	    }
		
		@Override
		public Object getCellEditorValue() {
			return editor.getOption();
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
