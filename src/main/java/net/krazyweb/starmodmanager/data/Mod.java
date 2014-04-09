package net.krazyweb.starmodmanager.data;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import net.krazyweb.helpers.Archive;
import net.krazyweb.helpers.ArchiveFile;
import net.krazyweb.helpers.FileHelper;
import net.krazyweb.helpers.JSONHelper;
import net.krazyweb.stardb.databases.AssetDatabase;
import net.krazyweb.stardb.exceptions.StarDBException;
import net.krazyweb.starmodmanager.dialogue.MessageDialogue;
import net.krazyweb.starmodmanager.dialogue.MessageDialogue.MessageType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.ParseException;

public class Mod implements Observable {
	
	private static final Logger log = LogManager.getLogger(Mod.class);
	
	private static final String NO_DESCRIPTION = "StarboundModManager___NO_DESCRIPTION_FOR_MOD";
	private static final String NO_AUTHOR = "StarboundModManager___NO_AUTHOR_FOR_MOD";
	private static final String NO_VERSION = "StarboundModManager___NO_VERSION_FOR_MOD";
	
	private String internalName;
	private String displayName;
	private String modVersion;
	private String gameVersion;
	private String author;
	private String description;
	private String url;
	private String archiveName;
	private String imageName;
	
	private long checksum;
	
	private int order = -1;
	
	private boolean hidden = false;
	private boolean installed = false;
	private boolean hasImage = false;
	
	private Set<String> dependencies;
	private Set<ModFile> files; //All files that the mod alters
	
	private SettingsModelInterface settings;
	private static LocalizerModelInterface localizer;
	
	private Set<Observer> observers;
	
	protected static class ModOrderComparator implements Comparator<Mod> {

		@Override
		public int compare(Mod mod1, Mod mod2) {
			return mod1.order - mod2.order;
		}
		
	}
	
	protected Mod(final LocalizerModelFactory localizerFactory, final SettingsModelFactory settingsFactory) {
		observers = new HashSet<>();
		localizer = localizerFactory.getInstance();
		settings = settingsFactory.getInstance();
	}
	
	public static Set<Mod> load(final Path path, final int order, final SettingsModelFactory settingsFactory, final DatabaseModelFactory databaseFactory, final LocalizerModelFactory localizerFactory) {
		
		log.debug("Loading mod: {}", path);
		
		Set<Mod> mods = new HashSet<>();
		
		//TODO Count all mods and send back progress info
		
		Set<Archive> archives = null;
		
		try {
			archives = processModFile(path, settingsFactory);
		} catch (IOException | StarDBException | ParseException e) {
			//TODO Error Dialogue
			log.error("", e);
			return new HashSet<Mod>();
		}
		
		for (Archive archive : archives) {
			
			Mod mod = new Mod(localizerFactory, settingsFactory);
			
			mod.setOrder(order);
			mod.files = new HashSet<>();
			mod.setArchiveName(archive.getFileName());
			
			//Get the modinfo file and parse it
			JsonObject obj = JsonObject.readFrom(new String(archive.getFile(".modinfo").getData()));
			
			mod.setInternalName(obj.get("name").asString());
			
			if (obj.get("version") != null) {
				mod.setGameVersion(obj.get("version").asString());
			} else {
				mod.setGameVersion("Field Empty");
			}
			
			Set<String> dependencies = new HashSet<>();
			Set<String> ignoredFileNames = new HashSet<>();
			
			if (obj.get("dependencies") != null) {
				JsonArray arr = obj.get("dependencies").asArray();
				for (int i = 0; i < arr.size(); i++) {
					dependencies.add(arr.get(i).asString());
				}
			}
			
			mod.setDependencies(dependencies);
			
			if (obj.get("metadata") != null) {
				
				JsonObject metadata = obj.get("metadata").asObject();
				
				mod.setDisplayName(JSONHelper.getString(metadata, "displayname", mod.getInternalName()));
				mod.setAuthor(JSONHelper.getString(metadata, "author", NO_AUTHOR));
				mod.setDescription(JSONHelper.getString(metadata, "description", NO_DESCRIPTION));
				mod.setURL(JSONHelper.getString(metadata, "support_url", ""));
				mod.setModVersion(JSONHelper.getString(metadata, "version", NO_VERSION));
				
				if (obj.get("ignoredfiles") != null) {
					JsonArray arr = obj.get("ignoredfiles").asArray();
					for (int i = 0; i < arr.size(); i++) {
						ignoredFileNames.add(arr.get(i).asString());
					}
				}
				
			} else {
				
				mod.setDisplayName(mod.getInternalName());
				mod.setAuthor(NO_AUTHOR);
				mod.setDescription(NO_DESCRIPTION);
				mod.setURL("");
				mod.setModVersion(NO_VERSION);
				
			}
			
			try {
				mod.setChecksum(FileHelper.getChecksum(new File(settingsFactory.getInstance().getPropertyString("modsdir") + File.separator + mod.archiveName).toPath())); //TODO Better Path manipulation
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
				
				if (!archiveFile.isFolder()) {
					mod.files.add(modFile);
				}
				
			}
			
			try {
				databaseFactory.getInstance().updateMod(mod);
			} catch (SQLException e) {
				log.error("", e);
				MessageDialogue dialogue = new MessageDialogue(localizer.getMessage("mod.dbconnectionerror"), localizer.getMessage("mod.dbconnectionerror.title"), MessageType.ERROR, new LocalizerFactory());
				dialogue.getResult();
				return new HashSet<Mod>();
			}
			
			mods.add(mod);
			
		}
		
		return mods;
		
	}

