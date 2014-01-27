package main.java.net.krazyweb.starmodmanager.view;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

public class NavBarButtons extends HBox {
	
	private final Button quickBackupButton;
	private final Button lockButton;
	private final Button refreshButton;
	private final Button expandButton;
	
	private final MainView mainView;
	
	public NavBarButtons(final MainView m) {
		
		mainView = m;
		
		quickBackupButton = new Button("Backup");
		quickBackupButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				//TODO Functionality
			}
		});
		
		lockButton = new Button("Lock");
		lockButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				mainView.toggleLockModList();
			}
		});
		
		refreshButton = new Button("Refresh");
		refreshButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				//TODO Functionality
			}
		});
		
		expandButton = new Button("Expand");
		expandButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				//TODO Functionality
			}
		});
		
		getChildren().addAll(
			quickBackupButton,
			lockButton,
			refreshButton,
			expandButton
		);
		
	}
	
}