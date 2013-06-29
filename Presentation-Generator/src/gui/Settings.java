package gui;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Settings {
	
	private final static Locale[] availableLocales = new Locale[]{new Locale("sv"), new Locale("en")};
	private static Locale defaultLocale = new Locale("sv");
	
	private static File currentDir;
	
	public static List<Locale> availableLocales() {
		return Arrays.asList(availableLocales.clone());
	}

	public static Locale getDefaultLocale() {
		return defaultLocale;
	}

	public static void setDefaultLocale(Locale defaultLocale) {
		Settings.defaultLocale = defaultLocale;
	}
	
	public static void setWorkingDirectory(File directory) {
		currentDir = directory;
	}
	
	public static File getWorkingDirectory() {
		return currentDir;
	}
	
}
