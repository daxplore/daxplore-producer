package org.daxplore.producer.gui.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.daxplore.producer.gui.MainController;
import org.daxplore.producer.gui.Settings;
import org.xml.sax.SAXException;

@SuppressWarnings("serial")
public class ToolsView extends JPanel {
	
	private JTextField textField;
	
	public static final String IMPORT_RAW_BUTTON_ACTION_COMMAND = "importRawButtonActionCommand";
	private JTable table;
	
	JScrollPane scrollPane;
	final MainController mainController;
	
	public ToolsView(final MainController mainController) {
		this.mainController = mainController;
		
		JButton btnImportfromraw = new JButton("ImportFromRaw");
		btnImportfromraw.setBounds(12, 67, 145, 25);
		btnImportfromraw.addActionListener(new ActionListener() {
			

			public void actionPerformed(ActionEvent e) {
				String localeText = textField.getText();
				if(localeText != null && !"".equals(localeText)) {
					Locale loc = new Locale(localeText);
					if(!"und".equals(loc.toLanguageTag())){
						try {
							mainController.getDaxploreFile().getMetaData().importFromRaw(mainController.getDaxploreFile(), loc);
						} catch (DaxploreException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
			}
		});
		setLayout(null);
		add(btnImportfromraw);
		
		JLabel lblEnterLocale = new JLabel("Enter locale");
		lblEnterLocale.setBounds(12, 12, 117, 15);
		add(lblEnterLocale);
		
		textField = new JTextField();
		textField.setBounds(147, 10, 114, 19);
		add(textField);
		textField.setColumns(10);
		
		JButton addTimepointsButton = new JButton("Replace timepoints");
		addTimepointsButton.setBounds(27, 189, 185, 47);
		addTimepointsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					mainController.getDaxploreFile().getMetaData().replaceAllTimepointsInQuestions();
				} catch (DaxploreException|SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		add(addTimepointsButton);
		
		JButton btnGenerateData = new JButton("Generate Data");
		btnGenerateData.setBounds(247, 101, 191, 25);
		btnGenerateData.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File uploadFile = showExportDialog();
				if(uploadFile == null) {
					return;
				}
				
				try {
					mainController.getDaxploreFile().writeUploadFile(uploadFile);
				} catch (TransformerFactoryConfigurationError | TransformerException | SQLException | DaxploreException | SAXException | IOException | ParserConfigurationException e1) {
					// TODO HAHAHAHAHAHA
					e1.printStackTrace();
				}
			}
		});
		add(btnGenerateData);
		
		scrollPane = new JScrollPane();
		scrollPane.setBounds(301, 234, 294, 204);
		add(scrollPane);
	}
	
	public void loadData() {
		if(mainController.fileIsSet()) {
			LocalesTableModel localeTableModel = new LocalesTableModel(mainController.getDaxploreFile().getAbout(), mainController);
			table = new JTable(localeTableModel);
			scrollPane.setViewportView(table);
		}
	}
	
	public File showExportDialog() {
		JFileChooser fc = new JFileChooser(Settings.getWorkingDirectory());
		
		FileFilter filter = new FileNameExtensionFilter("Zip files", "zip");
		fc.addChoosableFileFilter(filter);
		fc.setFileFilter(filter);
		
		int returnVal = fc.showSaveDialog(this);
		switch(returnVal) {
		case JFileChooser.APPROVE_OPTION:
			Settings.setWorkingDirectory(fc.getCurrentDirectory());
			return fc.getSelectedFile();
		default:
			return null;
		}
	}

}
