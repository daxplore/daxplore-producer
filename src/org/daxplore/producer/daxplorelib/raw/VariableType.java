/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.daxplorelib.raw;

/**
 * Different variable types and how they map between SPSS, sqlite and java
 * @author Axel Winkler
 */
public enum VariableType {
	NUMERIC ("real", Double.class), 
	TEXT ("text", String.class); 
	//MAPPED ("real", Double.class); //TODO: remove
	
	private final String sqltype;
	private final Class<?> javatype;
	
	VariableType(String sqltype, Class<?> javatype){
		this.sqltype = sqltype;
		this.javatype = javatype;
	}
	
	public static VariableType fromSqltype(String type) {
		for(VariableType t: VariableType.values()) {
			if(t.sqltype.equalsIgnoreCase(type)) {
				return t;
			}
		}
		return null;
	}
	
	public String sqltype(){ return sqltype; }
	
	public Class<?> javatype() {return javatype;}
}
