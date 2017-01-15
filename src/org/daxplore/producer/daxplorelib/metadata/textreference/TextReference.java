/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.daxplorelib.metadata.textreference;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Strings;

public class TextReference extends TextReferenceReference {
	//TODO using a static list inside a data structure (daxplore file) is highly problematic!
	private static List<Locale> activeLocales;
	
	Map<Locale, String> localeMap;
	boolean modified = false;
	
	
	protected TextReference(String refstring, Map<Locale,String> localeMap) {
		super(refstring);
		this.localeMap = localeMap;
	}
	
	public String getText(Locale locale) {
		return localeMap.get(locale);
	}
	
	/**
	 * Returns the user facing text for the TextReference.
	 * Returns the textref itself if the text is empty or null.
	 */
	public String getWithPlaceholder(Locale locale) {
		String text = localeMap.get(locale);
		if(Strings.isNullOrEmpty(text)) {
			return "[" + reference + "]";
		}
		return text; 
	}
	
	public void put(String text, Locale locale) {
		String value = (text==null ? "" : text);
		String currentValue = localeMap.get(locale);
		if(!value.equals(currentValue)) {
			localeMap.put(locale, value);
			modified = true;
		}
	}
	
	public boolean hasText(Locale locale) {
		return localeMap.get(locale) != null;
	}
	
	public boolean hasLocale(Locale locale) {
		return localeMap.containsKey(locale);
	}
	
	/**
	 * Get a list of locales set for this TextReference
	 * @return list of locales
	 */
	public List<Locale> getLocales() {
		return Lists.newLinkedList(localeMap.keySet());
	}
	
	public boolean equalsLocale(TextReference other, Locale locale) {
		if(!hasLocale(locale) || !other.hasLocale(locale)) {
			return false;
		}
		return getText(locale).equals(other.getText(locale));
	}
	
	public static void setActiveLocales(List<Locale> localelist) {
		activeLocales = localelist;
	}
	
	public List<Locale> getActiveLocales() {
		return activeLocales;
	}
}
