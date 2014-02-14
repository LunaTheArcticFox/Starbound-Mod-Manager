package net.krazyweb.starmodmanager.view;

import java.awt.Desktop;
import java.io.IOException;

import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.krazyweb.starmodmanager.ModManager;
import net.krazyweb.starmodmanager.data.LocalizerModelInterface.Language;
import net.krazyweb.starmodmanager.data.SettingsFactory;
import net.krazyweb.starmodmanager.data.SettingsModelInterface;

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
	
	protected void gamePathChanged(final String path) {
		//TODO Validate the path
		log.debug(path);
	}
	
	protected void modsPathChanged(final String path) {
		//TODO Validate the path
		log.debug(path);
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
		
		log.info("Opening log file.");
		
		try {
			Desktop.getDesktop().open(settings.getPropertyPath("logpath").toFile());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("", e);
		}
		
	}
	
}