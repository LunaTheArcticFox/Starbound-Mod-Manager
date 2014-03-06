package net.krazyweb.starmodmanager.data;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import name.fraser.neil.plaintext.diff_match_patch;
import name.fraser.neil.plaintext.diff_match_patch.Diff;
import name.fraser.neil.plaintext.diff_match_patch.Patch;
import net.krazyweb.helpers.Archive;
import net.krazyweb.helpers.FileHelper;
import net.krazyweb.stardb.databases.AssetDatabase;
import net.krazyweb.starmodmanager.data.Mod.ModOrderComparator;
import net.krazyweb.starmodmanager.dialogue.MessageDialogue;
import net.krazyweb.starmodmanager.dialogue.ProgressDialogue;
import net.krazyweb.starmodmanager.dialogue.MessageDialogue.MessageType;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedMessage;

public class ModList implements ModListModelInterface {
	
	private static class DumbBoolContainer {
		private boolean value;
	}
	
	private static final Logger log = LogManager.getLogger(ModList.class);
	
	private SettingsModelInterface settings;
	private DatabaseModelInterface database;
	private LocalizerModelInterface localizer;
	
	private boolean locked;
	
	private List<Mod> mods;
	
	private Set<Observer> observers;
	
	public ModList(final SettingsModelFactory settingsFactory, final DatabaseModelFactory databaseFactory, final LocalizerModelFactory localizerFactory) {
		observers = new HashSet<>();
		settings = settingsFactory.getInstance();
		database = databaseFactory.getInstance();
		localizer = localizerFactory.getInstance();
	}
	
	@Override
	public Task<Void> getLoadTask() {
		return new GetModListTask(this);
	}
	
	@Override
	public Task<Void> getNewModsTask() {
		return new GetNewModsTask(this);
	}
	
