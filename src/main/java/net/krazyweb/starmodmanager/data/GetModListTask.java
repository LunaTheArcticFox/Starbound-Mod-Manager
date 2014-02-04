package net.krazyweb.starmodmanager.data;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import net.krazyweb.helpers.FileHelper;
import net.krazyweb.starmodmanager.data.Mod.ModOrderComparator;

import org.apache.log4j.Logger;

public class GetModListTask extends Task<Void> {
	
	private static final Logger log = Logger.getLogger(GetModListTask.class);
	
	private List<Mod> mods;
	
	protected GetModListTask(final ModList modList) {
		
		setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(final WorkerStateEvent event) {
				modList.setModList(mods);
			}
		});
			
		setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(final WorkerStateEvent event) {
				log.error("", getException());
			}
		});
		
	}
	
	@Override
	protected Void call() throws Exception {
		
		// TODO Remove mods from database when archive has been deleted
		
		this.updateMessage("Loading Mod List");
		this.updateProgress(0.0, 1.0);

		List<String> modsInDatabase = Database.getInstance().getModNames();
		List<String> modNames = new ArrayList<>();
		Set<String> currentArchives = new HashSet<>();
		
		Set<Path> archives = new HashSet<>();
		Set<Path> toRemove = new HashSet<>();
		
		for (final String modData : modsInDatabase) {
			modNames.add(modData.split("\n")[0]);
			currentArchives.add(Paths.get(Settings.getInstance().getPropertyString("modsdir")).toAbsolutePath() + File.separator + modData.split("\n")[1]); //TODO Better Path manipulation
		}
		
		log.debug(modNames);
		
		FileHelper.listFiles(Settings.getInstance().getPropertyString("modsdir"), archives);
		
		for (Path path : archives) {
			if (currentArchives.contains(path.toString())) {
				toRemove.add(path);
			}
		}
		
		archives.removeAll(toRemove);
		
		int count = 1;
		int total = modNames.size() + archives.size();
		
		mods = new ArrayList<>();
		
		for (final String modName : modsInDatabase) {
			mods.add(Database.getInstance().getModByName(modName.split("\n")[0]));
			this.updateProgress((double) count, (double) total);
			count++;
		}
		
		Collections.sort(mods, new ModOrderComparator());
		
		for (Path path : archives) {
			
			Set<Mod> tempMods = Mod.load(path, mods.size());
			
			if (tempMods == null || tempMods.isEmpty()) {
				continue;
			}
			
			for (Mod mod : tempMods) {
				mods.add(mod);
			}

			this.updateProgress((double) count, (double) total);
			count++;
			
		}
		
		for (Mod mod : mods) {
			mod.setOrder(mods.indexOf(mod));
			Database.updateMod(mod);
		}
		
		this.updateProgress(1.0, 1.0);
		
		/*
		 * This gives the UI thread time to visually update to 100%
		 * before the dialogue closes without adding noticeable time
		 * to the loading process.
		 */
		Thread.sleep(15);
		
		return null;
		
	}
	
}