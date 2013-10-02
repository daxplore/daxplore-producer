package org.daxplore.producer.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Stack;

import javax.swing.JFrame;

import org.daxplore.producer.daxplorelib.DaxploreFile;
import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;
import org.daxplore.producer.gui.edit.EditTextController;
import org.daxplore.producer.gui.groups.GroupsController;
import org.daxplore.producer.gui.navigation.NavigationController;
import org.daxplore.producer.gui.open.OpenFileController;
import org.daxplore.producer.gui.question.QuestionController;
import org.daxplore.producer.gui.timeseries.TimeSeriesController;
import org.daxplore.producer.gui.tools.ToolsController;
import org.daxplore.producer.gui.widget.QuestionWidget;
import org.daxplore.producer.gui.widget.TextWidget;

/**
 * Main window handler class. Initialization of application goes here.
 * 
 * Singleton.
 */
public class MainController implements ActionListener {
	// data fields for main class.
	
	private MainView mainView;
	private ButtonPanelView buttonPanelView;

	private OpenFileController openFileController;
	private GroupsController groupsController;
	private EditTextController editTextController;
	private ToolsController toolsController;
	private NavigationController navigationController;
	private QuestionController questionController;
	private TimeSeriesController timeSeriesController;
	
	private Stack<HistoryItem> history = new Stack<>();
	private HistoryItem currentCommand;
	
	private DaxploreFile daxploreFile = null;
	private File spssFile = null;

	public enum Views {
		OPENFILEVIEW,
		EDITTEXTVIEW,
		GROUPSVIEW,
		TIMESERIESVIEW,
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
	
	public MainController() {
		//TODO: ugly hack, replace with eventbus
		QuestionWidget.mainController = this;
		TextWidget.mainController = this;

		buttonPanelView = new ButtonPanelView(this);
		
		openFileController = new OpenFileController(this);
		groupsController = new GroupsController(this);
		editTextController = new EditTextController(this);
		toolsController = new ToolsController(this);
		navigationController = new NavigationController(this);
		questionController = new QuestionController(this);
		timeSeriesController = new TimeSeriesController(this);

		mainView = new MainView(buttonPanelView);
		mainView.addView(openFileController.getView(), Views.OPENFILEVIEW);
		mainView.addView(groupsController.getView(), Views.GROUPSVIEW);
		mainView.addView(editTextController.getView(), Views.EDITTEXTVIEW);
		mainView.addView(toolsController.getView(), Views.TOOLSVIEW);
		mainView.addView(questionController.getView(), Views.QUESTIONVIEW);
		mainView.addView(timeSeriesController.getView(), Views.TIMESERIESVIEW);
		
		mainView.setNavigationView(navigationController.getView());
	}
	
	public void showWindow(boolean show) {
		mainView.showWindow(show);
	}

	public void switchTo(Views view) {
		mainView.switchTo(view);
		setToolbar(view);
		history.clear();
		currentCommand = new HistoryItem(view, null);
	}
	
	public void switchTo(Views view, Object command) {
		HistoryItem hi = new HistoryItem(view, command);
		history.push(currentCommand);
		currentCommand = hi;
		doCommand(hi);
		buttonPanelView.setActiveButton(hi.view);
		mainView.switchTo(hi.view);
		setToolbar(hi.view);
		navigationController.setHistoryAvailible(true);
	}
	
	public void historyBack(){
		HistoryItem hi = history.pop();
		doCommand(hi);
		buttonPanelView.setActiveButton(hi.view);
		mainView.switchTo(hi.view);
		setToolbar(hi.view);
		currentCommand = hi;
		if(history.empty()) {
			navigationController.setHistoryAvailible(false);
		}
	}
	
	public boolean hasHistory() {
		return history.empty();
	}
	
	private void doCommand(HistoryItem hi) {
		if(hi.command != null) {
			switch(hi.view) {
			case OPENFILEVIEW:
				break;
			case EDITTEXTVIEW:
				if(hi.command instanceof TextReference) {
					editTextController.jumpToTextReference((TextReference)hi.command);
				}
				break;
			case GROUPSVIEW:
				break;
			case TOOLSVIEW:
				break;
			case QUESTIONVIEW:
				if(hi.command instanceof MetaQuestion) {
					questionController.openMetaQuestion((MetaQuestion)hi.command);
				}
				break;
			case TIMESERIESVIEW:
			default:
				throw new AssertionError("Undefined history item command: " + hi.view);
			}
		}
	}
	
	private void setToolbar(Views view) {
		switch(view) {
		case EDITTEXTVIEW:
			navigationController.setToolbar(editTextController.getToolbar());
			return;
		case GROUPSVIEW:
			navigationController.setToolbar(groupsController.getToolbar());
			return;
		case OPENFILEVIEW:
		case QUESTIONVIEW:
		case TIMESERIESVIEW:
		case TOOLSVIEW:
		default:
			navigationController.setToolbar(null);
			break;
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		try { //from buttonPanelView
			Views view = Views.valueOf(e.getActionCommand());
			switchTo(view);
		} catch (IllegalArgumentException e2) {
			//place for other types of buttons
		}
	}
	
	public void updateStuff() {
		buttonPanelView.setActive(fileIsSet());
		toolsController.loadData();
		groupsController.loadData();
		editTextController.loadData();
		timeSeriesController.loadData();
	}

	//TODO decouple more
	public JFrame getMainWindow() {
		return mainView.getMainFrame();
	}
	
	public ButtonPanelView getButtonPanelView() {
		return buttonPanelView;
	}

	public OpenFileController getOpenFileController() {
		return openFileController;
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
