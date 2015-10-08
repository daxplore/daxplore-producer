/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Locale;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JDialog;
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
import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;
import org.daxplore.producer.gui.Dialogs.FileLocalePair;
import org.daxplore.producer.gui.Dialogs.FileLocaleUsedTriplet;
import org.daxplore.producer.gui.event.ChangeMainViewEvent;
import org.daxplore.producer.gui.event.DaxploreFileUpdateEvent;
import org.daxplore.producer.gui.event.DisplayLocaleSelectEvent;
import org.daxplore.producer.gui.event.EditQuestionEvent;
import org.daxplore.producer.gui.event.EmptyEvents.DiscardChangesEvent;
import org.daxplore.producer.gui.event.EmptyEvents.ExportTextsEvent;
import org.daxplore.producer.gui.event.EmptyEvents.ExportUploadEvent;
import org.daxplore.producer.gui.event.EmptyEvents.HistoryGoBackEvent;
import org.daxplore.producer.gui.event.EmptyEvents.ImportTextsEvent;
import org.daxplore.producer.gui.event.EmptyEvents.QuitProgramEvent;
import org.daxplore.producer.gui.event.EmptyEvents.ReloadTextsEvent;
import org.daxplore.producer.gui.event.EmptyEvents.RepaintWindowEvent;
import org.daxplore.producer.gui.event.EmptyEvents.SaveFileEvent;
import org.daxplore.producer.gui.event.ErrorMessageEvent;
import org.daxplore.producer.gui.event.HistoryAvailableEvent;
import org.daxplore.producer.gui.menu.ActionManager;
import org.daxplore.producer.gui.menu.MenuBarController;
import org.daxplore.producer.gui.menu.ToolbarController;
import org.daxplore.producer.gui.resources.GuiTexts;
import org.daxplore.producer.gui.settings.SettingsController;
import org.daxplore.producer.gui.utility.JLabelSelectable;
import org.daxplore.producer.gui.view.build.GroupsController;
import org.daxplore.producer.gui.view.text.EditTextController;
import org.daxplore.producer.gui.view.time.TimeSeriesController;
import org.daxplore.producer.gui.view.variable.VariableController;

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
	private TimeSeriesController timeSeriesController;
	private SettingsController settingsController;
	
	private HistoryItem currentHistoryItem; 
	private Stack<HistoryItem> history = new Stack<>();
	
	public enum Views {
		EDITTEXTVIEW,
		GROUPSVIEW,
		TIMESERIESVIEW,
		SETTINGSVIEW
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
		groupsController = new GroupsController(eventBus, texts);
		editTextController = new EditTextController(eventBus, texts, actionManager);
		timeSeriesController = new TimeSeriesController(daxploreFile.getAbout(), eventBus, texts);
		settingsController = new SettingsController(eventBus, texts);

		mainView = new MainView(mainWindow);
		
		mainView.setMenuBar(menuBarController.getView());
		
		mainView.addTab(texts.get("view.groups.tab"), groupsController.getView(), Views.GROUPSVIEW);
		mainView.addTab(texts.get("view.edit_text.tab"), editTextController.getView(), Views.EDITTEXTVIEW);
		mainView.addTab(texts.get("view.time_series.tab"), timeSeriesController.getView(), Views.TIMESERIESVIEW);
		mainView.addTab(texts.get("view.settings.tab"), settingsController.getView(), Views.SETTINGSVIEW);
		
		mainView.setToolbar(toolbarController.getView());
		
		mainView.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				eventBus.post(new ChangeMainViewEvent(mainView.getSelectedView()));
			}
		});
		
		eventBus.post(new DaxploreFileUpdateEvent(daxploreFile));
		toolbarController.updateSelectedLocale();
	}
	
	public void setVisible(boolean visible) {
		mainView.setVisibe(visible);
	}
	
	@Subscribe
	public void on(DaxploreFileUpdateEvent e) {
		try {
			this.daxploreFile = e.getDaxploreFile();
		} catch (Exception ex) {
			Logger.getGlobal().log(Level.SEVERE, "Failed to handle event", ex);
		}
	}
	
	@Subscribe
	public void on(DiscardChangesEvent e) {
		try {
			daxploreFile.discardChanges();
			eventBus.post(new DaxploreFileUpdateEvent(daxploreFile));
		} catch (Exception ex) {
			Logger.getGlobal().log(Level.SEVERE, "Failed to handle event", ex);
		}
	}
	
	@Subscribe
	public void on(ChangeMainViewEvent e) {
		try {
			// Only adds history if it's a completely new view.
			// Should maybe be changed to take the command into account?
			if(currentHistoryItem != null && currentHistoryItem.view != e.getView()) {
				history.push(currentHistoryItem);
			}
			currentHistoryItem = new HistoryItem(e.getView(), e.getCommand());
			setView(e.getView(), e.getCommand());
		} catch (Exception ex) {
			Logger.getGlobal().log(Level.SEVERE, "Failed to handle event", ex);
		}
	}

	@Subscribe
	public void on(HistoryGoBackEvent e) {
		try {
			if(!history.empty()) {
				currentHistoryItem = history.pop();
				eventBus.post(new HistoryAvailableEvent(!history.empty()));
				setView(currentHistoryItem.view, currentHistoryItem.command);
			}
		} catch (Exception ex) {
			Logger.getGlobal().log(Level.SEVERE, "Failed to handle event", ex);
		}
	}
	
	@Subscribe
	public void on(ExportTextsEvent e) {
		try {
			List<Locale> localeList = daxploreFile.getTextReferenceManager().getAllLocales();
			FileLocaleUsedTriplet fileLocaleUsedTriplet = Dialogs.showExportDialog(preferences, texts, mainWindow, localeList);
			if(fileLocaleUsedTriplet == null) {
				return;
			}
			File file = fileLocaleUsedTriplet.file;
			Locale locale = fileLocaleUsedTriplet.locale;
			if(fileLocaleUsedTriplet.file == null) {
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
					//TODO use fileLocaleUsedTriplet.usedXXX
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
		} catch (Exception ex) {
			Logger.getGlobal().log(Level.SEVERE, "Failed to handle event", ex);
		}
	}
	
	@Subscribe
	public void on(ImportTextsEvent e) {
		try {
			List<Locale> localeList = Settings.availableLocales();
			FileLocalePair fileLocalePair = Dialogs.showImportDialog(preferences, texts, mainWindow, localeList);
			
			if(fileLocalePair == null) {
				return;
			}
			
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
					eventBus.post(new ReloadTextsEvent());
				} catch (IOException | DaxploreException e1) {
					eventBus.post(new ErrorMessageEvent("Failed to import texts", e1));
				}
			}
		} catch (Exception ex) {
			Logger.getGlobal().log(Level.SEVERE, "Failed to handle event", ex);
		}
	}
	
	@Subscribe
	public void on(EditQuestionEvent e) {
		try {
			VariableController vc = new VariableController(eventBus, texts, daxploreFile, e.getMetaQuestion());
			JDialog dialog = vc.getDialog();
			dialog.setSize(800, 800);
			dialog.setLocationRelativeTo(mainWindow);
			dialog.setVisible(true);
			eventBus.post(new ReloadTextsEvent());
		} catch (Exception ex) {
			Logger.getGlobal().log(Level.SEVERE, "Failed to handle event", ex);
		}
	}
	
	@Subscribe
	public void on(DisplayLocaleSelectEvent e) {
		try {
			Settings.setCurrentDisplayLocale(e.getLocale());
		} catch (Exception ex) {
			Logger.getGlobal().log(Level.SEVERE, "Failed to handle event", ex);
		}
	}
	
	@Subscribe
	public void on(ErrorMessageEvent e) {
		try {
			Object[] options = {texts.get("dialog.error.continue"), texts.get("dialog.error.show_debug")};
			if(e.getCause() == null) {
				JLabelSelectable message = new JLabelSelectable(e.getUserMessage());
				JOptionPane.showMessageDialog(mainWindow, message);
			} else {
				JLabelSelectable message = new JLabelSelectable(
						texts.format("dialog.error.message", e.getUserMessage(), e.getCause().getLocalizedMessage().replace("\n", "<br>")));
				Logger.getGlobal().log(Level.SEVERE, e.getUserMessage());
				e.getCause().printStackTrace();
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
		} catch (Exception ex) {
			Logger.getGlobal().log(Level.SEVERE, "Failed to handle event", ex);
		}
	}
	
	@Subscribe
	public void on(SaveFileEvent e) {
		try {
			daxploreFile.saveAll();
		} catch (Exception ex) {
			Logger.getGlobal().log(Level.SEVERE, "Failed to handle event", ex);
		}
	}
	
	@Subscribe
	public void on(QuitProgramEvent e) {
		try {
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
		} catch (Exception ex) {
			Logger.getGlobal().log(Level.SEVERE, "Failed to handle event", ex);
		}
	}
	
	@Subscribe
	public void on(RepaintWindowEvent e) {
		try {
			mainWindow.repaint();
		} catch (Exception ex) {
			Logger.getGlobal().log(Level.SEVERE, "Failed to handle event", ex);
		}
	}
	
	@Subscribe
	public void on(ExportUploadEvent e) {
		try {
			File exportTo = Dialogs.showExportDialog(mainWindow, preferences);
			if(exportTo == null) {
				return;
			}
			if(exportTo.exists()) {
				if(Dialogs.confirmOverwrite(mainWindow, texts, exportTo.getName())) {
					exportTo.delete();
				} else {
					return;
				}
			}
			try {
				daxploreFile.writeUploadFile(exportTo);
			} catch (DaxploreException e1) {
				eventBus.post(new ErrorMessageEvent("Error while generating export file", e1));
			}
		} catch (Exception ex) {
			Logger.getGlobal().log(Level.SEVERE, "Failed to handle event", ex);
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
			case TIMESERIESVIEW:
			default:
				throw new AssertionError("Undefined history item command: " + view);
			}
		}
	}
}
