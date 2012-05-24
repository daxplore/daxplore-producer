package daxplorelib.metadata;

import java.util.Locale;

import org.json.simple.JSONAware;

public class StringReference implements JSONAware {
	String reference;
	
	public StringReference(String reference){
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
}
