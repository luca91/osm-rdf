package gnagno.osm_rdfs.rdf;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import crosby.binary.Osmformat.Relation;
import gnagno.osm_rdfs.main.Main;
import gnagno.osm_rdfs.model.Element;
import gnagno.osm_rdfs.model.ParsedNode;
import gnagno.osm_rdfs.model.ParsedRelation;
import gnagno.osm_rdfs.model.ParsedWay;

public class RDFBuilder {
	
	public static final String RESOURCE_PREFIX = "http://osmtordfs.org/osm";
//	public static final String[] NODE_ADDR_TAG = {"addr:city", "addr:street","addr:housenumber"};
//	public static final String[] NODE_BUS_STOP_TAG = {"highway","name"};
	public static final String ONTOLOGY_SOURCE = "./ontology/osmtordfont.rdf";
	public static final String GRAPH_OUTPUT = "./output/osmtordf.rdf";
	public static final String[] PRIMARY_FEATURES = {"aerialway", "aeroway", "amenity","barrier","boundary","building","craft",
																								"emergency","geological","highway","historic","landuse","leisure","man_made",
																								"military","natural","office","places","power","public_transport","railway","route",
																								"shop","sport","tourism","waterway"};
	public static final String[] ADDRESS_FEATURES = {"addr:housenumber", "addr:street", "addr:postcode","addr:city","addr:country"};
	public static final String[] ADDRESS_PROPERTIES = {"addressHouseNr", "addressStreet", "addressPostcode", "addressCity", "addressCountry"};
	public static final String ELEMENT_CLASS_URI = RESOURCE_PREFIX + "#element";
	public static final String NODE_CLASS_URI = RESOURCE_PREFIX + "#node";
	public static final String WAY_CLASS_URI = RESOURCE_PREFIX + "#way";
	public static final String RELATION_CLASS_URI = RESOURCE_PREFIX + "#relation";
	public static final String NODE_URI = "/nodes/";
	public static final String WAY_URI = "/ways/";
	public static final String RELATION_URI = "/relations/";
	
	private Model model;
//	private HashMap<String,OntClass> classes;
	private HashMap<String,Property> primFeatures;
	private HashMap<String,Property> addrProps;
	private HashMap<String, Resource> resources;
	
	public RDFBuilder(){
		init();
	}
	
