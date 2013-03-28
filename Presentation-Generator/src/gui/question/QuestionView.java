package gui.question;

import gui.MainController;
import gui.widget.TextWidget;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import daxplorelib.metadata.MetaQuestion;
import daxplorelib.metadata.MetaScale;
import daxplorelib.metadata.MetaScale.Option;
import javax.swing.BoxLayout;
import java.awt.Dimension;

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
	private JScrollPane beforeScrollPane;
	private JScrollPane afterScrollPane;
	private JPanel panel_2;
	private JPanel panel_3;
	private JButton addButton;
	private JButton upButton;
	private JButton downButton;
	private JButton removeButton;
	
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
		add(panel, BorderLayout.NORTH);
		
		questionID = new JLabel();
		
		fullTextRefHolder = new JPanel();
		fullTextRefHolder.setBorder(new TitledBorder(new LineBorder(new Color(0,0,75)), "Full text"));
		fullTextRefHolder.add(fullText);
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		shortTextRefHolder = new JPanel();
		shortTextRefHolder.setBorder(new TitledBorder(new LineBorder(new Color(0,0,75)), "Short text"));
		shortTextRefHolder.add(shortText);
		panel.add(shortTextRefHolder);
		panel.add(fullTextRefHolder);
		panel.add(questionID);
		
		panel_1 = new JPanel();
		add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));
		
		beforeScrollPane = new JScrollPane();
		beforeScrollPane.setPreferredSize(new Dimension(100, 3));
		panel_1.add(beforeScrollPane);
		
		panel_2 = new JPanel();
		panel_1.add(panel_2);
		panel_2.setLayout(new BorderLayout(0, 0));
		
		
		scaleScrollPane = new JScrollPane();
		panel_2.add(scaleScrollPane, BorderLayout.CENTER);
		
		panel_3 = new JPanel();
		panel_2.add(panel_3, BorderLayout.SOUTH);
		
		addButton = new JButton("Add");
		panel_3.add(addButton);
		
		upButton = new JButton("Up");
		panel_3.add(upButton);
		
		downButton = new JButton("Down");
		panel_3.add(downButton);
		
		removeButton = new JButton("Remove");
		panel_3.add(removeButton);
		
		afterScrollPane = new JScrollPane();
		afterScrollPane.setPreferredSize(new Dimension(100, 3));
		panel_1.add(afterScrollPane);
	}

	public QuestionController getController() {
		return controller;
	}
	
	void setMetaQuestion(MetaQuestion mq) {
		questionID.setText(mq.getId());
		fullText.setContent(mq.getFullTextRef());
		shortText.setContent(mq.getShortTextRef());
	    this.validate();
	    this.repaint();
	}
	
	JScrollPane getScaleScrollPane() {
		return scaleScrollPane;
	}

	JScrollPane getBeforeScrollPane() {
		return beforeScrollPane;
	}

	JScrollPane getAfterScrollPane() {
		return afterScrollPane;
	}

}
