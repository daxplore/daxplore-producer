package uploader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;



public class JsonTest {
	
	public static void main(String[] args) throws IOException, ParseException {
		System.out.println(getDefinitions("A1", "B17"));
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static String getDefinitions(String question, String selector) throws IOException, ParseException{
		JSONArray definitions = new JSONArray();
		ContainerFactory containerFactory = new ContainerFactory() {
			@Override
			public Map createObjectContainer() {
				return new LinkedHashMap();
			}
			@Override
			public List creatArrayContainer() {
				return new LinkedList();
			}
		};
		
		File jsonFile = new File("src/static/questions_en.json");
		BufferedReader br = new BufferedReader(new FileReader(jsonFile));
		JSONParser parser = new JSONParser();
		List<Map> questionList = (List<Map>)parser.parse(br, containerFactory);
		
		for(Map map: questionList){
			Object o = map.get("column");
			String column = (String)o;
			if(column.equals(question) || column.equals(selector)){
				definitions.add(map);
			}
		}
		
		return definitions.toJSONString();
	}
}
