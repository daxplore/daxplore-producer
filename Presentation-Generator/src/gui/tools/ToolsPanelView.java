package gui.tools;

import gui.GuiMain;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JLabel;

public class ToolsPanelView extends JPanel {
	
	private GuiMain guiMain;
	private JTextField importRawTextField;
	private ToolsPanelController toolsPanelController;
	private JButton importRawButton;
	
	public static final String IMPORT_RAW_BUTTON_ACTION_COMMAND = "importRawButtonActionCommand";
	
	public ToolsPanelView(GuiMain guiMain) {
		
		JLabel rawImportLabel = new JLabel("Perform raw import");
		add(rawImportLabel);
		
		importRawButton = new JButton("Import Raw");
		importRawButton.addActionListener(toolsPanelController);
		importRawButton.setActionCommand(IMPORT_RAW_BUTTON_ACTION_COMMAND);
		add(importRawButton);
		
		importRawTextField = new JTextField();
		add(importRawTextField);
		importRawTextField.setColumns(10);
	}
}
