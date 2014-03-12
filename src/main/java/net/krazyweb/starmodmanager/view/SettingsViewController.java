package net.krazyweb.starmodmanager.view;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.krazyweb.helpers.FileHelper;
import net.krazyweb.starmodmanager.ModManager;
import net.krazyweb.starmodmanager.data.LocalizerFactory;
import net.krazyweb.starmodmanager.data.LocalizerModelInterface;
import net.krazyweb.starmodmanager.data.LocalizerModelInterface.Language;
import net.krazyweb.starmodmanager.data.SettingsFactory;
import net.krazyweb.starmodmanager.data.SettingsModelInterface;
import net.krazyweb.starmodmanager.dialogue.MessageDialogue;
import net.krazyweb.starmodmanager.dialogue.MessageDialogue.MessageType;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SettingsViewController {
	
	private static final Logger log = LogManager.getLogger(SettingsViewController.class);
	
	private SettingsView view;
	
	private SettingsModelInterface settings;
	
	protected SettingsViewController(final SettingsView view) {
		
		this.view = view;
		this.view.build();
		
		settings = new SettingsFactory().getInstance();
		
		/*
		 * The following essentially pre-renders the view,
		 * preventing delay when adding it to the main window
		 * for the first time. I haven't found a better way
		 * around this problem yet. If this isn't done, it
		 * takes a noticeable amount of time to load the view
		 * on the first click, which is jarring and should be
		 * avoided.
		 */
		Stage stage = new Stage();
		Scene scene = new Scene((VBox) this.view.getContent());
		stage.setScene(scene);
		stage.setOpacity(0);
		stage.initStyle(StageStyle.UTILITY);
		stage.show();
		ModManager.getPrimaryStage().toFront();
		stage.close();
		
	}
	
	protected void openFileBrowser(final TextField target) {
		
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setInitialDirectory(Paths.get(target.textProperty().get()).toFile());
		
		File output = chooser.showDialog(new Stage());
		
		if (output != null) {
			target.setText(Paths.get(output.getAbsolutePath()).toAbsolutePath().toString());
			target.requestFocus();
			target.getParent().requestFocus();
		}
		
	}
	
	protected void gamePathChanged(final String path) {
		settings.setProperty("starboundpath", path);
		//TODO Validate the path
		log.debug(path);
		//TODO copy installed mods to new folder
	}
	
	protected void modsPathChanged(final String path) {

		Set<Path> paths = new HashSet<>();
		FileHelper.listFiles(settings.getPropertyPath("modsdir"), paths);
		Path oldPath = settings.getPropertyPath("modsdir");
		
		Path newPath = Paths.get("").toAbsolutePath().relativize(Paths.get(path));
		
		try {
			Files.createDirectories(newPath);
		} catch (final IOException e) {
			log.error("", e);
		}
		
		settings.setProperty("modsdir", newPath);
		//TODO Validate the path
		log.debug(newPath);
		
		for (Path p : paths) {
			
			FileHelper.copyFile(p, settings.getPropertyPath("modsdir").resolve(p.getFileName()));
			
			try {
				FileHelper.deleteFile(p);
			} catch (final IOException e) {
				log.error("", e);
			}
			
		}
		
		Set<Path> filesLeftInOldPath = new HashSet<>();
		FileHelper.listFiles(oldPath, filesLeftInOldPath);
		
		Set<Path> toRemove = new HashSet<>();
		
		for (Path p : filesLeftInOldPath) {
			log.trace("Checking path {} against {}", p, newPath);
			try {
				if (Files.isSameFile(p, newPath)) {
					log.debug("Old path contains new path, will not delete.");
					return;
				}
			} catch (final IOException e) {
				log.error("", e);
			}
		}
		
		for (Path p : filesLeftInOldPath) {
			if (Files.isDirectory(p)) {
				toRemove.add(p);
			}
		}
		
		filesLeftInOldPath.removeAll(toRemove);
		
		for (Path p : filesLeftInOldPath) {
			log.debug("File left in old path: {}", p);
		}
		
		if (filesLeftInOldPath.size() == 0) {
			try {
				if (Files.exists(oldPath)) {
					FileHelper.deleteFile(oldPath);
				}
			} catch (final IOException e) {
				log.error("", e);
			}
		} else {
			log.debug("Files are present in the old path. Will not delete.");
		}
		
	}
	
	protected void languageChanged(final Language language) {
		settings.setProperty("locale", language.getLocale());
	}
	
	protected void loggerLevelChanged(final Level level) {
		settings.setProperty("loggerlevel", level);
		settings.setLoggerLevel(level);
	}
	
	protected void checkVersionChanged(final boolean checked) {
		settings.setProperty("checkversiononlaunch", checked);
	}
	
	protected void backupSavesOnLaunchChanged(final boolean checked) {
		settings.setProperty("backuponlaunch", checked);
	}
	
	protected void confirmButtonDelayChanged(final String value) {
		settings.setProperty("confirmdelay", value);
	}
	
	protected void openLog() {
		
		LocalizerModelInterface localizer = new LocalizerFactory().getInstance();
		
		log.info("Opening log file.");
		
		try {
			Desktop.getDesktop().open(settings.getPropertyPath("logpath").toFile());
		} catch (final IOException e) {
			log.error("", e);
			MessageDialogue dialogue = new MessageDialogue(localizer.getMessage("settingsviewcontroller.logerror"), localizer.getMessage("settingsviewcontroller.logerror.title"), MessageType.ERROR, new LocalizerFactory());
			dialogue.getResult();
		}
		
	}
	
}