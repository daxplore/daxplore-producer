package org.daxplore.producer.gui.resources;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import org.daxplore.producer.tools.MyTools;

/**
 * A small one-off ResourceBundle wrapper class that reads localized GuiTexts_?.properties files
 * from the resource package.
 * 
 * <p>Hides unused methods, adds some convenience via Message format and allows for
 * a completely different implementation if we want to switch to a UTF-8 format later.</p>
 */
public class GuiTexts {
	private ResourceBundle textBundle;
	
	public GuiTexts(Locale locale) {
		textBundle = ResourceBundle.getBundle("org.daxplore.producer.gui.resources.GuiTexts", locale, this.getClass().getClassLoader());
	}
	
	/**
	 * See {@link ResourceBundle#getString(String)}
	 */
	public String get(String key) {
		if(textBundle.containsKey(key)) {
			return textBundle.getString(key);
		}
		
		//TODO log missing property?
		//TODO fall back to default locale?
		return "<" + key + ">";
	}
	
	public boolean contains(String key) {
		return textBundle.containsKey(key);
	}
	
	/**
	 * Helper method for getting formatted texts using the {@link MessageFormat} format.
	 * 
	 * @param key key the key for the desired string
	 * @param arguments the arguments to put into the string
	 * @return the string array for the given key formatted with the arguments
	 */
	public String format(String key, Object... arguments) {
		if(textBundle.containsKey(key)) {
			return MessageFormat.format(get(key), arguments);
		}
		
		//TODO log missing property?
		//TODO fall back to default locale?
		return "<" + key + ": " + MyTools.join(arguments, ", ") + ">";
	}
}
