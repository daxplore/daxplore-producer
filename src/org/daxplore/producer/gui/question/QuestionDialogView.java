package org.daxplore.producer.gui.question;

import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;
import org.daxplore.producer.gui.widget.TextWidget;

import com.google.common.eventbus.EventBus;

public class QuestionDialogView extends JDialog{

	
	private JScrollPane scaleScrollPane;
	private JScrollPane beforeScrollPane;
	private JScrollPane afterScrollPane;
	private JScrollPane timePointScrollPane;
	
	private JLabel questionID;
	private JPanel fullTextRefHolder;
	private JPanel shortTextRefHolder;
	private TextWidget fullText;
	private TextWidget shortText;

	public QuestionDialogView(EventBus eventBus, ActionListener actionListener) {
		
	}

	void setMetaQuestion(MetaQuestion mq) {
		questionID.setText(mq.getId());
		fullText.setContent(mq.getFullTextRef());
		shortText.setContent(mq.getShortTextRef());
	    validate();
	    repaint();
	}
	
	void setScaleTable(JTable table) {
		scaleScrollPane.setViewportView(table);
	}
	
	void setBeforeTable(JTable table) {
		beforeScrollPane.setViewportView(table);
	}
	
	void setAfterTable(JTable table) {
		afterScrollPane.setViewportView(table);
	}
	
	void setTimePointTable(JTable table) {
		timePointScrollPane.setViewportView(table);
	}
}
