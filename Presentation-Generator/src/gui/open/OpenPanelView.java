package gui.open;

import gui.GUIFile;
import gui.GUIMain;
import gui.importwizard.CharsetPanelDescriptor;
import gui.importwizard.FinalImportPanelDescriptor;
import gui.importwizard.ImportWizardDescriptor;
import gui.importwizard.ImportWizardDialog;
import gui.importwizard.OpenFilePanelDescriptor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.TitledBorder;

/**
 * Open panel view, displays the open and create daxplore file function as well as the import
 * SPSS file wizard button and information panel. If a file is loaded, the panels will show
 * file information.
 * @author hkfs89
 *
 */
public class OpenPanelView extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private JTextField fileNameField = new JTextField();
	private JTextField importDateField = new JTextField();;
	private JTextField creationDateField = new JTextField();
	private JTextField lastImportFileNameField = new JTextField();
	public JTextPane spssFileInfoText = new JTextPane();
	private JButton openFileButton;
	private JButton createNewFileButton;
	private JButton importWizardButton;
	
	private OpenController openController;
	
	public OpenController getOpenController() {
		return openController;
	}

	public void setOpenController(OpenController openController) {
		this.openController = openController;
	}

	public static final String OPEN_BUTTON_ACTION_COMMAND = "OpenButtonActionCommand";
	public static final String CREATE_BUTTON_ACTION_COMMAND = "CreateButtonActionCommand";
	public static final String IMPORT_BUTTON_ACTION_COMMAND = "ImportButtonActionCommand";
	
	/**
	 * OpenPanelView constructor.
	 * @param guiMain
	 */
	public OpenPanelView(final GUIMain guiMain) {
		
		openController = new OpenController(guiMain, this);
		getFileNameField().setEditable(false);
		getFileNameField().setBounds(166, 75, 240, 27);
		getFileNameField().setColumns(10);

		getImportDateField().setEditable(false);
		getImportDateField().setBounds(166, 203, 240, 27);
		getImportDateField().setColumns(10);

		getCreationDateField().setEditable(false);
		getCreationDateField().setBounds(166, 108, 240, 27);
		getCreationDateField().setColumns(10);

		getLastImportFileNameField().setEditable(false);
		getLastImportFileNameField().setBounds(166, 168, 240, 27);
		getLastImportFileNameField().setColumns(10);
		
		JPanel daxploreFilePanel = new JPanel();
		daxploreFilePanel.setBorder(new TitledBorder(null, "Daxplore file information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		JPanel importSPSSPanel = new JPanel();
		importSPSSPanel.setBorder(new TitledBorder(null, "Import SPSS File", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		GroupLayout gl_openPanel = new GroupLayout(this);
		
		gl_openPanel.setHorizontalGroup(
			gl_openPanel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_openPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_openPanel.createParallelGroup(Alignment.TRAILING)
						.addComponent(importSPSSPanel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 784, Short.MAX_VALUE)
						.addComponent(daxploreFilePanel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 784, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_openPanel.setVerticalGroup(
			gl_openPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_openPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(daxploreFilePanel, GroupLayout.PREFERRED_SIZE, 379, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(importSPSSPanel, GroupLayout.PREFERRED_SIZE, 343, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(25, Short.MAX_VALUE))
		);
		importSPSSPanel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane importTableScrollPane = new JScrollPane();
		
		importTableScrollPane.setViewportView(spssFileInfoText);
		importSPSSPanel.add(importTableScrollPane);
		
		importWizardButton = new JButton("Import SPSS file...");
		importWizardButton.setActionCommand(IMPORT_BUTTON_ACTION_COMMAND);
		importSPSSPanel.add(importWizardButton, BorderLayout.SOUTH);
		importWizardButton.setPreferredSize(new Dimension(84, 28));
		importWizardButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {

				if (guiMain.getGuiFile().getDaxploreFile() == null) {
					JOptionPane
							.showMessageDialog(
									guiMain.getGuiMainFrame(),
									"Create or open a daxplore project file before you import an SPSS file.",
									"Daxplore file warning", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				ImportWizardDialog importWizardDialog = new ImportWizardDialog(guiMain);
				
				ImportWizardDescriptor openFilePanelDescriptor = new OpenFilePanelDescriptor();
		        importWizardDialog.registerWizardPanel(OpenFilePanelDescriptor.IDENTIFIER, openFilePanelDescriptor);
		        
				ImportWizardDescriptor charsetPanelDescriptor = new CharsetPanelDescriptor();
		        importWizardDialog.registerWizardPanel(CharsetPanelDescriptor.IDENTIFIER, charsetPanelDescriptor);
		        
				ImportWizardDescriptor finalImportPanelDescriptor = new FinalImportPanelDescriptor();
		        importWizardDialog.registerWizardPanel(FinalImportPanelDescriptor.IDENTIFIER, finalImportPanelDescriptor);
		        
		        importWizardDialog.setCurrentPanel(OpenFilePanelDescriptor.IDENTIFIER);
		        
				importWizardDialog.setVisible(true);
			}
		});
		
		JLabel fileNameLabel = new JLabel("Filename:");
		fileNameLabel.setBounds(19, 81, 115, 15);
		
		JLabel importDateLabel = new JLabel("Import date:");
		importDateLabel.setBounds(19, 209, 115, 15);
		
		JLabel creationDateLabel = new JLabel("Creation date:");
		creationDateLabel.setBounds(19, 114, 115, 15);
		
		JLabel lastImportedFileLabel = new JLabel("Last import filename:");
		lastImportedFileLabel.setBounds(19, 174, 135, 15);
		
		daxploreFilePanel.setLayout(null);
		daxploreFilePanel.add(fileNameLabel);
		daxploreFilePanel.add(getFileNameField());
		daxploreFilePanel.add(importDateLabel);
		daxploreFilePanel.add(getImportDateField());
		daxploreFilePanel.add(creationDateLabel);
		daxploreFilePanel.add(getCreationDateField());
		daxploreFilePanel.add(lastImportedFileLabel);
		daxploreFilePanel.add(getLastImportFileNameField());
		
		openFileButton = new JButton("Open file...");
		openFileButton.setBounds(19, 35, 135, 27);
		openFileButton.setActionCommand(OPEN_BUTTON_ACTION_COMMAND);
		daxploreFilePanel.add(openFileButton);
		openFileButton.setToolTipText("Opens a daxplore file");
		openFileButton.addActionListener(openController);
		
		createNewFileButton = new JButton("Create new file...");
		createNewFileButton.setBounds(168, 35, 135, 27);
		createNewFileButton.setActionCommand(CREATE_BUTTON_ACTION_COMMAND);
		daxploreFilePanel.add(createNewFileButton);
		createNewFileButton.addActionListener(openController);
		createNewFileButton.setToolTipText("Creates a new daxplore project file");
		
		setLayout(gl_openPanel);
	}

	public JTextField getFileNameField() {
		return fileNameField;
	}

	public void setFileNameField(JTextField fileNameField) {
		this.fileNameField = fileNameField;
	}

	public JTextField getLastImportFileNameField() {
		return lastImportFileNameField;
	}

	public void setLastImportFileNameField(JTextField lastImportFileNameField) {
		this.lastImportFileNameField = lastImportFileNameField;
	}

	public JTextField getImportDateField() {
		return importDateField;
	}

	public void setImportDateField(JTextField importDateField) {
		this.importDateField = importDateField;
	}

	public JTextField getCreationDateField() {
		return creationDateField;
	}

	public void setCreationDateField(JTextField creationDateField) {
		this.creationDateField = creationDateField;
	}
}