package org.daxplore.producer.gui.event;

import java.util.Locale;

public class DisplayLocaleSelectEvent {
	private Locale locale;
	
	public DisplayLocaleSelectEvent(Locale locale) {
		this.locale = locale;
	}
	
	public Locale getLocale() {
		return locale;
	}
}