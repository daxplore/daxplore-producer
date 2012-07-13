package gui;

import gui.view.ButtonPanelView;
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
					window.frmDaxploreProducer.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	// data fields for main class.
	public JFrame frmDaxploreProducer;
	
	public JFrame getFrmDaxploreProducer() {
		return frmDaxploreProducer;
	}

	public void setFrmDaxploreProducer(JFrame frmDaxploreProducer) {
		this.frmDaxploreProducer = frmDaxploreProducer;
	}

	final JPanel mainPanel = new JPanel();
	private ButtonPanelView buttonPanelView = new ButtonPanelView();


	// file handler.
	public GUIFile daxploreDataModel = new GUIFile();

	public SPSSFile getSpssFile() {
		return daxploreDataModel.getSpssFile();
	}

	public void setSpssFile(SPSSFile spssFile) {
		this.daxploreDataModel.setSpssFile(spssFile);
	}

	
	public DaxploreFile getDaxploreFile() {
		return daxploreDataModel.getDaxploreFile();
	}

	public void setDaxploreFile(DaxploreFile daxploreFile) {
		this.daxploreDataModel.setDaxploreFile(daxploreFile);
	}
	
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
		
		frmDaxploreProducer = new JFrame();
		frmDaxploreProducer.setIconImage(Toolkit.getDefaultToolkit().getImage(GUIMain.class.getResource("/gui/resources/Colorful_Chart_Icon_vol2.png")));
		frmDaxploreProducer.setTitle("Daxplore Producer Developer Version");
		frmDaxploreProducer.setBounds(100, 100, 900, 787);
		frmDaxploreProducer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmDaxploreProducer.getContentPane().setLayout(new BorderLayout(0, 0));
		
		mainPanel.setBorder(new MatteBorder(0, 1, 0, 0, (Color) Color.GRAY));
		mainPanel.setLayout(new CardLayout(0, 0));
		
		frmDaxploreProducer.getContentPane().add(mainPanel, BorderLayout.CENTER);
		
		JPanel buttonPanel = buttonPanelView.radioButtonCreator(this);
		frmDaxploreProducer.getContentPane().add(buttonPanel, BorderLayout.WEST);
		
		OpenPanelView openPanelView = new OpenPanelView(this);
		mainPanel.add(openPanelView, "openPanel");
		
		JPanel groupsPanel = new JPanel();
		mainPanel.add(groupsPanel, "importPanel");
		groupsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JLabel lblOldImportPanel = new JLabel("GroupsPanel");
		groupsPanel.add(lblOldImportPanel);
		
		JPanel editPanel = new JPanel();
		mainPanel.add(editPanel, "editPanel");
		editPanel.setLayout(null);
		
		JLabel lblEdit = new JLabel("Edit");
		lblEdit.setBounds(397, 5, 42, 16);
		editPanel.add(lblEdit);
		
	}
}
