package org.daxplore.producer.daxplorelib.metadata.textreference;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TextReference extends TextReferenceReference{
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
		String currentValue = localeMap.get(locale);
		if(!text.equals(currentValue)) {
			localeMap.put(locale, text);
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
		return new LinkedList<>(localeMap.keySet());
	}
	
	public boolean equalsLocale(TextReference other, Locale locale) {
		if(!has(locale) || !other.has(locale)) {
			return false;
		}
		return get(locale).equals(other.get(locale));
	}
}
