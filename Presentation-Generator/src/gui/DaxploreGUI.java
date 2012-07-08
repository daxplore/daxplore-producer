package gui;

import gui.controller.OpenController;

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
import javax.swing.JFileChooser;
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
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.jgoodies.forms.factories.DefaultComponentFactory;


import java.awt.Toolkit;
import java.io.File;
import javax.swing.JTextPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.JScrollPane;
import java.awt.Window.Type;

/**
 * Main window handler class. Initialization of application goes here.
 */
public class DaxploreGUI {

	private JFrame frmDaxploreProducer;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JTable tableSPSS;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		// use an appropriate look and feel here.
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
					window.frmDaxploreProducer.setVisible(true);
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
		
		frmDaxploreProducer = new JFrame();
		frmDaxploreProducer.setIconImage(Toolkit.getDefaultToolkit().getImage(DaxploreGUI.class.getResource("/gui/resources/Colorful_Chart_Icon_vol2.png")));
		frmDaxploreProducer.setTitle("Daxplore Producer Developer Version");
		frmDaxploreProducer.setResizable(false);
		frmDaxploreProducer.setBounds(100, 100, 864, 732);
		frmDaxploreProducer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmDaxploreProducer.getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel buttonPanel = new JPanel();
		frmDaxploreProducer.getContentPane().add(buttonPanel, BorderLayout.WEST);
		buttonPanel.setLayout(new GridLayout(0, 1, 0, 0));
		
		final JPanel mainPanel = new JPanel();
		mainPanel.setBorder(new MatteBorder(0, 1, 0, 0, (Color) Color.GRAY));
		frmDaxploreProducer.getContentPane().add(mainPanel, BorderLayout.CENTER);
		mainPanel.setLayout(new CardLayout(0, 0));
		
		final JPanel openPanel = new JPanel();
		mainPanel.add(openPanel, "openPanel");
		openPanel.setLayout(null);
		
