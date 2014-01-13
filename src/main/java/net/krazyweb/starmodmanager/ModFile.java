package main.java.net.krazyweb.starmodmanager;

public class ModFile {
	
	private String path;
	
	private boolean json;
	private boolean ignored;
	private boolean autoMerged; //The file uses the official "__merge" system.
	
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}

	public boolean isJson() {
		return json;
	}

	public void setJson(boolean json) {
		this.json = json;
	}

	public boolean isIgnored() {
		return ignored;
	}

	public void setIgnored(boolean ignored) {
		this.ignored = ignored;
	}

	public boolean isAutoMerged() {
		return autoMerged;
	}

	public void setAutoMerged(boolean autoMerged) {
		this.autoMerged = autoMerged;
	}
	
}