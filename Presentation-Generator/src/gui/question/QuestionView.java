package gui.question;

import gui.MainController;
import gui.widget.TextWidget;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListCellRenderer;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import daxplorelib.metadata.MetaQuestion;
import daxplorelib.metadata.MetaScale;
import daxplorelib.metadata.MetaScale.Option;

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
	private JScrollPane scaleScrollPane;
	
	class OptionListRenderer implements ListCellRenderer<MetaScale.Option> {

		@Override
		public Component getListCellRendererComponent(JList<? extends Option> list, Option value, int index, boolean isSelected, boolean cellHasFocus) {
			return null;
		}
		
	}
	
	public QuestionView(MainController mainController) {
		this.mainController = mainController;
		this.controller = new QuestionController(this.mainController, this);
		setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.WEST);
		
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
		add(panel_1, BorderLayout.CENTER);
		
		
		scaleScrollPane = new JScrollPane();
		panel_1.add(scaleScrollPane);
	}

	public QuestionController getController() {
		return controller;
	}
	
	void setMetaQuestion(MetaQuestion mq, DefaultListModel<MetaScale.Option> dlm) {
		questionID.setText(mq.getId());
		fullText.setContent(mq.getFullTextRef());
		shortText.setContent(mq.getShortTextRef());
		ScaleTableModel scaleTableModel = new ScaleTableModel(mq.getScale());
		ScaleTable scaleTable = new ScaleTable(scaleTableModel);
		scaleScrollPane.setViewportView(scaleTable);
	    this.validate();
	    this.repaint();
	}
}
