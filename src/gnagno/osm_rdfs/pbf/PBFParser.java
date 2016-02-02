package gnagno.osm_rdfs.pbf;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;

import crosby.binary.Osmformat.DenseNodes;
import crosby.binary.Osmformat.HeaderBlock;
import crosby.binary.Osmformat.Info;
import crosby.binary.Osmformat.Node;
import crosby.binary.Osmformat.Relation;
import crosby.binary.BinaryParser;
import crosby.binary.Osmformat;
import crosby.binary.file.BlockInputStream;
import crosby.binary.file.BlockReaderAdapter;
import gnagno.osm_rdfs.main.Main;
import gnagno.osm_rdfs.model.ParsedInfo;
import gnagno.osm_rdfs.model.ParsedNode;
import gnagno.osm_rdfs.model.ParsedRelation;
import gnagno.osm_rdfs.model.ParsedWay;

public class PBFParser extends BinaryParser{
	
	private String file;
	
	public PBFParser(String file){
		this.file = file;
	}
	
	public void parsePBF(){
		InputStream input;
		try {
			input = new FileInputStream(file);
			BlockReaderAdapter brad = this;
			new BlockInputStream(input, brad).process();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	   /** Get the osmosis object representing a the user in a given Info protobuf.
     * @param info The info protobuf.
     * @return The OsmUser object */
    OsmUser getUser(Osmformat.Info info) {
        // System.out.println(info);
        if (info.hasUid() && info.hasUserSid()) {
            if (info.getUid() < 0) {
              return OsmUser.NONE;
            }
            return new OsmUser(info.getUid(), getStringById(info.getUserSid()));
        } else {
            return OsmUser.NONE;
        }
    }

    /** The magic number used to indicate no version number metadata for this entity. */
    static final int NOVERSION = -1;
    /** The magic number used to indicate no changeset metadata for this entity. */
    static final int NOCHANGESET = -1;
	

    @Override
    protected void parseRelations(List<Relation> rels) {
    	System.out.print("[INFO] Parsing relations...");
        if (!rels.isEmpty())
            for(Relation rel : rels){
            	ParsedRelation pr = new ParsedRelation(rel.getId());
            	pr.setTagList(getKeyValueMapped(rel.getKeysList(), rel.getValsList()));
            	for(Long l: rel.getMemidsList())
            		if(Main.nodes.get(l) != null)
            			pr.addMember(l, Main.nodes.get(l));
            	Main.relations.put(rel.getId(), pr);
            }
            System.out.println("Done.");
    }

	@Override
    protected void parseDense(DenseNodes nodes) {
    	System.out.print("[INFO] Parsing dense nodes...");
        long lastId=0;
        long lastLat=0;
        long lastLon=0;

        for (int i=0, tagsIndex = 0 ; i<nodes.getIdCount() ; i++, tagsIndex++) {
            lastId += nodes.getId(i);
            lastLat += nodes.getLat(i);
            lastLon += nodes.getLon(i);
            Osmformat.DenseInfo odi = nodes.getDenseinfo();
            ParsedInfo pi = new ParsedInfo(odi.getVersion(i), odi.getTimestamp(i), odi.getChangeset(i), parseLat(lastLat), parseLon(lastLon));
            ParsedNode pn;
            if(nodes.getKeysVals(tagsIndex) != 0){
            	HashMap<String, String> tags = getDenseKeyValueMapped(nodes.getKeysValsList(), tagsIndex); 
            	pn = new ParsedNode(lastId, pi, tags);
            	tagsIndex += (tags.size() * 2);
            }
            else
            	pn = new ParsedNode(lastId, pi);
            Main.nodes.put(lastId, pn);
        }
        System.out.println("Done.");
    }

    @Override
    protected void parseNodes(List<Node> nodes) {
    	System.out.print("[INFO] Parsing regular nodes...");
        for (Node n : nodes) {
            Info i = n.getInfo();
            ParsedInfo pi = new ParsedInfo(i.getVersion(), i.getTimestamp(), i.getChangeset(), parseLat(n.getLat()), parseLon(n.getLon()));
            ParsedNode pn = new ParsedNode(n.getId(), pi, getKeyValueMapped(n.getKeysList(), n.getValsList())); 
            Main.nodes.put(n.getId(), pn);
        }
        System.out.println("Done.");
    }

    @Override
    protected void parseWays(List<Osmformat.Way> ways) {
    	System.out.print("[INFO] Parsing ways...");
    	 for (Osmformat.Way i : ways) {
    		 ParsedWay pw = new ParsedWay(i.getId());
             for (int j = 0; j < i.getKeysCount(); j++) {
            	 String tag = getStringById(i.getKeys(j));
            	 pw.addTag(tag, getStringById(i.getVals(j)));
             long lastId = 0;
             for (long l : i.getRefsList()) {
            	 if(Main.nodes.get(l) != null)
            		 pw.addMember(l, Main.nodes.get(l));
                 lastId = l + lastId;
             }
             if (i.hasInfo()) {
                 Osmformat.Info info = i.getInfo();
                 pw.setInfo(parseInfo(info));
             }
             Main.ways.put(pw.getId(), pw);
             }
    	 }
    	 System.out.println("Done.");
    }

    @Override
    protected void parse(HeaderBlock header) {
        System.out.println("[INFO] Got header block.");
    }

    public void complete() {
        System.out.println("[INFO] Complete!");
    }  
    
    public String[] getTagStrings(){
		return super.strings;
	}
    
    private HashMap<String,String> getKeyValueMapped(List<Integer> keys, List<Integer> vals){
    	HashMap<String, String> result = new HashMap<String, String>();
    	for(int i = 0; i < keys.size(); i++)
    		result.put(getStringById(keys.get(i)), getStringById(vals.get(i)));
    	return result;
    }
    
    private HashMap<String,String> getDenseKeyValueMapped(List<Integer> keysVals, int startIndex){
    	HashMap<String, String> result = new HashMap<String, String>();
    	for(; keysVals.get(startIndex) != 0; startIndex += 2)
    		result.put(getStringById(keysVals.get(startIndex)), getStringById(keysVals.get(startIndex + 1)));
    	return result;
    }
    
    private ParsedInfo parseInfo(Osmformat.Info info){
    	ParsedInfo result = null;
		return result;
    }
    
    private ParsedInfo parseInfo(Osmformat.DenseInfo info){
    	ParsedInfo result = null;
		return result;
    }
}
