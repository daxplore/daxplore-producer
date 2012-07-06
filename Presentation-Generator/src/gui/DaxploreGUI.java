package gui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import java.awt.Button;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JDesktopPane;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

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
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JTree;
import javax.swing.JTable;
import javax.swing.ImageIcon;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.JScrollBar;
import javax.swing.border.BevelBorder;
import javax.swing.JRadioButton;
import javax.swing.border.CompoundBorder;
import javax.swing.ButtonGroup;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.JTextField;
import javax.swing.JSeparator;
import javax.swing.border.MatteBorder;
import java.awt.Color;

/**
 * Main window handler class. Initialization of application goes here.
 */
public class DaxploreGUI {

	private JFrame frame;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JTextField textField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		try {
		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
		    }
		} catch (Exception e) {
		    // If Nimbus is not available, you can set the GUI to another look and feel.
		}
		
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
		
		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(new MatteBorder(0, 1, 0, 0, (Color) Color.GRAY));
		frame.getContentPane().add(mainPanel, BorderLayout.CENTER);
		mainPanel.setLayout(new CardLayout(0, 0));
		
		final JPanel openPanel = new JPanel();
		mainPanel.add(openPanel, "name_1340892548506154000");
		openPanel.setLayout(null);
		
		JButton btnNewButton = new JButton("Open file...");
		btnNewButton.setBounds(23, 24, 94, 27);
		openPanel.add(btnNewButton);
		
		JButton btnNewButton_1 = new JButton("Create new file...");
		btnNewButton_1.setBounds(129, 24, 162, 27);
		openPanel.add(btnNewButton_1);
		
		textField = new JTextField();
		textField.setBounds(50, 81, 122, 27);
		openPanel.add(textField);
		textField.setColumns(10);
		
		JLabel lblNewLabel_2 = new JLabel("File");
		lblNewLabel_2.setBounds(23, 87, 57, 15);
		openPanel.add(lblNewLabel_2);
		
		final JPanel importPanel = new JPanel();
		mainPanel.add(importPanel, "name_1340892556990884000");
		
		JLabel lblNewLabel_1 = new JLabel("Import");
		importPanel.add(lblNewLabel_1);
		
		JPanel editPanel = new JPanel();
		mainPanel.add(editPanel, "name_1340892590961725000");
		editPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JLabel lblEdit = new JLabel("Edit");
		editPanel.add(lblEdit);
		
		JPanel sortPanel = new JPanel();
		mainPanel.add(sortPanel, "name_1340892592836118000");
		sortPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JLabel lblNewLabel_4 = new JLabel("Sort data");
		sortPanel.add(lblNewLabel_4);
		
		JPanel packagePanel = new JPanel();
		mainPanel.add(packagePanel, "name_1340892595957519000");
		
		JLabel lblNewLabel_5 = new JLabel("Package");
		packagePanel.add(lblNewLabel_5);
		
		JPanel questionsPanel = new JPanel();
		mainPanel.add(questionsPanel, "name_1341574307100846000");
		
		JLabel questionsLabel = new JLabel("Questions");
		questionsPanel.add(questionsLabel);
		
		// button control section
		JRadioButton btnOpen = new JRadioButton("");
		btnOpen.setRolloverEnabled(false);
		btnOpen.setSelectedIcon(new ImageIcon(DaxploreGUI.class.getResource("/gui/resources/8_selected.png")));
		btnOpen.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		buttonGroup.add(btnOpen);
		btnOpen.setIcon(new ImageIcon(DaxploreGUI.class.getResource("/gui/resources/8.png")));
		panel.add(btnOpen);
		
		JRadioButton btnImport = new JRadioButton("");
		btnImport.setSelectedIcon(new ImageIcon(DaxploreGUI.class.getResource("/gui/resources/4_selected.png")));
		btnImport.setRolloverEnabled(false);
		btnImport.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		buttonGroup.add(btnImport);
		btnImport.setIcon(new ImageIcon(DaxploreGUI.class.getResource("/gui/resources/4.png")));
		panel.add(btnImport);

		JRadioButton btnQuestions = new JRadioButton("");
		btnQuestions.setSelectedIcon(new ImageIcon(DaxploreGUI.class.getResource("/gui/resources/6_selected.png")));
		btnQuestions.setRolloverEnabled(false);
		btnQuestions.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		buttonGroup.add(btnQuestions);
		btnQuestions.setIcon(new ImageIcon(DaxploreGUI.class.getResource("/gui/resources/6.png")));
		panel.add(btnQuestions);
	
		JRadioButton btnEditor = new JRadioButton("");
		btnEditor.setSelectedIcon(new ImageIcon(DaxploreGUI.class.getResource("/gui/resources/21_selected.png")));
		btnEditor.setRolloverEnabled(false);
		btnEditor.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		buttonGroup.add(btnEditor);
		btnEditor.setIcon(new ImageIcon(DaxploreGUI.class.getResource("/gui/resources/21.png")));
		panel.add(btnEditor);
		
		JRadioButton btnSort = new JRadioButton("");
		btnSort.setSelectedIcon(new ImageIcon(DaxploreGUI.class.getResource("/gui/resources/24_selected.png")));
		btnSort.setRolloverEnabled(false);
		btnSort.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		buttonGroup.add(btnSort);
		btnSort.setIcon(new ImageIcon(DaxploreGUI.class.getResource("/gui/resources/24.png")));
		panel.add(btnSort);
		
		JRadioButton btnPackage = new JRadioButton("");
		btnPackage.setSelectedIcon(new ImageIcon(DaxploreGUI.class.getResource("/gui/resources/28_selected.png")));
		btnPackage.setRolloverEnabled(false);
		btnPackage.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		buttonGroup.add(btnPackage);
		btnPackage.setIcon(new ImageIcon(DaxploreGUI.class.getResource("/gui/resources/28.png")));
		panel.add(btnPackage);
	}
	private class SwingAction extends AbstractAction {
		public SwingAction() {
			putValue(NAME, "SwingAction");
			putValue(SHORT_DESCRIPTION, "Some short description");
		}
		public void actionPerformed(ActionEvent e) {
		}
	}
}
