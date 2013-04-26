package daxplorelib.calc;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

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
		
		public JsonElement toJSONObject() {
			JsonObject json = new JsonObject();
			for(int i = 0; i < bars.size(); i++) {
				JsonArray barsJSON = new JsonArray();
				for(int v : bars.get(i)) {
					barsJSON.add(new JsonPrimitive(v));
				}
				json.add(Integer.toString(i), barsJSON);
			}
			
			JsonArray allJSON = new JsonArray();
			for(int v : all) {
				allJSON.add(new JsonPrimitive(v));
			}
			json.add("all", allJSON);
			
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
	
	public JsonElement toJSONObject() {
		JsonObject json = new JsonObject();
		json.add("q", new JsonPrimitive(question.getId()));
		json.add("p", new JsonPrimitive(perspective.getId()));
		
		JsonObject values = new JsonObject();
		for(Map.Entry<MetaTimepointShort, BarGroups> entry: data.entrySet()) {
			values.add(Integer.toString(entry.getKey().getTimeindex()), entry.getValue().toJSONObject());
		}
		json.add("values", values);
		
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