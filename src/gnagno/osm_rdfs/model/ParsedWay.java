package gnagno.osm_rdfs.model;

import java.util.HashMap;

public class ParsedWay extends Element{
	
	public static final int MAX_MEMBERS = 2000;
	private HashMap<Long, ParsedNode> membersList;

	public ParsedWay(long id) {
		super(id);
		initMemberList();
	}
	
	public ParsedWay(long id, HashMap<String,String> tags){
		super(id, tags);
	}
	
	public void addMember(long memberId, ParsedNode member){
		this.membersList.put(memberId, member);
	}
	
	private void initMemberList(){
		this.membersList = new HashMap<Long,ParsedNode>();
	}
	
	public HashMap<Long,ParsedNode> getMemberList(){
		return this.membersList;
	}

}
