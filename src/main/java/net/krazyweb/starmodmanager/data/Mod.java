package net.krazyweb.starmodmanager.data;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import net.krazyweb.helpers.Archive;
import net.krazyweb.helpers.ArchiveFile;
import net.krazyweb.helpers.FileHelper;
import net.krazyweb.helpers.JSONHelper;

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
	
	protected static Set<Mod> load(final Path path, final int order) {
		
		log.debug("Loading mod: " + path);
		
		Set<Mod> mods = new HashSet<>();
		
		Archive modArchive = new Archive(path);
			
		if (!modArchive.extract()) {
			//TODO Error messages
			return null;
		}
		
		//TODO Count all mods and send back progress info
		
		Set<Archive> archives = processArchive(modArchive);
		
		for (Archive archive : archives) {
			
			Mod mod = new Mod();
			
			mod.setOrder(order);
			mod.files = new HashSet<>();
			mod.setArchiveName(archive.getFileName());
			
			//Get the modinfo file and parse it
			JsonReader reader = Json.createReader(new ByteArrayInputStream(archive.getFile(".modinfo").getData()));
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
				mod.setAuthor(JSONHelper.getString(metadata, "author", Localizer.getInstance().getMessage("mod.unknownauthor")));
				mod.setDescription(JSONHelper.getString(metadata, "description", Localizer.getInstance().getMessage("mod.nodescription")));
				mod.setURL(JSONHelper.getString(metadata, "support_url", ""));
				mod.setModVersion(JSONHelper.getString(metadata, "version", Localizer.getInstance().getMessage("mod.unknownversion")));
				
				if (obj.containsKey("ignoredfiles")) {
					JsonArray arr = obj.getJsonArray("ignoredfiles");
					for (int i = 0; i < arr.size(); i++) {
						ignoredFileNames.add(arr.getString(i));
					}
				}
				
			} else {
				
				mod.setDisplayName(mod.getInternalName());
				mod.setAuthor(Localizer.getInstance().getMessage("mod.unknownauthor"));
				mod.setDescription(Localizer.getInstance().getMessage("mod.nodescription"));
				mod.setURL("");
				mod.setModVersion(Localizer.getInstance().getMessage("mod.unknownversion"));
				
			}
			
			try {
				mod.setChecksum(FileHelper.getChecksum(new File(Settings.getInstance().getPropertyString("modsdir") + File.separator + mod.archiveName).toPath()));
			} catch (IOException e) {
				log.error("Setting Checksum", e);
			}
			
			for (ArchiveFile archiveFile : archive.getFiles()) {
				
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
		
		return mods;
		
	}

	private static Set<Archive> processArchive(final Archive archive) {
		
		Set<Archive> archives = new HashSet<>();
		
		for (ArchiveFile file : archive.getFiles()) {
			
			if (FileHelper.getExtension(file.getPath()).equals("modinfo")) {
				
				Set<ArchiveFile> files = new HashSet<>();
				
				Path subDir = file.getPath().getParent();
				
				for (ArchiveFile f2 : archive.getFiles()) {

					Path filePath = f2.getPath();
					String fileName = filePath.getFileName().toString().toLowerCase();
					
					if (fileName.equals("desktop.ini") || fileName.equals("thumbs.db")) {
						continue;
					}
					
					if (subDir == null) {

						files.add(f2);
						
					} else if (filePath.startsWith(subDir)) {
						
						Path newPath;
						
						//Unfortunately, having identical ending indices for the subpath() method doesn't work.
						//Thus, the following is needed for all 3 possibilities.
						if (filePath.getNameCount() > 2) {
							newPath = filePath.subpath(subDir.getNameCount(), filePath.getNameCount());
						} else if (filePath.getNameCount() > 1) {
							newPath = filePath.getName(1);
						} else {
							newPath = filePath;
						}
						
						f2.setPath(newPath);
						
						files.add(f2);
						
					}
				}
				
				if (subDir == null) {
					subDir = Paths.get(archive.getFileName().substring(0, archive.getFileName().lastIndexOf(".")));
				}
				
				Archive newArchive = new Archive(subDir + ".zip");
				newArchive.getFiles().addAll(files);
				
				newArchive.writeToFile(new File(Settings.getInstance().getPropertyString("modsdir") + File.separator + newArchive.getFileName()));
				
				archives.add(newArchive);
				
			}
		}
		
		return archives;
		
	}
	
	public String getInternalName() {
		return internalName;
	}

	protected void setInternalName(final String internalName) {
		this.internalName = internalName;
	}

	public String getDisplayName() {
		return displayName;
	}

	protected void setDisplayName(final String displayName) {
		this.displayName = displayName;
	}

	public String getModVersion() {
		return modVersion;
	}

	protected void setModVersion(final String version) {
		this.modVersion = version;
	}

	public String getGameVersion() {
		return gameVersion;
	}

	protected void setGameVersion(final String gameVersion) {
		this.gameVersion = gameVersion;
	}

	public String getAuthor() {
		return author;
	}

	protected void setAuthor(final String author) {
		this.author = author;
	}

	public String getDescription() {
		return description;
	}

	protected void setDescription(final String description) {
		this.description = description;
	}

	public String getURL() {
		return url;
	}

	protected void setURL(final String url) {
		this.url = url;
	}

	public String getArchiveName() {
		return archiveName;
	}

	protected void setArchiveName(final String file) {
		this.archiveName = file;
	}

	protected long getChecksum() {
		return checksum;
	}

	protected void setChecksum(final long checksum) {
		this.checksum = checksum;
	}

	public boolean isHidden() {
		return hidden;
	}

	protected void setHidden(final boolean hidden) {
		this.hidden = hidden;
	}

	public boolean isInstalled() {
		return installed;
	}

	protected void setInstalled(final boolean installed) {
		this.installed = installed;
	}

	public Set<String> getDependencies() {
		return dependencies;
	}

	protected void setDependencies(final Set<String> dependencies2) {
		this.dependencies = dependencies2;
	}

	public Set<ModFile> getFiles() {
		return files;
	}

	protected void setFiles(final Set<ModFile> files) {
		this.files = files;
	}

	public int getOrder() {
		return order;
	}

	protected void setOrder(final int order) {
		this.order = order;
	}
	
}