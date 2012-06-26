package daxplorelib.metadata;

import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

public class MetaGroup implements JSONAware{
	
	protected TextReference textRef;
	protected List<MetaQuestion> questions;
	protected int index;
	
	public MetaGroup() {
		
	}
	
	public TextReference getTextRef() {
		return textRef;
	}

	public void setTextRef(TextReference textRef) {
		this.textRef = textRef;
	}

	public List<MetaQuestion> getQuestions() {
		return questions;
	}

	public void setQuestions(List<MetaQuestion> questions) {
		this.questions = questions;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String toJSONString() {
		JSONObject obj = new JSONObject();
		obj.put("textref", textRef);
		obj.put("index", index);
		JSONArray questions = new JSONArray();
		for(MetaQuestion q : this.questions){
			questions.add(q.getId());
		}
		obj.put("questions", questions);
		return obj.toJSONString();
	}
	
	
	
}
