package org.daxplore.producer.gui.view.variable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.EventObject;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.daxplore.producer.gui.resources.Colors;
import org.daxplore.producer.gui.widget.AbstractWidget;
import org.daxplore.producer.gui.widget.AbstractWidgetEditor;
import org.daxplore.producer.gui.widget.AbstractWidgetEditor.InvalidContentException;
import org.daxplore.producer.gui.widget.QuestionWidget;

import com.google.common.eventbus.EventBus;
import com.google.common.primitives.Ints;

@SuppressWarnings("serial")
public class RawVariableTable extends JTable {
	
	private int mouseOverRow;
	private RawVariableCellRenderer cellRenderer;
	
	public RawVariableTable(EventBus eventBus, RawVariableTableModel model) {
		super(model);
		
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mouseOverRow = -1;
        
        cellRenderer = new RawVariableCellRenderer(eventBus);
        setDefaultRenderer(Double.class, cellRenderer);
        setDefaultEditor(Double.class, cellRenderer);
        setDefaultRenderer(String.class, cellRenderer);
        setDefaultEditor(String.class, cellRenderer);
        setDefaultRenderer(Integer.class, cellRenderer);
        setDefaultEditor(Integer.class, cellRenderer);
        
        //TODO should we have to create a QuestionWidget here?
        setRowHeight(new QuestionWidget(eventBus).getPreferredSize().height);
        
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
	
	public void setAvailableNumbers(List<Integer> list) {
		cellRenderer.setAvailableNumbers(list);
	}
	
	class RawVariableCellRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {
		
		private AbstractWidgetEditor<?> editor;
		private AbstractWidget<?> renderer;
		private ChoiceEditor cEditor;
		private ChoiceEditor cRenderer;
		
		public RawVariableCellRenderer(EventBus eventBus) {
			cEditor = new ChoiceEditor(null);
			cRenderer = new ChoiceEditor(null);
		}
		
		public void setAvailableNumbers(List<Integer> list) {
			cEditor.setAvailibleNumbers(list);
			cRenderer.setAvailibleNumbers(list);
		}
		
	    @Override
	    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	    	if(column == 3) {
	    		renderer = cRenderer;
	    		cRenderer.setContent((Integer)value);
	    	} else {
	    		return null;
	    	}
	    	
	    	Color bgColor = Colors.getRowColor(isSelected, mouseOverRow==row, row%2==0);

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
			if(column == 3) {
	    		editor = cEditor;
	    		cEditor.setContent((Integer)value);
	    	} else {
	    		return null;
	    	}
			
	    	Color bgColor = Colors.getRowColor(isSelected, mouseOverRow==row, row%2==0);
	    	
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
	
	class ChoiceEditor extends AbstractWidgetEditor<Integer> implements ActionListener {
		
		List<Integer> availableNumbers;
		JComboBox<String> chooser;
		Integer content;
		
		public ChoiceEditor(List<Integer> availableNumbers) {
			chooser = new JComboBox<String>();
			chooser.addActionListener(this);
			setAvailibleNumbers(availableNumbers);
			this.add(chooser);
		}
		
		public void setAvailibleNumbers(List<Integer> list) {
			availableNumbers = list;
			chooser.removeAllItems();
			chooser.addItem("-- none --");
			for(Integer c: availableNumbers) {
				chooser.addItem(c.toString());
			}
			if(content != null && availableNumbers.contains(content)) {
				chooser.setSelectedItem(content.toString());
			} else {
				chooser.setSelectedIndex(0);
				content = null;
			}
		}
		
		@Override
		public Integer getContent() throws InvalidContentException {
			return content;
		}

		@Override
		public void setContent(Integer value) {
			content = value;
			chooser.setSelectedItem(content.toString());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			content = Ints.tryParse(chooser.getItemAt(chooser.getSelectedIndex()));
		}
		
	}
}
