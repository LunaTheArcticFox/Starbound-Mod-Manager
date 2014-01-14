package main.java.net.krazyweb.starmodmanager;

import java.io.File;
import java.sql.SQLException;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.apache.log4j.Logger;

public class Main extends Application {
	
	private static final Logger log = Logger.getLogger(Main.class);;
	
	private ModList modList;

	@Override
	public void start(Stage primaryStage) {
		
		//TODO Verify file write permissions and capability
		
		Settings.initialize();
		
		try {
			Database.initialize();
		} catch (SQLException e) {
			//TODO Notify user of failed database connection
			e.printStackTrace();
		}
		
		modList = new ModList();
		//modList.moveMod("modname", 10); //Shifts 10 positions up (towards index 0)
		//modList.moveMod("modname", -3); //Shifts 3 positions down (towards list.size())
		
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
