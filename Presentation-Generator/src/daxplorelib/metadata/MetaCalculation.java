package daxplorelib.metadata;

import org.json.simple.JSONAware;
import org.json.simple.JSONValue;

public class MetaCalculation implements JSONAware{
	String column;
	
	public MetaCalculation(String column){
		this.column = column;
	}
	
	public String getColumn(){
		return column;
	}

	@Override
	public String toJSONString() {
		return '"' + column + '"';
	}
}
