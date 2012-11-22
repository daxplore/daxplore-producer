package gui;

import gui.edit.EditPanelView;
import gui.groups.GroupsPanelView;
import gui.open.OpenPanelView;
import gui.tools.ToolsPanelView;

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
import javax.swing.JTable;

/**
 * Main window handler class. Initialization of application goes here.
 */
public class GuiMain {

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
					GuiMain window = new GuiMain();
					window.guiMainFrame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	// data fields for main class.
	private JFrame guiMainFrame;
	private GuiFile guiFile;
	final JPanel mainPanel = new JPanel();
	
	private OpenPanelView openPanelView;
	private GroupsPanelView groupsPanelView;
	private EditPanelView editPanelView;
	private ButtonPanelView buttonPanelView;
	private ToolsPanelView toolsPanelView;
	
	// getters and setters.
	public ButtonPanelView getButtonPanelView() {
		return buttonPanelView;
	}

	public void setButtonPanelView(ButtonPanelView buttonPanelView) {
		this.buttonPanelView = buttonPanelView;
	}

	public GuiFile getGuiFile() {
		return guiFile;
	}

	public void setGuiFile(GuiFile guiFile) {
		this.guiFile = guiFile;
	}

	public JFrame getGuiMainFrame() {
		return guiMainFrame;
	}

	public OpenPanelView getOpenPanelView() {
		return openPanelView;
	}

	public void setOpenPanelView(OpenPanelView openPanelView) {
		this.openPanelView = openPanelView;
	}

	public GroupsPanelView getGroupsPanelView() {
		return groupsPanelView;
	}

	public void setGroupsPanelView(GroupsPanelView groupsPanelView) {
		this.groupsPanelView = groupsPanelView;
	}

	public EditPanelView getEditPanelView() {
		return editPanelView;
	}

	public void setEditPanelView(EditPanelView editPanelView) {
		this.editPanelView = editPanelView;
	}

	public void switchTo(String label) {
		CardLayout cl = (CardLayout)(mainPanel.getLayout());
	    cl.show(mainPanel, label);
	}
	
	public void updateStuff() {
		buttonPanelView.updateButtonPanel();
		groupsPanelView.loadData();
		editPanelView.loadData();
	}

	/**
	 * Create the application.
	 */
	public GuiMain() {
		initGUI();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initGUI() {
		
		guiMainFrame = new JFrame();
		guiMainFrame.setIconImage(Toolkit.getDefaultToolkit().getImage(GuiMain.class.getResource("/gui/resources/Colorful_Chart_Icon_vol2.png")));
		guiMainFrame.setTitle("Daxplore Producer Developer Version");
		guiMainFrame.setBounds(100, 100, 900, 787);
		guiMainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		guiMainFrame.getContentPane().setLayout(new BorderLayout(0, 0));
		
		// file handler init.
		guiFile = new GuiFile();
		
		// create main panel window.
		mainPanel.setBorder(new MatteBorder(0, 1, 0, 0, (Color) Color.GRAY));
		mainPanel.setLayout(new CardLayout(0, 0));
		guiMainFrame.getContentPane().add(mainPanel, BorderLayout.CENTER);
		
		// panel views. TODO: Remake the controller interface.
		buttonPanelView = new ButtonPanelView(this);
		guiMainFrame.getContentPane().add(buttonPanelView, BorderLayout.WEST);
		
		openPanelView = new OpenPanelView(this);
		mainPanel.add(openPanelView, "openPanel");
		
		groupsPanelView = new GroupsPanelView(this);
		mainPanel.add(groupsPanelView, "groupsPanel");
		
		editPanelView = new EditPanelView(this);
		mainPanel.add(editPanelView, "editPanel");
		
		toolsPanelView = new ToolsPanelView(this);
		mainPanel.add(toolsPanelView, "toolsPanel");
		
		mainPanel.setRequestFocusEnabled(true);
	}
}
