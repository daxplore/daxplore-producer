package statsafty;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
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
		statStore = createStatStoreMap();
	}
	
	public void checkRows(ResultSet resultSet) throws IOException, SQLException{
		Map<String, Integer> predictions = new LinkedHashMap<String, Integer>();
		int totalRows = 0;
		int totalCorrect = 0, totalFailed = 0, totalNulls = 0;
		int studyNumber = 2;
		String studyName = "d";
		while (resultSet.next()) {
			if(resultSet.getInt("STUDY")!=studyNumber){
				continue;
			}
			Map<String, Integer> selectors = new HashMap<String, Integer>();
			selectors.put("A1", resultSet.getInt("A1"));
			selectors.put("A3AGG", resultSet.getInt("A3"));
			selectors.put("A4", resultSet.getInt("A4"));
			selectors.put("A5", resultSet.getInt("A5"));
			selectors.put("A6", resultSet.getInt("A6"));
			selectors.put("A7AGG", resultSet.getInt("A7AGG"));
			// selectors.put("A8", 1);
			selectors.put("A12cAGG", resultSet.getInt("A12cAGG"));
			selectors.put("A12d", resultSet.getInt("A12d"));
			
			int nulls = 0;
			
			for (String question : questions) {
				List<LinkedList<Double>> allResults = new LinkedList<LinkedList<Double>>();
				
				/* Get the summed up content for the question (the total count)*/
				LinkedList<Double> questionTotalCount = new LinkedList<Double>();
				String questionTotalStat = getStat(question);
				JSONObject questionTotalJSONObject = (JSONObject)JSONValue.parse(questionTotalStat);
				if(!questionTotalJSONObject.containsKey(studyName)) {
					nulls++;
					continue;
				}
				JSONArray allJSONArray = (JSONArray)questionTotalJSONObject.get(studyName);
				for (int i = 0; i < allJSONArray.size(); i++) {
					Number number = (Number)allJSONArray.get(i);
					questionTotalCount.add(number.doubleValue());
				}
				
				/* Get the values for each question with the given selector for this individual */
				Iterator<String> selectorItererator = selectors.keySet().iterator();
				while (selectorItererator.hasNext()) {
					String selector = selectorItererator.next();
					
					String questionSelectorStat = getStat(question, selector, (selectors.get(selector)+1));
					if (questionSelectorStat == null) {
						nulls++;
						continue;
					}
					
					JSONObject questionSelectorJSONObject = (JSONObject)JSONValue.parse(questionSelectorStat);
					if(!questionSelectorJSONObject.containsKey(studyName)) {
						nulls++;
						continue;
					}
					JSONArray questionSelectorJSONArray = (JSONArray)questionSelectorJSONObject.get(studyName);
					
					LinkedList<Double> questionSelectorResult = new LinkedList<Double>();
					for (int i = 0; i < allJSONArray.size(); i++) {
						Number number;
						if (i >= questionSelectorJSONArray.size()) {
							number = 0;
						} else {
							number = (Number)questionSelectorJSONArray.get(i);
						}
						questionSelectorResult.add(number.doubleValue() / questionTotalCount.get(i));
					}
					allResults.add(questionSelectorResult);
				}

				Double[] maximumAPosterioriValues = questionTotalCount.toArray(new Double[0]);
				for (LinkedList<Double> result : allResults) {
					for (int i = 0; i < result.size(); i++) {
						maximumAPosterioriValues[i] *= result.get(i);
					}
				}
				predictions.put(question, (maxIndex(maximumAPosterioriValues) + 1));
			}
			Integer correct = 0, failed = 0;
			for (String question : questions) {
				int real = resultSet.getInt(question);
				if (!resultSet.wasNull()) {
					if (real == predictions.get(question)) {
						correct++;
					} else {
						failed++;
					}
				}
			}
			System.out.println(MessageFormat.format("{0,number,00%}:\t{1,number,00.0%} correct\t({2} tested, {3} correct, {4} failed, {5} nulls)",
					resultSet.getRow()/2346.0, (double)correct/(correct + failed), (correct+failed), correct, failed, nulls));
			totalRows++;
			totalCorrect += correct;
			totalFailed += failed;
			totalNulls += nulls;
		}
		System.out.println(MessageFormat.format("\nTotal:  {0,number,00.0%} correct\t({1} tested, {2} correct, {3} failed, {4} nulls)",
				(double)totalCorrect/(totalCorrect + totalFailed), (totalCorrect+totalFailed), totalCorrect, totalFailed, totalNulls));
		System.out.println(MessageFormat.format("Avg:    {0,number,00.0%} correct\t({1,number,#} tested, {2,number,#} correct, {3,number,#} failed, {4,number,#} nulls)",
				(double)totalCorrect/(totalCorrect + totalFailed), (double)(totalCorrect+totalFailed)/totalRows, (double)totalCorrect/totalRows, (double)totalFailed/totalRows, (double)totalNulls/totalRows));
		
		/* The percentage of the questions we expect to get right by chance */
		/* Counted as an average of the individual probabilities of getting a single question right */
		/* Is this the right thing to calculate? */
		double byChance = 0;
		int questionsUsed = 0;
		for(String question : questions){
			String allStat = getStat(question);
			JSONObject objAll = (JSONObject) JSONValue.parse(allStat);
			if(!objAll.containsKey(studyName)) {
				continue;
			}
			JSONArray arrAll = (JSONArray) objAll.get(studyName);
			byChance += 1.0/arrAll.size();
			questionsUsed++;
		}
		byChance /= questionsUsed;
		System.out.println(MessageFormat.format("Chance: {0,number,00.0%}", byChance));
	}

	public static int maxIndex(Double[] array) {
		Double max = array[0];
		int index = 0;
		for (int i = 1; i < array.length; i++) {
			if (array[i] > max) {
				index = i;
				max = array[i];
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
	
	public static Map<String, String> createStatStoreMap() throws IOException{
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
	
	
	public static void main(String[] args) throws ClassNotFoundException, IOException {
		Class.forName("org.sqlite.JDBC");
		Connection connection = null;
		try {
			connection = DriverManager.getConnection("jdbc:sqlite:rawdata.db");
			connection.setReadOnly(true);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);
			
			//Test with naive bayesian classifier:
			NaiveBayesClassifier classifier = new NaiveBayesClassifier();
			ResultSet rs = statement.executeQuery("select * from rawdata");
			classifier.checkRows(rs);
		} catch (SQLException e) {
			// if the error message is "out of memory",
			// it probably means no database file is found
			e.printStackTrace();
			System.err.println(e.getMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				// connection close failed.
				System.err.println(e);
			}
		}
	}
}
