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
import java.util.Set;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import net.krazyweb.helpers.Archive;
import net.krazyweb.helpers.FileHelper;
import net.krazyweb.starmodmanager.dialogue.ProgressDialogue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedMessage;

public class ModList implements ModListModelInterface {
	
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
	public void addMods(final List<Path> files) {
		
		final ProgressDialogue progress = new ProgressDialogue();
		progress.start(localizer.getMessage("modlist.addingmods"));
		
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
					
					this.updateMessage(localizer.getMessage("modlist.loadingmod") + file.getFileName());
					
					Set<Mod> modsToAdd = Mod.load(file, mods.size(), new SettingsFactory(), new DatabaseFactory(), new LocalizerFactory());
					
					if (modsToAdd != null && !modsToAdd.isEmpty()) {
						for (Mod mod : modsToAdd) {
							if (!currentMods.contains(mod.getInternalName())) {
								mods.add(mod);
								newMods.add(mod);
							} else {
								//TODO Notify user of mod existence
								log.debug("Mod already exists, skipping: {}", file);
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

	@Override
	public void deleteMod(final Mod mod) {
		
		if (mod.isInstalled()) {
			uninstallMod(mod);
		}
		
		try {
			database.deleteMod(mod);
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
	
	@Override
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
					database.updateMod(mod);
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
	
	@Override
	public void uninstallMod(final Mod mod) {
		
		final Task<Integer> installModsTask = new Task<Integer>() {

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
					database.updateMod(mod);
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
	
	@Override
	public void hideMod(final Mod mod) {
		
		mod.setHidden(true);
		try {
			database.updateMod(mod);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void moveMod(final Mod mod, final int amount) {
		
		if (locked) {
			log.debug("Mod list locked; cannot move mod: {}", mod.getInternalName());
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
			log.debug("  [{}] {}", m.getOrder(), m.getInternalName());
			try {
				database.updateMod(m);
			} catch (SQLException e) {
				e.printStackTrace();
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
		this.mods = list;
		notifyObservers("modlistupdated");
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