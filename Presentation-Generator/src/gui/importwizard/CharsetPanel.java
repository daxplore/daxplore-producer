package gui.importwizard;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.nio.charset.Charset;
import java.util.SortedMap;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import tools.CharsetTest;

public class CharsetPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	static final String ENCODING_COMBO_BOX_LIST_LABEL = "<Select encoding type>";
	static final String ENCODING_COMBO_BOX_SEPARETOR =  "----------------------";
	protected JComboBox<String> encodingComboBox;
	protected JScrollPane encodingListPanel;
	protected JPanel contentPanel;
	

	/**
	 * Constructor.
	 */
	public CharsetPanel() {
		
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		contentPanel = new JPanel();
		add(contentPanel);
		contentPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel specifyEncodingPanel = new JPanel();
		contentPanel.add(specifyEncodingPanel, BorderLayout.NORTH);
		
		JLabel lblNewLabel = new JLabel("Specify encoding:");
		specifyEncodingPanel.add(lblNewLabel);
						
		encodingComboBox = new JComboBox<String>();
		specifyEncodingPanel.add(encodingComboBox);
		
		encodingListPanel = new JScrollPane();
		contentPanel.add(encodingListPanel, BorderLayout.CENTER);
		encodingListPanel.setBounds(0, 0, 0, -36);
		
		JPanel tablePanel = new JPanel();
		encodingListPanel.setViewportView(tablePanel);
		
		SortedMap<String, Charset> cset = Charset.availableCharsets();

		// populate the combobox with available charsets.
		encodingComboBox.addItem(ENCODING_COMBO_BOX_LIST_LABEL);
		encodingComboBox.addItem("US-ASCII");
		encodingComboBox.addItem("UTF-8");
		encodingComboBox.addItem("ISO-8859-1");
		encodingComboBox.addItem("windows-1252");
		encodingComboBox.addItem(ENCODING_COMBO_BOX_SEPARETOR);
		for (String charname : cset.keySet()) {
			if(CharsetTest.charset8bitTest(cset.get(charname))){
				encodingComboBox.addItem(charname);
			}
		}
	}
	
	public void addEncodingComboBoxAction(ActionListener l) {
		encodingComboBox.addActionListener(l);
	}
	
	public void setEncodingList(JList list) {
		encodingListPanel.getViewport().setView(list);
		encodingListPanel.validate();
	}
}
