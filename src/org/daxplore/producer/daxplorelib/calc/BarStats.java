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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class BarStats {
	
	private LinkedHashMap<Integer, Frequencies> frequencyData = new LinkedHashMap<>();
	private LinkedHashMap<Integer, Means> meanData = new LinkedHashMap<>();
	private MetaQuestion question, perspective;
	private boolean useFrequency = false, useMean = false;
	
	private static class Frequencies {
		private List<int[]> frequencies;
		private int[] all;
		
		private Frequencies(int[][] barsData, int[] all) {
			frequencies = new LinkedList<>();
			for(int i = 0; i < barsData.length; i++) {
				frequencies.add(barsData[i]);
			}
			this.all = all;
		}
		
		public JsonElement toJSONObject() {
			JsonObject json = new JsonObject();
			for(int i = 0; i < frequencies.size(); i++) {
				JsonArray barsJSON = new JsonArray();
				for(int v : frequencies.get(i)) {
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
	
	private static class Means {
		private double[] mean;
		private Double allmean;
		private double[] counts;
		
		public Means(double[] mean, double allmean, double[] counts) {
			this.mean = mean;
			this.allmean = allmean;
			this.counts = counts;
		}
		
		public JsonElement toJSONObject() {
			JsonObject json = new JsonObject();
			JsonArray array = new JsonArray();
			for(Double d : mean) {
				array.add(new JsonPrimitive(d));
			}
			json.add("mean", array);
			json.add("all", new JsonPrimitive(allmean));
			
			array = new JsonArray();
			for(Double d : counts) {
				array.add(new JsonPrimitive(d));
			}
			json.add("count", array);
			
			return json;
		}
		
	}
	
	BarStats(MetaQuestion question, MetaQuestion perspective) {
		this.question = question;
		this.perspective = perspective;
	}
	
	void addFrequencyData(int timeindex, int[][] crosstabs, int[] frequencies) {
		frequencyData.put(timeindex, new Frequencies(crosstabs, frequencies));
		useFrequency = true;
	}

	void addMeanData(int timeindex, double[] means, double allmean, double[] counts) {
		meanData.put(timeindex, new Means(means, allmean, counts));
		useMean = true;
	}
	
	public JsonElement toJSONObject() {
		JsonObject json = new JsonObject();
		json.add("q", new JsonPrimitive(question.getColumn()));
		json.add("p", new JsonPrimitive(perspective.getColumn()));
		
		if(useFrequency) {
			JsonObject frequnecyValues = new JsonObject();
			for(Map.Entry<Integer, Frequencies> entry: frequencyData.entrySet()) {
				frequnecyValues.add(Integer.toString(entry.getKey()), entry.getValue().toJSONObject());
			}
			json.add("freq", frequnecyValues);
		}
		
		if(useMean) {
			JsonObject meanValues = new JsonObject();
			for(Map.Entry<Integer, Means> entry: meanData.entrySet()) {
				meanValues.add(Integer.toString(entry.getKey()), entry.getValue().toJSONObject());
			}
			json.add("mean", meanValues);
		}
		
		return json;
	}
}


/*
{
"q":"QUESTION",
"p":"PERSPECTIVE",
"freq": 
	{"0":
		{"0":[844,222,210,65,75],
		"1":[54,100,20,10,1],
		"2":[28,17,2,0,5],
		"3":[20,4,0,10,0],
		"4":[21,12,10,18,2],
		"all":[1111,380,250,93,90]
		}
	},
"mean":
	{"0":
		{
		"mean":[1.72,1.65,1.5555555555555556,1.1666666666666667,5.136],
		"all":1.9609665427509293,
		"count":[1111,380,250,93,90],
		}
	}
}
*/
