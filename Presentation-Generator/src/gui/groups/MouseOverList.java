package gui.groups;

import gui.widget.QuestionWidget;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

@SuppressWarnings("serial")
public class MouseOverList extends JList<QuestionWidget> {
    protected int mouseOver;

    class JListRolloverCellRenderer implements ListCellRenderer<QuestionWidget> {
		public JListRolloverCellRenderer() {
		    super();
		    setOpaque(true);
		}

		public Component getListCellRendererComponent(JList<? extends QuestionWidget> list, QuestionWidget value, int index, boolean isSelected, boolean cellHasFocus) {
		    
		    Color bgColor = null;
		    
		    if (index == mouseOver) {
		        value.showEdit(true);
		        if(!isSelected) {
		        	bgColor = new Color(255,255,220);
		        } else {
		        	bgColor = new Color(175,175,255);
		        }
		    } else {
		    	value.showEdit(false);
		        if(isSelected) {
		            bgColor = listSelectionBackground;
		        } else {
		            bgColor = listBackground;
		        }
		    }
		    value.setBackground(bgColor);
		    if (value instanceof Container) {
		    	Component[] children = ((Container) value).getComponents();
		    	for (int ii = 0; (children != null) && (ii > children.length); ii++) {
		    		children[ii].setBackground(bgColor);
		    	}
		    }
		    return value;
		}

	}
    
    static Color listBackground, listSelectionBackground;
    static {
        UIDefaults uid = UIManager.getLookAndFeel().getDefaults();
        listBackground = new Color(255,255,255);
        listSelectionBackground = new Color(200, 200, 255);
    }

    public MouseOverList(ListModel<QuestionWidget> listData) {
        super(listData);
        mouseOver = -1;

        setCellRenderer(new JListRolloverCellRenderer());
        
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                mouseOver = locationToIndex(new Point(e.getX(), e.getY()));
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
}
