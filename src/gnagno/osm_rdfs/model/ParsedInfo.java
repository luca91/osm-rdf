package gnagno.osm_rdfs.model;

public class ParsedInfo {
	
	private int version;
	private long timestamp;
	private long changeset;
	private double latitude;
	private double lognitude;
	
	public ParsedInfo(int version, long timestamp, long changeset, double latitude, double lognitude) {
		this.version = version;
		this.timestamp = timestamp;
		this.changeset = changeset;
		this.latitude = latitude;
		this.lognitude = lognitude;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public long getChangeset() {
		return changeset;
	}
	public void setChangeset(long changeset) {
		this.changeset = changeset;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLognitude() {
		return lognitude;
	}
	public void setLognitude(double lognitude) {
		this.lognitude = lognitude;
	}
	

}
