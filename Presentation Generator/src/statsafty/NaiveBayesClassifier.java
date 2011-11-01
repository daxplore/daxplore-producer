package statsafty;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
	
	final Map<String, String> statStore;
	
	final String[] questions = { "A16", "A9", "B17", "B18", "B21", "B22", "B24a", "B24b", "B24c", "B25b", "B25c", "B25d", "B26a", "B26b", "B28a", "B28c",
			"B28e", "B28f", "B28g", "B29a", "B29b", "B29d", "B30", "B31", "B32", "B38", "C41a", "C41b", "C41c", "C41d", "C41e", "C44a", "C44b", "C44c",
			"C44d", "C44e", "C44f", "C44g", "C45a", "C45b", "C46b", "C46h", "C46i", "C50a", "C50b", "C50c", "C50d", "C50f", "C50g", "C50h", "C50j",
			"C54a", "C55", "C56a", "C56b", "C56e", "C57a", "C57b", "C57c", "C58", "C62a", "C62b", "C62d", "D63", "D66a", "D66b", "D66c", "E72", "E73",
			"E75" };
	
	public NaiveBayesClassifier() throws IOException{
		statStore = statStoreMap();
	}
	
	public void checkRows(ResultSet rs) throws IOException, SQLException{
		Map<String, Integer> predictions = new LinkedHashMap<String, Integer>();
		int totalCorrect = 0, totalFailed = 0, totalNulls = 0, totalRows = 0;
		while (rs.next()) {
			if(rs.getInt("STUDY")==1){
				continue;
			}
			totalRows++;
			Map<String, Integer> selectors = new HashMap<String, Integer>();
			selectors.put("A1", rs.getInt("A1"));
			selectors.put("A3AGG", rs.getInt("A3"));
			selectors.put("A4", rs.getInt("A4"));
			selectors.put("A5", rs.getInt("A5"));
			selectors.put("A6", rs.getInt("A6"));
			selectors.put("A7AGG", rs.getInt("A7AGG"));
			// selectors.put("A8", 1);
			selectors.put("A12cAGG", rs.getInt("A12cAGG"));
			selectors.put("A12d", rs.getInt("A12d"));
			
			int nulls = 0;
			for (String question : questions) {
				
				List<LinkedList<Double>> allres = new LinkedList<LinkedList<Double>>();
				
				LinkedList<Double> questionTotalCount = new LinkedList<Double>();
				String allStat = getStat(question);
				JSONObject objAll = (JSONObject) JSONValue.parse(allStat);
				JSONArray arrAll = (JSONArray) objAll.get("d");
				for (int i = 0; i < arrAll.size(); i++) {
					Number alln = (Number) arrAll.get(i);
					questionTotalCount.add(alln.doubleValue());
				}
				
				Iterator<String> selIter = selectors.keySet().iterator();
				while (selIter.hasNext()) {
					String selector = selIter.next();
					
					String thisStat = getStat(question, selector, (selectors.get(selector)+1));
					if (thisStat == null) {
						nulls++;
						continue;
					}
					
					JSONObject objThis = (JSONObject) JSONValue.parse(thisStat);
					if(!objThis.containsKey("d")) {
						nulls++;
						continue;
					}
					JSONArray arrThis = (JSONArray) objThis.get("d");
					
					LinkedList<Double> res = new LinkedList<Double>();
					for (int i = 0; i < arrAll.size(); i++) {
						Number thisn;
						if (i >= arrThis.size()) {
							thisn = 0;
						} else {
							thisn = (Number) arrThis.get(i);
						}
						res.add(thisn.doubleValue() / questionTotalCount.get(i));
					}
					allres.add(res);

				}
				// allres.add(allArr);

				Double[] NBCValue = questionTotalCount.toArray(new Double[0]);
				for (LinkedList<Double> r : allres) {
					for (int i = 0; i < r.size(); i++) {
						NBCValue[i] *= r.get(i);
					}
				}
				predictions.put(question, (maxIndex(NBCValue) + 1));
			}
			Integer correct = 0, failed = 0;
			for (String question : questions) {
				int real = rs.getInt(question);
				if (!rs.wasNull()) {
					if (real == predictions.get(question)) {
						correct++;
					} else {
						failed++;
					}
				}
			}
			System.out.println((int)(0.5+100.0*rs.getRow()/2346.0) + "%: EnkatID " + rs.getString("EnkatID") + ":\t" + (int)(0.5+100.0*correct/(correct + failed)) + "% = " + correct + "/" + (correct + failed) + " (" + failed + " failed, " + nulls + " null)");
			totalCorrect += correct;
			totalFailed += failed;
			totalNulls += nulls;
		}
		System.out.println("Total: " + (int)(0.5+100.0*totalCorrect/(totalCorrect + totalFailed)) + "% = " + totalCorrect + "/" + (totalCorrect + totalFailed) + " (" + totalFailed + " failed, " + totalNulls + " null)");
	}

	public static int maxIndex(Double[] arr) {
		Double max = arr[0];
		int index = 0;
		for (int i = 1; i < arr.length; i++) {
			if (arr[i] > max) {
				index = i;
				max = arr[i];
			}
		}
		return index;
	}
	
	public String getStat(String question){
		return statStore.get("Q="+question.toUpperCase());
	}
	
	public String getStat(String question, String selector, int alternative){
		return statStore.get(selector.toUpperCase()+"="+alternative+"+Q="+question.toUpperCase());
	}
	
	public static Map<String, String> statStoreMap() throws IOException{
		Map<String, String> statStoreMap = new HashMap<String, String>();
    	File file = new File("src/statsafty/statstoredata.js");
    	BufferedReader br = new BufferedReader(new FileReader(file));

    	String line;
        try {
        	String[] tokens;
        	line = br.readLine();
        	if(line.equals("key,json")){
				while ((line = br.readLine()) != null) {
					tokens = line.split(",", 2);
					if(tokens.length >1){
						String key = tokens[0];
						String value = tokens[1].substring(1, tokens[1].length()-1).replace("\"\"", "\"");
						statStoreMap.put(key, value);
					}
				}
        	} else {
        		throw new IOException("Invalid statstore csv file");
        	}
        } finally {
        	br.close();
        }
        return statStoreMap;
    }
}
