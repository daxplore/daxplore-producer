package daxplorelib.calc;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import daxplorelib.metadata.MetaTimepointShort;

import tools.MyTools;

public class BarStats {
	
	LinkedHashMap<MetaTimepointShort, BarGroups> data = new LinkedHashMap<MetaTimepointShort, BarGroups>();
	
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
		
		public String toJsonString() {
			List<String> barJson = new LinkedList<String>();
			for(int i = 0; i < bars.size(); i++) {
				barJson.add("\"" +i + "\":[" + MyTools.join(bars.get(i), ",")+"]");
			}
			barJson.add("\"all\":[" + MyTools.join(all, ",") + "]");
			return "{" + MyTools.join(barJson, ",") + "}";
		}
	}
	
	public BarStats() {
		
	}
	
	public void addTimePoint(MetaTimepointShort timepoint, int[][] crosstabs, int[] frequencies) {
		data.put(timepoint, new BarGroups(crosstabs, frequencies));
	}
	
	public String toJsonString() {
		List<String> tpJson = new LinkedList<String>();
		for(Map.Entry<MetaTimepointShort, BarGroups> entry: data.entrySet()) {
			tpJson.add("\"" + entry.getKey().getId() + "\":" + entry.getValue().toJsonString());
		}
		return "{" + MyTools.join(tpJson, ",") + "}";
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