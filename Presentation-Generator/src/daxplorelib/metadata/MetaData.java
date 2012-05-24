package daxplorelib.metadata;

import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Locale;

import daxplorelib.DaxploreFile;

public class MetaData {
	
	DaxploreFile dax;
	
	List<MetaQuestion> questions;
	
	List<MetaGroup> groups;
	MetaGroup selectors;
	
	public enum Formats {
		DATABASE,RESOURCE,JSON,RAW
	}
	
	public MetaData(DaxploreFile daxplorefile){
		dax = daxplorefile;
	}
	
	public void importStructure(Reader r, Formats format){
		
	}
	
	public void importL10n(Reader r, Formats format, Locale locale) {
		
	}
	
	public void importConfig(Reader r, Formats format){
		
	}
	
	public void exportStructure(Writer w, Formats format){
		
	}
	
	public void exportL10n(Writer w, Formats format, Locale locale) {
		
	}
	
	public void exportConfig(Writer w, Formats format){
		
	}
	
	public void save(){
		
	}
}
