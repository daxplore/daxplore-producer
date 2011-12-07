package daxplorelib.fileformat;

/**
 * Different variable types and how they map between SPSS, sqlite and java
 * @author Axel Winkler
 */
public enum VariableType {
	NUMERIC ("real", Double.class), 
	TEXT ("text", String.class), 
	MAPPED ("real", Double.class);
	
	private final String sqltype;
	private final Class<?> javatype;
	
	VariableType(String sqltype, Class<?> javatype){
		this.sqltype = sqltype;
		this.javatype = javatype;
	}
	public String sqltype(){ return sqltype; };
	
	public Class<?> javatype() {return javatype;}
}
