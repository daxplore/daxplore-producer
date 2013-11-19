package org.daxplore.producer.gui.event;

import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;

public class EditQuestionEvent {
	MetaQuestion metaQuestion;

	public EditQuestionEvent(MetaQuestion metaQuestion) {
		this.metaQuestion = metaQuestion;
	}
	
	public MetaQuestion getMetaQuestion() {
		return metaQuestion;
	}
}
