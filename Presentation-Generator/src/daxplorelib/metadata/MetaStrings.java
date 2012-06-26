package daxplorelib.metadata;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class MetaStrings implements Iterable<TextReference>{
	TreeMap<TextReference, Map<Locale,String>> texts = new TreeMap<TextReference, Map<Locale, String>>();
	
	public MetaStrings(){
		
	}
	
	public String get(TextReference textRef, Locale locale) throws Exception{
		if(texts.containsKey(textRef)){
			Map<Locale, String> languages = texts.get(textRef);
			if(languages.containsKey(locale)){
				return languages.get(locale);
			} else {
				throw new Exception("Text for \"" + textRef + "\" does not exist in locale(" + locale.toString() + ")");
			}
		} else {
			throw new Exception("Texts does not exist for reference: " + textRef);
		}
	}
	
	public void put(TextReference textRef, Locale locale, String text){
		if(texts.containsKey(textRef)){
			texts.get(textRef).put(locale, text);
		} else {
			Map<Locale, String> tempmap = new HashMap<Locale, String>();
			tempmap.put(locale, text);
			texts.put(textRef, tempmap);
		}
	}

	@Override
	public Iterator<TextReference> iterator() {
		return texts.keySet().iterator();
	}
}
