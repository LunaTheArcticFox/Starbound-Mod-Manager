package main.java.net.krazyweb.starmodmanager.view;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

public class Tabs extends HBox {
	
	private Button modListButton;
	private Button backupListButton;
	private Button settingsButton;
	private Button aboutButton;
	
	private MainView mainView;
	
	public Tabs(final MainView m) {
		
		mainView = m;
		
		modListButton = new Button("Mods");
		modListButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				mainView.showModList();
			}
		});
		
		backupListButton = new Button("Backups");
		backupListButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				mainView.showBackupList();
			}
		});
		
		settingsButton = new Button("Settings");
		settingsButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				mainView.showSettings();
			}
		});
		
		aboutButton = new Button("About");
		aboutButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
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