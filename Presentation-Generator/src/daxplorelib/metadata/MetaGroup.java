package daxplorelib.metadata;

import java.util.List;

import org.json.simple.JSONAware;

public class MetaGroup implements JSONAware{
	
	StringReference text;
	List<MetaQuestion> questions;
	int index;
	
	public MetaGroup() {
		
	}

	@Override
	public String toJSONString() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
}
