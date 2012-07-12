package gui;

import gui.openpanel.CreateDaxploreFile;
import gui.openpanel.ImportSPSSFile;
import gui.openpanel.OpenDaxploreFile;
import gui.openpanel.OpenSPSSFile;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.Toolkit;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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

import org.opendatafoundation.data.spss.SPSSFile;

import daxplorelib.DaxploreFile;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JProgressBar;

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
					DaxploreGUI window = new DaxploreGUI();
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

	private MainPanelView mainPanelView = new MainPanelView(new ButtonGroup());
	public OpenPanelView openPanelView = new OpenPanelView();

	// getters and setters galore.
	// TODO: Organize the methods to seperate files.
	public String getSpssFileInfoText() {
		return openPanelView.getSpssFileInfoText().getText();
	}

	public void setSpssFileInfoText(String spssFileInfoText) {
		this.openPanelView.getSpssFileInfoText().setText(spssFileInfoText);
	}

	// file handler.
	DaxploreDataModel daxploreDataModel = new DaxploreDataModel();

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

	/**
	 * Create the application.
	 */
	public DaxploreGUI() {
		initGUI();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initGUI() {
		
		frmDaxploreProducer = new JFrame();
		frmDaxploreProducer.setIconImage(Toolkit.getDefaultToolkit().getImage(DaxploreGUI.class.getResource("/gui/resources/Colorful_Chart_Icon_vol2.png")));
		frmDaxploreProducer.setTitle("Daxplore Producer Developer Version");
		frmDaxploreProducer.setBounds(100, 100, 900, 787);
		frmDaxploreProducer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmDaxploreProducer.getContentPane().setLayout(new BorderLayout(0, 0));
		
		openPanelView.setSpssFileInfoText(new JTextPane());
		
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
		importDateLabel.setBounds(19, 209, 115, 15);
		
		JPanel importSPSSPanel = new JPanel();
		importSPSSPanel.setBorder(new TitledBorder(null, "Import SPSS File", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GroupLayout gl_openPanel = new GroupLayout(openPanel);
		gl_openPanel.setHorizontalGroup(
			gl_openPanel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_openPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_openPanel.createParallelGroup(Alignment.TRAILING)
						.addComponent(importSPSSPanel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 784, Short.MAX_VALUE)
						.addComponent(metaDataPanel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 784, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_openPanel.setVerticalGroup(
			gl_openPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_openPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(metaDataPanel, GroupLayout.PREFERRED_SIZE, 379, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(importSPSSPanel, GroupLayout.PREFERRED_SIZE, 343, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(25, Short.MAX_VALUE))
		);
		
		JButton openSPSSFileButton = new JButton("Open SPSS file...");
		openSPSSFileButton.setBounds(20, 50, 153, 27);
		openSPSSFileButton.addActionListener(new OpenSPSSFile(this, openSPSSFileButton));
		
		JScrollPane importTableScrollPane = new JScrollPane();
		importTableScrollPane.setBounds(20, 89, 763, 217);
		
		// progress bar for import spss panel goes here.
		JProgressBar importSpssFileProgressBar = new JProgressBar();
		importSpssFileProgressBar.setBounds(600, 307, 183, 19);
		importSPSSPanel.add(importSpssFileProgressBar);
		
		JButton importSpssFileButton = new JButton("");
		importSpssFileButton.addActionListener(new ImportSPSSFile(this, importSpssFileButton, importSpssFileProgressBar));
		importSpssFileButton.setToolTipText("Import SPSS file");
		importSpssFileButton.setIcon(new ImageIcon(DaxploreGUI.class.getResource("/gui/resources/Arrow-Up-48.png")));
		importSpssFileButton.setBounds(332, 19, 90, 58);
		
		importTableScrollPane.setViewportView(openPanelView.getSpssFileInfoText());
		importSPSSPanel.setLayout(null);
		importSPSSPanel.add(importSpssFileButton);
		importSPSSPanel.add(openSPSSFileButton);
		importSPSSPanel.add(importTableScrollPane);
		
		JLabel creationDateLabel = new JLabel("Creation date:");
		creationDateLabel.setBounds(19, 114, 115, 15);
		
		JLabel lastImportedFileLabel = new JLabel("Last import filename:");
		lastImportedFileLabel.setBounds(19, 174, 135, 15);
		
		metaDataPanel.setLayout(null);
		metaDataPanel.add(fileNameLabel);
		metaDataPanel.add(openPanelView.getFileNameField());
		metaDataPanel.add(importDateLabel);
		metaDataPanel.add(openPanelView.getImportDateField());
		metaDataPanel.add(creationDateLabel);
		metaDataPanel.add(openPanelView.getCreationDateField());
		metaDataPanel.add(lastImportedFileLabel);
		metaDataPanel.add(openPanelView.getLastImportFileNameField());
		
		JButton openFileButton = new JButton("Open file...");
		openFileButton.setBounds(19, 35, 135, 27);
		metaDataPanel.add(openFileButton);
		openFileButton.setToolTipText("Opens a daxplore file");
		openFileButton.addActionListener(new OpenDaxploreFile(this, openFileButton));
		
		JButton createNewFileButton = new JButton("Create new file...");
		createNewFileButton.setBounds(168, 35, 135, 27);
		metaDataPanel.add(createNewFileButton);
		createNewFileButton.addActionListener(new CreateDaxploreFile(this, createNewFileButton));
		createNewFileButton.setToolTipText("Creates a new daxplore project file");
		
		openPanel.setLayout(gl_openPanel);
		
		JPanel groupsPanel = new JPanel();
		mainPanel.add(groupsPanel, "importPanel");
		groupsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JLabel lblOldImportPanel = new JLabel("Old Import panel");
		groupsPanel.add(lblOldImportPanel);
		
		JPanel editPanel = new JPanel();
		mainPanel.add(editPanel, "editPanel");
		editPanel.setLayout(null);
		
		JLabel lblEdit = new JLabel("Edit");
		lblEdit.setBounds(397, 5, 42, 16);
		editPanel.add(lblEdit);
		
		openPanelView.setSpssFileInfoText(new JTextPane());
		mainPanelView.radioButtonCreator(buttonPanel, mainPanel);
	}
}
