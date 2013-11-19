package org.daxplore.producer.gui;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Settings {
	
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
		Settings.defaultLocale = defaultLocale;
	}
	
	public static void setCurrentDisplayLocale(Locale locale) {
		currentLocale = locale;
	}
	
	public static Locale getCurrentDisplayLocale() {
		return currentLocale;
	}
}
