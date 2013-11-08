package org.daxplore.producer.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Stack;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.DaxploreFile;
import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;
import org.daxplore.producer.gui.edit.EditTextController;
import org.daxplore.producer.gui.event.ChangeMainViewEvent;
import org.daxplore.producer.gui.event.DaxploreFileUpdateEvent;
import org.daxplore.producer.gui.event.EmptyEvents.HistoryGoBackEvent;
import org.daxplore.producer.gui.event.EmptyEvents.QuitProgramEvent;
import org.daxplore.producer.gui.event.EmptyEvents.SaveFileEvent;
import org.daxplore.producer.gui.event.ErrorMessageEvent;
import org.daxplore.producer.gui.event.HistoryAvailableEvent;
import org.daxplore.producer.gui.groups.GroupsController;
import org.daxplore.producer.gui.navigation.NavigationController;
import org.daxplore.producer.gui.open.OpenFileController;
import org.daxplore.producer.gui.question.QuestionController;
import org.daxplore.producer.gui.resources.GuiTexts;
import org.daxplore.producer.gui.timeseries.TimeSeriesController;
import org.daxplore.producer.gui.tools.ToolsController;

import com.google.common.base.Throwables;
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
	private JFrame mainWindow;
	private ActionManager actionManager;
	private GuiTexts guiTexts;

	private MainView mainView;
	private ButtonPanelView buttonPanelView;

	private MenuBarController menuBarController;
	private ToolbarController toolbarController;
	
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
	
	public MainController(JFrame mainWindow, final EventBus eventBus, DaxploreFile daxploreFile) {
		this.mainWindow = mainWindow;
		this.eventBus = eventBus;
		this.daxploreFile = daxploreFile;

		eventBus.register(this);
		guiTexts = new GuiTexts(Locale.ENGLISH);
		
		actionManager = new ActionManager(eventBus, guiTexts);
		
		mainWindow.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		mainWindow.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				eventBus.post(new QuitProgramEvent());
			}
		});
		
		
		//TODO remove *this* as an argument, only needed for old import wizard
		menuBarController = new MenuBarController(actionManager);
		toolbarController = new ToolbarController(eventBus, actionManager);
		openFileController = new OpenFileController(eventBus, mainWindow, this);
		groupsController = new GroupsController(eventBus, mainWindow);
		editTextController = new EditTextController(eventBus);
		toolsController = new ToolsController(eventBus);
		navigationController = new NavigationController(eventBus);
		questionController = new QuestionController(eventBus);
		timeSeriesController = new TimeSeriesController(eventBus);

		buttonPanelView = new ButtonPanelView(this);

		mainView = new MainView(mainWindow);
		
		mainView.setMenuBar(menuBarController.getView());
		
		mainView.addTab(guiTexts.get("view.open_file.tab"), openFileController.getView(), Views.OPENFILEVIEW);
		mainView.addTab(guiTexts.get("view.groups.tab"), groupsController.getView(), Views.GROUPSVIEW);
		mainView.addTab(guiTexts.get("view.edit_text.tab"), editTextController.getView(), Views.EDITTEXTVIEW);
		mainView.addTab(guiTexts.get("view.tools.tab"), toolsController.getView(), Views.TOOLSVIEW);
		mainView.addTab(guiTexts.get("view.questions.tab"), questionController.getView(), Views.QUESTIONVIEW);
		mainView.addTab(guiTexts.get("view.time_series.tab"), timeSeriesController.getView(), Views.TIMESERIESVIEW);
		
		mainView.setToolbar(toolbarController.getView());
		mainView.setNavigationView(navigationController.getView());
		
		mainView.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				eventBus.post(new ChangeMainViewEvent(mainView.getSelectedView()));
			}
		});
		
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
		if(!history.empty()) {
			currentHistoryItem = history.pop();
			eventBus.post(new HistoryAvailableEvent(!history.empty()));
			setView(currentHistoryItem.view, currentHistoryItem.command);
		}
	}
	
	@Subscribe
	public void onErrorMessage(ErrorMessageEvent e) {
		Object[] options = {guiTexts.get("dialog.error.continue"), guiTexts.get("dialog.error.show_debug")};
		int answer = JOptionPane.showOptionDialog(mainWindow,
				new JLabelSelectable(e.getUserMessage()),
				guiTexts.get("dialog.error.title"),
		        JOptionPane.DEFAULT_OPTION,
		        JOptionPane.ERROR_MESSAGE,
		        null,
		        options,
		        options[0]);
		if(answer == 1) {
			JOptionPane.showMessageDialog(mainWindow, 
					new JLabelSelectable(Throwables.getStackTraceAsString(e.getCause())), 
					guiTexts.get("dialog.error.debug_title"),
					JOptionPane.PLAIN_MESSAGE);
		}
	}
	
	@Subscribe
	public void onSaveFile(SaveFileEvent e) {
		try {
			daxploreFile.saveAll();
		} catch (DaxploreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	@Subscribe
	public void onProgramQuit(QuitProgramEvent e) {
		if(daxploreFile.getUnsavedChangesCount() == 0) {
			try {
				daxploreFile.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			mainWindow.dispose();
		} else {
			Object[] options = { "Save and quit", "Discard changes", "Do not quit" };
			int choice = JOptionPane.showOptionDialog(mainWindow,
					"Do you want to save your changes before closing the program?", "Save changes - Daxplore Presenter",
					JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
			switch(choice) {
			case JOptionPane.YES_OPTION:
				try {
					daxploreFile.saveAll();
				} catch (DaxploreException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					daxploreFile.close();
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				mainWindow.dispose();
				break;
			case JOptionPane.NO_OPTION:
				try {
					daxploreFile.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				mainWindow.dispose();
				break;
			case JOptionPane.CANCEL_OPTION:
			default:
				// do nothing
			}
		}
	}
	
	private void setView(Views view, Object command) {
		buttonPanelView.setActiveButton(view);
		setToolbar(view);
		if(mainView.getSelectedView() != view) {
			mainView.switchTo(view);
		}
		
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
