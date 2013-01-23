package gui.tools;

import gui.GuiMain;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import daxplorelib.DaxploreException;

public class ToolsPanelView extends JPanel {

	private JTextField textField;

	public static final String IMPORT_RAW_BUTTON_ACTION_COMMAND = "importRawButtonActionCommand";

	public ToolsPanelView(final GuiMain guiMain) {

		JButton btnImportfromraw = new JButton("ImportFromRaw");
		btnImportfromraw.setBounds(12, 67, 145, 25);
		btnImportfromraw.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String localeText = textField.getText();
				if (localeText != null && !"".equals(localeText)) {
					Locale loc = new Locale(localeText);
					if (!"und".equals(loc.toLanguageTag())) {
						try {
							guiMain.getGuiFile()
									.getDaxploreFile()
									.getMetaData()
									.importFromRaw(
											guiMain.getGuiFile()
													.getDaxploreFile(), loc);
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

		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					guiMain.getGuiFile().getDaxploreFile().getMetaData().save();
				} catch (DaxploreException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnSave.setBounds(370, 263, 68, 25);
		add(btnSave);

		JLabel lblEnterLocale = new JLabel("Enter locale");
		lblEnterLocale.setBounds(12, 12, 117, 15);
		add(lblEnterLocale);

		textField = new JTextField();
		textField.setBounds(147, 10, 114, 19);
		add(textField);
		textField.setColumns(10);

	}
}
