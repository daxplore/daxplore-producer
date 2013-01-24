package gui;

import gui.edit.EditTextView;
import gui.groups.GroupsView;
import gui.navigation.NavigationView;
import gui.open.OpenFileView;
import gui.tools.ToolsView;

import java.io.File;

import javax.swing.JFrame;

import daxplorelib.DaxploreFile;

/**
 * Main window handler class. Initialization of application goes here.
 */
public class MainController {
	// data fields for main class.
	
	private JFrame mainFrame;
	private OpenFileView openFileView;
	private GroupsView groupsView;
	private EditTextView editTextView;
	private ButtonPanelView buttonPanelView;
	private ToolsView toolsView;
	private NavigationView navigationView;

	private MainView mainView;
	
	private DaxploreFile daxploreFile = null;
	private File spssFile = null;

	public enum Views {
		OPENFILEVIEW,
		EDITTEXTVIEW,
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

	public OpenFileView getOpenFileView() {
		return openFileView;
	}

	public void setOpenFileView(OpenFileView openFileView) {
		this.openFileView = openFileView;
	}

	public GroupsView getGroupsView() {
		return groupsView;
	}

	public void setGroupsView(GroupsView groupsPanelView) {
		this.groupsView = groupsPanelView;
	}

	public EditTextView getEditTextView() {
		return editTextView;
	}

	public void setEditTextView(EditTextView editTextView) {
		this.editTextView = editTextView;
	}
	
	public NavigationView getNavigationView() {
		return navigationView;
	}
	
	public void setNavigationView(NavigationView navigationView){
		this.navigationView = navigationView;
	}

	public void switchTo(Views view) {
		mainView.showInMain(view);
	}
	
	public void updateStuff() {
		buttonPanelView.updateButtonPanel();
		groupsView.getController().loadData();
		editTextView.loadData();
	}

	public ToolsView getToolsView() {
		return toolsView;
	}

	public void setToolsView(ToolsView toolsView) {
		this.toolsView = toolsView;
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
	public boolean fileIsSet() {
		return daxploreFile != null;
	}

}
