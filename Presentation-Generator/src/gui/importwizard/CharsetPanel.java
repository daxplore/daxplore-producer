package gui.importwizard;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import java.nio.charset.Charset;
import java.util.SortedMap;
import javax.swing.BoxLayout;

public class CharsetPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	static final String ENCODING_COMBO_BOX_LIST_LABEL = "<Select encoding type>";
	protected JComboBox encodingComboBox;
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
						
		encodingComboBox = new JComboBox();
		specifyEncodingPanel.add(encodingComboBox);
		
		encodingListPanel = new JScrollPane();
		contentPanel.add(encodingListPanel, BorderLayout.CENTER);
		encodingListPanel.setBounds(0, 0, 0, -36);
		
		JPanel tablePanel = new JPanel();
		encodingListPanel.setViewportView(tablePanel);
		
		SortedMap<String, Charset> cset = Charset.availableCharsets();

		// populate the combobox with available charsets.
		encodingComboBox.addItem(ENCODING_COMBO_BOX_LIST_LABEL);
		for (String charname : cset.keySet()) {
			encodingComboBox.addItem(charname);
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