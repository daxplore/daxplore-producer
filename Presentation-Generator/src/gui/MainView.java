package gui;

import gui.MainController.Views;
import gui.edit.EditPanelView;
import gui.groups.GroupsView;
import gui.navigation.NavigationPanelView;
import gui.open.OpenPanelView;
import gui.tools.ToolsPanelView;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.MatteBorder;

public class MainView {

	private JFrame mainControllerFrame;
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
		mainControllerFrame.setIconImage(Toolkit.getDefaultToolkit().getImage(MainController.class.getResource("/gui/resources/Colorful_Chart_Icon_vol2.png")));
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
		
		mainController.setOpenPanelView(new OpenPanelView(mainController));
		mainPanel.add(mainController.getOpenPanelView(), Views.OPENPANEL.toString());
		
		mainController.setGroupsPanelView(new GroupsView(mainController));
		mainPanel.add(mainController.getGroupsPanelView(), Views.GROUPSVIEW.toString());
		
		mainController.setEditPanelView(new EditPanelView(mainController));
		mainPanel.add(mainController.getEditPanelView(), Views.EDITVIEW.toString());
		
		mainController.setToolsPanelView(new ToolsPanelView(mainController));
		mainPanel.add(mainController.getToolsPanelView(), Views.TOOLSVIEW.toString());
		
		mainPanel.setRequestFocusEnabled(true);
		
		mainController.setNavigationPanelView(new NavigationPanelView(mainController));
		panel.add(mainController.getNavigationPanelView(), BorderLayout.SOUTH);
	}
	
	public void showInMain(Views view) {
	    mainLayout.show(mainPanel, view.toString());
	}
}
