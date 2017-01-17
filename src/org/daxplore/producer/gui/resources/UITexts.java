/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui.resources;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import org.daxplore.producer.daxplorelib.resources.DefaultPresenterTexts;
import org.daxplore.producer.gui.GuiSettings;
import org.daxplore.producer.tools.MyTools;

/**
 * A small one-off ResourceBundle wrapper class that reads localized GuiTexts_?.properties files
 * from the resource package.
 * 
 * <p>Hides unused methods, adds some convenience via Message format and allows for
 * a completely different implementation if we want to switch to a UTF-8 format later.</p>
 * 
 * <p>Same as {@link DefaultPresenterTexts}.</p>
 */
public class UITexts {
	
	private static UITexts texts;
	
	private ResourceBundle textBundle;
	
	static {
		// set default bundle
		texts = new UITexts(GuiSettings.getProgramLocale());
	}
	
	private UITexts(Locale locale) {
		textBundle = ResourceBundle.getBundle("org.daxplore.producer.gui.resources.UITexts", locale, this.getClass().getClassLoader());
	}
	
	public static void setLocale(Locale locale) {
		texts = new UITexts(locale);
	}
	
	/**
	 * See {@link ResourceBundle#getString(String)}
	 */
	public static String get(String key) {
		if(texts.textBundle.containsKey(key)) {
			return texts.textBundle.getString(key);
		}
		
		//TODO log missing property?
		//TODO fall back to default locale?
		return "[" + key + "]";
	}
	
	public static boolean contains(String key) {
		return texts.textBundle.containsKey(key);
	}
	
	/**
	 * Helper method for getting formatted texts using the {@link MessageFormat} format.
	 * 
	 * @param key key the key for the desired string
	 * @param arguments the arguments to put into the string
	 * @return the string for the given key formatted with the arguments
	 */
	public static String format(String key, Object... arguments) {
		if(texts.textBundle.containsKey(key)) {
			return MessageFormat.format(get(key), arguments);
		}
		
		//TODO log missing property?
		//TODO fall back to default locale?
		return "[" + key + ": " + MyTools.join(arguments, ", ") + "]";
	}
}
