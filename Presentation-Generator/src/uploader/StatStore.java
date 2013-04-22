package uploader;

import javax.jdo.PersistenceManager;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
public class StatStore {
	@PrimaryKey
	private String key;
	@Persistent
	private String json;

	public StatStore(String key, String json, PersistenceManager pm) {
		this.key = key;
		this.json = stripJSON(json);
		pm.makePersistent(this);
	}

	private String stripJSON(String json) {
		json = json.substring(1, json.length() - 1);
		json = json.replaceAll("\"\"", "\"");
		return json;
	}

	public String getKey() {
		return key;
	}

	public String getJSON() {
		return json;
	}
}
