package org.daxplore.producer.gui.utility;

import java.nio.charset.Charset;
import java.util.Locale;

public class DisplayCharset {
	public Charset charset;
	public String alternativeText;
	public DisplayCharset(Charset charset) {
		this.charset = charset;
	}
	public DisplayCharset(String text) {
		alternativeText = text;
	}
	public String toString() {
		if(charset!=null) {
			return charset.displayName(Locale.ENGLISH);
		} else {
			return alternativeText;
		}
	}
}
