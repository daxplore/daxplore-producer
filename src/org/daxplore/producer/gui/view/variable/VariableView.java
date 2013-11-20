package org.daxplore.producer.gui.view.variable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;
import org.daxplore.producer.gui.view.variable.VariableController.QuestionCommand;
import org.daxplore.producer.gui.widget.TextWidget;

import com.google.common.eventbus.EventBus;

@SuppressWarnings("serial")
public class VariableView extends JPanel {
	
	private JScrollPane scaleScrollPane = new JScrollPane();
	private JScrollPane beforeScrollPane = new JScrollPane();
	private JScrollPane afterScrollPane = new JScrollPane();
	private JScrollPane timePointScrollPane = new JScrollPane();
	
	private JLabel questionID = new JLabel();
	private JPanel fullTextRefHolder;
	private JPanel shortTextRefHolder;
	private TextWidget fullText;
	private TextWidget shortText;

	public VariableView(EventBus eventBus, ActionListener actionListener, MetaQuestion metaQuestion, JTable scaleTable, JTable beforeTable, JTable afterTable, JTable timePointTable) {
		scaleScrollPane.setViewportView(scaleTable);
		beforeScrollPane.setViewportView(beforeTable);
		afterScrollPane.setViewportView(afterTable);
		timePointScrollPane.setViewportView(timePointTable);
		
		fullText = new TextWidget(eventBus);
		shortText = new TextWidget(eventBus);
		
		questionID.setText(metaQuestion.getId());
		fullText.setContent(metaQuestion.getFullTextRef());
		shortText.setContent(metaQuestion.getShortTextRef());
		
		setLayout(new BorderLayout(0, 0));
		
		JPanel topPanel = new JPanel(new BorderLayout());
		
		shortTextRefHolder = new JPanel();
		shortTextRefHolder.setBorder(new TitledBorder(new LineBorder(new Color(0,0,75)), "Short text"));
		shortTextRefHolder.add(shortText);
		
		fullTextRefHolder = new JPanel();
		fullTextRefHolder.setBorder(new TitledBorder(new LineBorder(new Color(0,0,75)), "Full text"));
		fullTextRefHolder.add(fullText);
		
		topPanel.add(shortTextRefHolder, BorderLayout.WEST);
		topPanel.add(fullTextRefHolder, BorderLayout.EAST);
		
		add(topPanel, BorderLayout.NORTH);
		
		JPanel scalePanel = new JPanel();
		add(scalePanel, BorderLayout.CENTER);
		scalePanel.setLayout(new BoxLayout(scalePanel, BoxLayout.X_AXIS));
		
		beforeScrollPane.setPreferredSize(new Dimension(100, 3));
		scalePanel.add(beforeScrollPane);
		
		JPanel midPanel = new JPanel();
		scalePanel.add(midPanel);
		midPanel.setLayout(new BorderLayout(0, 0));
		
		midPanel.add(scaleScrollPane, BorderLayout.CENTER);
		
		JPanel avcionButtonPanel = new JPanel();
		midPanel.add(avcionButtonPanel, BorderLayout.SOUTH);
		
		JButton addButton = new JButton("Add");
		addButton.setActionCommand(QuestionCommand.ADD.toString());
		addButton.addActionListener(actionListener);
		avcionButtonPanel.add(addButton);
		
		JButton upButton = new JButton("Up");
		upButton.setActionCommand(QuestionCommand.UP.toString());
		upButton.addActionListener(actionListener);
		avcionButtonPanel.add(upButton);
		
		JButton downButton = new JButton("Down");
		downButton.setActionCommand(QuestionCommand.DOWN.toString());
		downButton.addActionListener(actionListener);
		avcionButtonPanel.add(downButton);
		
		JButton removeButton = new JButton("Remove");
		removeButton.setActionCommand(QuestionCommand.REMOVE.toString());
		removeButton.addActionListener(actionListener);
		avcionButtonPanel.add(removeButton);
		
		JButton invertButton = new JButton("Invert");
		invertButton.setActionCommand(QuestionCommand.INVERT.toString());
		invertButton.addActionListener(actionListener);
		avcionButtonPanel.add(invertButton);
		
		afterScrollPane.setPreferredSize(new Dimension(100, 3));
		scalePanel.add(afterScrollPane);
		
		add(scalePanel, BorderLayout.CENTER);
		
		JPanel timepointPanel = new JPanel(new BorderLayout());
		timePointScrollPane.setPreferredSize(new Dimension(100, 150)); //TODO: fix size issues, to big if left to it's own devices
		timepointPanel.add(timePointScrollPane, BorderLayout.CENTER);
		add(timepointPanel, BorderLayout.SOUTH);
	}
	
}
