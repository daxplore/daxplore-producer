package daxplorelib.metadata;

import java.util.Locale;

import org.json.simple.JSONAware;

public class TextReference implements JSONAware, Comparable<TextReference> {
	String reference;
	
	public TextReference(String reference){
		this.reference = reference;
	}
	
	public String getRef() {
		return reference;
	}
	
	public String get(Locale locale){
		return null;
	}
	
	public String put(Locale locale){
		return null;
	}

	@Override
	public String toJSONString() {
		return '"'+ reference + '"';
	}

	@Override
	public int compareTo(TextReference o) {
		return reference.compareTo(o.reference);
	}
}
