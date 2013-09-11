package org.daxplore.producer.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
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
import org.daxplore.producer.gui.edit.EditTextView;
import org.daxplore.producer.gui.groups.GroupsView;
import org.daxplore.producer.gui.navigation.NavigationView;
import org.daxplore.producer.gui.open.OpenFileView;
import org.daxplore.producer.gui.question.QuestionView;
import org.daxplore.producer.gui.timeseries.TimeSeriesView;
import org.daxplore.producer.gui.tools.ToolsView;

public class MainView {

	public JFrame mainControllerFrame;
	final JPanel mainPanel = new JPanel();
	private JPanel panel;
	
	MainController mainController;
	private CardLayout mainLayout;
	
	/**
	 * Main execution loop, includes the thread handler, required for swing
	 * applications. Do not move the main() method from this file as it will
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
					MainView window = new MainView();
					window.mainControllerFrame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * Create the application.
	 * @return 
	 */
	public MainView() {
		mainController = new MainController(this);
		
		mainControllerFrame = new JFrame();
		mainController.setMainFrame(mainControllerFrame);
		mainControllerFrame.setIconImage(Toolkit.getDefaultToolkit().getImage(MainController.class.getResource("/org/daxplore/producer/gui/resources/Colorful_Chart_Icon_vol2.png")));
		mainControllerFrame.setTitle("Daxplore Producer Developer Version");
		mainControllerFrame.setBounds(100, 100, 900, 787);
		mainControllerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainControllerFrame.getContentPane().setLayout(new BorderLayout(0, 0));
		

		
		// panel views. TODO: Remake the controller interface.
		mainController.setButtonPanelView(new ButtonPanelView(mainController));
		mainControllerFrame.getContentPane().add(mainController.getButtonPanelView(), BorderLayout.WEST);
		
		panel = new JPanel();
		mainControllerFrame.getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));
		panel.add(mainPanel, BorderLayout.CENTER);
		
		// create main panel window.
		mainPanel.setBorder(new MatteBorder(0, 1, 0, 0, (Color) Color.GRAY));
		mainLayout = new CardLayout(0,0);
		mainPanel.setLayout(mainLayout);
		
		mainController.setOpenFileView(new OpenFileView(mainController));
		mainPanel.add(mainController.getOpenFileView(), Views.OPENFILEVIEW.toString());
		
		mainController.setGroupsView(new GroupsView(mainController));
		mainPanel.add(mainController.getGroupsView(), Views.GROUPSVIEW.toString());
		
		mainController.setEditTextView(new EditTextView(mainController));
		mainPanel.add(mainController.getEditTextView(), Views.EDITTEXTVIEW.toString());
		
		mainController.setToolsView(new ToolsView(mainController));
		mainPanel.add(mainController.getToolsView(), Views.TOOLSVIEW.toString());
		
		mainController.setQuestionView(new QuestionView(mainController));
		mainPanel.add(mainController.getQuestionView(), Views.QUESTIONVIEW.toString());

		mainController.setTimeSeriesView(new TimeSeriesView(mainController));
		mainPanel.add(mainController.getTimeSeriesView(), Views.TIMESERIESVIEW.toString());
		
		mainPanel.setRequestFocusEnabled(true);
		
		mainController.setNavigationView(new NavigationView(mainController));
		panel.add(mainController.getNavigationView(), BorderLayout.SOUTH);
	}
	
	public void showInMain(Views view) {
	    mainLayout.show(mainPanel, view.toString());
	}
}
