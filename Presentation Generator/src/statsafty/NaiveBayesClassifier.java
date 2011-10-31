package statsafty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

	/**
	 * @param args
	 * @throws InvalidQueryException
	 * @throws UnsupportedOperationException
	 * @throws CapabilityDisabledException
	 * @throws JDOObjectNotFoundException
	 * @throws IOException
	 * @throws SQLException
	 * @throws ClassNotFoundException 
	 */
	public static void checkRow(ResultSet rs) throws IOException, SQLException, ClassNotFoundException {
		Class.forName("org.sqlite.JDBC");
		Connection connection;
		connection = DriverManager.getConnection("jdbc:sqlite:");
		Statement statement = connection.createStatement();
		statement.setQueryTimeout(30); // set timeout to 30 sec.
		// Restore the database from a backup file
		Statement stat = connection.createStatement();
		stat.executeUpdate("restore from statstore.db");
		
		Integer totalCorrect = 0, totalFailed = 0;
		while (rs.next()) {
			Map<String, Integer> selectors = new HashMap<String, Integer>();
			selectors.put("A1", rs.getInt("A1"));
			selectors.put("A3AGG", rs.getInt("A3"));
			selectors.put("A4", rs.getInt("A4"));
			selectors.put("A5", rs.getInt("A5"));
			selectors.put("A7AGG", rs.getInt("A7AGG"));
			// selectors.put("A8", 1);
			selectors.put("A12cAGG", rs.getInt("A12cAGG"));
			selectors.put("A12d", rs.getInt("A12d"));

			Map<String, Integer> predictions = new LinkedHashMap<String, Integer>();
			String[] questions = { "A16", "A9", "B17", "B18", "B21", "B22", "B24a", "B24b", "B24c", "B25b", "B25c", "B25d", "B26a", "B26b", "B28a", "B28c",
					"B28e", "B28f", "B28g", "B29a", "B29b", "B29d", "B30", "B31", "B32", "B38", "C41a", "C41b", "C41c", "C41d", "C41e", "C44a", "C44b", "C44c",
					"C44d", "C44e", "C44f", "C44g", "C45a", "C45b", "C46b", "C46h", "C46i", "C50a", "C50b", "C50c", "C50d", "C50f", "C50g", "C50h", "C50j",
					"C54a", "C55", "C56a", "C56b", "C56e", "C57a", "C57b", "C57c", "C58", "C62a", "C62b", "C62d", "D63", "D66a", "D66b", "D66c", "E72", "E73",
					"E75" };
			int nulls = 0;
			for (String question : questions) {
				Iterator<String> selIter = selectors.keySet().iterator();
				List<LinkedList<Double>> allres = new LinkedList<LinkedList<Double>>();
				LinkedList<Double> allArr = new LinkedList<Double>();
				while (selIter.hasNext()) {
					String s = selIter.next();
					//LinkedList<String> result = getStats(question, s, selectors.get(s));
					LinkedList<String> result = getStats(question.toUpperCase(), s.toUpperCase(), selectors.get(s), connection);
				
					if (result == null) {
						nulls++;
						continue;
					}
					
					/*
					for(String res : result)
						System.out.print(res+"\t");
					System.out.print("\n");
					for(String res : resultLocal)
						System.out.print(res+"\t");
					System.out.println("\n~~~");
						*/	
					JSONObject objAll = (JSONObject) JSONValue.parse(result.get(0));
					JSONArray arrAll = (JSONArray) objAll.get("d");
					JSONObject objThis = (JSONObject) JSONValue.parse(result.get(1));
					JSONArray arrThis = (JSONArray) objThis.get("d");

					LinkedList<Double> res = new LinkedList<Double>();
					allArr = new LinkedList<Double>();
					for (int i = 0; i < arrAll.size(); i++) {
						Number alln = (Number) arrAll.get(i);
						allArr.add(alln.doubleValue());
						Number thisn;
						if (i >= arrThis.size()) {
							thisn = 0;
						} else {
							thisn = (Number) arrThis.get(i);
						}
						res.add(thisn.doubleValue() / alln.doubleValue());
					}
					allres.add(res);

				}
				// allres.add(allArr);

				Double[] NBCValue = allArr.toArray(new Double[0]);
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
			System.out.println("Row " + rs.getRow() + ": " + correct + "/" + (correct + failed) + " (" + failed + " failed, " + nulls + " null)");
			totalCorrect += correct;
			totalFailed += failed;
		}
		System.out.println("Total: " + totalCorrect + "/" + (totalCorrect + totalFailed) + " (" + totalFailed + " failed)");
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

	public static LinkedList<String> getStats(String question, String selector, Integer alternative) {
		LinkedList<String> linkedlist = new LinkedList<String>();
		StringBuilder req = new StringBuilder("");
		req.append("q=" + question);
		req.append("&sel=" + selector);
		req.append("&alt=" + new Integer(alternative + 1).toString());
		req.append("&total=true");
		try {
			URL pifokus = new URL("http://127.0.0.1:8888/getStats?" + req.toString());
			URLConnection pifokusc = pifokus.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(pifokusc.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null)
				linkedlist.add(inputLine);
			in.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			return null;
		}
		return linkedlist;
	}
	
	public static LinkedList<String> getStats(String question, String selector, int alternative, Connection connection) throws SQLException{
		LinkedList<String> linkedlist = new LinkedList<String>();
		alternative++;
		PreparedStatement statement = connection.prepareStatement("SELECT value FROM statstore WHERE key=? OR key=?");
		String key1 = "Q="+question;
		statement.setString(1, key1);
		String key2 = selector+"="+alternative+"+Q="+question;
		statement.setString(2, key2);
		ResultSet result = statement.executeQuery();
		//System.out.println("SELECT value FROM statstore WHERE key='"+key1+"' OR key='"+key2+"'");
		int i = 0;
		while(result.next()){
			String value = result.getString("value");
			//System.out.println(i+": "+value);
			i++;
			linkedlist.add(value);
		}
		if(i!=2)return null;
		//System.out.println("---");
		return linkedlist;
	}
}
