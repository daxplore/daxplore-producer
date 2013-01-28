package gui;

import gui.edit.EditTextView;
import gui.groups.GroupsView;
import gui.navigation.NavigationView;
import gui.open.OpenFileView;
import gui.question.QuestionView;
import gui.tools.ToolsView;
import gui.widget.QuestionWidget;
import gui.widget.TextWidget;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Stack;

import javax.swing.JFrame;

import daxplorelib.DaxploreFile;
import daxplorelib.metadata.MetaQuestion;
import daxplorelib.metadata.TextReference;

/**
 * Main window handler class. Initialization of application goes here.
 */
public class MainController implements ActionListener {
	// data fields for main class.
	
	private JFrame mainFrame;
	private OpenFileView openFileView;
	private GroupsView groupsView;
	private EditTextView editTextView;
	private ButtonPanelView buttonPanelView;
	private ToolsView toolsView;
	private NavigationView navigationView;
	private QuestionView questionView;
	private MainView mainView;
	
	private Stack<HistoryItem> history = new Stack<HistoryItem>();
	private HistoryItem currentCommand;
	
	private DaxploreFile daxploreFile = null;
	private File spssFile = null;

	public enum Views {
		OPENFILEVIEW,
		EDITTEXTVIEW,
		GROUPSVIEW,
		TOOLSVIEW,
		QUESTIONVIEW;
	}
	
	private class HistoryItem {
		public Views view;
		public Object command;
		public HistoryItem(Views view, Object command) {
			super();
			this.view = view;
			this.command = command;
		}
	}
	
	
	public MainController(MainView mainView) {
		this.mainView = mainView;
		QuestionWidget.mainController = this;
		TextWidget.mainController = this;
	}
	
	public void switchTo(Views view) {
		mainView.showInMain(view);
		history.clear();
		currentCommand = new HistoryItem(view, null);
	}
	
	public void switchTo(Views view, Object command) {
		HistoryItem hi = new HistoryItem(view, command);
		history.push(currentCommand);
		currentCommand = hi;
		doCommand(hi);
		navigationView.getController().setHistoryAvailible(true);
	}
	
	private void doCommand(HistoryItem hi) {
		if(hi.command != null) {
			switch(hi.view) {
			case OPENFILEVIEW:
				break;
			case EDITTEXTVIEW:
				if(hi.command instanceof TextReference) {
					editTextView.getController().jumpToTextReference((TextReference)hi.command);
				}
				break;
			case GROUPSVIEW:
				break;
			case TOOLSVIEW:
				break;
			case QUESTIONVIEW:
				if(hi.command instanceof MetaQuestion) {
					questionView.getController().openMetaQuestion((MetaQuestion)hi.command);
				}
				break;
			}
		}
		buttonPanelView.setActiveButton(hi.view);
		mainView.showInMain(hi.view);
	}
	
	public void historyBack(){
		HistoryItem hi = history.pop();
		doCommand(hi);
		currentCommand = hi;
		if(history.empty()) {
			navigationView.getController().setHistoryAvailible(false);
		}
	}
	
	public boolean hasHistory() {
		return history.empty();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try { //from buttonPanelView
			Views view = Views.valueOf(Views.class, e.getActionCommand());
			switchTo(view);
		} catch (IllegalArgumentException e2) {
			//place for other types of buttons
		}
	}
	
	public void updateStuff() {
		buttonPanelView.updateButtonPanel();
		groupsView.getController().loadData();
		editTextView.getController().loadData();
	}

	
	// getters and setters.
	public JFrame getMainFrame() {
		return mainFrame;
	}
	
	public void setMainFrame(JFrame panel) {
		this.mainFrame = panel;
	}
	
	public ButtonPanelView getButtonPanelView() {
		return buttonPanelView;
	}

	public void setButtonPanelView(ButtonPanelView buttonPanelView) {
		this.buttonPanelView = buttonPanelView;
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

	public QuestionView getQuestionView() {
		return questionView;
	}

	public void setQuestionView(QuestionView questionView) {
		this.questionView = questionView;
	}

	public ToolsView getToolsView() {
		return toolsView;
	}

	public void setToolsView(ToolsView toolsView) {
		this.toolsView = toolsView;
	}

	/* Files and stuff */
	
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
