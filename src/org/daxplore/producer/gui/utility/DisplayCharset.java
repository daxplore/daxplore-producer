/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui.utility;

import java.nio.charset.Charset;

import org.daxplore.producer.gui.GuiSettings;

public class DisplayCharset {
	public Charset charset;
	public String alternativeText;
	
	public DisplayCharset(Charset charset) {
		this.charset = charset;
	}
	
	public DisplayCharset(String text) {
		alternativeText = text;
	}
	
	@Override
	public String toString() {
		if(charset!=null) {
			return charset.displayName(GuiSettings.getProgramLocale());
		}
		return alternativeText;
	}
}
