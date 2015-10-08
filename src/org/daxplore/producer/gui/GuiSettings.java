/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class GuiSettings {
	
	private static Locale programLocale = Locale.ENGLISH;
	private final static Locale[] availableLocales = new Locale[]{new Locale("sv"), new Locale("en")};
	private static Locale defaultLocale = new Locale("sv");
	private static Locale currentLocale;
	
	public static List<Locale> availableLocales() {
		return Arrays.asList(availableLocales.clone());
	}

	public static Locale getDefaultLocale() {
		return defaultLocale;
	}

	public static void setDefaultLocale(Locale defaultLocale) {
		GuiSettings.defaultLocale = defaultLocale;
	}
	
	public static void setCurrentDisplayLocale(Locale locale) {
		currentLocale = locale;
	}
	
	public static Locale getCurrentDisplayLocale() {
		return currentLocale;
	}
	
	public static Locale getProgramLocale() {
		return programLocale;
	}
}
