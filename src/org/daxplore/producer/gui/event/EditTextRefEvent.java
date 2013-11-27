package org.daxplore.producer.gui.event;

import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;

public class EditTextRefEvent {
	private TextReference textRef;
	
	public EditTextRefEvent(TextReference textRef) {
		this.textRef = textRef;
	}
	
	public TextReference getTextReference() {
		return textRef;
	}
}
