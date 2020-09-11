/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.daxplorelib.calc;

import java.math.BigDecimal;
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
	private MetaQuestion question;
	private List<MetaQuestion> perspectives;
	private boolean useFrequency = false, useMean = false;
	
	private static class Frequencies {
		List<MetaQuestion> perspectives;
		private List<int[]> frequencies;
		private int[] all;
		
		private Frequencies(List<MetaQuestion> perspectives, int[][] barsData, int[] all) {
			this.perspectives = perspectives;
			frequencies = new LinkedList<>();
			for(int i = 0; i < barsData.length; i++) {
				frequencies.add(barsData[i]);
			}
			this.all = all;
		}
		
		public JsonElement toJSONObject() {
			JsonObject json = new JsonObject();
			if (perspectives.size() == 1) {
				for(int i = 0; i < frequencies.size(); i++) {
					JsonArray barsJSON = new JsonArray();
					for(int v : frequencies.get(i)) {
						barsJSON.add(new JsonPrimitive(v));
					}
					json.add(Integer.toString(i), barsJSON);
				}
			} else {
				int p1OptionCount = perspectives.get(0).getScale().getOptionCount();
				int p2OptionCount = perspectives.get(1).getScale().getOptionCount();
				for (int p1 = 0; p1 < p1OptionCount; p1++) {
					for (int p2 = 0; p2 < p2OptionCount; p2++) {
						int combinedPerspective = p1 * p2OptionCount + p2;
						JsonArray barsJSON = new JsonArray();
						for(int v : frequencies.get(combinedPerspective)) {
							barsJSON.add(new JsonPrimitive(v));
						}
						json.add(p1 + "," + p2, barsJSON);
					}
				}
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
		private int[] counts;
		private int allcount;
		
		public Means(double[] mean, double allmean, int[] counts, int allcount) {
			this.mean = mean;
			this.allmean = allmean;
			this.counts = counts;
			this.allcount = allcount;
		}
		
		public JsonElement toJSONObject() {
			int decimalPlaces = 2; //TODO turn into producer setting, also used in MetaQuestion
			JsonObject json = new JsonObject();
			JsonArray array = new JsonArray();
			for(Double d : mean) {
				array.add(new JsonPrimitive((new BigDecimal(d)).setScale(decimalPlaces, BigDecimal.ROUND_HALF_UP)));
			}
			json.add("mean", array);
			json.add("all", new JsonPrimitive((new BigDecimal(allmean)).setScale(decimalPlaces, BigDecimal.ROUND_HALF_UP)));
			
			array = new JsonArray();
			for(int d : counts) {
				array.add(new JsonPrimitive(d));
			}
			json.add("count", array);
			json.add("allcount", new JsonPrimitive(allcount));
			
			return json;
		}
		
	}
	
	BarStats(MetaQuestion question, List<MetaQuestion> perspectives) {
		this.question = question;
		this.perspectives = perspectives;
	}
	
	void addFrequencyData(int timeindex, int[][] crosstabs, int[] frequencies) {
		frequencyData.put(timeindex, new Frequencies(perspectives, crosstabs, frequencies));
		useFrequency = true;
	}

	void addMeanData(int timeindex, double[] means, double allmean, int[] counts, int allcount) {
		meanData.put(timeindex, new Means(means, allmean, counts, allcount));
		useMean = true;
	}
	
	public JsonElement toJSONObject() {
		JsonObject json = new JsonObject();
		json.add("q", new JsonPrimitive(question.getColumn()));
		JsonArray perspectiveJson = new JsonArray();
		for (MetaQuestion p : perspectives) {
			perspectiveJson.add(new JsonPrimitive(p.getColumn()));
		}
		json.add("p", perspectiveJson);
		
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
		"mean":[1.72, 1.65, 1.56, 1.17, 5.14],
		"all":1.96,
		"count":[1111,380,250,93,90],
		"allcount":2331
		}
	}
}
*/
