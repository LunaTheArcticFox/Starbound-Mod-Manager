package main.java.net.krazyweb.starmodmanager.data;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import main.java.net.krazyweb.helpers.Archive;
import main.java.net.krazyweb.helpers.ArchiveFile;
import main.java.net.krazyweb.helpers.FileHelper;
import main.java.net.krazyweb.helpers.JSONHelper;

import org.apache.log4j.Logger;

public class Mod {
	
	private static final Logger log = Logger.getLogger(Mod.class);
	
	private String internalName;
	private String displayName;
	private String modVersion;
	private String gameVersion;
	private String author;
	private String description;
	private String url;
	private String archiveName;
	
	private long checksum;
	
	private int order = -1;
	
	private boolean hidden = false;
	private boolean installed = false;
	
	private Set<String> dependencies;
	private Set<ModFile> files; //All files that the mod alters
	
	protected static class ModOrderComparator implements Comparator<Mod> {

		@Override
		public int compare(Mod mod1, Mod mod2) {
			return mod1.order - mod2.order;
		}
		
	}
	
	protected Mod() {
	}
	
	protected static Set<Mod> load(final File file, final int order) {
		
		log.debug("Loading mod: " + file.getPath());
		
		Set<Mod> mods = new HashSet<>();
		
		/* 
		 * As long as the mod archive object is in memory, so are its entire contents.
		 * This is advantageous, as the files must only be read from the disk once,
		 * as opposed to copied from disk, to disk, read, then deleted, then copied again.
		 */
		
		Archive modArchive = new Archive(file);
			
		if (!modArchive.extract()) {
			//TODO Error messages
			return null;
		}
		
		/*
		 * After cleaning the archive, the .modinfo will be at the top level.
		 * This removes the need to store and modify subdirectories in the future.
		 * Additionally, installation of cleaned mods will be a simple extraction.
		 */
		//TODO Detect multiple modinfos
		
		for (ArchiveFile f : modArchive.getFiles()) {
			if (f.getPath().endsWith(".modinfo")) {
				
				Set<ArchiveFile> newFiles = new HashSet<>();
				
				log.debug(f.getPath());
				String subDir = f.getPath().substring(0, f.getPath().lastIndexOf("/"));
				
				for (ArchiveFile f2 : modArchive.getFiles()) {
					if (f2.getPath().startsWith(subDir)) {
						newFiles.add(f2);
					}
				}
				
				String newDir;
				
				if (subDir.contains("/")) {
					newDir = subDir.substring(subDir.lastIndexOf("/"));
				} else {
					newDir = subDir;
				}
				
				Archive newArchive = new Archive(newDir + ".zip");
				
				newArchive.getFiles().addAll(newFiles);
				
				cleanArchive(newArchive);
				
				newArchive.writeToFile(new File(Settings.getModsDirectory() + File.separator + newArchive.getFileName()));
				
				Mod mod = new Mod();
				
				mod.setOrder(order);
				mod.files = new HashSet<>();
				mod.setArchiveName(newArchive.getFileName());
				
				//Get the modinfo file and parse it
				JsonReader reader = Json.createReader(new ByteArrayInputStream(newArchive.getFile(".modinfo").getData()));
				JsonObject obj = reader.readObject();
				
				mod.setInternalName(obj.getString("name"));
				mod.setGameVersion(obj.getString("version"));
				
				Set<String> dependencies = new HashSet<>();
				Set<String> ignoredFileNames = new HashSet<>();
				
				if (obj.containsKey("dependencies")) {
					JsonArray arr = obj.getJsonArray("dependencies");
					for (int i = 0; i < arr.size(); i++) {
						dependencies.add(arr.getString(i));
					}
				}
				
				mod.setDependencies(dependencies);
				
				if (obj.containsKey("metadata")) {
					
					JsonObject metadata = obj.getJsonObject("metadata");
					
					mod.setDisplayName(JSONHelper.getString(metadata, "displayname", mod.getInternalName()));
					mod.setAuthor(JSONHelper.getString(metadata, "author", "Unknown"));
					mod.setDescription(JSONHelper.getString(metadata, "description", "[No Description]"));
					mod.setURL(JSONHelper.getString(metadata, "support_url", ""));
					mod.setModVersion(JSONHelper.getString(metadata, "version", "Unknown"));
					
					if (obj.containsKey("ignoredfiles")) {
						JsonArray arr = obj.getJsonArray("ignoredfiles");
						for (int i = 0; i < arr.size(); i++) {
							ignoredFileNames.add(arr.getString(i));
						}
					}
					
				} else {
					
					mod.setDisplayName(mod.getInternalName());
					mod.setAuthor("Unknown");
					mod.setDescription("[No Description]");
					mod.setURL("");
					mod.setModVersion("Unknown");
					
				}
				
				try {
					mod.setChecksum(FileHelper.getChecksum(new File(Settings.getModsDirectory().getAbsolutePath() + File.separator + mod.archiveName)));
				} catch (IOException e) {
					log.error("Setting Checksum", e);
				}
				
				for (ArchiveFile archiveFile : newArchive.getFiles()) {
					
					ModFile modFile = new ModFile();
					modFile.setPath(archiveFile.getPath());
					
					//Find and list all ignored files
					for (String ignored : ignoredFileNames) {
						if (archiveFile.getPath().endsWith(ignored) || archiveFile.getPath().endsWith(".txt")) {
							modFile.setIgnored(true);
						}
					}
					
					//Scan all json files and find those with mergeability
					if (!archiveFile.isFolder() && FileHelper.isJSON(archiveFile.getPath())) {
						
						modFile.setJson(true);
						
						String fileContents = new String(archiveFile.getData());
						
						if (fileContents.contains("__merge")) {
							modFile.setAutoMerged(true);
						}
						
					}
					
					mod.files.add(modFile);
					
				}
				
				try {
					Database.updateMod(mod);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
				mods.add(mod);
				
			}
		}
		
		return mods;
		
	}
	
	private static void cleanArchive(final Archive archive) {
		
		Set<ArchiveFile> filesToRemove = new HashSet<>();
		
		String modBaseDirectory = "";
		
		for (ArchiveFile file : archive.getFiles()) {
			if (file.getPath().endsWith(".modinfo")) {
				modBaseDirectory = file.getPath().substring(0, file.getPath().lastIndexOf('/') + 1);
			}
		}
		
		for (ArchiveFile file : archive.getFiles()) {
			
			if (!file.getPath().startsWith(modBaseDirectory)) {
				filesToRemove.add(file);
			} else {
				file.setPath(file.getPath().replace(modBaseDirectory, ""));
			}
			
			if (file.getPath().endsWith("Thumbs.db")) {
				filesToRemove.add(file);
			}
			
		}
		
		archive.getFiles().removeAll(filesToRemove);
		
	}

	public String getInternalName() {
		return internalName;
	}

	protected void setInternalName(String internalName) {
		this.internalName = internalName;
	}

	public String getDisplayName() {
		return displayName;
	}

	protected void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getModVersion() {
		return modVersion;
	}

	protected void setModVersion(String version) {
		this.modVersion = version;
	}

	public String getGameVersion() {
		return gameVersion;
	}

	protected void setGameVersion(String gameVersion) {
		this.gameVersion = gameVersion;
	}

	public String getAuthor() {
		return author;
	}

	protected void setAuthor(String author) {
		this.author = author;
	}

	public String getDescription() {
		return description;
	}

	protected void setDescription(String description) {
		this.description = description;
	}

	public String getURL() {
		return url;
	}

	protected void setURL(String url) {
		this.url = url;
	}

	public String getArchiveName() {
		return archiveName;
	}

	protected void setArchiveName(String file) {
		this.archiveName = file;
	}

	protected long getChecksum() {
		return checksum;
	}

	protected void setChecksum(long checksum) {
		this.checksum = checksum;
	}

	public boolean isHidden() {
		return hidden;
	}

	protected void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public boolean isInstalled() {
		return installed;
	}

	protected void setInstalled(boolean installed) {
		this.installed = installed;
	}

	public Set<String> getDependencies() {
		return dependencies;
	}

	protected void setDependencies(Set<String> dependencies2) {
		this.dependencies = dependencies2;
	}

	public Set<ModFile> getFiles() {
		return files;
	}

	protected void setFiles(Set<ModFile> files) {
		this.files = files;
	}

	public int getOrder() {
		return order;
	}

	protected void setOrder(int order) {
		this.order = order;
	}
	
}