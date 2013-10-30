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
import org.daxplore.producer.gui.event.ChangeMainViewEvent;
import org.daxplore.producer.gui.event.DaxploreFileUpdateEvent;
import org.daxplore.producer.gui.event.HistoryGoBackEvent;
import org.daxplore.producer.gui.groups.GroupsController;
import org.daxplore.producer.gui.navigation.NavigationController;
import org.daxplore.producer.gui.open.OpenFileController;
import org.daxplore.producer.gui.question.QuestionController;
import org.daxplore.producer.gui.timeseries.TimeSeriesController;
import org.daxplore.producer.gui.tools.ToolsController;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

/**
 * Main window handler class. Initialization of application goes here.
 * 
 * Singleton.
 */
public class MainController implements ActionListener {
	// data fields for main class.
	
	private EventBus eventBus;
	private DaxploreFile daxploreFile = null;

	private MainView mainView;
	private ButtonPanelView buttonPanelView;

	private OpenFileController openFileController;
	private GroupsController groupsController;
	private EditTextController editTextController;
	private ToolsController toolsController;
	private NavigationController navigationController;
	private QuestionController questionController;
	private TimeSeriesController timeSeriesController;
	
	private HistoryItem currentHistoryItem; 
	private Stack<HistoryItem> history = new Stack<>();
	
	//TODO remove direct spss file reference
	private File spssFile;

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
			this.view = view;
			this.command = command;
		}
	}
	
	public MainController(JFrame mainWindow, EventBus eventBus, DaxploreFile daxploreFile) {
		this.eventBus = eventBus;
		eventBus.register(this);
		
		this.daxploreFile = daxploreFile;
		
		//TODO remove *this* as an argument, only needed for old import wizard
		openFileController = new OpenFileController(eventBus, mainWindow, this);
		groupsController = new GroupsController(eventBus, mainWindow);
		editTextController = new EditTextController(eventBus);
		toolsController = new ToolsController(eventBus);
		navigationController = new NavigationController(eventBus);
		questionController = new QuestionController(eventBus);
		timeSeriesController = new TimeSeriesController(eventBus);

		buttonPanelView = new ButtonPanelView(this);

		mainView = new MainView(mainWindow);

		mainView.addView(openFileController.getView(), Views.OPENFILEVIEW);
		mainView.addView(groupsController.getView(), Views.GROUPSVIEW);
		mainView.addView(editTextController.getView(), Views.EDITTEXTVIEW);
		mainView.addView(toolsController.getView(), Views.TOOLSVIEW);
		mainView.addView(questionController.getView(), Views.QUESTIONVIEW);
		mainView.addView(timeSeriesController.getView(), Views.TIMESERIESVIEW);
		
		mainView.setButtonPanelView(buttonPanelView);
		mainView.setNavigationView(navigationController.getView());
		
		currentHistoryItem = new HistoryItem(Views.OPENFILEVIEW, null);
		
		eventBus.post(new DaxploreFileUpdateEvent(daxploreFile));
	}
	
	@Subscribe
	public void onDaxploreFileUpdate(DaxploreFileUpdateEvent e) {
		this.daxploreFile = e.getDaxploreFile();
		buttonPanelView.setActive(daxploreFile != null);
	}
	
	@Subscribe
	public void onViewChange(ChangeMainViewEvent e) {
		// Only adds history if it's a completely new view.
		// Should maybe be changed to take the command into account?
		if(currentHistoryItem != null && currentHistoryItem.view != e.getView()) {
			history.push(currentHistoryItem);
			navigationController.setHistoryAvailible(true);
		}
		currentHistoryItem = new HistoryItem(e.getView(), e.getCommand());
		setView(e.getView(), e.getCommand());
	}

	@Subscribe
	public void onHistoryGoBack(HistoryGoBackEvent e) {
		currentHistoryItem = history.pop();
		navigationController.setHistoryAvailible(!history.empty());
		setView(currentHistoryItem.view, currentHistoryItem.command);
	}
	
	private void setView(Views view, Object command) {
		buttonPanelView.setActiveButton(view);
		setToolbar(view);
		mainView.switchTo(view);
		
		if(command != null) {
			switch(view) {
			case OPENFILEVIEW:
				break;
			case EDITTEXTVIEW:
				if(command instanceof TextReference) {
					editTextController.jumpToTextReference((TextReference)command);
				}
				break;
			case GROUPSVIEW:
				break;
			case TOOLSVIEW:
				break;
			case QUESTIONVIEW:
				if(command instanceof MetaQuestion) {
					questionController.openMetaQuestion((MetaQuestion)command);
				}
				break;
			case TIMESERIESVIEW:
			default:
				throw new AssertionError("Undefined history item command: " + view);
			}
		}
	}
	
	public boolean hasHistory() {
		return history.empty();
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
			eventBus.post(new ChangeMainViewEvent(view));
		} catch (IllegalArgumentException e2) {
			//place for other types of buttons
		}
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
	//TODO remove when not needed by the old importer
	
	public DaxploreFile getDaxploreFile() {
		return daxploreFile;
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
