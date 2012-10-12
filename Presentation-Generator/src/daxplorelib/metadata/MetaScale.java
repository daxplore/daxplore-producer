package daxplorelib.metadata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import daxplorelib.DaxploreTable;

import tools.MyTools;
import tools.Pair;

public class MetaScale {
	protected static final DaxploreTable table = 
			new DaxploreTable("CREATE TABLE metascale (id INTEGER, textref STRING, ord INTEGER, value REAL)", "metascale");

	public class MetaScaleManager {
		Map<Integer, MetaScale> scaleMap = new HashMap<Integer, MetaScale>();
		Connection connection;
		
		public MetaScaleManager(Connection connection) {
			this.connection = connection;
		}
		
		protected void init() {
			
		}
		
		public MetaScale getMetaScale(int id) {
			if(scaleMap.containsKey(id)) {
				return scaleMap.get(id);
			} else {
				//get stuff from db here and create a metascale
				return null;
			}
		}
		
		public MetaScale createMetaScale() {
			//create MetaScale here (look at MetaScale(List<Pair<TextReference,Double>>, Connection) for code
			return null;
		}
		
		public void remove(int id) {
			
		}
		
		public void saveAll() {
			for(MetaScale ms: scaleMap.values()) {
				if(ms.modified) {
					//Save here
					ms.modified = false;
				}
			}
			//save all unsaved MetaScales
		}
	}
	
	public class Option {
		TextReference textRef;
		int value;
		MetaScaleTransformation transformation;
		
		public Option(TextReference textRef, int value, MetaScaleTransformation transformation) {
			this.textRef = textRef; this.value = value; this.transformation = transformation;
		}
	}
	
	
	/** Each Option's position is defined by the order of this list */
	List<Option> options;
	Option ignore;

	int id;
	boolean modified = false;
	
	public MetaScale(int id, List<Option> options) {
		this.id = id;
		this.options = options;
	}
	
	public int getId() {
		return id;
	}
	
	public List<Option> getOptions() {
		return options;
	}

	public void setOptions(List<Option> options) {
		this.options = options;
		modified = true;
	}

	public Option getIgnoreOption() {
		return ignore;
	}
	
	public void setIgnoreOption(Option ignore) {
		this.ignore = ignore;
		modified = true;
	}
	
	/**
	 * 
	 * @param value
	 * @return -1 equals ignore
	 */
	public int transform(int value) {
		for(Option opt: options) {
			if(opt.transformation.contains(value)) {
				return opt.value;
			}
		}
		if(ignore.transformation.contains(value)) {
			return -1;
		}
		throw new Error(); //TODO
	}
	
}
