package main.java.net.krazyweb.starmodmanager.view;

import main.java.net.krazyweb.starmodmanager.data.Localizer.Language;
import main.java.net.krazyweb.starmodmanager.data.Settings;

public class SettingsViewController {
	
	private SettingsView view;
	
	protected SettingsViewController(final SettingsView view) {
		this.view = view;
		view.build();
	}
	
	protected void languageChanged(final Language language) {
		Settings.getInstance().setProperty("locale", language.getLocale());
	}
	
}