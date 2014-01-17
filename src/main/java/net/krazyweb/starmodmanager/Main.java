package main.java.net.krazyweb.starmodmanager;

import java.io.File;
import java.sql.SQLException;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.apache.log4j.Logger;

public class Main extends Application {
	
	private static final Logger log = Logger.getLogger(Main.class);
	
	private ModList modList;
	
	private Stage primaryStage;
	
	@Override
	public void start(Stage primaryStage) {
		
		this.primaryStage = primaryStage;
		
		final ProgressDialogue startup = new ProgressDialogue();
		startup.start(primaryStage);
		
		//TODO Verify file write permissions and capability
		
		Task<Integer> dataThread = new Task<Integer>() {

			@Override
			protected Integer call() throws Exception {
				
				new Thread() {
					@Override
					public void run() {
						Settings.initialize();
					}
				}.start();
				
				while (!Settings.isComplete()) {
					this.updateProgress(Settings.getProgress() / 2.0, 1.0);
					this.updateMessage(Settings.getMessage());
				}
				
				this.updateProgress(50, 100);
				this.updateMessage("Initializing Database...");
				
				try {
					Database.initialize();
				} catch (SQLException e) {
					//TODO Notify user of failed database connection
					e.printStackTrace();
				}

				this.updateMessage("Complete!");
				this.updateProgress(100, 100);
				
				return 1;
				
			}
		};
		
		dataThread.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent t) {
				buildMainUI();
			}
		});
		
		startup.bar.progressProperty().bind(dataThread.progressProperty());
		startup.text.textProperty().bind(dataThread.messageProperty());
		
		Thread t = new Thread(dataThread);
		t.setName("Initialization Thread");
		t.setDaemon(true);
		t.start();
		
	}
	
	public void buildMainUI() {
		
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
