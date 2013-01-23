package gui.navigation;

import gui.GuiMain;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JButton;

import daxplorelib.DaxploreException;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Component;
import javax.swing.Box;
import java.awt.BorderLayout;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.MatteBorder;

@SuppressWarnings("serial")
public class NavigationPanelView extends JPanel {

	GuiMain guiMain;

	public NavigationPanelView(final GuiMain guiMain) {
		this.guiMain = guiMain;
		setLayout(new BorderLayout(0, 0));
		setBorder(new MatteBorder(0, 1, 0, 0, (Color) Color.GRAY));

		JButton backButton = new JButton("Back");
		add(backButton, BorderLayout.WEST);

		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					guiMain.getGuiFile().getDaxploreFile().getMetaData().save();
				} catch (DaxploreException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		add(saveButton, BorderLayout.EAST);

		JPanel centerPanel = new JPanel();
		add(centerPanel, BorderLayout.CENTER);
		centerPanel.setLayout(new BorderLayout(0, 0));

		JSeparator westSeperator = new JSeparator();
		westSeperator.setOrientation(SwingConstants.VERTICAL);
		centerPanel.add(westSeperator, BorderLayout.WEST);

		JSeparator eastSeperator = new JSeparator();
		eastSeperator.setOrientation(SwingConstants.VERTICAL);
		centerPanel.add(eastSeperator, BorderLayout.EAST);

		JPanel toolbarPanel = new JPanel();
		centerPanel.add(toolbarPanel, BorderLayout.CENTER);
	}

}
