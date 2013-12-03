package org.daxplore.producer.gui.event;

import java.awt.Component;

import org.daxplore.producer.daxplorelib.metadata.textreference.TextReference;

public class EditTextRefEvent {
	private TextReference textRef;
	private Component dialogParent;

	public EditTextRefEvent(TextReference textRef) {
		this.textRef = textRef;
	}

	public EditTextRefEvent(TextReference textRef, Component dialogParent) {
		this.textRef = textRef;
		this.dialogParent = dialogParent;
	}
	
	public TextReference getTextReference() {
		return textRef;
	}
	
	public Component getDialogParent() {
		return dialogParent;
	}
}
