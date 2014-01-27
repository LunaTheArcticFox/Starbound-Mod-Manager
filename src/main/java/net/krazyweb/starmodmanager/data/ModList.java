package main.java.net.krazyweb.starmodmanager.data;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import main.java.net.krazyweb.starmodmanager.view.ModListView;
import main.java.net.krazyweb.starmodmanager.view.ProgressDialogue;

import org.apache.log4j.Logger;

public class ModList {
	
	private static final Logger log = Logger.getLogger(ModList.class);
	
	private boolean locked;
	
	private ModListView modListView;
	
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
		progress.start(new Stage());
		
		final Task<Integer> addModsTask = new Task<Integer>() {

			@Override
			protected Integer call() throws Exception {
				
				this.updateProgress(0, files.size());
				
				for (int i = 0; i < files.size(); i++) {
					
					Path file = files.get(i);
					
					this.updateMessage("Loading Mod: " + file.getFileName());
					
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
		
		try {
			Database.deleteMod(mod);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		mods.remove(mod);
		
	}
	
	public void installMod(final Mod mod) {
		
		final Task<Integer> installModsTask = new Task<Integer>() {

			@Override
			protected Integer call() throws Exception {
				
				mod.install();
				
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
				
				mod.uninstall();
				
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
		
		log.debug("=============\nPerforming rotation, results:");
		
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
			log.debug("[" + m.getOrder() + "] \t" + m.getInternalName());
			try {
				Database.updateMod(m);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public void lockList() {
		locked = true;
	}
	
	public void unlockList() {
		locked = false;
	}
	
	public void refreshMods() {
		try {
			Database.getModList();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}