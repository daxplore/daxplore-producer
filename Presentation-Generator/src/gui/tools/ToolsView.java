package gui.tools;

import gui.MainController;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import daxplorelib.DaxploreException;
import daxplorelib.calc.Crosstabs;
import daxplorelib.metadata.MetaGroup;
import daxplorelib.metadata.MetaQuestion;

@SuppressWarnings("serial")
public class ToolsView extends JPanel {
	
	private JTextField textField;
	
	public static final String IMPORT_RAW_BUTTON_ACTION_COMMAND = "importRawButtonActionCommand";
	
	public ToolsView(final MainController mainController) {
		
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
				Crosstabs crosstabs = mainController.getDaxploreFile().getCrosstabs();
				try {
					MetaGroup perspectives = mainController.getDaxploreFile().getMetaData().getMetaGroupManager().getPerspectiveGroup();
					for(MetaGroup group : mainController.getDaxploreFile().getMetaData().getMetaGroupManager().getQuestionGroups()) {
						for(MetaQuestion question : group.getQuestions()) {
							for(MetaQuestion perspective : perspectives.getQuestions()) {
								System.out.println(crosstabs.crosstabs2(question, perspective).toJsonString());
							}
						}
					}
				} catch (DaxploreException | SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		add(btnGenerateData);
		
	}
}
