package gnagno.osm_rdfs.model;

import java.util.HashMap;

public class ParsedRelation extends Element{
	
	private HashMap<Long,Element> membersList;
	
	public ParsedRelation(long id){
		super(id);
		initMemberList();
	}

	public ParsedRelation(long id, HashMap<String, String> tags) {
		super(id, tags);
	}
	
	public void addMember(long memberId, Element member){
		this.membersList.put(memberId, member);
	}
	
	private void initMemberList(){
		this.membersList = new HashMap<Long,Element>();
	}
	
	public HashMap<Long,Element> getMemberList(){
		return this.membersList;
	}

}
