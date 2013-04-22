package daxplorelib.calc;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import daxplorelib.metadata.MetaQuestion;
import daxplorelib.metadata.MetaTimepointShort;

public class BarStats {
	
	LinkedHashMap<MetaTimepointShort, BarGroups> data = new LinkedHashMap<MetaTimepointShort, BarGroups>();
	private MetaQuestion question, perspective;
	
	public static class BarGroups {
		List<int[]> bars;
		int[] all;
		
		public BarGroups(int[][] barsData, int[] all) {
			bars = new LinkedList<int[]>();
			for(int i = 0; i < barsData.length; i++) {
				bars.add(barsData[i]);
			}
			this.all = all;
		}
		
		@SuppressWarnings("unchecked")
		public JSONAware toJSONObject() {
			JSONObject json = new JSONObject();
			for(int i = 0; i < bars.size(); i++) {
				JSONArray barsJSON = new JSONArray();
				for(int v : bars.get(i)) {
					barsJSON.add(v);
				}
				json.put(i, barsJSON);
			}
			
			JSONArray allJSON = new JSONArray();
			for(int v : all) {
				allJSON.add(v);
			}
			json.put("all", allJSON);
			
			return json;
		}
	}
	
	public BarStats(MetaQuestion question, MetaQuestion perspective) {
		this.question = question;
		this.perspective = perspective;
	}
	
	public void addTimePoint(MetaTimepointShort timepoint, int[][] crosstabs, int[] frequencies) {
		data.put(timepoint, new BarGroups(crosstabs, frequencies));
	}
	
	@SuppressWarnings("unchecked")
	public JSONAware toJSONObject() {
		JSONObject json = new JSONObject();
		json.put("q", question.getId());
		json.put("p", perspective.getId());
		
		JSONObject values = new JSONObject();
		for(Map.Entry<MetaTimepointShort, BarGroups> entry: data.entrySet()) {
			values.put(entry.getKey().getTimeindex(), entry.getValue().toJSONObject());
		}
		json.put("values", values);
		
		return json;
	}

}


/*
{
	tp1 : {
		0 : [2,4,5,7],
		1 : [2,2,3,2],
		all: [4,6,8,9];
	},
	tp2 : {
	};
}

*/