package main.java.net.krazyweb.starmodmanager.view;

import main.java.net.krazyweb.starmodmanager.data.Localizer;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

public class NavBarTabs extends HBox {
	
	private final Button modListButton;
	private final Button backupListButton;
	private final Button settingsButton;
	private final Button aboutButton;
	
	private final MainView mainView;
	
	public NavBarTabs(final MainView m) {
		
		mainView = m;
		
		modListButton = new Button(Localizer.getMessage("navbartabs.mods"));
		modListButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				mainView.showModList();
			}
		});
		
		backupListButton = new Button(Localizer.getMessage("navbartabs.backups"));
		backupListButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				mainView.showBackupList();
			}
		});
		
		settingsButton = new Button(Localizer.getMessage("navbartabs.settings"));
		settingsButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				mainView.showSettings();
			}
		});
		
		aboutButton = new Button(Localizer.getMessage("navbartabs.about"));
		aboutButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				mainView.showAbout();
			}
		});
		
		getChildren().addAll(
			modListButton,
			backupListButton,
			settingsButton,
			aboutButton
		);
		
	}
	
}