package gui.question;

import gui.MainController;

import javax.swing.DefaultListModel;

import daxplorelib.metadata.MetaQuestion;
import daxplorelib.metadata.MetaScale;
import daxplorelib.metadata.MetaScale.Option;

public class QuestionController {
	
	QuestionView view;
	DefaultListModel<MetaScale.Option> listModel;
	
	public QuestionController(MainController mainController, QuestionView view) {
		this.view = view;
	}

	public void openMetaQuestion(MetaQuestion mq) {
		listModel = new DefaultListModel<MetaScale.Option>();
		for(Option opt: mq.getScale().getOptions()) {
			listModel.addElement(opt);
		}
		view.setMetaQuestion(mq, listModel);
		
	}
	
}
