package org.daxplore.producer.daxplorelib.metadata.textreference;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TextReference extends TextReferenceReference{
	protected Map<Locale, String> localeMap;
	
	protected boolean modified = false;
	
	
	protected TextReference(String refstring, Map<Locale,String> localeMap) {
		super(refstring);
		this.localeMap = localeMap;
	}
	
	public String get(Locale locale) {
		return localeMap.get(locale);
	}
	
	public void put(String text, Locale locale) {
		localeMap.put(locale, text);
		modified = true;
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
		return new LinkedList<Locale>(localeMap.keySet());
	}
	
	public boolean equalsLocale(TextReference other, Locale locale) throws SQLException {
		if(has(locale) && other.has(locale)) {
			return get(locale).equals(other.get(locale));
		} else return false;
	}
}