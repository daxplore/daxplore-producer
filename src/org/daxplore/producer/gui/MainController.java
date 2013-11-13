package org.daxplore.producer.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.DaxploreFile;
import org.daxplore.producer.daxplorelib.ImportExportManager.L10nFormat;
import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;
import org.daxplore.producer.gui.Dialogs.FileLocalePair;
import org.daxplore.producer.gui.edit.EditTextController;
import org.daxplore.producer.gui.event.ChangeMainViewEvent;
import org.daxplore.producer.gui.event.DaxploreFileUpdateEvent;
import org.daxplore.producer.gui.event.EmptyEvents.DiscardChangesEvent;
import org.daxplore.producer.gui.event.EmptyEvents.ExportTextsEvent;
import org.daxplore.producer.gui.event.EmptyEvents.ExportUploadEvent;
import org.daxplore.producer.gui.event.EmptyEvents.HistoryGoBackEvent;
import org.daxplore.producer.gui.event.EmptyEvents.ImportTextsEvent;
import org.daxplore.producer.gui.event.EmptyEvents.QuitProgramEvent;
import org.daxplore.producer.gui.event.EmptyEvents.SaveFileEvent;
import org.daxplore.producer.gui.event.ErrorMessageEvent;
import org.daxplore.producer.gui.event.HistoryAvailableEvent;
import org.daxplore.producer.gui.groups.GroupsController;
import org.daxplore.producer.gui.question.QuestionController;
import org.daxplore.producer.gui.resources.GuiTexts;
import org.daxplore.producer.gui.timeseries.TimeSeriesController;
import org.daxplore.producer.gui.tools.ToolsController;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

/**
 * Main window handler class. Initialization of application goes here.
 * 
 * Singleton.
 */
public class MainController {
	private DaxploreFile daxploreFile = null;

	private EventBus eventBus;
	private DaxplorePreferences preferences;
	private JFrame mainWindow;
	private ActionManager actionManager;
	private GuiTexts texts;

	private MainView mainView;

	private MenuBarController menuBarController;
	private ToolbarController toolbarController;
	
	private GroupsController groupsController;
	private EditTextController editTextController;
	private ToolsController toolsController;
	private QuestionController questionController;
	private TimeSeriesController timeSeriesController;
	
	private HistoryItem currentHistoryItem; 
	private Stack<HistoryItem> history = new Stack<>();
	
	public enum Views {
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
	
