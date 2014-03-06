package net.krazyweb.starmodmanager.data;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import net.krazyweb.helpers.FileHelper;
import net.krazyweb.starmodmanager.dialogue.MessageDialogue;
import net.krazyweb.starmodmanager.dialogue.MessageDialogue.MessageType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GetNewModsTask extends Task<Void> {
	
	private static final Logger log = LogManager.getLogger(GetNewModsTask.class);
	
	private Set<Path> files;

	private SettingsModelInterface settings;
	private LocalizerModelInterface localizer;
	
	private ModList modList;
	
	protected GetNewModsTask(final ModList modList) {
		
		this.modList = modList;

		settings = new SettingsFactory().getInstance();
		localizer = new LocalizerFactory().getInstance();
		
		setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(final WorkerStateEvent event) {
				modList.addMods(new ArrayList<>(files));
			}
		});
			
		setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(final WorkerStateEvent event) {
				log.error("", getException());
				MessageDialogue dialogue = new MessageDialogue(localizer.getMessage("newmodstask.error"), localizer.getMessage("newmodstask.error.title"), MessageType.ERROR, new LocalizerFactory());
				dialogue.getResult();
			}
		});
		
	}
	
	@Override
	protected Void call() throws Exception {
		
		// TODO Remove mods from database when archive has been deleted
		
		this.updateMessage("Loading Mod List");
		this.updateProgress(0.0, 1.0);
		
		files = new HashSet<>();
		
		FileHelper.listFiles(settings.getPropertyPath("modsdir"), files);
		
		Set<Path> toRemove = new HashSet<>();
		
		for (Path p : files) {
			for (Mod m : modList.getMods()) {
				if (Files.isSameFile(settings.getPropertyPath("modsdir").resolve(m.getArchiveName()), p)) {
					toRemove.add(p);
				}
			}
		}
		
		files.removeAll(toRemove);
		
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