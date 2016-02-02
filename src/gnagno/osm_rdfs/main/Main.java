package gnagno.osm_rdfs.main;

import java.util.HashMap;

import org.openstreetmap.osmosis.core.domain.v0_6.Way;

import crosby.binary.Osmformat.Relation;
import gnagno.osm_rdfs.model.ParsedNode;
import gnagno.osm_rdfs.model.ParsedRelation;
import gnagno.osm_rdfs.model.ParsedWay;
import gnagno.osm_rdfs.pbf.PBFParser;
import gnagno.osm_rdfs.rdf.RDFBuilder;

public class Main {
	
	public static HashMap<Long, ParsedNode> nodes;
	public static HashMap<Long, ParsedWay> ways;
	public static HashMap<Long, ParsedRelation> relations;
	public static String[] strings;
	
	public static void main(String[] args){
		init();
		PBFParser p = new PBFParser(args[0]);
		p.parsePBF();
		strings = p.getTagStrings();
		System.out.println("[INFO] Nodes: " + nodes.size());
		System.out.println("[INFO] Ways: " + ways.size());
		System.out.println("[INFO] Relations: " + relations.size());
		RDFBuilder builder = new RDFBuilder();
		builder.createGraph();
		System.out.println("[INFO] Resources: " + builder.getResources().size());
		builder.closeModel();
	}
	
	public static void init(){
		initNodes();
		initWays();
		initRelations();
	}
	
	public static void initNodes(){
		nodes = new HashMap<Long, ParsedNode>();
	}
	
	public static void initWays(){
		ways = new HashMap<Long, ParsedWay>();
	}
	
	public static void initRelations(){
		relations = new HashMap<Long, ParsedRelation>();
	}
	
	public static String getStringById(int id){
		return strings[id];
	}

}
