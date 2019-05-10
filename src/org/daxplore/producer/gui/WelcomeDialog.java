/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui;

import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.daxplorelib.DaxploreFile;
import org.daxplore.producer.daxplorelib.resources.DaxploreProperties;
import org.daxplore.producer.gui.event.DaxploreFileUpdateEvent;
import org.daxplore.producer.gui.resources.IconResources;
import org.daxplore.producer.gui.utility.DaxploreLogger;
import org.daxplore.producer.gui.utility.GuiTools;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class WelcomeDialog {

	private JFrame welcomeFrame;
	
	public static void main(String[] args) {
		try {
			DaxploreLogger.setup();
		} catch (IOException e2) {
			System.out.println("Couldn't set upp logger");
			e2.printStackTrace();
		}
		
		// do a java version check, if target system doesn't have java 7, exit.
		if (GuiTools.javaVersionCheck() != true) {
			JOptionPane.showMessageDialog(null,
					"This program only supports Java 7 or higher.",
					"Daxplore warning",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
			
		
		// Use the Nimbus look and feel (Java 6+)
		try {
		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
		    }
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			try {
				UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		// thread handler for main window.
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					WelcomeDialog welcomeDialog = new WelcomeDialog();
					welcomeDialog.welcomeFrame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	
	EventBus eventBus;
	DaxplorePreferences preferences;
	
	public WelcomeDialog() {
		welcomeFrame = new JFrame();
		eventBus = new EventBus();
		eventBus.register(this);
		preferences = new DaxplorePreferences();
		
		welcomeFrame.setSize(500, 300);
		welcomeFrame.setLocationRelativeTo(null);
		welcomeFrame.setIconImage(IconResources.getImage("window-icon.png"));
		welcomeFrame.setTitle("Daxplore Producer Developer Version");
		welcomeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		welcomeFrame.getContentPane().setLayout(new GridLayout(1, 2));
		
		JButton newProjectButton = new JButton("Create new project");
		newProjectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DaxploreWizard wizard = DaxploreWizard.createNewFileWizard(welcomeFrame, eventBus, preferences);
				wizard.show();
			}
		});
		welcomeFrame.add(newProjectButton);
		
		JButton existingProjectButton = new JButton("Open existing project");
		existingProjectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser(preferences.getWorkingDirectory());
			    FileNameExtensionFilter filter = new FileNameExtensionFilter(
			        "Daxplore project files (.daxplore)", "daxplore");
			    fileChooser.setFileFilter(filter);
			    int returnVal = fileChooser.showOpenDialog(welcomeFrame);
			    if(returnVal == JFileChooser.APPROVE_OPTION) {
			    	preferences.setWorkingDirectory(fileChooser.getCurrentDirectory());
					final File file = fileChooser.getSelectedFile();
					System.out.println("Opening: " + file.getName() + ".");
					
					try {
						// Open file, will throw Exception if it's not an SQLite file
						DaxploreFile daxploreFile = DaxploreFile.createFromExistingFile(file);
						
						// Check that the SQLite application ID matched the one used by Daxplore Producer
						if (!daxploreFile.hasValidApplicationID()) {
							// TODO direct user error dialog
							throw new DaxploreException("Not a Daxplore save file."
								+ " This file uses SQLite application ID " + daxploreFile.getFileApplicationVersion() + "."
								+ " Daxplore files use application ID " + DaxploreProperties.daxploreFileApplicationID);
						}
						
						// Check if the file is from a newer version of Daxplore Producer
						if (daxploreFile.getFileVersion() > DaxploreProperties.daxploreFileVersion) {
							// TODO direct user error dialog
							throw new DaxploreException("File version type is " + daxploreFile.getFileVersion() 
									+ ", but this version of Daxplore Producer only supports file version "
									+ DaxploreProperties.daxploreFileVersion
									+ " or lower. Please upgrade your Daxplore Producer."); 
						}
						
						// Check if the file is from an older version of Daxplore Producer.
						// Offer to upgrade old files to the current file version.
						if (daxploreFile.getFileVersion() < DaxploreProperties.daxploreFileVersion) {
							boolean confirmUpgrade = Dialogs.confirmDaxploreFileUpgrade(welcomeFrame, file.getName(), daxploreFile.getFileVersion());
							if (confirmUpgrade) {
								daxploreFile.upgradeFileVersion();
							} else {
								// Fall back to initial new/open dialog
								return;
							}
						}
						
						daxploreFile.initializeInternalStructures();
						eventBus.post(new DaxploreFileUpdateEvent(daxploreFile));
					} catch (DaxploreException e2) {
						// TODO more complete error message, including stack trace?
						e2.printStackTrace();
						JOptionPane.showMessageDialog(welcomeFrame, e2.getMessage());
					}
				} else {
					System.out.println("Open command cancelled by user.");
				}
			}
		});
		welcomeFrame.add(existingProjectButton);
	}
	
	@Subscribe
	public void on(final DaxploreFileUpdateEvent event) {
		try {
			eventBus.unregister(this);
	
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					try {
						JFrame window = new JFrame();
						MainController mainController = new MainController(window, eventBus, preferences, event.getDaxploreFile());
						mainController.setVisible(true);
						
						welcomeFrame.setVisible(false);
						welcomeFrame.dispose();
					} catch (Exception e1) {
						// TODO more complete error message, including stack trace?
						e1.printStackTrace();
						JOptionPane.showMessageDialog(welcomeFrame, e1.getMessage());
					}
				}
			});
		} catch (Exception ex) {
			Logger.getGlobal().log(Level.SEVERE, "Failed to handle event", ex);
		}
	}
}
