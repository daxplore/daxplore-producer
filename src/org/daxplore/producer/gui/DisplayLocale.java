package org.daxplore.producer.gui;

import java.util.Locale;

public class DisplayLocale {
	public Locale locale;
	public String alternativeText;
	public DisplayLocale(Locale locale) {
		this.locale = locale;
	}
	public DisplayLocale(String text) {
		alternativeText = text;
	}
	public String toString() {
		if(locale!=null) {
			return locale.getDisplayLanguage(Locale.ENGLISH);
		} else {
			return alternativeText;
		}
	}
}
