package gui.tools;

import gui.MainController;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.json.simple.JSONArray;

import daxplorelib.DaxploreException;
import daxplorelib.calc.Crosstabs;
import daxplorelib.metadata.MetaGroup;
import daxplorelib.metadata.MetaQuestion;
import javax.swing.JScrollPane;
import javax.swing.JTable;

@SuppressWarnings("serial")
public class ToolsView extends JPanel {
	
	Charset charset = Charset.forName("UTF-8");
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
			@SuppressWarnings("unchecked")
			@Override
			public void actionPerformed(ActionEvent e) {
				File uploadFile = showExportDialog();
				if(uploadFile == null) {
					return;
				}
				
				Locale locale = new Locale("sv");
				Crosstabs crosstabs = mainController.getDaxploreFile().getCrosstabs();
				Logger.getGlobal().log(Level.INFO, "Starting to generate json data");
				try {
					MetaGroup perspectives = mainController.getDaxploreFile().getMetaData().getMetaGroupManager().getPerspectiveGroup();
					SortedSet<MetaQuestion> selectedQuestions = new TreeSet<MetaQuestion>(new Comparator<MetaQuestion>() {
						@Override
						public int compare(MetaQuestion o1, MetaQuestion o2) {
							return o1.getId().compareTo(o2.getId());
						}
					});
					for(MetaQuestion perspective : perspectives.getQuestions()) {
						selectedQuestions.add(perspective);
					}
					JSONArray dataJSON = new JSONArray();
					for(MetaGroup group : mainController.getDaxploreFile().getMetaData().getMetaGroupManager().getQuestionGroups()) {
						for(MetaQuestion question : group.getQuestions()) {
							selectedQuestions.add(question);
							for(MetaQuestion perspective : perspectives.getQuestions()) {
								dataJSON.add(crosstabs.crosstabs2(question, perspective).toJSONObject());
							}
						}
					}
					
					JSONArray questionJSON = new JSONArray();
					for(MetaQuestion q : selectedQuestions) {
						questionJSON.add(q.toJSONObject(locale));
					}
					
				    ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(uploadFile));
				    
				    writeZipString(zout, "data/data.json", dataJSON.toJSONString());
				    
				    writeZipString(zout, "meta/questions_"+locale.toLanguageTag()+".json", questionJSON.toJSONString());
				    
				    String groupJSONString = mainController.getDaxploreFile().getMetaData().getMetaGroupManager().getQuestionGroupsJSON(locale);
				    writeZipString(zout, "meta/groups_"+locale.toLanguageTag()+".json", groupJSONString);
				    
				    writeZipString(zout, "meta/perspectives_"+locale.toLanguageTag()+".json", perspectives.toJSONObject(locale).toJSONString());
				    
				    zout.flush();
				    zout.close();

				} catch (DaxploreException | SQLException | IOException e1) {
					// TODO Auto-generated catch block
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
		JFileChooser fc = new JFileChooser();
		int returnVal = fc.showSaveDialog(this);
		switch(returnVal) {
		case JFileChooser.APPROVE_OPTION:
			return fc.getSelectedFile();
		default:
			return null;
		}
	}
	
	public void writeZipString(ZipOutputStream zout, String filename, String dataString) throws IOException {
		ZipEntry entry = new ZipEntry(filename);
	    zout.putNextEntry(entry);
	    ByteBuffer buffer = charset.encode(dataString);
	    byte[] outbytes = new byte[buffer.limit()];
	    buffer.get(outbytes);
	    zout.write(outbytes);
	    zout.flush();
	    zout.closeEntry();
	}
}
