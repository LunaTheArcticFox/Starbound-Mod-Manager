package net.krazyweb.starmodmanager.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Set;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import net.krazyweb.helpers.Archive;
import net.krazyweb.helpers.FileHelper;
import net.krazyweb.starmodmanager.dialogue.ProgressDialogue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModList extends Observable implements Progressable {
	
	private static final Logger log = LogManager.getLogger(ModList.class);
	
	private SettingsModelInterface settings;
	
	private boolean locked;
	
	private List<Mod> mods;

	private Task<?> task;
	
	private ReadOnlyDoubleProperty progress;
	private ReadOnlyStringProperty message;
	
	public ModList(final SettingsModelFactory settingsFactory) {
		settings = settingsFactory.getInstance();
	}
	
	public void load() {
		
		GetModListTask t = new GetModListTask(this);
		
		this.setProgress(t.progressProperty());
		this.setMessage(t.messageProperty());
		
		this.task = t;
		
	}
	
	public void addMods(final List<Path> files) {
		
		final ProgressDialogue progress = new ProgressDialogue();
		progress.start(Localizer.getInstance().getMessage("modlist.addingmods"));
		
		final Set<String> currentMods = new HashSet<>();
		for (Mod mod : mods) {
			currentMods.add(mod.getInternalName());
		}
		
		final List<Mod> newMods = new ArrayList<>();
		
		final Task<Integer> addModsTask = new Task<Integer>() {

			@Override
			protected Integer call() throws Exception {
				
				this.updateProgress(0, files.size());
				
				for (int i = 0; i < files.size(); i++) {
					
					Path file = files.get(i);
					
					this.updateMessage(Localizer.getInstance().getMessage("modlist.loadingmod") + file.getFileName());
					
					Set<Mod> modsToAdd = Mod.load(file, mods.size());
					
					if (modsToAdd != null && !modsToAdd.isEmpty()) {
						for (Mod mod : modsToAdd) {
							if (!currentMods.contains(mod.getInternalName())) {
								mods.add(mod);
								newMods.add(mod);
							} else {
								//TODO Notify user of mod existence
								log.debug("Mod already exists, skipping: " + file);
							}
						}
					}
					
					this.updateProgress(i, files.size());
					
				}
				
				return 1;
				
			}
			
		};
		
		addModsTask.setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent t) {
				//TODO Appropriate error messages.
				log.error("Error occurred while getting mods!", addModsTask.getException());
				progress.close();
			}
		});
		
		addModsTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent t) {
				progress.close();
				for (Mod mod : newMods) {
					setChanged();
					notifyObservers(new Object[] { "modadded", mod });
				}
			}
		});
		
		progress.bar.progressProperty().bind(addModsTask.progressProperty());
		progress.text.textProperty().bind(addModsTask.messageProperty());
		
		Thread t = new Thread(addModsTask);
		t.setName("Add Mods Thread");
		t.setDaemon(true);
		t.start();
		
	}
	
	public void deleteMod(final Mod mod) {
		
		if (mod.isInstalled()) {
			uninstallMod(mod);
		}
		
		try {
			HyperSQLDatabase.deleteMod(mod);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		try {
			FileHelper.deleteFile(Paths.get(settings.getPropertyString("modsdir") + File.separator + mod.getArchiveName())); //TODO Better Path manipulation
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		mods.remove(mod);
		
	}
	
	public void installMod(final Mod mod) {
		
		//TODO Update to use the modlist wide task for progress bars
		
		final Task<Integer> installModsTask = new Task<Integer>() {

			@Override
			protected Integer call() throws Exception {
				
				//Grab every installed mod, then separate out those with conflicts
				//Find all the conflicting files in each of those mods
				//Find all the non-conflicting files in each of those mods
				//Copy the non-conflicting files to a new patch folder
				//
				
				Archive archive = new Archive(settings.getPropertyString("modsdir") + File.separator + mod.getArchiveName()); //TODO Better Path manipulation
				archive.extract();
				archive.extractToFolder(new File(settings.getPropertyString("starboundpath") + File.separator + "mods" + File.separator + mod.getInternalName())); //TODO Better Path manipulation
				
				return 1;
				
			}
			
		};
		
		installModsTask.setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(final WorkerStateEvent t) {
				//TODO Appropriate error messages.
				log.error("Error occurred while installing mods!", installModsTask.getException());
			}
		});
		
		installModsTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(final WorkerStateEvent t) {
				mod.setInstalled(true);
				try {
					HyperSQLDatabase.updateMod(mod);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					log.error("", e);
				}
			}
		});
		
		Thread t = new Thread(installModsTask);
		t.setName("Install Mods Thread");
		t.setDaemon(true);
		t.start();
		
	}
	
	public void uninstallMod(final Mod mod) {
		
		final Task<Integer> installModsTask = new Task<Integer>() {

			@Override
			protected Integer call() throws Exception {
				
				log.info("Uninstalling mod: " + mod.getInternalName());

				try {
					log.debug("Deleting from: " + settings.getPropertyPath("starboundpath").resolve("mods").resolve(mod.getInternalName()));
					FileHelper.deleteFile(
						settings.getPropertyPath("starboundpath").resolve("mods").resolve(mod.getInternalName())
					);
				} catch (IOException e) {
					log.error("Uninstalling Mod: " + mod.getInternalName(), e);
				}
				
				return 1;
				
			}
			
		};
		
		installModsTask.setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(final WorkerStateEvent t) {
				//TODO Appropriate error messages.
				log.error("Error occurred while uninstalling mods!", installModsTask.getException());
				//updateView();
			}
		});
		
		installModsTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(final WorkerStateEvent t) {
				mod.setInstalled(false);
				try {
					HyperSQLDatabase.updateMod(mod);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					log.error("", e);
				}
			}
		});
		
		Thread t = new Thread(installModsTask);
		t.setName("Uninstall Mods Thread");
		t.setDaemon(true);
		t.start();
		
	}
	
	public void hideMod(final Mod mod) {
		
		mod.setHidden(true);
		try {
			HyperSQLDatabase.updateMod(mod);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	/*
	 * See: http://stackoverflow.com/questions/4938626/moving-items-around-in-an-arraylist
	 */
	public void moveMod(final Mod mod, final int amount) {
		
		if (locked) {
			log.debug("Mod list locked; cannot move mod: " + mod.getInternalName());
			return;
		}
		
		log.debug("Performing rotation, results:");
		
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
			log.debug("  [" + m.getOrder() + "] " + m.getInternalName());
			try {
				HyperSQLDatabase.updateMod(m);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public List<Mod> getMods() {
		List<Mod> modListCopy = new ArrayList<>(mods);
		return modListCopy;
	}
	
	public int indexOf(final Mod mod) {
		return mods.indexOf(mod);
	}
	
	public void lockList() {
		locked = true;
	}
	
	public void unlockList() {
		locked = false;
	}
	
	public boolean isLocked() {
		return locked;
	}
	
	public void refreshMods() {
		/*try {
			Database.getModList();
		} catch (SQLException e) {
			e.printStackTrace();
		}*/
	}
	
	protected void setModList(final List<Mod> list) {
		this.mods = list;
		setChanged();
		this.notifyObservers("modlistupdated");
	}
	
	private void setProgress(final ReadOnlyDoubleProperty progress) {
		this.progress = progress;
	}
	
	private void setMessage(final ReadOnlyStringProperty message) {
		this.message = message; 
	}

	@Override
	public ReadOnlyDoubleProperty getProgressProperty() {
		return progress;
	}

	@Override
	public ReadOnlyStringProperty getMessageProperty() {
		return message;
	}

	@Override
	public void processTask() {
		Thread thread = new Thread(task);
		thread.setName("Settings Task Thread");
		thread.setDaemon(true);
		thread.start();
	}
	
}