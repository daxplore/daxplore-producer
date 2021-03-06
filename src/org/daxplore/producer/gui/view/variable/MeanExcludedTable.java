package org.daxplore.producer.gui.view.variable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.IllegalComponentStateException;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.EventObject;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.daxplore.producer.gui.resources.Colors;
import org.daxplore.producer.gui.resources.UITexts;
import org.daxplore.producer.gui.widget.AbstractWidgetEditor;
import org.jdesktop.swingx.JXTable;

import com.google.common.eventbus.EventBus;
import com.google.common.primitives.Ints;

@SuppressWarnings("serial")
public class MeanExcludedTable extends JXTable {
	private int mouseOverRow;
	private MeanExcludedCellRenderer cellRenderer;
	private JCheckBox cellBooleanCheckBox = new JCheckBox();
	private JPanel cellTextWrapper = new JPanel();
	private JLabel cellTextLabel = new JLabel();
	private boolean enabled = true;
	
	public MeanExcludedTable(EventBus eventBus, MeanExcludedTableModel model) {
		super(model);
		
		setSortable(false);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mouseOverRow = -1;
        
        cellRenderer = new MeanExcludedCellRenderer();
        setDefaultRenderer(Double.class, cellRenderer);
        setDefaultEditor(Double.class, cellRenderer);
        setDefaultRenderer(String.class, cellRenderer);
        setDefaultEditor(String.class, cellRenderer);
        setDefaultRenderer(Integer.class, cellRenderer);
        setDefaultEditor(Integer.class, cellRenderer);
        
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
	
	@Override
	public Component prepareEditor(TableCellEditor editor, int row, int col) {
	    final Component c = super.prepareEditor(editor, row, col);
	    c.setBackground(Colors.getRowColor(true, false, row%2==0));
	    if(c instanceof ChoiceEditor) {
	    	SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					try {
						((ChoiceEditor)c).showPopup();
					} catch(IllegalComponentStateException e){
						// the component is no longer visible, do nothing
					}
				}
			});
	    }
	    return c;
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		this.enabled = enabled;
		cellTextLabel.setEnabled(enabled);
		cellTextWrapper.setEnabled(enabled);
		cellBooleanCheckBox.setEnabled(enabled);
		getTableHeader().setForeground(enabled ? Color.BLACK : Color.GRAY);
	}
	
	class MeanExcludedCellRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor, PopupMenuListener {
		
		public MeanExcludedCellRenderer() {
			cellTextWrapper.setLayout(new BorderLayout());
			cellTextWrapper.add(cellTextLabel, BorderLayout.CENTER);
		}
		
	    @Override
	    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	    	Component comp;
	    	if(column == 3) {
	    		comp = cellBooleanCheckBox;
	    		cellBooleanCheckBox.setSelected((Boolean)value);
	    	} else if(value == null) {
	    		cellTextLabel.setText(UITexts.get("table.text.null"));
	    		cellTextWrapper.setToolTipText(UITexts.get("table.tooltip.null"));
	    		comp = cellTextWrapper;
	    	} else {
	    		cellTextLabel.setText(value.toString());
	    		cellTextWrapper.setToolTipText(value.toString());
	    		comp = cellTextWrapper;
	    	}
	    	
	    	Color bgColor = Colors.getRowColor(isSelected, mouseOverRow==row && enabled, row%2==0);

	    	comp.setBackground(bgColor);
		    if (value instanceof Container) {
		    	Component[] children = ((Container) value).getComponents();
		    	for (int ii = 0; (children != null) && (ii > children.length); ii++) {
		    		children[ii].setBackground(bgColor);
		    	}
		    }

		    return comp;
	    }
		
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			Component comp;
			if(column == 3) {
	    		comp = cellBooleanCheckBox;
				cellBooleanCheckBox.setSelected((Boolean)value);
	    	} else {
	    		return null;
	    	}
			
	    	Color bgColor = Colors.getRowColor(isSelected, mouseOverRow==row && enabled, row%2==0);
	    	
	    	comp.setBackground(bgColor);
		    if (value instanceof Container) {
		    	Component[] children = ((Container) value).getComponents();
		    	for (int ii = 0; (children != null) && (ii > children.length); ii++) {
		    		children[ii].setBackground(bgColor);
		    	}
		    }

		    return comp;
		}
		
		@Override
		public Object getCellEditorValue() {
			return cellBooleanCheckBox.isSelected();
		}
		
	    @Override
	    public boolean isCellEditable(EventObject anEvent) {
	        return true;
	    }
	    
	    @Override
	    public boolean shouldSelectCell(EventObject anEvent) {
	        return true;
	    }

		@Override
		public void popupMenuCanceled(PopupMenuEvent e) {}

		@Override
		public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			if(e.getSource() == cellBooleanCheckBox) {
				stopCellEditing();
			}
		}

		@Override
		public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}
	}
	
	class ChoiceEditor extends JComboBox<String> implements AbstractWidgetEditor<Integer>, ItemListener {
		private List<Integer> availableNumbers;
		private Integer content;
		
		public ChoiceEditor(List<Integer> availableNumbers) {
			addItemListener(this);
			setAvailibleNumbers(availableNumbers);
		}
		
		public void setAvailibleNumbers(List<Integer> list) {
			availableNumbers = list;
			removeAllItems();
			addItem(UITexts.get("table.text.to_nothing"));
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
