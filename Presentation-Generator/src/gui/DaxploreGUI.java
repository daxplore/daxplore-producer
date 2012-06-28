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
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.GridLayout;
import javax.swing.JLabel;

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
		frame.getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.WEST);
		panel.setLayout(new GridLayout(0, 1, 0, 0));
		
		JPanel panel_1 = new JPanel();
		frame.getContentPane().add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new CardLayout(0, 0));
		
		JPanel openPanel = new JPanel();
		panel_1.add(openPanel, "name_1340892548506154000");
		
		JLabel lblNewLabel = new JLabel("Open");
		openPanel.add(lblNewLabel);
		
		JPanel importPanel = new JPanel();
		panel_1.add(importPanel, "name_1340892556990884000");
		
		JLabel lblNewLabel_1 = new JLabel("Import");
		importPanel.add(lblNewLabel_1);
		
		JPanel questionsPanel = new JPanel();
		panel_1.add(questionsPanel, "name_1340892588651402000");
		
		JLabel lblNewLabel_2 = new JLabel("Questions");
		questionsPanel.add(lblNewLabel_2);
		
		JPanel editPanel = new JPanel();
		panel_1.add(editPanel, "name_1340892590961725000");
		
		JLabel lblNewLabel_3 = new JLabel("Edit file");
		editPanel.add(lblNewLabel_3);
		
		JPanel sortPanel = new JPanel();
		panel_1.add(sortPanel, "name_1340892592836118000");
		
		JLabel lblNewLabel_4 = new JLabel("Sort data");
		sortPanel.add(lblNewLabel_4);
		
		JPanel packagePanel = new JPanel();
		panel_1.add(packagePanel, "name_1340892595957519000");
		
		JLabel lblNewLabel_5 = new JLabel("Package");
		packagePanel.add(lblNewLabel_5);
		
		JButton btnOpen = new JButton("Open");
		btnOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				System.out.println("Pressed open button.");
			}
		});
		panel.add(btnOpen);
		
		JButton btnImport = new JButton("Import");
		panel.add(btnImport);
		btnImport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		
		JButton btnQuestions = new JButton("Questions");
		panel.add(btnQuestions);
		btnQuestions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		
		JButton btnEditor = new JButton("Edit file");
		panel.add(btnEditor);
		
		JButton btnSort = new JButton("Sort data");
		panel.add(btnSort);
		
		JButton btnPackage = new JButton("Package");
		panel.add(btnPackage);		
	}
}
