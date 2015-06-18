/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.daxplorelib.calc;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.daxplore.producer.daxplorelib.metadata.MetaQuestion;
import org.daxplore.producer.daxplorelib.metadata.MetaTimepointShort;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class BarStats {
	
	private LinkedHashMap<MetaTimepointShort, BarGroups> data = new LinkedHashMap<>();
	private MetaQuestion question, perspective;
	
	private static class BarGroups {
		private List<int[]> bars;
		private int[] all;
		
		private BarGroups(int[][] barsData, int[] all) {
			bars = new LinkedList<>();
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
	
	BarStats(MetaQuestion question, MetaQuestion perspective) {
		this.question = question;
		this.perspective = perspective;
	}
	
	void addTimePoint(MetaTimepointShort timepoint, int[][] crosstabs, int[] frequencies) {
		data.put(timepoint, new BarGroups(crosstabs, frequencies));
	}
	
	public JsonElement toJSONObject() {
		JsonObject json = new JsonObject();
		json.add("q", new JsonPrimitive(question.getColumn()));
		json.add("p", new JsonPrimitive(perspective.getColumn()));
		
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
