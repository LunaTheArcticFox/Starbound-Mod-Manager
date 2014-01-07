package net.krazyweb.starmodmanager;

import java.io.File;
import java.util.HashSet;

import net.krazyweb.starmodmanager.helpers.FileHelper;

public class Mod {
	
	private String internalName;
	private String displayName;
	private String modVersion;
	private String gameVersion;
	private String author;
	private String description;
	private String url;
	private String fileName;
	private String checksum;
	
	private boolean hidden;
	private boolean installed;
	
	private HashSet<String> dependencies;
	private HashSet<String> modifiedFiles;
	private HashSet<String> ignoredFiles; //Files that are ignored when copying to the game's folders, including readmes.
	
	private Mod() {
	}
	
	public static Mod load(final File file) {
		
		if (!FileHelper.verify(file)) {
			//TODO Inform user of invalid filetype.
			return null;
		}
		
		/*
		 * TODO Extract the mod and repackage it to fit the mod manager's standards.
		 * This includes:
		 * 		- Removing nested directories
		 * 		- Repackaging non-zip archives as .zip archives.
		 * Do not delete the temporary files after this step.
		 */
		
		Mod mod = new Mod();
		
		/*
		 * TODO Take the temporary files from the above step and extract necessary info.
		 */
		
		
		
		Database.addMod(mod);
		
		/*
		 * TODO Once the data has been retrieved, delete the temporary files.
		 */
		
		return mod;
		
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

	public String getModVersion() {
		return modVersion;
	}

	public void setModVersion(String version) {
		this.modVersion = version;
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