package main.java.net.krazyweb.starmodmanager.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import main.java.net.krazyweb.helpers.Archive;
import main.java.net.krazyweb.helpers.FileHelper;
import main.java.net.krazyweb.starmodmanager.dialogue.ProgressDialogue;
import main.java.net.krazyweb.starmodmanager.view.ModListView;

import org.apache.log4j.Logger;

public class ModList {
	
	private static final Logger log = Logger.getLogger(ModList.class);
	
	private boolean locked;
	
	private final ModListView modListView;
	
	private List<Mod> mods;
	
	public ModList(final ModListView modListView) {
		
		this.modListView = modListView;
		
		//TODO Get locked status from settings.
		try {
			
			mods = Database.getModList();
			
			for (Mod mod : mods) {
				mod.setOrder(mods.indexOf(mod));
				Database.updateMod(mod);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public void addMods(final List<Path> files) {
		
		final ProgressDialogue progress = new ProgressDialogue();
		progress.start(Localizer.getMessage("modlist.addingmods"));
		
		final Task<Integer> addModsTask = new Task<Integer>() {

			@Override
			protected Integer call() throws Exception {
				
				this.updateProgress(0, files.size());
				
				for (int i = 0; i < files.size(); i++) {
					
					Path file = files.get(i);
					
					this.updateMessage(Localizer.getMessage("modlist.loadingmod") + file.getFileName());
					
					Set<Mod> modsToAdd = Mod.load(file, mods.size());
					
					if (modsToAdd != null && !modsToAdd.isEmpty()) {
						for (Mod mod : modsToAdd) {
							mods.add(mod);
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
				updateView();
			}
		});
		
		addModsTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent t) {
				progress.close();
				updateView();
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
			Database.deleteMod(mod);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		try {
			FileHelper.deleteFile(Paths.get(Settings.getModsDirectory() + File.separator + mod.getArchiveName()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		mods.remove(mod);
		
		updateView();
		
	}
	
	public void installMod(final Mod mod) {
		
		final Task<Integer> installModsTask = new Task<Integer>() {

			@Override
			protected Integer call() throws Exception {
				
				//Grab every installed mod, then separate out those with conflicts
				//Find all the conflicting files in each of those mods
				//Find all the non-conflicting files in each of those mods
				//Copy the non-conflicting files to a new patch folder
				//
				
				Archive archive = new Archive(Settings.getModsDirectory() + File.separator + mod.getArchiveName());
				archive.extract();
				archive.extractToFolder(new File(Settings.getModsInstallDirectory().toString() + File.separator + mod.getInternalName()));
				
				mod.setInstalled(true);
				
				Database.updateMod(mod);
				
				return 1;
				
			}
			
		};
		
		installModsTask.setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent t) {
				//TODO Appropriate error messages.
				log.error("Error occurred while installing mods!", installModsTask.getException());
				updateView();
			}
		});
		
		installModsTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent t) {
				updateView();
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
					FileHelper.deleteFile(Paths.get(Settings.getModsInstallDirectory().toString() + File.separator + mod.getInternalName()));
				} catch (IOException e) {
					log.error("Uninstalling Mod: " + mod.getInternalName(), e);
				}
				
				mod.setInstalled(false);
				
				Database.updateMod(mod);
				
				return 1;
				
			}
			
		};
		
		installModsTask.setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent t) {
				//TODO Appropriate error messages.
				log.error("Error occurred while uninstalling mods!", installModsTask.getException());
				updateView();
			}
		});
		
		installModsTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent t) {
				updateView();
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
			Database.updateMod(mod);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		updateView();
		
	}
	
	public void requestUpdate() {
		updateView();
	}
	
	private void updateView() {

		List<Mod> modListCopy = new ArrayList<>(mods);
		
		modListView.updateModList(modListCopy);
		
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
				Database.updateMod(m);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		//updateView();
		
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
		updateView();
	}
	
	public void unlockList() {
		locked = false;
		updateView();
	}
	
	public boolean isLocked() {
		return locked;
	}
	
	public void refreshMods() {
		try {
			Database.getModList();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}