package org.daxplore.producer.gui.view.variable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.EventObject;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.daxplore.producer.gui.resources.Colors;
import org.daxplore.producer.gui.resources.GuiTexts;
import org.daxplore.producer.gui.widget.AbstractWidgetEditor;
import org.daxplore.producer.gui.widget.TextWidget;
import org.daxplore.producer.gui.widget.AbstractWidgetEditor.InvalidContentException;
import org.jdesktop.swingx.JXTable;

import com.google.common.eventbus.EventBus;
import com.google.common.primitives.Ints;

@SuppressWarnings("serial")
public class RawVariableTable extends JXTable {
	
	private int mouseOverRow;
	private RawVariableCellRenderer cellRenderer;
	RawVariableTableModel model;
	
	public RawVariableTable(EventBus eventBus, GuiTexts texts, RawVariableTableModel model) {
		super(model);
		this.model = model;
		
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mouseOverRow = -1;
        
        cellRenderer = new RawVariableCellRenderer(eventBus, model.availableToNumbers);
        setDefaultRenderer(Double.class, cellRenderer);
        setDefaultEditor(Double.class, cellRenderer);
        setDefaultRenderer(String.class, cellRenderer);
        setDefaultEditor(String.class, cellRenderer);
        setDefaultRenderer(Integer.class, cellRenderer);
        setDefaultEditor(Integer.class, cellRenderer);
        
        //TODO should we have to create a TextWidget here?
        setRowHeight(new TextWidget(eventBus, texts).getPreferredSize().height);
        packAll();
        
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
		model.setAvailableToNumbers(list);
		cellRenderer.setAvailableNumbers(list);
	}
	
	@Override
	public Component prepareEditor(TableCellEditor editor, int row, int col) {
	    final Component c = super.prepareEditor(editor, row, col);
	    c.setBackground(Colors.getRowColor(true, false, row%2==0));
	    if(c instanceof ChoiceEditor) {
	    	SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					((ChoiceEditor)c).showPopup();
				}
			});
	    }
	    return c;
	}
	
	class RawVariableCellRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {
		
		private AbstractWidgetEditor<?> editor;
		private ChoiceEditor cEditor;
		private ChoiceEditor cRenderer;
		private JPanel cellTextWrapper = new JPanel();
		private JLabel cellTextLabel = new JLabel();
		
		public RawVariableCellRenderer(EventBus eventBus, List<Integer> availableToNumbers) {
			cEditor = new ChoiceEditor(availableToNumbers);
			cRenderer = new ChoiceEditor(availableToNumbers);
			cellTextWrapper.add(cellTextLabel);
		}
		
		public void setAvailableNumbers(List<Integer> list) {
			cEditor.setAvailibleNumbers(list);
			cRenderer.setAvailibleNumbers(list);
		}
		
	    @Override
	    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	    	Component comp;
	    	if(column == 3) {
	    		comp = cRenderer;
	    		cRenderer.setContent((Integer)value);
	    	} else if(value == null) {
	    		cellTextLabel.setText("<html><b>null</b></html>");
	    		comp = cellTextWrapper;
	    	} else {
	    		cellTextLabel.setText(value.toString());
	    		comp = cellTextWrapper;
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
			if(column == 3) {
				cEditor.setContent((Integer)value);
				comp = cEditor;
				editor = cEditor;
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
	
	class ChoiceEditor extends JComboBox<String> implements AbstractWidgetEditor<Integer>, ItemListener {
		
		List<Integer> availableNumbers;
		Integer content;
		
		public ChoiceEditor(List<Integer> availableNumbers) {
			addItemListener(this);
			setAvailibleNumbers(availableNumbers);
		}
		
		public void setAvailibleNumbers(List<Integer> list) {
			availableNumbers = list;
			removeAllItems();
			addItem("none");
			for(Integer c: availableNumbers) {
				addItem(c.toString());
			}
			if(content != null && availableNumbers.contains(content)) {
				setSelectedItem(content.toString());
			} else {
				setSelectedIndex(0);
				content = null;
			}
		}
		
		@Override
		public Integer getContent() {
			return content;
		}

		@Override
		public void setContent(Integer value) {
			content = value;
			if(content != null) {
				setSelectedItem(content.toString());
			} else {
				setSelectedIndex(0);
			}
		}

		@Override
		public void itemStateChanged(ItemEvent e) {
			if(e.getStateChange() == ItemEvent.SELECTED) {
				content = Ints.tryParse(getItemAt(getSelectedIndex()));
			}
		}
		
	}
}
