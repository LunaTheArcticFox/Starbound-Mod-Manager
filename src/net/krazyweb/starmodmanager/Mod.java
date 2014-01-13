package net.krazyweb.starmodmanager;

import java.io.File;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashSet;

import net.krazyweb.starmodmanager.helpers.Archive;

public class Mod {
	
	private String internalName;
	private String displayName;
	private String modVersion;
	private String gameVersion;
	private String author;
	private String description;
	private String url;
	private String archiveName;
	
	private long checksum;
	
	private int order;
	
	private boolean hidden;
	private boolean installed;
	
	private HashSet<String> dependencies;
	private HashSet<ModFile> files; //All files that the mod alters
	
	public static class ModOrderComparator implements Comparator<Mod> {

		@Override
		public int compare(Mod mod1, Mod mod2) {
			return mod1.order - mod2.order;
		}
		
	}
	
	public Mod() {
	}
	
	public static Mod load(final File file) {
		
		/* 
		 * As long as the mod archive object is in memory, so are its entire contents.
		 * This is advantageous, as the files must only be read from the disk once,
		 * as opposed to copied from disk, to disk, read, then deleted, then copied again.
		 */
		
		Archive modArchive = new Archive(file);
			
		if (!modArchive.extract()) {
			return null;
		}
		
		/*
		 * After cleaning the archive, the .modinfo will be at the top level.
		 * This removes the need to store and modify subdirectories in the future.
		 * Additionally, installation of cleaned mods will be a simple extraction.
		 */
		modArchive.clean();
		modArchive.writeToFile(new File("mods/" + modArchive.getFileName()));
		
		//modArchive.extractToFolder(new File("mods/testFolder"));
		
		Mod mod = new Mod();
		
		/*
		 * TODO Take the temporary files from the above step and extract necessary info.
		 */
		
		
		
		try {
			Database.updateMod(mod);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
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

	public String getURL() {
		return url;
	}

	public void setURL(String url) {
		this.url = url;
	}

	public String getArchiveName() {
		return archiveName;
	}

	public void setArchiveName(String file) {
		this.archiveName = file;
	}

	public long getChecksum() {
		return checksum;
	}

	public void setChecksum(long checksum) {
		this.checksum = checksum;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
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

	public HashSet<ModFile> getFiles() {
		return files;
	}

	public void setFiles(HashSet<ModFile> files) {
		this.files = files;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}
	
}