package org.daxplore.producer.gui.view.variable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.daxplore.producer.gui.resources.Colors;
import org.jdesktop.swingx.JXTable;

public class MeanExcludedTable extends JXTable {

	protected int mouseOver;
	@SuppressWarnings("rawtypes")
	private Class editingClass;
	
	public MeanExcludedTable(MeanExcludedTableModel model) {
		super(model);
				
		setSortable(false);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		mouseOver = -1;
		
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
	
	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
		Component component = super.prepareRenderer(renderer, row, column);
		Color bgColor = Colors.getRowColor(isRowSelected(row), row == mouseOver, row %2 == 0);
		component.setBackground(bgColor);
		if(component instanceof Container) {
			Component[] children = ((Container)component).getComponents();
			for (int ii = 0; (children != null) && (ii < children.length); ii++) {
				children[ii].setBackground(bgColor);
			}
		}
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
