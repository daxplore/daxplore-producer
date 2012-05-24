package daxplorelib.metadata;

import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

/*
 * questionobject
{
	id: string
	data: <dataobject>,
	longtext: <stringreference>,
	shorttext: <stringreference>,
	options: [<stringreference>],
}
 */

public class MetaQuestion implements JSONAware, Comparable<MetaQuestion>{
	String id;
	String fullTextRef, shortTextRef;
	MetaCalculation calculation;
	
	List<StringReference> options;
	
	public MetaQuestion(String id, String fullTextRef, String shortTextRef, MetaCalculation calculation, List<StringReference> options){
		this.id = id;
		this.fullTextRef = fullTextRef;
		this.shortTextRef = shortTextRef;
		this.calculation = calculation;
		this.options = options;
	}
	
	public MetaQuestion(JSONObject obj){
		id = (String)obj.get("id");
		fullTextRef = (String)obj.get("fulltext");
		shortTextRef = (String)obj.get("shorttext");
		calculation = new MetaCalculation((String)obj.get("data"));
		
	}
	
	public String getId(){
		return id;
	}
	
	public String getFullTextRef(){
		return fullTextRef;
	}
	
	public String getShortTextRef(){
		return shortTextRef;
	}
	
	public List<StringReference> getOptionsRefs(){
		return options;
	}

	@Override
	public int compareTo(MetaQuestion arg0) {
		return id.compareTo(arg0.id);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public String toJSONString() {
		JSONObject obj = new JSONObject();
		obj.put("id", id);
		obj.put("fulltext", fullTextRef);
		obj.put("shorttext", shortTextRef);

		JSONArray opts = new JSONArray();
		opts.addAll(options);
		
		obj.put("options", opts);
		
		return obj.toJSONString();
	}
}
