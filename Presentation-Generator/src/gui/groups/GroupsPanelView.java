package gui.groups;

import gui.GuiMain;
import gui.QuestionWidget;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionListener;

import daxplorelib.DaxploreException;
import daxplorelib.metadata.MetaQuestion;

@SuppressWarnings("serial")
public class GroupsPanelView extends JPanel {
	
	private GuiMain guiMain;
	private List<QuestionWidget> questionList = new LinkedList<QuestionWidget>();
	private JScrollPane questionsScrollPane = new JScrollPane();
	private JScrollPane groupsScollPane = new JScrollPane();
	
	private class QuestionListModel implements ListModel<QuestionWidget> {
		@Override
		public int getSize() {
			return questionList.size();
		}

		@Override
		public QuestionWidget getElementAt(int index) {
			return questionList.get(index);
		}

		@Override
		public void addListDataListener(ListDataListener l) {
			// TODO Auto-generated method stub
		}
		@Override
		public void removeListDataListener(ListDataListener l) {
			// TODO Auto-generated method stub
		}
	}
	
	private class QuestionListCellRenderer implements ListCellRenderer<QuestionWidget> {

		@Override
		public Component getListCellRendererComponent(JList<? extends QuestionWidget> list, QuestionWidget value, int index, boolean isSelected, boolean cellHasFocus) {
			if(isSelected) {
				value.setBackground(new Color(255, 255, 200));
			} else {
				value.setBackground(new Color(255,255,255));
			}
			return value;
		}
	}
	
	private class GroupListModel implements ListModel<QuestionWidget> {

		@Override
		public int getSize() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public QuestionWidget getElementAt(int index) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void addListDataListener(ListDataListener l) {
			// TODO Auto-generated method stub
		}

		@Override
		public void removeListDataListener(ListDataListener l) {
			// TODO Auto-generated method stub
		}
	}
	
	public GroupsPanelView(GuiMain guiMain) {
		this.guiMain = guiMain;
		
		JPanel questionsPanel = new JPanel();
		questionsPanel.setBounds(6, 43, 334, 692);
		setLayout(null);
		add(questionsPanel);
		questionsPanel.setLayout(new BorderLayout(0, 0));
		
		questionsPanel.add(questionsScrollPane);
		
		JPanel groupsPanel = new JPanel();
		groupsPanel.setBounds(350, 43, 334, 450);
		add(groupsPanel);
		groupsPanel.setLayout(new BorderLayout(0,0));
		groupsPanel.add(groupsScollPane);

	}
	
	public void loadData() {
		if(guiMain.getGuiFile().isSet()) {
			try {
				List<MetaQuestion> mqList = guiMain.getGuiFile().getDaxploreFile().getMetaData().getAllQuestions();
				int i = 0;
				for(MetaQuestion mq: mqList) {
					questionList.add(new QuestionWidget(mq));
					i++;
				}
				System.out.println("Added "+ i + " questions");
				JList<QuestionWidget> list = new JList<QuestionWidget>(new QuestionListModel());
				list.setCellRenderer(new QuestionListCellRenderer());
				list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				questionsScrollPane.setViewportView(list);
			} catch (DaxploreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
}
