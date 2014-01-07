package net.krazyweb.starmodmanager;

import java.util.HashSet;

public class Mod {
	
	private String internalName;
	private String displayName;
	private String version;
	private String gameVersion;
	private String author;
	private String description;
	private String url;
	private String fileName;
	private String checksum;
	
	private boolean hidden;
	private boolean installed;
	
	private HashSet<String> dependencies;
	
	public Mod(final String filepath) {
		
	}

	public String getInternalName() {
		return internalName;
	}

	public void setInternalName(String internalName) {
		this.internalName = internalName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getGameVersion() {
		return gameVersion;
	}

	public void setGameVersion(String gameVersion) {
		this.gameVersion = gameVersion;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getFile() {
		return fileName;
	}

	public void setFile(String file) {
		this.fileName = file;
	}

	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void hide() {
		this.hidden = true;
	}

	public boolean isInstalled() {
		return installed;
	}

	public void setInstalled(boolean installed) {
		this.installed = installed;
	}

	public HashSet<String> getDependencies() {
		return dependencies;
	}

	public void setDependencies(HashSet<String> dependencies) {
		this.dependencies = dependencies;
	}
	
}