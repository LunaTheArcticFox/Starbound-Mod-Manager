package net.krazyweb.starmodmanager.view;

import org.apache.log4j.Logger;

import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.krazyweb.starmodmanager.ModManager;
import net.krazyweb.starmodmanager.data.Localizer.Language;
import net.krazyweb.starmodmanager.data.Settings;

public class SettingsViewController {
	
	private static final Logger log = Logger.getLogger(SettingsViewController.class);
	
	private SettingsView view;
	
	protected SettingsViewController(final SettingsView view) {
		
		this.view = view;
		this.view.build();
		
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
		Settings.getInstance().setProperty("locale", language.getLocale());
	}
	
	protected void checkVersionChanged(final boolean checked) {
		
	}
	
	protected void backupSavesOnLaunchChanged(final boolean checked) {
		
	}
	
	protected void confirmButtonDelayChanged(final String value) {
		Settings.getInstance().setProperty("confirmdelay", value);
	}
	
}