package gui;

import gui.view.ButtonPanelView;
import gui.view.EditPanelView;
import gui.view.GroupsPanelView;
import gui.view.OpenPanelView;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.MatteBorder;

import org.opendatafoundation.data.spss.SPSSFile;

import daxplorelib.DaxploreFile;

/**
 * Main window handler class. Initialization of application goes here.
 */
public class GUIMain {

	/**
	 * Main execution loop, includes the thread handler, required for swing
	 * applications. Do not move the main() method from this file as it will
	 * break windowbuilder parsing.
	 */
	public static void main(String[] args) {
		
		// do a java version check, if target system doesn't have java 7, exit.
		if (GUITools.javaVersionCheck() != true) {
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
					GUIMain window = new GUIMain();
					window.guiMainFrame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	// data fields for main class.
	private JFrame guiMainFrame;
	
	public JFrame getGuiMainFrame() {
		return guiMainFrame;
	}

	final JPanel mainPanel = new JPanel();

	public void switchTo(String label) {
		CardLayout cl = (CardLayout)(mainPanel.getLayout());
	    cl.show(mainPanel, label);
	}

	/**
	 * Create the application.
	 */
	public GUIMain() {
		initGUI();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initGUI() {
		
		guiMainFrame = new JFrame();
		guiMainFrame.setIconImage(Toolkit.getDefaultToolkit().getImage(GUIMain.class.getResource("/gui/resources/Colorful_Chart_Icon_vol2.png")));
		guiMainFrame.setTitle("Daxplore Producer Developer Version");
		guiMainFrame.setBounds(100, 100, 900, 787);
		guiMainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		guiMainFrame.getContentPane().setLayout(new BorderLayout(0, 0));
		
		// file handler init.
		GUIFile guiFile = new GUIFile();
		
		// create main panel window.
		mainPanel.setBorder(new MatteBorder(0, 1, 0, 0, (Color) Color.GRAY));
		mainPanel.setLayout(new CardLayout(0, 0));
		guiMainFrame.getContentPane().add(mainPanel, BorderLayout.CENTER);
		
		// create left button panel.
		ButtonPanelView buttonPanelView = new ButtonPanelView(this);
		guiMainFrame.getContentPane().add(buttonPanelView, BorderLayout.WEST);
		
		// add the open panel view.
		OpenPanelView openPanelView = new OpenPanelView(this, guiFile);
		mainPanel.add(openPanelView, "openPanel");
		
		// add the groups panel view.
		GroupsPanelView groupsPanelView = new GroupsPanelView(this, guiFile);
		mainPanel.add(groupsPanelView, "groupsPanel");
		
		// add the edit panel view.
		EditPanelView editPanelView = new EditPanelView(this, guiFile);
		mainPanel.add(editPanelView, "editPanel");	
	}
}