	private static Set<Archive> processModFile(final Path path, final SettingsModelFactory settingsFactory) throws IOException, StarDBException, ParseException {
		
		Set<Archive> archives = new HashSet<>();
		
		if (FileHelper.identifyType(path, false).equals("pak")) {
			processPakFile(path, archives, settingsFactory);
		} else {
			processArchive(path, archives, settingsFactory);
		}
		
		return archives;
		
	}
	
	private static void processPakFile(final Path path, final Set<Archive> output, final SettingsModelFactory settingsFactory) throws IOException, StarDBException {
		
		SettingsModelInterface settings = settingsFactory.getInstance();
		
		AssetDatabase database = AssetDatabase.open(path);
		log.debug(database.getFileList());
		
		byte[] modinfoFile = database.getAsset("/pak.modinfo");
		String[] modinfoContents = new String(modinfoFile).split("\n");
		
		String modName = "";
		String assetsPath = "";
		
		//TODO Use actual JSON parser
		for (String line : modinfoContents) {
			if (line.contains("\"name\"")) {
				modName = line.trim().split(":")[1];
				modName = modName.substring(modName.indexOf("\"") + 1, modName.lastIndexOf("\""));
			}
			if (line.contains("\"path\"")) {
				assetsPath = line.trim().split(":")[1];
				assetsPath = assetsPath.substring(assetsPath.indexOf("\"") + 1, assetsPath.lastIndexOf("\""));
			}
		}
		
		if (assetsPath.startsWith(".")) {
			assetsPath = assetsPath.replace(".", "");
		}
		
		if (assetsPath.startsWith("/")) {
			assetsPath = assetsPath.replace("/", "");
		}
		
		if (assetsPath.startsWith("./")) {
			assetsPath = assetsPath.replace("./", "");
		}

		Archive modArchive = new Archive(settings.getPropertyPath("modsdir").resolve(modName + ".zip"));
		
		modArchive.addFile(new ArchiveFile(modinfoFile, Paths.get(modName + ".modinfo"), false));
		
		ArchiveFile modinfo = modArchive.getFile(".modinfo");
		JsonObject o2 = JsonObject.readFrom(new String(modinfo.getData()));
		o2.set("path", "assets");
		
		modinfo.setData(o2.toString().getBytes());
		
		log.debug("Assets path is '{}'", assetsPath);
		
		for (String file : database.getFileList()) {

			log.trace("{} is in {}", file, path);
			
			if (file.endsWith(".modinfo") || file.endsWith("desktop.ini") || file.endsWith("thumbs.db")) {
				continue;
			}
			
			if (assetsPath.isEmpty()) {
				modArchive.addFile(new ArchiveFile(database.getAsset(file), Paths.get("assets/" + file.substring(1)), false));
			} else {
				modArchive.addFile(new ArchiveFile(database.getAsset(file), Paths.get("assets/" + file.substring(assetsPath.length())), false));
			}
			
		}
		
		Files.deleteIfExists(settings.getPropertyPath("modsdir").resolve(path.subpath(path.getNameCount() - 1, path.getNameCount())));
		
		modArchive.writeToFile();
		
		output.add(modArchive);
		
	}
	
