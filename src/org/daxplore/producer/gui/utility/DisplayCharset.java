package org.daxplore.producer.gui.utility;

import java.nio.charset.Charset;

import org.daxplore.producer.gui.Settings;

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
			return charset.displayName(Settings.getProgramLocale());
		}
		return alternativeText;
	}
}
