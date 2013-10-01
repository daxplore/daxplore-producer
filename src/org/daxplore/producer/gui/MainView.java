package org.daxplore.producer.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.MatteBorder;

import org.daxplore.producer.gui.MainController.Views;

public class MainView {

	private JFrame mainControllerFrame;
	private final JPanel mainPanel = new JPanel();
	private JPanel panel;
	
	private CardLayout mainLayout;
	
	//TODO figure out if we can move the main method to somewhere else?
	/**
	 * Main execution loop, includes the thread handler, required for swing
	 * applications.
	 * 
	 * Do not move the main() method from this file as it will
	 * break windowbuilder parsing.
	 */
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
		} catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
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
				MainController mainController = new MainController();
				mainController.showWindow(true);
			}
		});
	}
	
	MainView(ButtonPanelView buttonPanelView) {
		mainControllerFrame = new JFrame();
		mainControllerFrame.setIconImage(Toolkit.getDefaultToolkit().getImage(MainController.class.getResource("/org/daxplore/producer/gui/resources/Colorful_Chart_Icon_vol2.png")));
		mainControllerFrame.setTitle("Daxplore Producer Developer Version");
		mainControllerFrame.setBounds(100, 100, 900, 787);
		mainControllerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainControllerFrame.getContentPane().setLayout(new BorderLayout(0, 0));
		
		// panel views. TODO: Remake the controller interface.
		mainControllerFrame.getContentPane().add(buttonPanelView, BorderLayout.WEST);
		
		panel = new JPanel();
		mainControllerFrame.getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));
		panel.add(mainPanel, BorderLayout.CENTER);
		
		// create main panel window.
		mainPanel.setBorder(new MatteBorder(0, 1, 0, 0, Color.GRAY));
		mainLayout = new CardLayout(0,0);
		mainPanel.setLayout(mainLayout);

		mainPanel.setRequestFocusEnabled(true);
		mainControllerFrame.setVisible(true);
	}
	
	void showWindow(boolean show) {
		mainControllerFrame.setVisible(show);
	}
	
	void addView(Component component, Views view) {
		mainPanel.add(component, view.toString());
	}
	
	void switchTo(Views view) {
	    mainLayout.show(mainPanel, view.toString());
	}
	
	JFrame getMainFrame() {
		return mainControllerFrame;
	}
}