	private static void processArchive(final Path path, final Set<Archive> output, final SettingsModelFactory settingsFactory) throws IOException, StarDBException {
		
		Archive originalArchive = new Archive(path);
		
		if (!originalArchive.extract()) {
			throw new IOException("Could not extract archive.");
		}
		
		Set<ArchiveFile> usedPaks = new HashSet<>();
		
		for (ArchiveFile file : originalArchive.getFiles()) {
			
			if (FileHelper.getExtension(file.getPath()).equals("modinfo")) {
				
				JsonObject o = JsonObject.readFrom(new String(file.getData()));
				
				String preformattedPath = o.get("path").asString().replaceAll("\"", "");

				if (preformattedPath.startsWith("./")) {
					preformattedPath = preformattedPath.replace("./", "");
				}
				
				if (preformattedPath.startsWith(".")) {
					preformattedPath = preformattedPath.replace(".", "");
				}
				
				if (preformattedPath.startsWith("/")) {
					preformattedPath = preformattedPath.replace("/", "");
				}
				
				Path modinfoPath = file.getPath();
				
				if (modinfoPath.getNameCount() > 2) {
					modinfoPath = modinfoPath.subpath(0, modinfoPath.getNameCount() - 1);
				} else if (modinfoPath.getNameCount() > 1){
					modinfoPath = modinfoPath.getName(0);
				} else {
					modinfoPath = Paths.get("");
				}

				Archive outputArchive = new Archive(settingsFactory.getInstance().getPropertyPath("modsdir").resolve(Paths.get(o.get("name").asString() + ".zip")));
				
				if (file.getPath().getNameCount() == 1) {
					outputArchive.addFile(new ArchiveFile(file.getData(), file.getPath(), false));
					log.debug("'{}' -> '{}' relativized to '{}'", modinfoPath, file.getPath(), file.getPath());
				} else {
					outputArchive.addFile(new ArchiveFile(file.getData(), modinfoPath.relativize(file.getPath()).normalize(), false));
					log.debug("'{}' -> '{}' relativized to '{}'", modinfoPath, file.getPath(), modinfoPath.relativize(file.getPath()).normalize());
				}
				
				Path assetsPath = modinfoPath.resolve(Paths.get(preformattedPath));
				
				log.debug("Assets path for modinfo '{}': {}", file.getPath(), assetsPath);
				
				if (originalArchive.getFile(assetsPath) != null && !assetsPath.toString().isEmpty() && !originalArchive.getFile(assetsPath).isFolder()) {
					
					if (FileHelper.identifyType(originalArchive.getFile(assetsPath).getData()).equals("pak")) {
						
						log.debug("Assets for mod '{}' identified as .pak file: {}", modinfoPath, assetsPath);
						
						usedPaks.add(originalArchive.getFile(assetsPath));
						
						ArchiveFile modinfo = outputArchive.getFile(".modinfo");
						JsonObject o2 = JsonObject.readFrom(new String(modinfo.getData()));
						o2.set("path", "assets");
						
						modinfo.setData(o2.toString().getBytes());
						
						//TODO Update StarDB to let me pass in a byte array instead of needing to open a file
						
						Path tempPath = Paths.get("tempPak" + System.nanoTime());
						
						SeekableByteChannel tempPakFile = Files.newByteChannel(tempPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
						tempPakFile.write(ByteBuffer.wrap(originalArchive.getFile(assetsPath).getData()));
						tempPakFile.close();
						
						AssetDatabase database = AssetDatabase.open(tempPath);
						
						for (String assetFile : database.getFileList()) {
							outputArchive.addFile(new ArchiveFile(database.getAsset(assetFile), Paths.get("assets/" + assetFile), false));
							log.trace("Asset extracted: {} | {}", assetFile, Paths.get("assets/" + assetFile));
						}
						
						Files.deleteIfExists(tempPath);
						
					}
					
				} else {

					ArchiveFile modinfo = outputArchive.getFile(".modinfo");
					JsonObject o2 = JsonObject.readFrom(new String(modinfo.getData()));
					o2.set("path", "assets");
					
					modinfo.setData(o2.toString().getBytes());
					
					log.debug("Assets for mod '{}' is a standard assets folder: {}", modinfoPath, assetsPath);
					
					for (ArchiveFile f2 : originalArchive.getFiles()) {
						
						if ((assetsPath.toString().isEmpty() || f2.getPath().startsWith(assetsPath)) && !f2.isFolder() && !f2.getPath().toString().endsWith(".modinfo")) {
							
							if (modinfoPath.toString().isEmpty()) {
								
								if (f2.getPath().getNameCount() == 1) {
									
									if (!f2.getPath().startsWith(assetsPath)) {
										outputArchive.addFile(new ArchiveFile(f2.getData(), Paths.get("assets/").resolve(f2.getPath()).normalize(), false));
										log.trace("'{}' -> '{}' relativized to '{}'", modinfoPath, f2.getPath(), Paths.get("assets/").resolve(f2.getPath()).normalize());
									} else {
										outputArchive.addFile(new ArchiveFile(f2.getData(), f2.getPath(), false));
										log.trace("'{}' -> '{}' relativized to '{}'", modinfoPath, f2.getPath(), f2.getPath());
									}
									
								} else {
									
									if (!f2.getPath().startsWith(assetsPath)) {
										outputArchive.addFile(new ArchiveFile(f2.getData(), Paths.get("assets/").resolve(f2.getPath()).normalize(), false));
										log.trace("'{}' -> '{}' relativized to '{}'", modinfoPath, f2.getPath(), Paths.get("assets/").resolve(f2.getPath()).normalize());
									} else {
										outputArchive.addFile(new ArchiveFile(f2.getData(), f2.getPath(), false));
										log.trace("'{}' -> '{}' relativized to '{}'", modinfoPath, f2.getPath(), f2.getPath());
									}
										
								}
								
							} else {
								
								if (f2.getPath().getNameCount() == 1) {
									
									if (!modinfoPath.relativize(f2.getPath()).startsWith("assets")) {
										outputArchive.addFile(new ArchiveFile(f2.getData(), Paths.get("assets/").resolve(f2.getPath()), false));
										log.trace("'{}' -> '{}' relativized to '{}'", modinfoPath, f2.getPath(), Paths.get("assets/").resolve(f2.getPath()));
									} else {
										outputArchive.addFile(new ArchiveFile(f2.getData(), f2.getPath(), false));
										log.trace("'{}' -> '{}' relativized to '{}'", modinfoPath, f2.getPath(), f2.getPath());
									}
									
								} else {
									
									if (!modinfoPath.relativize(f2.getPath()).startsWith("assets")) {
										outputArchive.addFile(new ArchiveFile(f2.getData(), Paths.get("assets/").resolve(modinfoPath.relativize(f2.getPath()).normalize()), false));
										log.trace("'{}' -> '{}' relativized to '{}'", modinfoPath, f2.getPath(), Paths.get("assets/").resolve(modinfoPath.relativize(f2.getPath()).normalize()));
									} else {
										outputArchive.addFile(new ArchiveFile(f2.getData(), modinfoPath.relativize(f2.getPath()).normalize(), false));
										log.trace("'{}' -> '{}' relativized to '{}'", modinfoPath, f2.getPath(), modinfoPath.relativize(f2.getPath()).normalize());
									}
										
								}
							
							}
						}
					}
					
				}
				
				outputArchive.writeToFile(settingsFactory.getInstance().getPropertyPath("modsdir").resolve(Paths.get(o.get("name").asString() + ".zip")).toFile()); //TODO
				
				output.add(outputArchive);
				
			}
			
		}
		
		for (ArchiveFile file : originalArchive.getFiles()) {
			
			if (!usedPaks.contains(file) && !file.isFolder() && FileHelper.identifyType(file.getData()).equals("pak")) {
				
				log.debug("Additional .pak identified: {}", file.getPath());
				
				Path tempPath = Paths.get("tempPak" + System.nanoTime());
				
				SeekableByteChannel tempPakFile = Files.newByteChannel(tempPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
				tempPakFile.write(ByteBuffer.wrap(file.getData()));
				tempPakFile.close();
				
				AssetDatabase database = AssetDatabase.open(tempPath);
				log.debug(database.getFileList());
				
				if (database.getAsset("/pak.modinfo") != null) {
					log.debug("{} has a .modinfo file, parsing into mod.", file.getPath());
					processPakFile(tempPath, output, settingsFactory);
				} else {
					log.debug("{} does not contain a .modinfo file, skipping.", file.getPath());
				}
				
				Files.deleteIfExists(tempPath);
				
			}
			
		}
		
	}
	
	public boolean conflictsWith(final Mod mod) {
		
		for (ModFile file : files) {
			
			if (file.isAutoMerged() || file.isIgnored()) {
				continue;
			}
			
			for (ModFile otherFile : mod.files) {
				
				if (otherFile.isAutoMerged() || otherFile.isIgnored()) {
					continue;
				}
				
				if (file.getPath().equals(otherFile.getPath())) {
					return true;
				}
				
			}
			
		}
		
		return false;
		
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
		if (modVersion.equals(NO_VERSION)) {
			return localizer.getMessage("mod.unknownversion");
		}
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
		if (author.equals(NO_AUTHOR)) {
			return localizer.getMessage("mod.unknownauthor");
		}
		return author;
	}

	protected void setAuthor(final String author) {
		this.author = author;
	}

	public String getDescription() {
		if (description.equals(NO_DESCRIPTION)) {
			return localizer.getMessage("mod.nodescription");
		}
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
		notifyObservers("installstatuschanged");
	}
	
	public boolean hasImage() {
		return hasImage;
	}
	
	public String getImageLocation() {
		return settings.getPropertyPath("modsdir").resolve(settings.getPropertyPath("modsimagedir")).resolve(Paths.get(imageName)).toAbsolutePath().toString();
	}
	
	protected void setImage() {
		//TODO
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

	@Override
	public void addObserver(final Observer observer) {
		observers.add(observer);
	}

	@Override
	public void removeObserver(final Observer observer) {
		observers.remove(observer);
	}
	
	private final void notifyObservers(final Object message) {
		for (final Observer o : observers) {
			o.update(this, message);
		}
	}
	
}