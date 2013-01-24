package gui;

import gui.edit.EditPanelView;
import gui.groups.GroupsView;
import gui.navigation.NavigationPanelView;
import gui.open.OpenPanelView;
import gui.tools.ToolsPanelView;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.MatteBorder;

/**
 * Main window handler class. Initialization of application goes here.
 */
public class MainController {
	// data fields for main class.
	private GuiFile guiFile;
	
	private JFrame mainFrame;
	private OpenPanelView openPanelView;
	private GroupsView groupsPanelView;
	private EditPanelView editPanelView;
	private ButtonPanelView buttonPanelView;
	private ToolsPanelView toolsPanelView;
	private NavigationPanelView navigationPanelView;

	private MainView mainView;
	
	public enum Views {
		OPENPANEL,
		EDITVIEW,
		GROUPSVIEW,
		TOOLSVIEW;
	}
	
	
	public MainController(MainView mainView) {
		this.mainView = mainView;
		// file handler init.
		guiFile = new GuiFile();
	}
	
	// getters and setters.
	public ButtonPanelView getButtonPanelView() {
		return buttonPanelView;
	}

	public void setButtonPanelView(ButtonPanelView buttonPanelView) {
		this.buttonPanelView = buttonPanelView;
	}

	public GuiFile getGuiFile() {
		return guiFile;
	}

	public void setGuiFile(GuiFile guiFile) {
		this.guiFile = guiFile;
	}

	public JFrame getMainFrame() {
		return mainFrame;
	}
	
	public void setMainFrame(JFrame panel) {
		this.mainFrame = panel;
	}

	public OpenPanelView getOpenPanelView() {
		return openPanelView;
	}

	public void setOpenPanelView(OpenPanelView openPanelView) {
		this.openPanelView = openPanelView;
	}

	public GroupsView getGroupsPanelView() {
		return groupsPanelView;
	}

	public void setGroupsPanelView(GroupsView groupsPanelView) {
		this.groupsPanelView = groupsPanelView;
	}

	public EditPanelView getEditPanelView() {
		return editPanelView;
	}

	public void setEditPanelView(EditPanelView editPanelView) {
		this.editPanelView = editPanelView;
	}
	
	public NavigationPanelView getNavigationPanelView() {
		return navigationPanelView;
	}
	
	public void setNavigationPanelView(NavigationPanelView navigationPanelView){
		this.navigationPanelView = navigationPanelView;
	}

	public void switchTo(Views view) {
		mainView.showInMain(view);
	}
	
	public void updateStuff() {
		buttonPanelView.updateButtonPanel();
		groupsPanelView.getController().loadData();
		editPanelView.loadData();
	}

	public ToolsPanelView getToolsPanelView() {
		return toolsPanelView;
	}

	public void setToolsPanelView(ToolsPanelView toolsPanelView) {
		this.toolsPanelView = toolsPanelView;
	}


}
