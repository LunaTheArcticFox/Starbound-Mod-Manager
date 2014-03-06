package net.krazyweb.starmodmanager.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
import net.krazyweb.starmodmanager.dialogue.MessageDialogue;
import net.krazyweb.starmodmanager.dialogue.MessageDialogue.MessageType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GetModListTask extends Task<Void> {
	
	private static final Logger log = LogManager.getLogger(GetModListTask.class);
	
	private List<Mod> mods;

	private SettingsModelInterface settings;
	private LocalizerModelInterface localizer;
	private DatabaseModelInterface database;
	
	private boolean recoverableErrorOccurred = false;
	
	protected GetModListTask(final ModList modList) {

		settings = new SettingsFactory().getInstance();
		localizer = new LocalizerFactory().getInstance();
		database = new DatabaseFactory().getInstance();
		
		setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(final WorkerStateEvent event) {
				
				log.debug("Task Succeeded");
				
				if (recoverableErrorOccurred) {
					MessageDialogue dialogue = new MessageDialogue(localizer.getMessage("modlisttask.recoverableerror"), localizer.getMessage("modlisttask.recoverableerror.title"), MessageType.ERROR, new LocalizerFactory());
					dialogue.getResult();
				}
				
				modList.setModList(mods);
				
			}
		});
			
		setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(final WorkerStateEvent event) {
				log.error("", getException());
				MessageDialogue dialogue = new MessageDialogue(localizer.getMessage("modlisttask.error"), localizer.getMessage("modlisttask.error.title"), MessageType.ERROR, new LocalizerFactory());
				dialogue.getResult();
			}
		});
		
	}
	
	@Override
	protected Void call() throws Exception {
		
		// TODO Remove mods from database when archive has been deleted
		
		this.updateMessage("Loading Mod List");
		this.updateProgress(0.0, 1.0);

		List<String> modsInDatabase = database.getModNames();
		List<String> modNames = new ArrayList<>();
		Set<String> currentArchives = new HashSet<>();
		
		Set<Path> archives = new HashSet<>();
		Set<Path> toRemove = new HashSet<>();
		
		for (final String modData : modsInDatabase) {
			modNames.add(modData.split("\n")[0]);
			currentArchives.add(settings.getPropertyPath("modsdir").resolve(modData.split("\n")[1]).toAbsolutePath().toString()); //TODO Better Path manipulation
		}
		
		log.debug(modNames);
		
		FileHelper.listFiles(settings.getPropertyString("modsdir"), archives); //TODO investigate using path
		
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
			Mod tempMod = null;
			if ((tempMod = database.getModByName(modName.split("\n")[0])) != null) {
				if (!tempMod.isHidden()) {
					mods.add(tempMod);
				}
			}
			this.updateProgress((double) count, (double) total);
			count++;
		}
		
		Collections.sort(mods, new ModOrderComparator());
		
		toRemove = new HashSet<>();
		
		for (Path path : archives) {
			
			Set<Mod> tempMods = Mod.load(path, mods.size(), new SettingsFactory(), new DatabaseFactory(), new LocalizerFactory());
			
			if (tempMods == null || tempMods.isEmpty()) {
				recoverableErrorOccurred = true;
				continue;
			}
			
			for (Mod mod : tempMods) {
				
				mods.add(mod);
				
				for (Path path2 : archives) {
					if (Files.isSameFile(settings.getPropertyPath("modsdir").resolve(mod.getArchiveName()), path2)) {
						toRemove.add(path2);
					} else {
						log.debug("File is used by mod manager, will not delete: '{}' = '{}'", settings.getPropertyPath("modsdir").resolve(mod.getArchiveName()), path2);
					}
				}
				
			}

			this.updateProgress((double) count, (double) total);
			count++;
			
		}
		
		archives.removeAll(toRemove);
		
		for (Path path : archives) {
			log.debug("File is not used by mod manager, deleting: '{}'", path);
			try {
				FileHelper.deleteFile(path);
			} catch (final IOException e) {
				log.error("", e);
			}
		}
		
		for (Mod mod : mods) {
			mod.setOrder(mods.indexOf(mod));
			database.updateMod(mod);
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