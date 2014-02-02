package net.krazyweb.starmodmanager.view;

import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.krazyweb.starmodmanager.ModManager;
import net.krazyweb.starmodmanager.data.Localizer.Language;
import net.krazyweb.starmodmanager.data.Settings;

public class SettingsViewController {
	
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
	
	protected void languageChanged(final Language language) {
		Settings.getInstance().setProperty("locale", language.getLocale());
	}
	
}