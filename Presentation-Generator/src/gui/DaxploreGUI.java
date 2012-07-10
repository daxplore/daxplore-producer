package gui;

import gui.openpanel.CreateDaxploreFile;
import gui.openpanel.ImportSPSSFile;
import gui.openpanel.OpenDaxploreFile;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EtchedBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.FlowLayout;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JTextField;

import daxplorelib.DaxploreFile;

/**
 * Main window handler class. Initialization of application goes here.
 */
public class DaxploreGUI {

	/**
	 * Main execution loop, includes the thread handler, required for swing
	 * applications. Do not move the main() method from this file as it will
	 * break windowbuilder parsing.
	 */
	public static void main(String[] args) {
		
		// set the look and feel here, currently we use nimbus, looks nice :)
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
	public JFrame frmDaxploreProducer;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	public JTextField fileNameField;
	public JTextField importDateField;
	public JTextField creationDateField;
	public JTextField lastImportFileNameField;
	
	private JTable importTable;
	
	private DaxploreFile daxploreFile;

	
	public DaxploreFile getDaxploreFile() {
		return daxploreFile;
	}

	public void setDaxploreFile(DaxploreFile daxploreFile) {
		this.daxploreFile = daxploreFile;
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
		frmDaxploreProducer.setBounds(100, 100, 900, 787);
		frmDaxploreProducer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmDaxploreProducer.getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setBorder(new MatteBorder(0, 0, 0, 1, (Color) Color.LIGHT_GRAY));
		frmDaxploreProducer.getContentPane().add(buttonPanel, BorderLayout.WEST);
		buttonPanel.setLayout(new GridLayout(0, 1, 0, 0));
		
		final JPanel mainPanel = new JPanel();
		mainPanel.setBorder(new MatteBorder(0, 1, 0, 0, (Color) Color.GRAY));
		frmDaxploreProducer.getContentPane().add(mainPanel, BorderLayout.CENTER);
		mainPanel.setLayout(new CardLayout(0, 0));
		
		JPanel openPanel = new JPanel();
		mainPanel.add(openPanel, "openPanel");
		
		JPanel metaDataPanel = new JPanel();
		metaDataPanel.setBorder(new TitledBorder(null, "Daxplore file information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		JLabel fileNameLabel = new JLabel("Filename:");
		fileNameLabel.setBounds(19, 81, 115, 15);
		
		JLabel importDateLabel = new JLabel("Import date:");
		importDateLabel.setBounds(19, 114, 115, 15);
		
		JPanel importSPSSPanel = new JPanel();
		importSPSSPanel.setBorder(new TitledBorder(null, "Import SPSS File", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GroupLayout gl_openPanel = new GroupLayout(openPanel);
		gl_openPanel.setHorizontalGroup(
			gl_openPanel.createParallelGroup(Alignment.TRAILING)
				.addGroup(Alignment.LEADING, gl_openPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_openPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(importSPSSPanel, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 794, Short.MAX_VALUE)
						.addComponent(metaDataPanel, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 794, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_openPanel.setVerticalGroup(
			gl_openPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_openPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(metaDataPanel, GroupLayout.PREFERRED_SIZE, 379, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(importSPSSPanel, GroupLayout.DEFAULT_SIZE, 368, Short.MAX_VALUE)
					.addContainerGap())
		);
		
		JButton importSPSSFileButton = new JButton("Import file...");
		importSPSSFileButton.addActionListener(new ImportSPSSFile(this, importSPSSFileButton));
		
		JScrollPane importTableScrollPane = new JScrollPane();
		
		importTable = new JTable();
		importTableScrollPane.setViewportView(importTable);
		GroupLayout gl_importSPSSPanel = new GroupLayout(importSPSSPanel);
		gl_importSPSSPanel.setHorizontalGroup(
			gl_importSPSSPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_importSPSSPanel.createSequentialGroup()
					.addGap(8)
					.addGroup(gl_importSPSSPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(importSPSSFileButton, GroupLayout.PREFERRED_SIZE, 105, GroupLayout.PREFERRED_SIZE)
						.addComponent(importTableScrollPane, GroupLayout.PREFERRED_SIZE, 746, GroupLayout.PREFERRED_SIZE)))
		);
		gl_importSPSSPanel.setVerticalGroup(
			gl_importSPSSPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_importSPSSPanel.createSequentialGroup()
					.addGap(7)
					.addComponent(importSPSSFileButton)
					.addGap(12)
					.addComponent(importTableScrollPane, GroupLayout.PREFERRED_SIZE, 269, GroupLayout.PREFERRED_SIZE))
		);
		importSPSSPanel.setLayout(gl_importSPSSPanel);
		
		JLabel creationDateLabel = new JLabel("Creation date:");
		creationDateLabel.setBounds(19, 147, 115, 15);
		
		JLabel lastImportedFileLabel = new JLabel("Last import filename:");
		lastImportedFileLabel.setBounds(19, 180, 135, 15);
		
		fileNameField = new JTextField();

		fileNameField.setEditable(false);
		fileNameField.setBounds(166, 75, 240, 27);
		fileNameField.setColumns(10);
		
		importDateField = new JTextField();
		importDateField.setEditable(false);
		importDateField.setBounds(166, 108, 240, 27);
		importDateField.setColumns(10);
		
		creationDateField = new JTextField();
		creationDateField.setEditable(false);
		creationDateField.setBounds(166, 141, 240, 27);
		creationDateField.setColumns(10);
		
		lastImportFileNameField = new JTextField();
		lastImportFileNameField.setEditable(false);
		lastImportFileNameField.setBounds(166, 174, 240, 27);
		lastImportFileNameField.setColumns(10);
		metaDataPanel.setLayout(null);
		metaDataPanel.add(fileNameLabel);
		metaDataPanel.add(fileNameField);
		metaDataPanel.add(importDateLabel);
		metaDataPanel.add(importDateField);
		metaDataPanel.add(creationDateLabel);
		metaDataPanel.add(creationDateField);
		metaDataPanel.add(lastImportedFileLabel);
		metaDataPanel.add(lastImportFileNameField);
		
		JButton openFileButton = new JButton("Open file...");
		openFileButton.setBounds(19, 35, 135, 27);
		metaDataPanel.add(openFileButton);
		openFileButton.setToolTipText("Opens a daxplore file");
		openFileButton.addActionListener(new OpenDaxploreFile(this, openFileButton));
		
		JButton createNewFileButton = new JButton("Create new file...");
		createNewFileButton.setBounds(168, 35, 135, 27);
		metaDataPanel.add(createNewFileButton);
		createNewFileButton.addActionListener(new CreateDaxploreFile());
		createNewFileButton.setToolTipText("Creates a new daxplore project file");
		
		openPanel.setLayout(gl_openPanel);
		
		JPanel importPanel = new JPanel();
		mainPanel.add(importPanel, "importPanel");
		importPanel.setLayout(null);
		
		JLabel lblOldImportPanel = new JLabel("Old Import panel");
		lblOldImportPanel.setBounds(288, 16, 124, 15);
		importPanel.add(lblOldImportPanel);
		
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
		
		radioButtonCreator(buttonPanel, mainPanel);
	}

	/**
	 * Handles the display and functionality of the left panel buttons.
	 * @param buttonPanel
	 * @param mainPanel
	 */
	private void radioButtonCreator(JPanel buttonPanel, final JPanel mainPanel) {
		JRadioButton OpenButton = new JRadioButton("");
		OpenButton.setToolTipText("Manage file(s)");
		OpenButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				CardLayout cl = (CardLayout)(mainPanel.getLayout());
			    cl.show(mainPanel, "openPanel");
			}
		});
		
		OpenButton.setRolloverEnabled(false);
		OpenButton.setSelectedIcon(new ImageIcon(DaxploreGUI.class.getResource("/gui/resources/8_selected.png")));
		OpenButton.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		buttonGroup.add(OpenButton);
		OpenButton.setIcon(new ImageIcon(DaxploreGUI.class.getResource("/gui/resources/8.png")));
		buttonPanel.add(OpenButton);
		
		JRadioButton ImportButton = new JRadioButton("");
		ImportButton.setToolTipText("Import SPSS file(s)");
		ImportButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				CardLayout cl = (CardLayout)(mainPanel.getLayout());
			    cl.show(mainPanel, "importPanel");
			}
		});
		
		ImportButton.setSelectedIcon(new ImageIcon(DaxploreGUI.class.getResource("/gui/resources/4_selected.png")));
		ImportButton.setRolloverEnabled(false);
		ImportButton.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		buttonGroup.add(ImportButton);
		ImportButton.setIcon(new ImageIcon(DaxploreGUI.class.getResource("/gui/resources/4.png")));
		buttonPanel.add(ImportButton);

		JRadioButton QuestionsButton = new JRadioButton("");
		QuestionsButton.setToolTipText("Question edit");
		QuestionsButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				CardLayout cl = (CardLayout)(mainPanel.getLayout());
			    cl.show(mainPanel, "questionsPanel");
			}
		});
		
		QuestionsButton.setSelectedIcon(new ImageIcon(DaxploreGUI.class.getResource("/gui/resources/6_selected.png")));
		QuestionsButton.setRolloverEnabled(false);
		QuestionsButton.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		buttonGroup.add(QuestionsButton);
		QuestionsButton.setIcon(new ImageIcon(DaxploreGUI.class.getResource("/gui/resources/6.png")));
		buttonPanel.add(QuestionsButton);
	
		JRadioButton EditButton = new JRadioButton("");
		EditButton.setToolTipText("Edit SPSS data");
		EditButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				CardLayout cl = (CardLayout)(mainPanel.getLayout());
			    cl.show(mainPanel, "editPanel");
			}
		});
		
		EditButton.setSelectedIcon(new ImageIcon(DaxploreGUI.class.getResource("/gui/resources/21_selected.png")));
		EditButton.setRolloverEnabled(false);
		EditButton.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		buttonGroup.add(EditButton);
		EditButton.setIcon(new ImageIcon(DaxploreGUI.class.getResource("/gui/resources/21.png")));
		buttonPanel.add(EditButton);
		
		JRadioButton SortButton = new JRadioButton("");
		SortButton.setToolTipText("Sort data");
		SortButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				CardLayout cl = (CardLayout)(mainPanel.getLayout());
			    cl.show(mainPanel, "sortPanel");
			}
		});
		
		
		SortButton.setSelectedIcon(new ImageIcon(DaxploreGUI.class.getResource("/gui/resources/24_selected.png")));
		SortButton.setRolloverEnabled(false);
		SortButton.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		buttonGroup.add(SortButton);
		SortButton.setIcon(new ImageIcon(DaxploreGUI.class.getResource("/gui/resources/24.png")));
		buttonPanel.add(SortButton);
		
		JRadioButton PackageButton = new JRadioButton("");
		PackageButton.setToolTipText("Package files");
		PackageButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				CardLayout cl = (CardLayout)(mainPanel.getLayout());
			    cl.show(mainPanel, "packagePanel");
			}
		});
		
		PackageButton.setSelectedIcon(new ImageIcon(DaxploreGUI.class.getResource("/gui/resources/28_selected.png")));
		PackageButton.setRolloverEnabled(false);
		PackageButton.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		buttonGroup.add(PackageButton);
		PackageButton.setIcon(new ImageIcon(DaxploreGUI.class.getResource("/gui/resources/28.png")));
		buttonPanel.add(PackageButton);
	}
}