	public void createGraph(){
		createRelationsResources();
		createWaysResources();
		createNodesResources();
		try {
			model.write(new FileWriter(GRAPH_OUTPUT));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Model getModel() {
		return model;
	}

	public void setModel(OntModel model) {
		this.model = model;
	}
	
	private Resource createResource(Element e){
		if(e instanceof ParsedNode){
			Resource result = (Resource) model.createResource(getResourceURL(e, NODE_URI));
			result = addPrimFeatProperty(result, e);
			result = addAddressProperty(result, e);
			return result;
		}
		else if(e instanceof ParsedWay){
			Resource result = (Resource) model.createResource(getResourceURL(e, WAY_URI));
			result = addPrimFeatProperty(result, e);
			result = addAddressProperty(result, e);
			return result;
		}
		else if(e instanceof ParsedRelation){
			Resource result = (Resource) model.createResource(getResourceURL(e, RELATION_URI));
			return addPrimFeatProperty(result, e);
		}
		return null;
	}

//	private Resource createNodeResource(ParsedNode pn){
//		Resource result = (Resource) model.createResource(getResourceURL(pn, NODE_URI));
//		return addPrimFeatProperty(result, pn);
//	}
//	
//	private Resource createWayResource(ParsedWay w){
//		Resource result = (Resource) model.createResource(getResourceURL(w, WAY_URI));
//		return addPrimFeatProperty(result, w);
//	}
//	
//	private Resource createRelationResource(ParsedRelation pr){
//		Resource result = (Resource) model.createResource(getResourceURL(pr, RELATION_URI));
//		return addPrimFeatProperty(result, pr);
//	}
	
	private void createNodesResources(){
		System.out.print("[INFO] Creating node resources...");
		for(ParsedNode pn: Main.nodes.values()){
			String uri = getResourceURL(pn, NODE_URI);
			if(!hasModelResource(uri))
				saveResource(createResource(pn));
			else if(!hasResource(uri))
				saveResource(model.getResource(uri));
		}
		System.out.println("Done.");
	}
	
	private void createWaysResources(){
		System.out.print("[INFO] Creating way resources...");
		for(ParsedWay w: Main.ways.values()){
			String uri = getResourceURL(w, WAY_URI);
			if(!hasModelResource(uri)){
				saveResource(createResource(w));
				scanMembersList(w);
			}
			else if(!hasResource(uri))
				saveResource(model.getResource(uri));
		}
		System.out.println("Done.");
	}
	
	private void createRelationsResources(){
		System.out.print("[INFO] Creating relation resources...");
		for(ParsedRelation r: Main.relations.values()){
			String uri = getResourceURL(r, RELATION_URI);
			if(!hasModelResource(uri)){
				saveResource(createResource(r));
				scanMembersList(r);
			}
			else if(!hasResource(uri))
				saveResource(model.getResource(uri));
		}
		System.out.println("Done.");
	}
	
	private Resource addPrimFeatProperty(Resource r, Element e){
		String primFeat = getTagValueByKey(e.getTagList(), PRIMARY_FEATURES);
		if(primFeat.equals("highway") && !getTagValueByKey(e.getTagList(), "name").equals(""))
			r.addProperty(addrProps.get("addr:street"), e.getTagList().get("name"));
		if(primFeat != null)
			return r.addProperty(primFeatures.get(primFeat), e.getTagList().get(primFeat));
		return r;
	}
	
	private Resource addAddressProperty(Resource r, Element e){
		HashMap<String,String> tags = e.getTagList();
		if(!getTagValueByKey(tags, "addr:street").equals(""))
			for(String s: ADDRESS_FEATURES){
				if(tags.get(s) != null)
					r.addProperty(addrProps.get(s), tags.get(s));
			}
		return r;
	}
	
	private void saveResource(Resource r){
		resources.put(r.getURI(), r);
	}
	
	private void init(){
		initModel();
	}
	
	private void initModel(){
//		model = ModelFactory.createModel(OntModelSpec.OWL_MEM);
		model = ModelFactory.createDefaultModel();
		resources = new HashMap<String,Resource>();
		createPrimaryFeatureResources();
		createAddrPropResources();
//		classes = new HashMap<String,OntClass>();
//		OntClass element = model.createClass(ELEMENT_CLASS_URI);
//		OntClass node = model.createClass(NODE_CLASS_URI);
//		OntClass way = model.createClass(WAY_CLASS_URI);
//		OntClass relation = model.createClass(RELATION_CLASS_URI);
//		DatatypeProperty lat = model.createDatatypeProperty(RESOURCE_PREFIX + "#latitude");
//		lat.addDomain(node);
//		lat.addRange(model.createResource(XSDDatatype.XSDfloat.getURI()));
//		DatatypeProperty lon = model.createDatatypeProperty(RESOURCE_PREFIX + "#longitude");
//		lon.addDomain(node);
//		lon.addRange(getDataTypeResource(XSDDatatype.XSDfloat));
//		element.addSubClass(node.asResource());
//		element.addSubClass(way.asResource());
//		element.addSubClass(relation.asResource());
//		classes.put(ELEMENT_CLASS_URI, element);
//		classes.put(NODE_CLASS_URI, node);
//		classes.put(WAY_CLASS_URI, way);
//		classes.put(RELATION_CLASS_URI, relation);
//		try {
//			model.write(new FileWriter("osm-rdf.owl"));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		try {
			model.read(new InputStreamReader(new FileInputStream(ONTOLOGY_SOURCE), Charset.forName("UTF-8")), "RDF/XML");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void createPrimaryFeatureResources(){
		primFeatures = new HashMap<String,Property>();
		for(String s: PRIMARY_FEATURES){
			Resource r = model.createResource(RESOURCE_PREFIX + "#" + s);
			Property p = model.createProperty(r.getURI());
			primFeatures.put(s, p);
		}
	}
	
	private void createAddrPropResources(){
		addrProps = new HashMap<String,Property>();
		for(int i = 0; i < ADDRESS_PROPERTIES.length; i++){
			Resource r = model.createResource(RESOURCE_PREFIX + "#" + ADDRESS_PROPERTIES[i]);
			Property p = model.createProperty(r.getURI());
			addrProps.put(ADDRESS_FEATURES[i], p);
		}
	}
	
//	private Resource getDataTypeResource(XSDDatatype type){
//		return model.createResource(type.getURI());
//	}
	
//	private String buildURLWithTags(HashMap<String,String> map){
//		String result = "";
//		for(String s: map.keySet()){
//			String value = map.get(s); 
//			if(map.get(value) != null){
//				buildURLWithTags(map, result);
//			}
//			else{
//				result += s + "/";
//				if(map.get("name") != null)
//					result += map.get("name");
//				break;
//			}
//		}
//		return result;
//	}
	
//	private String buildURLWithTags(HashMap<String,String> map, String url){
//		for(String s: map.keySet()){
//			String value = map.get(s); 
//			if(map.get(value) != null){
//				map.remove(s);
//				buildURLWithTags(map, url);
//			}
//			else
//				url += s + "/";
//		}
//		return url;
//	}
	
	private String getResourceURL(Element e, String elementType){
		return RESOURCE_PREFIX + elementType + e.getId(); 
	}
	
//	private String getRelationResouceURL(ParsedRelation r){
//		HashMap<String,String> tagsMap = r.getTagList();
//		String url = RESOURCE_PREFIX +  buildURLWithTags(tagsMap);
//		return url;
//	}
//	
//	private String getWayResourceURL(ParsedWay w){
//		String url = RESOURCE_PREFIX ;
//		HashMap<String,String> tags = w.getTagList();
//		url += getTagValueByKey(tags, "highway", true);
//		url += getTagValueByKey(tags, "building", true);
//		return url;
//	}
	
//	private String getNodeResourceURL(ParsedNode pn){
//		String url = RESOURCE_PREFIX;
//		HashMap<String,String> tags = pn.getTagList();
//		if(tags != null){
//			String tag = tags.get("highway"); 
//			if(tag != null && tag.equals("bus_stop")){
//				url += getNodeURLFromTagsArray(NODE_BUS_STOP_TAG);
//				return url;
//			}
//			tag = tags.get("addr:city");
//			if (tag != null && tag.equals("yes")){
//				url += getNodeURLFromTagsArray(NODE_ADDR_TAG);
//				return url;
//			}
//		}
//		return url;
//	}
	
	private void scanMembersList(Element e){
		if(e instanceof ParsedWay){
			String url = getResourceURL(e, WAY_URI);
			Resource container = model.getResource(url);
			for(ParsedNode pn: ((ParsedWay) e).getMemberList().values()){
				Resource r = null;
				if(Main.nodes.get(pn.getId()) != null && !hasModelResource(getResourceURL(pn, NODE_URI))){
					r = createResource(Main.nodes.get(pn.getId()));
					r.addProperty(model.getProperty("partOf"), url);
					saveResource(r);
				}
				else if(!hasResource(url)){
					r = model.getResource(getResourceURL(pn, NODE_URI));
					saveResource(r);
				}
				container.addProperty(model.getProperty("composedOf"), r.getURI());
			}
		}
		else if(e instanceof ParsedRelation){
			for(Element mt: ((ParsedRelation) e).getMemberList().values()){
				createMemberTypeResource(mt);
			}
		}
	}
	
	private void createMemberTypeResource(Element mt){
		Resource r = null;
		String url = getResourceURL(mt, RELATION_URI);
		Resource container = model.getResource(url);
		if(!hasModelResource(url)){
			if(mt.getClass().equals(ParsedNode.class)){
				r = createResource(Main.nodes.get(mt.getId()));
				r.addProperty(model.getProperty("partOf"), url) ;
				saveResource(r);
			}
			else if(mt.getClass().equals(ParsedWay.class)){
				r = createResource(Main.ways.get(mt.getId()));
				r.addProperty(model.getProperty("partOf"), url);
				saveResource(r);
			}
			else if(mt.getClass().equals(Relation.class)){
				r = createResource(Main.relations.get(mt.getId()));
				r.addProperty(model.getProperty("partOf"), url);
				saveResource(r);
			}
		}
		else if(!hasModelResource(url)){
			r = model.getResource(url);
			saveResource(r);
		}
		container.addProperty(model.getProperty("composedOf"), r);
	}
	
	private boolean hasResource(String uri){
		if(resources.get(uri) != null)
			return true;
		return false;
	}
	
	private boolean hasModelResource(String uri){
		if(model.getResource(uri) != null){
			return true;
		}
		return false;
	}
	
	private String getTagValueByKey(HashMap<String,String> tags, String[] key){
		for(String s: key){
			if(tags.get(s) != null)
					return tags.get(s);
		}
		return "";
	}
	
	private String getTagValueByKey(HashMap<String,String> tags, String key){
		for(String s: tags.keySet()){
			if(s.equals(key))
					return tags.get(s);
		}
		return "";
	}
	
//	private String getNodeURLFromTagsArray(String[] tags){
//		String url = "";
//		 for(String s: tags)
//			 url += s + "/";
//		return url;
//	}
//	
//	private String getPrimaryFeature(HashMap<String,String> elementTags){
//		for(String s: PRIMARY_FEATURES)
//			if(elementTags.get(s) != null)
//				return s;
//		return "";
//	}

	public HashMap<String, Resource> getResources() {
		return resources;
	}

	public void setResources(HashMap<String, Resource> resources) {
		this.resources = resources;
	}
	
	public void closeModel(){
		if(model != null)
			model.close();
	}
	
	
}
