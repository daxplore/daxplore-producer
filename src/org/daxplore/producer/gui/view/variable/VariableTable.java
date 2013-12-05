package org.daxplore.producer.gui.view.variable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;
import org.daxplore.producer.gui.resources.Colors;
import org.daxplore.producer.gui.resources.GuiTexts;
import org.daxplore.producer.gui.widget.AbstractWidgetEditor;
import org.daxplore.producer.gui.widget.AbstractWidgetEditor.InvalidContentException;
import org.daxplore.producer.gui.widget.TextWidget;
import org.jdesktop.swingx.JXTable;

import com.google.common.eventbus.EventBus;

@SuppressWarnings("serial")
public class VariableTable extends JXTable {
	
	private int mouseOverRow;
	private VariableCellRenderer cellRenderer;
	
	public VariableTable(EventBus eventBus, GuiTexts texts, VariableTableModel model) {
		super(model);
		
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mouseOverRow = -1;
        
        cellRenderer = new VariableCellRenderer(eventBus, texts);
        setDefaultRenderer(TextReference.class, cellRenderer);
        setDefaultEditor(TextReference.class, cellRenderer);
        setDefaultRenderer(Integer.class, cellRenderer);
        setDefaultEditor(Integer.class, cellRenderer);
        
        //TODO should we have to create a TextWidget here?
        setRowHeight(new TextWidget(eventBus, texts).getPreferredSize().height);
        packAll();
        this.
        
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
			public void mouseMoved(MouseEvent e) {
            	mouseOverRow = rowAtPoint(e.getPoint());
                repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
			public void mouseExited(MouseEvent e) {
                mouseOverRow = -1;
                repaint();
            }
        });
	}
	
	class VariableCellRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {
		
		private AbstractWidgetEditor<?> editor;
		private TextWidget textEditor;
		private TextWidget textRenderer;
		private JLabel label = new JLabel();
		
		public VariableCellRenderer(EventBus eventBus, GuiTexts texts) {
			textEditor = new TextWidget(eventBus, texts);
			textRenderer = new TextWidget(eventBus, texts);
		}
				
	    @Override
	    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	    	Component comp;
	    	if(value instanceof TextReference) {
	    		comp = textRenderer;
	    		textRenderer.setContent((TextReference)value);
	    	} else {
	    		label.setText(value.toString());
	    		comp = label;
	    	}
	    	
	    	Color bgColor = Colors.getRowColor(isSelected, mouseOverRow==row, row%2==0);

	    	comp.setBackground(bgColor);
		    if (value instanceof Container) {
		    	Component[] children = ((Container) value).getComponents();
		    	for (int ii = 0; (children != null) && (ii > children.length); ii++) {
		    		children[ii].setBackground(bgColor);
		    	}
		    }
		    //table.setRowHeight(row, renderer.getPreferredSize().height);
		    return comp;
	    }
		
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			Component comp;
			if(value instanceof TextReference) {
				textEditor.setContent((TextReference)value);
				comp = textEditor;
				editor = textEditor;
	    	} else {
	    		return null;
	    	}
			
	    	Color bgColor = Colors.getRowColor(isSelected, mouseOverRow==row, row%2==0);
	    	
	    	comp.setBackground(bgColor);
		    if (value instanceof Container) {
		    	Component[] children = ((Container) value).getComponents();
		    	for (int ii = 0; (children != null) && (ii > children.length); ii++) {
		    		children[ii].setBackground(bgColor);
		    	}
		    }
		    //table.setRowHeight(row, editor.getPreferredSize().height);
		    return comp;
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
