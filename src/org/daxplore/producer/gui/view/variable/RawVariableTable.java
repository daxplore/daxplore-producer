/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui.view.variable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.IllegalComponentStateException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.daxplore.producer.gui.resources.Colors;
import org.daxplore.producer.gui.resources.GuiTexts;
import org.daxplore.producer.gui.widget.AbstractWidgetEditor;
import org.daxplore.producer.gui.widget.AbstractWidgetEditor.InvalidContentException;
import org.daxplore.producer.gui.widget.TextWidget;
import org.jdesktop.swingx.JXTable;

import com.google.common.eventbus.EventBus;
import com.google.common.primitives.Ints;

@SuppressWarnings("serial")
public class RawVariableTable extends JXTable {
	
	private int mouseOverRow;
	private RawVariableCellRenderer cellRenderer;
	private RawVariableTableModel model;
	private ChoiceEditor choiceEditor;
	private ChoiceEditor choiceRenderer;
	private JPanel cellTextWrapper = new JPanel();
	private JLabel cellTextLabel = new JLabel();
	private boolean enabled = true;
	
	public RawVariableTable(EventBus eventBus, GuiTexts texts, RawVariableTableModel model) {
		super(model);
		this.model = model;
		
		setSortable(false);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mouseOverRow = -1;
        
        cellRenderer = new RawVariableCellRenderer(texts, model.availableToNumbers);
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
		choiceEditor.setEnabled(enabled);
		choiceRenderer.setEnabled(enabled);
		cellTextLabel.setEnabled(enabled);
		cellTextWrapper.setEnabled(enabled);
		getTableHeader().setForeground(enabled ? Color.BLACK : Color.GRAY);
	}
	
	class RawVariableCellRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor, PopupMenuListener {
		
		private GuiTexts texts;
		private AbstractWidgetEditor<?> editor;
		
		public RawVariableCellRenderer(GuiTexts texts, List<Integer> availableToNumbers) {
			this.texts = texts;
			choiceEditor = new ChoiceEditor(texts, availableToNumbers);
			choiceRenderer = new ChoiceEditor(texts, availableToNumbers);
			cellTextWrapper.setLayout(new BorderLayout());
			cellTextWrapper.add(cellTextLabel, BorderLayout.CENTER);
			choiceEditor.addPopupMenuListener(this);
		}
		
		public void setAvailableNumbers(List<Integer> list) {
			choiceEditor.setAvailibleNumbers(list);
			choiceRenderer.setAvailibleNumbers(list);
		}
		
	    @Override
	    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	    	Component comp;
	    	if(column == 3)/* && model.getValueAt(row, 0) != null) */{
	    		comp = choiceRenderer;
	    		if(value!=null) {
	    			choiceRenderer.setToolTipText(value.toString());
	    		} else {
	    			choiceRenderer.setToolTipText(texts.get("table.tooltip.to_nothing"));
	    		}
	    		choiceRenderer.setContent((Integer)value);
	    	} else if(value == null) {
	    		cellTextLabel.setText(texts.get("table.text.null"));
	    		cellTextWrapper.setToolTipText(texts.get("table.tooltip.null"));
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
				choiceEditor.setContent((Integer)value);
				
				if(value!=null) {
					choiceEditor.setToolTipText(value.toString());
	    		} else {
	    			choiceEditor.setToolTipText(texts.get("table.tooltip.to_nothing"));
	    		}
				
				comp = choiceEditor;
				editor = choiceEditor;
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
			try {
				return editor.getContent();
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

		@Override
		public void popupMenuCanceled(PopupMenuEvent e) {}

		@Override
		public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			if(e.getSource() == choiceEditor) {
				stopCellEditing();
			}
		}

		@Override
		public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}
	}
	
	class ChoiceEditor extends JComboBox<String> implements AbstractWidgetEditor<Integer>, ItemListener {
		
		private GuiTexts texts;
		private List<Integer> availableNumbers;
		private Integer content;
		
		public ChoiceEditor(GuiTexts texts, List<Integer> availableNumbers) {
			this.texts = texts;
			addItemListener(this);
			setAvailibleNumbers(availableNumbers);
		}
		
		public void setAvailibleNumbers(List<Integer> list) {
			availableNumbers = list;
			removeAllItems();
			addItem(texts.get("table.text.to_nothing"));
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
