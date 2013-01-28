package gui.question;

import gui.MainController;
import gui.widget.TextWidget;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListCellRenderer;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import daxplorelib.metadata.MetaQuestion;
import daxplorelib.metadata.MetaScale;
import daxplorelib.metadata.MetaScale.Option;

import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JList;

@SuppressWarnings("serial")
public class QuestionView extends JPanel {
	
	private QuestionController controller;
	private MainController mainController;
	private JLabel questionID;
	private JPanel fullTextRefHolder;
	private JPanel shortTextRefHolder;
	private TextWidget fullText = new TextWidget();
	private TextWidget shortText = new TextWidget();
	private JPanel panel_1;
	private JList<MetaScale.Option> list;
	
	class OptionListRenderer implements ListCellRenderer<MetaScale.Option> {

		@Override
		public Component getListCellRendererComponent(JList<? extends Option> list, Option value, int index, boolean isSelected, boolean cellHasFocus) {
			return null;
		}
		
	}
	
	public QuestionView(MainController mainController) {
		this.mainController = mainController;
		this.controller = new QuestionController(this.mainController, this);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{440, 10, 0};
		gridBagLayout.rowHeights = new int[]{300, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.insets = new Insets(0, 0, 0, 5);
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		add(panel, gbc_panel);
		
		questionID = new JLabel();
		
		fullTextRefHolder = new JPanel();
		fullTextRefHolder.setBorder(new TitledBorder(new LineBorder(new Color(0,0,75)), "Full text"));
		fullTextRefHolder.add(fullText);
		
		shortTextRefHolder = new JPanel();
		shortTextRefHolder.setBorder(new TitledBorder(new LineBorder(new Color(0,0,75)), "Short text"));
		shortTextRefHolder.add(shortText);
		
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING, false)
							.addGroup(Alignment.LEADING, gl_panel.createSequentialGroup()
								.addContainerGap()
								.addComponent(shortTextRefHolder, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
							.addGroup(Alignment.LEADING, gl_panel.createSequentialGroup()
								.addContainerGap()
								.addComponent(fullTextRefHolder, GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE)))
						.addGroup(gl_panel.createSequentialGroup()
							.addContainerGap()
							.addComponent(questionID)))
					.addContainerGap(184, Short.MAX_VALUE))
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addComponent(questionID, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
					.addGap(19)
					.addComponent(fullTextRefHolder, GroupLayout.PREFERRED_SIZE, 64, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(shortTextRefHolder, GroupLayout.PREFERRED_SIZE, 64, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(195, Short.MAX_VALUE))
		);
		panel.setLayout(gl_panel);
		
		panel_1 = new JPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.anchor = GridBagConstraints.WEST;
		gbc_panel_1.fill = GridBagConstraints.VERTICAL;
		gbc_panel_1.gridx = 1;
		gbc_panel_1.gridy = 0;
		add(panel_1, gbc_panel_1);
		
		list = new JList<Option>();
		panel_1.add(list);
	}

	public QuestionController getController() {
		return controller;
	}
	
	void setMetaQuestion(MetaQuestion mq, DefaultListModel<MetaScale.Option> dlm) {
		questionID.setText(mq.getId());
		fullText.setTextReference(mq.getFullTextRef());
		shortText.setTextReference(mq.getShortTextRef());
		list.setModel(dlm);
	    this.validate();
	    this.repaint();
	}
}