	public MainController(JFrame mainWindow, final EventBus eventBus, GuiTexts texts,
			DaxplorePreferences preferences, DaxploreFile daxploreFile) {
		this.mainWindow = mainWindow;
		this.eventBus = eventBus;
		this.texts = texts;
		this.preferences = preferences;
		this.daxploreFile = daxploreFile;

		eventBus.register(this);
		
		actionManager = new ActionManager(eventBus, texts);
		
		mainWindow.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		mainWindow.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				eventBus.post(new QuitProgramEvent());
			}
		});
		
		
		menuBarController = new MenuBarController(actionManager);
		toolbarController = new ToolbarController(eventBus, actionManager);
		groupsController = new GroupsController(eventBus, mainWindow);
		editTextController = new EditTextController(eventBus);
		toolsController = new ToolsController(eventBus, preferences);
		questionController = new QuestionController(eventBus);
		timeSeriesController = new TimeSeriesController(eventBus);

		mainView = new MainView(mainWindow);
		
		mainView.setMenuBar(menuBarController.getView());
		
		mainView.addTab(texts.get("view.groups.tab"), groupsController.getView(), Views.GROUPSVIEW);
		mainView.addTab(texts.get("view.edit_text.tab"), editTextController.getView(), Views.EDITTEXTVIEW);
		mainView.addTab(texts.get("view.tools.tab"), toolsController.getView(), Views.TOOLSVIEW);
		mainView.addTab(texts.get("view.questions.tab"), questionController.getView(), Views.QUESTIONVIEW);
		mainView.addTab(texts.get("view.time_series.tab"), timeSeriesController.getView(), Views.TIMESERIESVIEW);
		
		mainView.setToolbar(toolbarController.getView());
		
		mainView.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				eventBus.post(new ChangeMainViewEvent(mainView.getSelectedView()));
			}
		});
		
		eventBus.post(new DaxploreFileUpdateEvent(daxploreFile));
	}
	
	@Subscribe
	public void on(DaxploreFileUpdateEvent e) {
		this.daxploreFile = e.getDaxploreFile();
	}
	
	@Subscribe
	public void on(DiscardChangesEvent e) {
		try {
			daxploreFile.discardChanges();
			eventBus.post(new DaxploreFileUpdateEvent(daxploreFile));
		} catch (DaxploreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	@Subscribe
	public void on(ChangeMainViewEvent e) {
		// Only adds history if it's a completely new view.
		// Should maybe be changed to take the command into account?
		if(currentHistoryItem != null && currentHistoryItem.view != e.getView()) {
			history.push(currentHistoryItem);
		}
		currentHistoryItem = new HistoryItem(e.getView(), e.getCommand());
		setView(e.getView(), e.getCommand());
	}

	@Subscribe
	public void on(HistoryGoBackEvent e) {
		if(!history.empty()) {
			currentHistoryItem = history.pop();
			eventBus.post(new HistoryAvailableEvent(!history.empty()));
			setView(currentHistoryItem.view, currentHistoryItem.command);
		}
	}
	
	@Subscribe
	public void on(ExportTextsEvent e) {
		try {
			List<Locale> localeList = daxploreFile.getTextReferenceManager().getAllLocales();
			FileLocalePair fileLocalePair = Dialogs.showExportDialog(mainWindow, localeList, preferences);
			File file = fileLocalePair.file;
			Locale locale = fileLocalePair.locale;
			if(fileLocalePair.file == null) {
				System.out.println("File is null");
				return; //TODO communicate error properly
			} 
			
			L10nFormat format;
			String filename = file.toPath().getFileName().toString().toLowerCase();
			//TODO allow different suffixes and user selection of type?
			if(filename.endsWith(".csv")) {
				format = L10nFormat.CSV;
			} else if(filename.endsWith(".properties")) {
				format = L10nFormat.PROPERTIES;
			} else {
				System.out.println("Unsupported file suffix: " + filename);
				return; //TODO communicate error properly
			}
			
			if(file.exists() && file.canWrite()) {
				try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), Charsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
					daxploreFile.exportL10n(writer, format, locale);
				}
			} else if (!file.exists()) {
				try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), Charsets.UTF_8, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
					daxploreFile.exportL10n(writer, format, locale);
				}
			} else {
				System.out.println("File is write protected");
				return; //TODO communicate error properly
			}

		} catch (DaxploreException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	@Subscribe
	public void on(ImportTextsEvent e) {
		List<Locale> localeList = Settings.availableLocales();
		FileLocalePair fileLocalePair = Dialogs.showImportDialog(mainWindow, localeList, preferences);
		File file = fileLocalePair.file;
		Locale locale = fileLocalePair.locale;
		if(file != null && file.exists() && file.canRead()) {
			
			L10nFormat format;
			String filename = file.toPath().getFileName().toString().toLowerCase();
			//TODO allow different suffixes and user selection of type?
			if(filename.endsWith(".csv")) {
				format = L10nFormat.CSV;
			} else if(filename.endsWith(".properties")) {
				format = L10nFormat.PROPERTIES;
			} else {
				System.out.println("Unsupported file suffix: " + filename); //TODO communicate error properly
				return;
			}
			
			daxploreFile.getAbout().addLocale(locale);
			try {
				daxploreFile.importL10n(
						Files.newBufferedReader(file.toPath(), Charsets.UTF_8), format, locale);
			} catch (FileNotFoundException e1) {
				throw new AssertionError("File exists but is not found");
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (DaxploreException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	@Subscribe
	public void on(ErrorMessageEvent e) {
		Object[] options = {texts.get("dialog.error.continue"), texts.get("dialog.error.show_debug")};
		if(e.getCause() == null) {
			JLabelSelectable message = new JLabelSelectable(e.getUserMessage());
			JOptionPane.showMessageDialog(mainWindow, message);
		} else {
			JLabelSelectable message = new JLabelSelectable(
					texts.format("dialog.error.message", e.getUserMessage(), e.getCause().getLocalizedMessage()));
			int answer = JOptionPane.showOptionDialog(mainWindow,
					message,
					texts.get("dialog.error.title"),
			        JOptionPane.DEFAULT_OPTION,
			        JOptionPane.ERROR_MESSAGE,
			        null,
			        options,
			        options[0]);
			if(answer == 1) {
				JPanel debugMessage = new JPanel(new BorderLayout(0, 10));
				debugMessage.add(message, BorderLayout.NORTH);
				JTextArea stacktraceText = new JTextArea(Throwables.getStackTraceAsString(e.getCause()));
				stacktraceText.setEditable(false);
				JScrollPane stacktraceScrollPane = new JScrollPane(stacktraceText);
				stacktraceScrollPane.setPreferredSize(new Dimension((int)stacktraceScrollPane.getPreferredSize().getWidth()+30, 300));
				debugMessage.add(stacktraceScrollPane, BorderLayout.CENTER);
				JOptionPane.showMessageDialog(mainWindow, 
						debugMessage, 
						texts.get("dialog.error.debug_title"),
						JOptionPane.PLAIN_MESSAGE);
			}
		}
	}
	
	@Subscribe
	public void on(SaveFileEvent e) {
		try {
			daxploreFile.saveAll();
		} catch (DaxploreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	@Subscribe
	public void on(QuitProgramEvent e) {
		if(daxploreFile.getUnsavedChangesCount() == 0) {
			try {
				daxploreFile.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			mainWindow.dispose();
		} else {
			Object[] options = {texts.get("dialog.quit.save_and_quit"),
					texts.get("dialog.quit.discard_and_quit"),
					texts.get("dialog.quit.do_not_quit")};
			int choice = JOptionPane.showOptionDialog(mainWindow,
					texts.get("dialog.quit.question"),
					texts.get("dialog.quit.title"),
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null,
					options,
					options[2]);
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
	
	@Subscribe
	public void on(ExportUploadEvent e) {
		File exportTo = Dialogs.showExportDialog(mainWindow, preferences);
		try {
			daxploreFile.writeUploadFile(exportTo);
		} catch (DaxploreException e1) {
			eventBus.post(new ErrorMessageEvent("Error while generating export file", e1));
		}
		
	}
	
	private void setView(Views view, Object command) {
		if(mainView.getSelectedView() != view) {
			mainView.switchTo(view);
		}
		
		if(command != null) {
			switch(view) {
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
}
