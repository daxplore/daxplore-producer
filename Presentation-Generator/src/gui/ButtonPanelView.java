package gui;


import java.awt.CardLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;

/**
 * Left button panel 
 * @author jorgenrosen
 *
 */
public class ButtonPanelView extends JPanel {
	
	public ButtonPanelView(final GuiMain guiMain) {
		
		ButtonGroup buttonGroup = new ButtonGroup();
		
		// create the button panel.
		setBorder(new MatteBorder(0, 0, 0, 1, (Color) Color.LIGHT_GRAY));
		setLayout(new GridLayout(0, 1, 0, 0));
		
		// open button.
		JRadioButton OpenButton = new JRadioButton("");
		OpenButton.setToolTipText("Manage file(s)");
		OpenButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				guiMain.switchTo("openPanel");
			}
		});
		
		OpenButton.setRolloverEnabled(false);
		OpenButton.setSelectedIcon(new ImageIcon(GuiMain.class.getResource("/gui/resources/8_selected.png")));
		OpenButton.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		buttonGroup.add(OpenButton);
		OpenButton.setIcon(new ImageIcon(GuiMain.class.getResource("/gui/resources/8.png")));
		add(OpenButton);

		// groups button.
		JRadioButton GroupsButton = new JRadioButton("");
		GroupsButton.setToolTipText("Edit groups and questions");
		GroupsButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				guiMain.switchTo("groupsPanel");
			}
		});
		
		GroupsButton.setSelectedIcon(new ImageIcon(ButtonPanelView.class.getResource("/gui/resources/29_selected.png")));
		GroupsButton.setRolloverEnabled(false);
		GroupsButton.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		buttonGroup.add(GroupsButton);
		GroupsButton.setIcon(new ImageIcon(ButtonPanelView.class.getResource("/gui/resources/29.png")));
		add(GroupsButton);
	
		// edit button.
		JRadioButton EditButton = new JRadioButton("");
		EditButton.setToolTipText("*SET TOOL TIP*");
		EditButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				guiMain.switchTo("editPanel");
			}
		});
		
		EditButton.setSelectedIcon(new ImageIcon(GuiMain.class.getResource("/gui/resources/21_selected.png")));
		EditButton.setRolloverEnabled(false);
		EditButton.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		buttonGroup.add(EditButton);
		EditButton.setIcon(new ImageIcon(GuiMain.class.getResource("/gui/resources/21.png")));
		add(EditButton);
	}
}