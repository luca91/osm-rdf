package gnagno.osm_rdfs.pbf;

import java.util.List;

import crosby.binary.Osmformat;

public class PBFBlock {

	private List<Osmformat.Way> ways;
	private String[] table;
	
	public PBFBlock(List<Osmformat.Way> ways, String[] table) {
		super();
		this.ways = ways;
		this.table = table;
	}

	public List<Osmformat.Way> getWays() {
		return ways;
	}

	public void setWays(List<Osmformat.Way> ways) {
		this.ways = ways;
	}

	public String[] getTable() {
		return table;
	}

	public void setTable(String[] table) {
		this.table = table;
	}
	
	
}
