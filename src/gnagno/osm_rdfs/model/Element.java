package gnagno.osm_rdfs.model;

import java.util.HashMap;

public class Element {
	
	private long id;
	private ParsedInfo info;
	private HashMap<String,String> keyValuePairs;
	
	public Element(long id){
		this.id = id;
		initTagsMap();
	}
	
	public Element(long id, HashMap<String,String> tags){
		this.id = id;
		this.keyValuePairs = tags;
	}
	
	public void setId(long id){
		this.id = id;
	}
	
	public long getId(){
		return this.id;
	}
	
	private void initTagsMap(){
		this.keyValuePairs = new HashMap<String,String>();
	}
	
	public void addTag(String key, String value){
		this.keyValuePairs.put(key, value);
	}
	
	public String getValueAtKey(String key){
		return this.keyValuePairs.get(key);
	}
	
	public void setTagList(HashMap<String,String> tags){
		this.keyValuePairs = tags;
	}
	
	public HashMap<String,String> getTagList(){
		return this.keyValuePairs;
	}

	public ParsedInfo getInfo() {
		return info;
	}

	public void setInfo(ParsedInfo info) {
		this.info = info;
	}
	

}
