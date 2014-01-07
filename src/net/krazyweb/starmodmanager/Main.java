package net.krazyweb.starmodmanager;

import java.io.File;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Main extends Application {
	
	private ModList modList;

	@Override
	public void start(Stage primaryStage) throws Exception {
		
		Database.initialize();
		Settings.initialize();
		
		modList = new ModList();
		
		try {
			
			BorderPane root = new BorderPane();
			Scene scene = new Scene(root, 400, 400);
			primaryStage.setScene(scene);
			primaryStage.show();

			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Select Mod File");
			
			File result = fileChooser.showOpenDialog(primaryStage);
			
			modList.addMod(result);
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
}