	@Override
	public void addMods(final List<Path> files) {
		
		final ProgressDialogue progress = new ProgressDialogue(localizer.getMessage("modlist.addingmods"));
		
		final Set<String> currentMods = new HashSet<>();
		for (Mod mod : mods) {
			currentMods.add(mod.getInternalName());
		}
		
		final List<Mod> newMods = new ArrayList<>();
		
		final DumbBoolContainer recoverableErrorOccurred = new DumbBoolContainer();
		recoverableErrorOccurred.value = false;
		
		final Task<Integer> addModsTask = new Task<Integer>() {

			@Override
			protected Integer call() throws Exception {
				
				this.updateProgress(0, files.size());
				
				Files.createDirectories(settings.getPropertyPath("modsdir"));
				
				Set<Path> toRemove = new HashSet<>();
				
				for (int i = 0; i < files.size(); i++) {
					
					Path file = files.get(i);
					
					this.updateMessage(localizer.getMessage("modlist.loadingmod") + file.getFileName());
					
					Set<Mod> modsToAdd = Mod.load(file, mods.size(), new SettingsFactory(), new DatabaseFactory(), new LocalizerFactory());
					
					if (modsToAdd == null || modsToAdd.isEmpty()) {
						recoverableErrorOccurred.value = true;
						continue;
					}
					
					
					for (Mod mod : modsToAdd) {
						if (!currentMods.contains(mod.getInternalName())) {
							
							mods.add(mod);
							newMods.add(mod);
							
							for (Path path : files) {
								if (Files.isSameFile(settings.getPropertyPath("modsdir").resolve(mod.getArchiveName()), path)) {
									toRemove.add(path);
									log.debug("File is used by mod manager, will not delete: {} - {}", settings.getPropertyPath("modsdir").resolve(mod.getArchiveName()), path);
								} else {
									
									Path parent = path.getParent();
									
									while (parent != null) {
										if (parent.equals(settings.getPropertyPath("modsdir"))) {
											break;
										}
										parent = parent.getParent(); 
									}
									
									if (parent == null) {
										log.debug("File is not in the mod manager's mod directory, will not delete: {} - {}", settings.getPropertyPath("modsdir").resolve(mod.getArchiveName()), path);
										toRemove.add(path);
									}
									
								}
							}
							
						} else {
							//TODO Notify user of mod existence
							log.debug("Mod already exists, skipping: {}", file);
							
							Path parent = file.getParent();
							
							while (parent != null) {
								if (parent.equals(settings.getPropertyPath("modsdir"))) {
									break;
								}
								parent = parent.getParent();
							}
							
							if (parent == null) {
								log.debug("File is not in the mod manager's mod directory, will not delete: {} - {}", settings.getPropertyPath("modsdir").resolve(mod.getArchiveName()), file);
								toRemove.add(file);
							}
							
						}
					}
					
					this.updateProgress(i, files.size());
					
				}
				
				files.removeAll(toRemove);
				
				for (Path path : files) {
					log.debug("Deleting file '{}' - File is unused.", path);
					try {
						FileHelper.deleteFile(path);
					} catch (final IOException e) {
						log.error("", e);
					}
				}
				
				return 1;
				
			}
			
		};
		
		addModsTask.setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent t) {
				log.error("Error occurred while getting mods!", addModsTask.getException());
				MessageDialogue dialogue = new MessageDialogue(localizer.getMessage("modlist.addmoderror"), localizer.getMessage("modlist.addmoderror.title"), MessageType.ERROR, new LocalizerFactory());
				dialogue.getResult();
				progress.close();
			}
		});
		
		addModsTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent t) {
				
				if (recoverableErrorOccurred.value) {
					MessageDialogue dialogue = new MessageDialogue(localizer.getMessage("modlisttask.recoverableerror"), localizer.getMessage("modlisttask.recoverableerror.title"), MessageType.ERROR, new LocalizerFactory());
					dialogue.getResult();
				}
				
				progress.close();
				
				for (Mod mod : newMods) {
					notifyObservers(new Object[] { "modadded", mod });
				}
				
			}
		});
		
		progress.getProgressBar().bind(addModsTask.progressProperty(), files.size());
		progress.getText().textProperty().bind(addModsTask.messageProperty());
		progress.start();
		
		Thread t = new Thread(addModsTask);
		t.setName("Add Mods Thread");
		t.setDaemon(true);
		t.start();
		
	}

	@Override
	public void deleteMod(final Mod mod) {
		
		if (mod.isInstalled()) {
			uninstallMod(mod);
		}
		
		try {
			database.deleteMod(mod);
		} catch (SQLException e) {
			log.error("", e);
			MessageDialogue dialogue = new MessageDialogue(localizer.getMessage("modlist.dbconnectionerror.deletemod"), localizer.getMessage("modlist.dbconnectionerror.title"), MessageType.ERROR, new LocalizerFactory());
			dialogue.getResult();
		}
		
		try {
			FileHelper.deleteFile(Paths.get(settings.getPropertyString("modsdir") + File.separator + mod.getArchiveName())); //TODO Better Path manipulation
		} catch (IOException e) {
			log.error("", e);
			MessageDialogue dialogue = new MessageDialogue(localizer.getMessage("modlist.fileerror.deletemod"), localizer.getMessage("modlist.fileerror.deletemod.title"), MessageType.ERROR, new LocalizerFactory());
			dialogue.getResult();
		}
		
		removeMod(mod);
		
		notifyObservers(new Object[] { "moddeleted", mod });
		
	}
	
	@Override
	public Task<Void> getInstallModTask(final Mod mod) {
		
		final Task<Void> installModsTask = new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				
				this.updateProgress(0, 1);
				
				//Get every installed mod, including the one to be installed
				List<Mod> installedMods = installedMods();
				installedMods.add(mod);
				
				//Then separate out those with conflicts
				List<Mod> conflictingMods = conflictingMods(installedMods);
				
				//No new conflicts were found, so just install the new mod normally
				if (!conflictingMods.contains(mod)) {
					Archive archive = new Archive(settings.getPropertyPath("modsdir").resolve(mod.getArchiveName()));
					archive.extract();
					archive.extractToFolder(settings.getPropertyPath("starboundpath").resolve("mods").resolve(mod.getInternalName()));
					this.updateProgress(1, 1);
					return null;
				}
				
				//Delete the folders of the other mods if they exist; new conflicts may exist with the new mod
				for (Mod m : conflictingMods) {
					FileHelper.deleteFile(settings.getPropertyPath("starboundpath").resolve("mods").resolve(m.getInternalName()));
				}
				
				Set<Path> modifiedFiles = conflictingFiles(conflictingMods);
				
				Map<Mod, Archive> modArchives = new HashMap<>();
				
				for (Mod m : installedMods) {
					Archive archive = new Archive(settings.getPropertyPath("modsdir").resolve(m.getArchiveName()));
					archive.extract();
					modArchives.put(m, archive);
				}
				
				//Find all the non-conflicting files in each of those mods
				//Copy the non-conflicting files to a new patch folder (deleting the old one)
				FileHelper.deleteFile(settings.getPropertyPath("starboundpath").resolve("mods").resolve(settings.getPropertyPath("patchfolder")));
				
				for (Mod m : conflictingMods) {
					for (ModFile file : m.getFiles()) {
						if (!modifiedFiles.contains(file.getPath()) && !file.isModinfo()) {
							log.debug("Path: {}", file.getPath());
							modArchives.get(m).extractFileToFolder(file.getPath(), settings.getPropertyPath("starboundpath").resolve("mods").resolve(settings.getPropertyPath("patchfolder").resolve("assets")));
						}
					}
				}
				
				//Copy all non-JSON conflicting files into the patch folder
				//Make sure to respect the load order here
				
				Collections.reverse(conflictingMods);
				
				log.debug("Load order for conflicting mods (reversed):");
				for (Mod m : conflictingMods) {
					log.debug("  [{}] - {}", m.getOrder(), m.getDisplayName());
				}
				
				for (Mod m : conflictingMods) {
					for (ModFile file : m.getFiles()) {
						if (modifiedFiles.contains(file.getPath()) && !file.isJson()) {
							modArchives.get(m).extractFileToFolder(file.getPath(), settings.getPropertyPath("starboundpath").resolve("mods").resolve(settings.getPropertyPath("patchfolder").resolve("assets")));
						}
					}
				}
				
				AssetDatabase db = AssetDatabase.open(settings.getPropertyPath("starboundpath").resolve("assets").resolve("packed.pak"));
								
				//Get all JSON files and merge them, then save them
				for (Path path : modifiedFiles) {
					
					if (!FileHelper.isJSON(path)) {
						continue;
					}

					String originalFile = null;
					String outputFile = "";
					
					if (db.getFileList().contains("/" + path.toString())) {
						
						log.debug("Retrieving asset {} from database.", "/" + path.toString());
						
						originalFile = new String(db.getAsset("/" + path.toString()));
						
					} else {
						
						boolean found = false;
						
						for (Mod m : conflictingMods) {
							
							if (found) {
								break;
							}
							
							for (ModFile file : m.getFiles()) {
								if (file.getPath().equals(path)) {
									originalFile = new String(modArchives.get(m).getFile(path).getData());
									found = true;
									break;
								}
							}
							
						}
						
					}
					
					diff_match_patch dpm = new diff_match_patch();
					LinkedList<Patch> patchesToApply = new LinkedList<Patch>();
					
					for (Mod m : conflictingMods) {
						for (ModFile file : m.getFiles()) {
							
							if (!file.getPath().toString().equals(path.toString())) {
								continue;
							}
							
							String changedFile = new String(modArchives.get(m).getFile(file.getPath()).getData());
							
							if (conflictingMods.indexOf(m) != conflictingMods.size() - 1) {
								
								log.debug("Merging file for {} : {}", m.getDisplayName(), file.getPath());
								
								LinkedList<Diff> diff = dpm.diff_main(originalFile, changedFile);
								LinkedList<Patch> patches = dpm.patch_make(diff);
								patchesToApply.addAll(patches);
							
							} else {
								
								log.debug("Merging file for 2 {}", m.getDisplayName());
								
								outputFile = (String) dpm.patch_apply(patchesToApply, changedFile)[0];
								
							}
								
						}
					}
					
					Path outputPath = settings.getPropertyPath("starboundpath").resolve("mods").resolve(settings.getPropertyPath("patchfolder")).resolve(path);
					
					Files.createDirectories(outputPath.getParent());
					
					OutputStream output = Files.newOutputStream(outputPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
					output.write(outputFile.getBytes());
					output.close();
					
				}
				
				//Finally, save the mod manager .modinfo file to the mod patch folder
				Path outputPath = settings.getPropertyPath("starboundpath").resolve("mods").resolve(settings.getPropertyPath("patchfolder")).resolve("ModManagerPatch.modinfo");
				
				Files.createDirectories(outputPath.getParent());
				
				byte[] modinfoData = IOUtils.toByteArray(ModList.class.getClassLoader().getResourceAsStream("ModManagerPatch.modinfo"));
				
				OutputStream output = Files.newOutputStream(outputPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
				output.write(modinfoData);
				output.close();
				
				this.updateProgress(1, 1);
				
				return null;
				
			}
			
		};
		
		installModsTask.setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(final WorkerStateEvent t) {
				log.error("Error occurred while installing mods!", installModsTask.getException());
				MessageDialogue dialogue = new MessageDialogue(localizer.getMessage("modlist.installmoderror"), localizer.getMessage("modlist.installmoderror.title"), MessageType.ERROR, new LocalizerFactory());
				dialogue.getResult();
			}
		});
		
		installModsTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(final WorkerStateEvent t) {
				mod.setInstalled(true);
				try {
					database.updateMod(mod);
				} catch (final SQLException e) {
					log.error("", e);
					MessageDialogue dialogue = new MessageDialogue(localizer.getMessage("modlist.dbconnectionerror.installmod"), localizer.getMessage("modlist.dbconnectionerror.title"), MessageType.ERROR, new LocalizerFactory());
					dialogue.getResult();
				}
			}
		});
		
		return installModsTask;
		
	}
	
	@Override
	public void uninstallMod(final Mod mod) {
		
		final Task<Integer> uninstallModsTask = new Task<Integer>() {

			@Override
			protected Integer call() throws Exception {
				
				log.info("Uninstalling mod: {}", mod.getInternalName());

				try {
					log.debug("Deleting from: {}", settings.getPropertyPath("starboundpath").resolve("mods").resolve(mod.getInternalName()));
					FileHelper.deleteFile(
						settings.getPropertyPath("starboundpath").resolve("mods").resolve(mod.getInternalName())
					);
				} catch (IOException e) {
					log.error(new ParameterizedMessage("Uninstalling Mod: {}", mod.getInternalName()), e);
				}
				
				return 1;
				
			}
			
		};
		
		uninstallModsTask.setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(final WorkerStateEvent t) {
				log.error("Error occurred while uninstalling mods!", uninstallModsTask.getException());
				MessageDialogue dialogue = new MessageDialogue(localizer.getMessage("modlist.uninstallmoderror"), localizer.getMessage("modlist.uninstallmoderror.title"), MessageType.ERROR, new LocalizerFactory());
				dialogue.getResult();
			}
		});
		
		uninstallModsTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(final WorkerStateEvent t) {
				mod.setInstalled(false);
				try {
					database.updateMod(mod);
				} catch (final SQLException e) {
					log.error("", e);
					MessageDialogue dialogue = new MessageDialogue(localizer.getMessage("modlist.dbconnectionerror.installmod"), localizer.getMessage("modlist.dbconnectionerror.title"), MessageType.ERROR, new LocalizerFactory());
					dialogue.getResult();
				}
			}
		});
		
		Thread t = new Thread(uninstallModsTask);
		t.setName("Uninstall Mods Thread");
		t.setDaemon(true);
		t.start();
		
	}
	
	@Override
	public void hideMod(final Mod mod) {
		
		mod.setHidden(true);
		
		try {
			database.updateMod(mod);
		} catch (final SQLException e) {
			log.error("", e);
			MessageDialogue dialogue = new MessageDialogue(localizer.getMessage("modlist.dbconnectionerror.installmod"), localizer.getMessage("modlist.dbconnectionerror.title"), MessageType.ERROR, new LocalizerFactory());
			dialogue.getResult();
		}
		
		removeMod(mod);

		notifyObservers(new Object[] { "modhidden", mod });
		
	}
	
	private void removeMod(final Mod mod) {
		
		mods.remove(mod);
		
		Collections.sort(mods, new ModOrderComparator());
		
		for (Mod m : mods) {
			m.setOrder(mods.indexOf(m));
		}
		
	}
	
	@Override
	public void moveMod(final Mod mod, final int amount) {
		
		if (locked) {
			log.trace("Mod list locked; cannot move mod: {}", mod.getInternalName());
			return;
		}
		
		log.trace("Performing rotation, results:");
		
		if (amount > 0) {
			
			if (mods.indexOf(mod) - amount > 0) {
				Collections.rotate(mods.subList(mods.indexOf(mod) - amount, mods.indexOf(mod) + 1), 1);
			} else {
				Collections.rotate(mods.subList(0, mods.indexOf(mod) + 1), 1);
			}
			
		} else {
			
			if (mods.indexOf(mod) - amount + 1 <= mods.size()) {
				Collections.rotate(mods.subList(mods.indexOf(mod), mods.indexOf(mod) - amount + 1), -1);
			} else {
				Collections.rotate(mods.subList(mods.indexOf(mod), mods.size()), -1);
			}
			
		}
		
		for (Mod m : mods) {
			m.setOrder(mods.indexOf(m));
			log.trace("  [{}] {}", m.getOrder(), m.getInternalName());
			try {
				database.updateMod(m);
			} catch (SQLException e) {
				log.error("", e);
				MessageDialogue dialogue = new MessageDialogue(localizer.getMessage("modlist.dbconnectionerror.installmod"), localizer.getMessage("modlist.dbconnectionerror.title"), MessageType.ERROR, new LocalizerFactory());
				dialogue.getResult();
			}
		}
		
	}
	
	@Override
	public List<Mod> getMods() {
		List<Mod> modListCopy = new ArrayList<>(mods);
		return modListCopy;
	}

	@Override
	public int indexOf(final Mod mod) {
		return mods.indexOf(mod);
	}

	@Override
	public void setLocked(final boolean locked) {
		this.locked = locked;
	}

	@Override
	public boolean isLocked() {
		return locked;
	}

	@Override
	public void refreshMods() {
		/*try {
			Database.getModList();
		} catch (SQLException e) {
			e.printStackTrace();
		}*/
	}

	@Override
	public void setModList(final List<Mod> list) {
		
		Set<Mod> toRemove = new HashSet<>();
		
		for (Mod m : list) {
			if (m.isHidden()) {
				toRemove.add(m);
			}
		}
		
		list.removeAll(toRemove);
		
		Collections.sort(list, new ModOrderComparator());

		for (Mod m : list) {
			m.setOrder(list.indexOf(m));
		}
		
		this.mods = list;
		notifyObservers("modlistupdated");
		
	}
	
	private List<Mod> installedMods() {

		List<Mod> installedMods = new ArrayList<>();
		
		for (Mod m : mods) {
			if (m.isInstalled()) {
				installedMods.add(m);
			}
		}
		
		return installedMods;
		
	}
	
	private List<Mod> conflictingMods(final List<Mod> modList) {
		
		List<Mod> conflictingMods = new ArrayList<>();
		
		for (Mod m1 : modList) {
			for (Mod m2 : modList) {
				if (m1 != m2 && m1.conflictsWith(m2)) {
					if (!conflictingMods.contains(m1)) {
						conflictingMods.add(m1);
					}
					if (!conflictingMods.contains(m2)) {
						conflictingMods.add(m2);
					}
				}
			}
		}
		
		Collections.sort(conflictingMods, new ModOrderComparator());
		
		return conflictingMods;
		
	}
	
	private Set<Path> conflictingFiles(final List<Mod> conflictingMods) {
		
		Map<Path, Integer> fileCounts = new HashMap<>();

		//First, count all instances of modified files.
		for (Mod m : conflictingMods) {
			for (ModFile modFile : m.getFiles()) {
				
				if (modFile.isAutoMerged() || modFile.isIgnored() || modFile.isModinfo()) {
					continue;
				}
				
				if (fileCounts.containsKey(modFile.getPath())) {
					fileCounts.put(modFile.getPath(), fileCounts.get(modFile.getPath()) + 1);
				} else {
					fileCounts.put(modFile.getPath(), 1);
				}
				
			}
		}
		
		Set<Path> modifiedFiles = new HashSet<>();
		
		//All files with an instance count > 1 have conflicts
		for (Path p : fileCounts.keySet()) {
			if (fileCounts.get(p) > 1) {
				modifiedFiles.add(p);
				log.debug("Modified File: {}", p);
			}
		}
		
		return modifiedFiles;
		
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