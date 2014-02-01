package main.java.net.krazyweb.starmodmanager.controllers;

import java.util.Observable;
import java.util.Observer;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import main.java.net.krazyweb.starmodmanager.data.Database;
import main.java.net.krazyweb.starmodmanager.data.Localizer;
import main.java.net.krazyweb.starmodmanager.data.ModList;
import main.java.net.krazyweb.starmodmanager.data.Settings;
import main.java.net.krazyweb.starmodmanager.view.LoaderView;

import org.apache.log4j.Logger;

public class ApplicationLoader implements Observer {
	
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(ApplicationLoader.class);
	
	private static final double STEP_MULTIPLIER = 1.0 / 5.0;
	
	private LoaderView view;
	private ModList modList;
	
	public ApplicationLoader() {
		
		view = new LoaderView();
		view.build();

		Settings.getInstance().addObserver(this);
		Database.getInstance().addObserver(this);
		Localizer.getInstance().addObserver(this);
		
		configureLogger();
		
	}
	
	private void configureLogger() {
		
		Settings settings = Settings.getInstance();
		settings.configureLogger();
		
		setProgressProperties(settings.getProgressProperty(), settings.getMessageProperty(), 1);
		
		settings.processTask();
		
	}
	
	private void initDatabase() {
		
		Database database = Database.getInstance();
		database.initialize();

		setProgressProperties(database.getProgressProperty(), database.getMessageProperty(), 2);
		
		database.processTask();
		
	}
	
	private void loadSettings() {
		
		Settings settings = Settings.getInstance();
		settings.load();

		setProgressProperties(settings.getProgressProperty(), settings.getMessageProperty(), 3);
		
		settings.processTask();
		
	}
	
	private void initializeLocalizer() {
		
		Localizer localizer = Localizer.getInstance();
		localizer.initialize();

		setProgressProperties(localizer.getProgressProperty(), localizer.getMessageProperty(), 4);
		
		localizer.processTask();
		
	}
	
	private void loadModList() {
		
		modList = new ModList();
		modList.load();
		
		modList.addObserver(this);

		setProgressProperties(modList.getProgressProperty(), modList.getMessageProperty(), 5);
		
		modList.processTask();
		
	}
	
	private void completeLoading() {
		
		Settings.getInstance().deleteObserver(this);
		Database.getInstance().deleteObserver(this);
		Localizer.getInstance().deleteObserver(this);
		modList.deleteObserver(this);
		
		new MainViewController(modList);
		
	}

	@Override
	public void update(final Observable observable, final Object data) {
		
		if (data instanceof String) {
			
			String message = (String) data;
			
			switch (message) {
				case "loggerconfigured":
					initDatabase();
					break;
				case "databaseinitialized":
					loadSettings();
					break;
				case "settingsloaded":
					initializeLocalizer();
					break;
				case "localizerloaded":
					loadModList();
					break;
				case "modlistloaded":
					completeLoading();
					break;
			}
			
		}
		
	}
	
	private void setProgressProperties(final ReadOnlyDoubleProperty progress, final ReadOnlyStringProperty message, final int step) {
		view.getProgressBar().progressProperty().bind(progress.multiply(STEP_MULTIPLIER).add((double) (step - 1) * STEP_MULTIPLIER));
		view.getText().setText("Loading");
	}
	
}