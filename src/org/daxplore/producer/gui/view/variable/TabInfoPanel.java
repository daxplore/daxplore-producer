package org.daxplore.producer.gui.view.variable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;
import org.daxplore.producer.gui.widget.TextWidget;

import com.google.common.eventbus.EventBus;

public class TabInfoPanel extends JPanel{
	
	private JPanel fullTextRefHolder;
	private JPanel shortTextRefHolder;
	private TextWidget fullText;
	private TextWidget shortText;
	private JLabel column = new JLabel();
	private JScrollPane timePointScrollPane = new JScrollPane();
	
	public TabInfoPanel(EventBus eventBus, MetaQuestion metaQuestion, JTable timePointTable) {
		timePointScrollPane.setViewportView(timePointTable);
		
		// Assorted texts (NORTH)
		fullText = new TextWidget(eventBus);
		shortText = new TextWidget(eventBus);
		
		column.setText(metaQuestion.getColumn());
		fullText.setContent(metaQuestion.getFullTextRef());
		shortText.setContent(metaQuestion.getShortTextRef());
		
		setLayout(new BorderLayout(0, 0));
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		
		shortTextRefHolder = new JPanel();
		shortTextRefHolder.setBorder(new TitledBorder(new LineBorder(new Color(0,0,75)), "Short text"));
		shortTextRefHolder.add(shortText);
		
		fullTextRefHolder = new JPanel();
		fullTextRefHolder.setBorder(new TitledBorder(new LineBorder(new Color(0,0,75)), "Full text"));
		fullTextRefHolder.add(fullText);
		
		topPanel.add(shortTextRefHolder, BorderLayout.WEST);
		topPanel.add(fullTextRefHolder, BorderLayout.EAST);
		
		add(topPanel, BorderLayout.NORTH);
		
		// Timepoints (SOUTH)
		JPanel timepointPanel = new JPanel(new BorderLayout());
		timePointScrollPane.setPreferredSize(new Dimension(100, 150)); //TODO: fix size issues, too big if left to it's own devices
		timepointPanel.add(timePointScrollPane, BorderLayout.CENTER);
		add(timepointPanel, BorderLayout.SOUTH);
	}

}
