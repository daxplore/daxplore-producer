package StatSafty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * @author axwi4586
 * TODO: Not For open source release!!!!!
 *
 */

public class NaiveBayesClassifier {

	/**
	 * @param args
	 * @throws InvalidQueryException 
	 * @throws UnsupportedOperationException 
	 * @throws CapabilityDisabledException 
	 * @throws JDOObjectNotFoundException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws  IOException {
		Map<String, Integer> selectors = new HashMap<String,Integer>();
		selectors.put("A1", 4);
		selectors.put("A3", 1);
		selectors.put("A4", 2);
		selectors.put("A5", 2);
		selectors.put("A7AGG", 4);
		selectors.put("A8", 1);
		selectors.put("A12cAGG", 3);
		selectors.put("A12d", 2);
		
		
		String[] questions = {"A1","A12cAGG","A12d","A16","A3","A4","A5","A7AGG","A8","A9","B17","B18","B21","B22","B24a","B24b","B24c","B25b","B25c","B25d","B26a","B26b","B28a","B28c","B28e","B28f","B28g","B29a","B29b","B29d","B30","B31","B32","B38","C41a","C41b","C41c","C41d","C41e","C44a","C44b","C44c","C44d","C44e","C44f","C44g","C45a","C45b","C46b","C46h","C46i","C50a","C50b","C50c","C50d","C50f","C50g","C50h","C50j","C54a","C55","C56a","C56b","C56e","C57a","C57b","C57c","C58","C62a","C62b","C62d","D63","D66a","D66b","D66c","E72","E73","E75"};
		for(String question: questions){
			Iterator<String> selIter = selectors.keySet().iterator();
			List<LinkedList<Double>> allres = new LinkedList<LinkedList<Double>>();
			LinkedList<Double> allArr = new LinkedList<Double>();
			while (selIter.hasNext()){
				String s = selIter.next();
				/*Map<String, String[]> parameterMap = new HashMap<String, String[]>();
				String[] Q = {question};
				String[] S = {s};
				String[] A = {selectors.get(s).toString()};
				String[] total = {"true"};
				parameterMap.put("q", Q);
				parameterMap.put("s", S);
				parameterMap.put("a", A);
				parameterMap.put("total", total);
				LinkedList<String> result = GetStatsServlet.getStats(parameterMap);*/
				LinkedList<String> result = getStats(question, s, selectors.get(s));
				
				
				JSONObject objAll = (JSONObject)JSONValue.parse(result.get(0));
				JSONArray arrAll = (JSONArray)objAll.get("d");
				JSONObject objThis = (JSONObject)JSONValue.parse(result.get(1));
				JSONArray arrThis = (JSONArray)objThis.get("d");
				
				LinkedList<Double> res = new LinkedList<Double>();
				allArr = new LinkedList<Double>();
				for(int i = 0; i < arrAll.size(); i++){
					Number alln = (Number)arrAll.get(i);
					allArr.add(alln.doubleValue());
					Number thisn;
					if(i >= arrThis.size()){
						thisn = 0;
					} else {
						thisn = (Number)arrThis.get(i);
					}
					res.add(thisn.doubleValue()/alln.doubleValue());
				}
				allres.add(res);
				
			} 
			//allres.add(allArr);
			
			Double[] NBCValue = allArr.toArray(new Double[0]);
			for(LinkedList<Double> r : allres){
				for(int i = 0; i < r.size(); i++){
					NBCValue[i] *= r.get(i);
				}
			}
			System.out.println(question + " = " + (maxIndex(NBCValue) +1));
		}
	}

	public static int maxIndex(Double[] arr){
		Double max = arr[0];
		int index = 0;
		for(int i = 1; i < arr.length; i++){
			if(arr[i] > max) {
				index = i;
				max = arr[i];
			}
		}
		return index;
	}
	
	public static LinkedList<String> getStats(String q, String s, Integer alt) throws IOException{
		LinkedList<String> linkedlist = new LinkedList<String>();
		StringBuilder req = new StringBuilder("");
		req.append("q=" + q);
		req.append("&sel=" + s);
		req.append("&alt=" + alt.toString());
		req.append("&total=true");
		URL pifokus = new URL("http://127.0.0.1:8888/getStats?" + req.toString());
		URLConnection pifokusc = pifokus.openConnection();
		BufferedReader in = new BufferedReader(
				new InputStreamReader(
						pifokusc.getInputStream()));
		String inputLine;
		while ((inputLine = in.readLine()) != null) 
			linkedlist.add(inputLine);
		in.close();
		
		return linkedlist;
	}
}
