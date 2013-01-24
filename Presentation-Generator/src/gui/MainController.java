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
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.MatteBorder;

import daxplorelib.DaxploreFile;

/**
 * Main window handler class. Initialization of application goes here.
 */
public class MainController {
	// data fields for main class.
	
	private JFrame mainFrame;
	private OpenPanelView openPanelView;
	private GroupsView groupsPanelView;
	private EditPanelView editPanelView;
	private ButtonPanelView buttonPanelView;
	private ToolsPanelView toolsPanelView;
	private NavigationPanelView navigationPanelView;

	private MainView mainView;
	
	private DaxploreFile daxploreFile = null;
	private File spssFile = null;

	public enum Views {
		OPENPANEL,
		EDITVIEW,
		GROUPSVIEW,
		TOOLSVIEW;
	}
	
	
	public MainController(MainView mainView) {
		this.mainView = mainView;
	}
	
	// getters and setters.
	public ButtonPanelView getButtonPanelView() {
		return buttonPanelView;
	}

	public void setButtonPanelView(ButtonPanelView buttonPanelView) {
		this.buttonPanelView = buttonPanelView;
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

	public DaxploreFile getDaxploreFile() {
		return daxploreFile;
	}

	public void setDaxploreFile(DaxploreFile daxploreFile) {
		this.daxploreFile = daxploreFile;
		
	}

	public File getSpssFile() {
		return spssFile;
	}

	public void setSpssFile(File spssFile) {
		this.spssFile = spssFile;
	}
	
	public void resetDaxploreFile() {
		daxploreFile = null;
	}
	
	public void resetSpssFile() {
		spssFile = null;
	}
	
	/**
	 * Returns true if a daxplore file is loaded into the system.
	 */
	public boolean isSet() {
		return daxploreFile != null;
	}

}
