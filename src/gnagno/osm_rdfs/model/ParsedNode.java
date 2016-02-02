package gnagno.osm_rdfs.model;

import java.util.HashMap;

public class ParsedNode extends Element{
		
	public ParsedNode(long id, ParsedInfo info) {
		super(id);
	}
	public ParsedNode(long id, ParsedInfo info,
			HashMap<String, String> keyValuePairs) {
		super(id,keyValuePairs);
	}

}
