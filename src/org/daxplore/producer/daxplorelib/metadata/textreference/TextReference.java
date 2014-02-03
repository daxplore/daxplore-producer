/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.daxplorelib.metadata.textreference;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.beust.jcommander.internal.Lists;

public class TextReference extends TextReferenceReference {
	//TODO using a static list inside a data structure (daxplore file) is highly problematic!
	private static List<Locale> activeLocales;
	
	Map<Locale, String> localeMap;
	boolean modified = false;
	
	
	protected TextReference(String refstring, Map<Locale,String> localeMap) {
		super(refstring);
		this.localeMap = localeMap;
	}
	
	public String get(Locale locale) {
		return localeMap.get(locale);
	}
	
	public void put(String text, Locale locale) {
		String value = (text==null ? "" : text);
		String currentValue = localeMap.get(locale);
		if(!value.equals(currentValue)) {
			localeMap.put(locale, value);
			modified = true;
		}
	}
	
	public boolean has(Locale locale) {
		return localeMap.containsKey(locale);
	}
	
	/**
	 * Get a list of locales set for this TextReference
	 * @return list of locales
	 * @throws SQLException 
	 */
	public List<Locale> getLocales() {
		return Lists.newLinkedList(localeMap.keySet());
	}
	
	public boolean equalsLocale(TextReference other, Locale locale) {
		if(!has(locale) || !other.has(locale)) {
			return false;
		}
		return get(locale).equals(other.get(locale));
	}
	
	public static void setActiveLocales(List<Locale> localelist) {
		activeLocales = localelist;
	}
	
	public List<Locale> getActiveLocales() {
		return activeLocales;
	}
}
