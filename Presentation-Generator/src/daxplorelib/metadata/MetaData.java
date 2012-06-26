package daxplorelib.metadata;

import java.io.Reader;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Locale;

import daxplorelib.SQLTools;

public class MetaData {
	
	Connection database;
	
	List<MetaQuestion> questions;
	
	List<MetaGroup> groups;
	MetaGroup selectors;
	
	public enum Formats {
		DATABASE,RESOURCE,JSON,RAW
	}
	
	public MetaData(Connection database) throws SQLException{
		this.database = database;
		if(SQLTools.tableExists("metadata", database)){
			Statement stmt = database.createStatement();
			stmt.executeUpdate("CREATE TABLE metadata ()");
			stmt.executeUpdate("CRATE TABLE texts (ref TEXT, locale TEXT, text TEXT)");
		} else {
			
		}

		
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
