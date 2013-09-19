package org.daxplore.producer.gui;

import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.daxplore.producer.daxplorelib.DaxploreFile;

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
			
		
		// set the look and feel here, currently we use nimbus.
		// only available from java 6 and up though.
		try {
		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
		    }
		} catch (Exception e) {
			try {
				UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
			} catch (ClassNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InstantiationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IllegalAccessException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (UnsupportedLookAndFeelException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		// thread handler for main window.
		EventQueue.invokeLater(new Runnable() {
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
	
	public WelcomeDialog() {
		welcomeFrame = new JFrame();
		
		welcomeFrame.setSize(500, 300);
		welcomeFrame.setLocationRelativeTo(null);
		welcomeFrame.setIconImage(Toolkit.getDefaultToolkit().getImage(MainController.class.getResource("/org/daxplore/producer/gui/resources/Colorful_Chart_Icon_vol2.png")));
		welcomeFrame.setTitle("Daxplore Producer Developer Version");
		welcomeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		welcomeFrame.getContentPane().setLayout(new GridLayout(1, 2));
		
		JButton newProjectButton = new JButton("Create new project");
		newProjectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
			}
		});
		welcomeFrame.add(newProjectButton);
		
		JButton existingProjectButton = new JButton("Open existing project");
		existingProjectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser(Settings.getWorkingDirectory());
			    FileNameExtensionFilter filter = new FileNameExtensionFilter(
			        "Daxplore project files (.daxplore)", "daxplore");
			    fileChooser.setFileFilter(filter);
			    int returnVal = fileChooser.showOpenDialog(welcomeFrame);
			    if(returnVal == JFileChooser.APPROVE_OPTION) {
			    	Settings.setWorkingDirectory(fileChooser.getCurrentDirectory());
					final File file = fileChooser.getSelectedFile();
					System.out.println("Opening: " + file.getName() + ".");
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							try {
								MainView window = new MainView();
								window.mainControllerFrame.setVisible(true);
								window.mainController.setDaxploreFile(DaxploreFile.createFromExistingFile(file));
								window.mainController.updateStuff();
								welcomeFrame.setVisible(false);
								welcomeFrame.dispose();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
				} else {
					System.out.println("Open command cancelled by user.");
				}
				
			}
		});
		welcomeFrame.add(existingProjectButton);
		
		
	}

}
