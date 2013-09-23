package org.daxplore.producer.gui.question;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;
import org.daxplore.producer.daxplorelib.metadata.MetaScale;
import org.daxplore.producer.daxplorelib.metadata.MetaScale.Option;
import org.daxplore.producer.gui.MainController;
import org.daxplore.producer.gui.question.QuestionController.Command;
import org.daxplore.producer.gui.widget.TextWidget;

@SuppressWarnings("serial")
public class QuestionView extends JPanel {
	
	private QuestionController controller;
	private MainController mainController;
	private JLabel questionID;
	private JPanel fullTextRefHolder;
	private JPanel shortTextRefHolder;
	private TextWidget fullText = new TextWidget();
	private TextWidget shortText = new TextWidget();
	private JScrollPane scaleScrollPane;
	private JScrollPane beforeScrollPane;
	private JScrollPane afterScrollPane;
	private JScrollPane timePointScrollPane;
	private JButton addButton;
	private JButton upButton;
	private JButton downButton;
	private JButton removeButton;
	private JButton invertButton;
	private JPanel panel_1;
	private JPanel panel_2;
	private JPanel panel_3;
	private JPanel panel_4;
	private JPanel panel_5;
	
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
		panel.setLayout(new BorderLayout(0, 0));
		
		panel_4 = new JPanel();
		panel.add(panel_4, BorderLayout.NORTH);
		
		shortTextRefHolder = new JPanel();
		panel_4.add(shortTextRefHolder);
		shortTextRefHolder.setBorder(new TitledBorder(new LineBorder(new Color(0,0,75)), "Short text"));
		shortTextRefHolder.add(shortText);
		
		fullTextRefHolder = new JPanel();
		panel_4.add(fullTextRefHolder);
		fullTextRefHolder.setBorder(new TitledBorder(new LineBorder(new Color(0,0,75)), "Full text"));
		fullTextRefHolder.add(fullText);
		
		questionID = new JLabel();
		panel_4.add(questionID);
		
		panel_5 = new JPanel();
		panel.add(panel_5, BorderLayout.CENTER);
		
		timePointScrollPane = new JScrollPane();
		panel_5.add(timePointScrollPane);
		
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
		addButton.setActionCommand(Command.ADD);
		addButton.addActionListener(controller);
		panel_3.add(addButton);
		
		upButton = new JButton("Up");
		upButton.setActionCommand(Command.UP);
		upButton.addActionListener(controller);
		panel_3.add(upButton);
		
		downButton = new JButton("Down");
		downButton.setActionCommand(Command.DOWN);
		downButton.addActionListener(controller);
		panel_3.add(downButton);
		
		removeButton = new JButton("Remove");
		removeButton.setActionCommand(Command.REMOVE);
		removeButton.addActionListener(controller);
		panel_3.add(removeButton);
		
		invertButton = new JButton("Invert");
		invertButton.setActionCommand(Command.INVERT);
		invertButton.addActionListener(controller);
		panel_3.add(invertButton);
		
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
	
	JScrollPane getTimePointScrollPane() {
		return timePointScrollPane;
	}
}
