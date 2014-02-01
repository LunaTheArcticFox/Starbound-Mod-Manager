package main.java.net.krazyweb.starmodmanager.controllers;

import main.java.net.krazyweb.starmodmanager.data.Localizer;
import main.java.net.krazyweb.starmodmanager.data.Settings;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ModManager extends Application {
	
	private static Stage primary;
	
	@Override
	public void start(final Stage primaryStage) throws Exception {
		primary = primaryStage;
		primaryStage.initStyle(StageStyle.DECORATED);
		primaryStage.setMinWidth(683);
		primaryStage.setMinHeight(700);
		new ApplicationLoader();
	}
	
	public static Stage getPrimaryStage() {
		return primary;
	}
	
	public static void main(final String[] args) {
		launch(args);
	}

}
