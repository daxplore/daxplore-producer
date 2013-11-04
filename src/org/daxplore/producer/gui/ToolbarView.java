package org.daxplore.producer.gui;

import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.daxplore.producer.gui.ProgramCommandListener.ProgramCommand;

@SuppressWarnings("serial")
public class ToolbarView extends JPanel {

	private JToolBar toolbar;
	
	public ToolbarView(ActionListener listener) {
		
		toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.setRollover(true);
		
		JButton backButton = new JButton("<-");
		backButton.setActionCommand(ProgramCommand.BACK.toString());
		backButton.addActionListener(listener);
		toolbar.add(backButton);
		
		add(toolbar);
		
	}
}
