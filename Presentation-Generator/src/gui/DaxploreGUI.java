package gui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import java.awt.Button;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JDesktopPane;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import java.awt.CardLayout;
import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.JPanel;

public class DaxploreGUI {

	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					DaxploreGUI window = new DaxploreGUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public DaxploreGUI() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 728, 637);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JButton btnOpen = new JButton("Open");
		btnOpen.setBounds(16, 27, 117, 50);
		frame.getContentPane().add(btnOpen);
		
		JButton btnImport = new JButton("Import");
		btnImport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		btnImport.setBounds(16, 89, 117, 50);
		frame.getContentPane().add(btnImport);
		
		JButton btnQuestions = new JButton("Questions");
		btnQuestions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		btnQuestions.setBounds(16, 151, 117, 50);
		frame.getContentPane().add(btnQuestions);
		
		JButton btnEditor = new JButton("Edit file");
		btnEditor.setBounds(16, 213, 117, 50);
		frame.getContentPane().add(btnEditor);
		
		JButton btnSort = new JButton("Sort data");
		btnSort.setBounds(16, 275, 117, 50);
		frame.getContentPane().add(btnSort);
		
		JButton btnPackage = new JButton("Package");
		btnPackage.setBounds(16, 337, 117, 50);
		frame.getContentPane().add(btnPackage);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(135, 6, 587, 603);
		frame.getContentPane().add(tabbedPane);
		
		JPanel panel = new JPanel();
		tabbedPane.addTab("New tab", null, panel, null);
		
		JPanel panel_1 = new JPanel();
		tabbedPane.addTab("New tab", null, panel_1, null);
	}
}
