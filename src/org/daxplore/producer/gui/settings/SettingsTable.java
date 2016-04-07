package org.daxplore.producer.gui.settings;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import jdk.internal.dynalink.DefaultBootstrapper;

import org.daxplore.producer.gui.resources.Colors;
import org.jdesktop.swingx.JXTable;


public class SettingsTable extends JXTable {

	protected int mouseOver;
	@SuppressWarnings("rawtypes")
	private Class editingClass;
	
	public SettingsTable(final AbstractTableModel model) {
		super(model);
				
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		mouseOver = -1;
		
		/*((JComponent)getDefaultRenderer(Boolean.class)).setOpaque(true);
		DefaultCellEditor a = ((DefaultCellEditor)getDefaultEditor(Boolean.class));
		((JComponent)a.getComponent()).setOpaque(true);*/
		
		
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
        });
        
	}
	
	/*private boolean hasFocus(int row, int column) {
        boolean rowIsLead =
            (selectionModel.getLeadSelectionIndex() == row);
        boolean colIsLead =
            (columnModel.getSelectionModel().getLeadSelectionIndex() == column);

        return (rowIsLead && colIsLead) && isFocusOwner();
	}*/
	
	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
		Component component = super.prepareRenderer(renderer, row, column);
		Color bgColor = Colors.getRowColor(isRowSelected(row), row == mouseOver, row %2 == 0);
		component.setBackground(bgColor);
		//System.out.println(component.getClass());
		if(component instanceof Container) {
			Component[] children = ((Container)component).getComponents();
			for (int ii = 0; (children != null) && (ii < children.length); ii++) {
				children[ii].setBackground(bgColor);
			}
		}
		//if(hasFocus()) {} else {}
		return component;
	}
	
	@Override
	public Component prepareEditor(TableCellEditor editor, int row, int column) {
		Component component = super.prepareEditor(editor, row, column);
		Color bgColor = Colors.getRowColor(isRowSelected(row), row == mouseOver, row %2 == 0);
		component.setBackground(bgColor);
		if(component instanceof Container) {
			Component[] children = ((Container)component).getComponents();
			for (int ii = 0; (children != null) && (ii < children.length); ii++) {
				children[ii].setBackground(bgColor);
			}
		}
		//if(hasFocus()) {} else {}
		return component;
	}
	
	
	@Override
	public TableCellRenderer getCellRenderer(int row, int column) {
		editingClass = null;
		TableCellRenderer renderer = super.getCellRenderer(row, column);
		int modelColumn = convertColumnIndexToModel(column);
		if (modelColumn == 1) {
			editingClass = getModel().getValueAt(row, modelColumn).getClass();
		    renderer = getDefaultRenderer(editingClass);
		}
		return renderer;
	}
	
	@Override
    public TableCellEditor getCellEditor(int row, int column) {
		editingClass = null;
		TableCellEditor editor = super.getCellEditor(row, column);
		int modelColumn = convertColumnIndexToModel(column);
		if (modelColumn == 1) {
			editingClass = getModel().getValueAt(row, modelColumn).getClass();
		    editor = getDefaultEditor(editingClass);
		}
		return editor;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Class getColumnClass(int column) {
		return editingClass != null ? editingClass : super.getColumnClass(column);
	}
}
