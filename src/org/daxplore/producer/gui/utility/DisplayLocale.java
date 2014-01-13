/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui.utility;

import java.util.Locale;

import org.daxplore.producer.gui.Settings;

public class DisplayLocale {
	
	public Locale locale;
	public String alternativeText;
	
	public DisplayLocale(Locale locale) {
		this.locale = locale;
	}
	
	public DisplayLocale(String text) {
		alternativeText = text;
	}
	
	@Override
	public String toString() {
		if(locale!=null) {
			return locale.getDisplayLanguage(Settings.getProgramLocale());
		}
		return alternativeText;
	}
}
