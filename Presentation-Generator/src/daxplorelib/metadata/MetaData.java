package daxplorelib.metadata;

import java.io.Reader;
import java.io.Writer;

import daxplorelib.DaxploreFile;
import daxplorelib.fileformat.ImportedData;

public class MetaData {
	
	public enum Formats {
		DATABASE,RESOURCE,JSON,RAW
	}
	
	public MetaData(DaxploreFile daxplorefile){
		
	}
	
	public MetaData(ImportedData rawmeta, DaxploreFile daxplorefile){
		
	}
	
	public MetaData(Reader r, Formats format, DaxploreFile daxplorefile){
		
	}
	
	public void export(Writer w, Formats format){
		
	}
	
	public void save(){
		
	}
}
