package net.krazyweb.starmodmanager.data;

import java.nio.file.Path;

public class ModFile {
	
	private Path path;
	
	private boolean json;
	private boolean ignored;
	private boolean autoMerged; //The file uses the official "__merge" system.
	
	public Path getPath() {
		return path;
	}
	
	public void setPath(final Path path) {
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

	public boolean isModinfo() {
		return path.toString().endsWith(".modinfo");
	}

	public void setAutoMerged(boolean autoMerged) {
		this.autoMerged = autoMerged;
	}
	
}