		final JButton openFileButton = new JButton("Open file...");
		openFileButton.setToolTipText("Opens an SPSS file");
		openFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == openFileButton) {
					JFileChooser fc = new JFileChooser();
					FileNameExtensionFilter filter = new FileNameExtensionFilter(
					        "SPSS Files", "spss");
					    fc.setFileFilter(filter);
			        int returnVal = fc.showOpenDialog(frmDaxploreProducer);

			        if (returnVal == JFileChooser.APPROVE_OPTION) {
			           File file = fc.getSelectedFile();
			           //This is where a real application would open the file.
			           System.out.println("Opening: " + file.getName() + ".");
			           OpenController.ReadSPSSFile(fc);
			        } else {
			           System.out.println("Open command cancelled by user.");
			        }
				}
			}
		});
		openFileButton.setBounds(6, 24, 150, 27);
		openPanel.add(openFileButton);
		
		JButton createNewFileButton = new JButton("Create new file...");
		createNewFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		createNewFileButton.setToolTipText("Creates a new SPSS file");
		createNewFileButton.setBounds(168, 24, 150, 27);
		openPanel.add(createNewFileButton);
		
		JLabel fileNameLabel = new JLabel("Filename:");
		fileNameLabel.setBounds(16, 88, 71, 15);
		openPanel.add(fileNameLabel);
		
		JTextPane fileNameText = new JTextPane();
		fileNameText.setEditable(false);
		fileNameText.setBounds(112, 79, 343, 24);
		openPanel.add(fileNameText);
		
		JLabel lastImportedLabel = new JLabel("Last imported:");
		lastImportedLabel.setBounds(15, 128, 85, 16);
		openPanel.add(lastImportedLabel);
		
		JTextPane lastImportedText = new JTextPane();
		lastImportedText.setEditable(false);
		lastImportedText.setBounds(112, 120, 343, 24);
		openPanel.add(lastImportedText);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(6, 173, 761, 520);
		openPanel.add(scrollPane);
		
		tableSPSS = new JTable();
		// test data, in reality we need to fill the model with SPSS file info.
		tableSPSS.setModel(new DefaultTableModel(
			new Object[][] {
				{"Kathy", "Smith", "Snowboarding", new Integer(5), Boolean.FALSE},
				{"John", "Doe", "Rowing", new Integer(3), Boolean.TRUE},
				{"Sue", "Black", "Knitting", new Integer(2), Boolean.FALSE},
				{"Jane", "White", "Speed reading", new Integer(20), Boolean.TRUE},
				{"Joe", "Brown", "Pool", new Integer(10), Boolean.FALSE},
			},
			new String[] {
				"First Name", "Last Name", "Sport", "Tries#", "Vegetarian"
			}
		) {
			/**
			 * 
			 */
			private static final long serialVersionUID = -6898131158931249619L;
			boolean[] columnEditables = new boolean[] {
				false, false, true, true, false
			};
			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});
		scrollPane.setViewportView(tableSPSS);
		
		JSeparator openSeparator = new JSeparator();
		openSeparator.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		openSeparator.setBounds(6, 67, 761, 94);
		openPanel.add(openSeparator);
		
		final JPanel importPanel = new JPanel();
		mainPanel.add(importPanel, "importPanel");
		importPanel.setLayout(null);
		
		JLabel lblNewLabel_1 = new JLabel("Import");
		lblNewLabel_1.setBounds(389, 5, 38, 16);
		importPanel.add(lblNewLabel_1);
		
		JPanel editPanel = new JPanel();
		mainPanel.add(editPanel, "editPanel");
		editPanel.setLayout(null);
		
		JLabel lblEdit = new JLabel("Edit");
		lblEdit.setBounds(397, 5, 42, 16);
		editPanel.add(lblEdit);
		
		JPanel sortPanel = new JPanel();
		mainPanel.add(sortPanel, "sortPanel");
		sortPanel.setLayout(null);
		
		JLabel lblNewLabel_4 = new JLabel("Sort data");
		lblNewLabel_4.setBounds(381, 5, 75, 16);
		sortPanel.add(lblNewLabel_4);
		
		JPanel packagePanel = new JPanel();
		mainPanel.add(packagePanel, "packagePanel");
		packagePanel.setLayout(null);
		
		JLabel lblNewLabel_5 = new JLabel("Package");
		lblNewLabel_5.setBounds(384, 5, 71, 16);
		packagePanel.add(lblNewLabel_5);
		
		JPanel questionsPanel = new JPanel();
		mainPanel.add(questionsPanel, "questionsPanel");
		questionsPanel.setLayout(null);
		
		JLabel questionsLabel = new JLabel("Questions");
		questionsLabel.setBounds(380, 5, 74, 16);
		questionsPanel.add(questionsLabel);
		
		// radio button control section
		JRadioButton btnOpen = new JRadioButton("");
		btnOpen.setToolTipText("Open and create SPSS file(s)");
		btnOpen.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				CardLayout cl = (CardLayout)(mainPanel.getLayout());
			    cl.show(mainPanel, "openPanel");
			}
		});
		btnOpen.setRolloverEnabled(false);
		btnOpen.setSelectedIcon(new ImageIcon(DaxploreGUI.class.getResource("/gui/resources/8_selected.png")));
		btnOpen.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		buttonGroup.add(btnOpen);
		btnOpen.setIcon(new ImageIcon(DaxploreGUI.class.getResource("/gui/resources/8.png")));
		buttonPanel.add(btnOpen);
		
		JRadioButton btnImport = new JRadioButton("");
		btnImport.setToolTipText("Import SPSS file(s)");
		btnImport.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				CardLayout cl = (CardLayout)(mainPanel.getLayout());
			    cl.show(mainPanel, "importPanel");
			}
		});
		btnImport.setSelectedIcon(new ImageIcon(DaxploreGUI.class.getResource("/gui/resources/4_selected.png")));
		btnImport.setRolloverEnabled(false);
		btnImport.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		buttonGroup.add(btnImport);
		btnImport.setIcon(new ImageIcon(DaxploreGUI.class.getResource("/gui/resources/4.png")));
		buttonPanel.add(btnImport);

		JRadioButton btnQuestions = new JRadioButton("");
		btnQuestions.setToolTipText("Question edit");
		btnQuestions.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				CardLayout cl = (CardLayout)(mainPanel.getLayout());
			    cl.show(mainPanel, "questionsPanel");
			}
		});
		btnQuestions.setSelectedIcon(new ImageIcon(DaxploreGUI.class.getResource("/gui/resources/6_selected.png")));
		btnQuestions.setRolloverEnabled(false);
		btnQuestions.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		buttonGroup.add(btnQuestions);
		btnQuestions.setIcon(new ImageIcon(DaxploreGUI.class.getResource("/gui/resources/6.png")));
		buttonPanel.add(btnQuestions);
	
		JRadioButton btnEdit = new JRadioButton("");
		btnEdit.setToolTipText("Edit SPSS data");
		btnEdit.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				CardLayout cl = (CardLayout)(mainPanel.getLayout());
			    cl.show(mainPanel, "editPanel");
			}
		});
		btnEdit.setSelectedIcon(new ImageIcon(DaxploreGUI.class.getResource("/gui/resources/21_selected.png")));
		btnEdit.setRolloverEnabled(false);
		btnEdit.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		buttonGroup.add(btnEdit);
		btnEdit.setIcon(new ImageIcon(DaxploreGUI.class.getResource("/gui/resources/21.png")));
		buttonPanel.add(btnEdit);
		
		JRadioButton btnSort = new JRadioButton("");
		btnSort.setToolTipText("Sort data");
		btnSort.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				CardLayout cl = (CardLayout)(mainPanel.getLayout());
			    cl.show(mainPanel, "sortPanel");
			}
		});
		btnSort.setSelectedIcon(new ImageIcon(DaxploreGUI.class.getResource("/gui/resources/24_selected.png")));
		btnSort.setRolloverEnabled(false);
		btnSort.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		buttonGroup.add(btnSort);
		btnSort.setIcon(new ImageIcon(DaxploreGUI.class.getResource("/gui/resources/24.png")));
		buttonPanel.add(btnSort);
		
		JRadioButton btnPackage = new JRadioButton("");
		btnPackage.setToolTipText("Package files");
		btnPackage.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				CardLayout cl = (CardLayout)(mainPanel.getLayout());
			    cl.show(mainPanel, "packagePanel");
			}
		});
		btnPackage.setSelectedIcon(new ImageIcon(DaxploreGUI.class.getResource("/gui/resources/28_selected.png")));
		btnPackage.setRolloverEnabled(false);
		btnPackage.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		buttonGroup.add(btnPackage);
		btnPackage.setIcon(new ImageIcon(DaxploreGUI.class.getResource("/gui/resources/28.png")));
		buttonPanel.add(btnPackage);
	}
}
