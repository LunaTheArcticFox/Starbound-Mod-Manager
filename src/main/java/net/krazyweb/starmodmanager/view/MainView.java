package main.java.net.krazyweb.starmodmanager.view;

import java.sql.SQLException;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import main.java.net.krazyweb.starmodmanager.data.Database;
import main.java.net.krazyweb.starmodmanager.data.Settings;

import org.apache.log4j.Logger;

public class MainView extends Application {
	
	private static final Logger log = Logger.getLogger(MainView.class);
	
	private Stage primaryStage;
	
	private ModListView modListView;
	private BackupListView backupListView;
	private SettingsView settingsView;
	private AboutView aboutView;
	
	private ScrollPane mainContentPane;
	
	/*
	 * The MainView portion of the application will manage sub-views and application-level
	 * commands. The top bar with the name and version does not need its own class, but
	 * the buttons and tabs should each be their own. Additionally, this should implement
	 * showX(), where X is the tab's name, then update a scrollpane to use that new content.
	 * This should also handle application initialization.
	 * 
	 * TODO:
	 * 	- ModListView
	 *  - BackupListView
	 *  - SettingsView
	 *  - AboutView
	 *  
	 *  The above "view"s should extend a node so they can be inserted directly into the pane. 
	 */
	
	@Override
	public void start(Stage primaryStage) {
		
		this.primaryStage = primaryStage;
		
		initialize();
		
	}
	
	private void initialize() {
		
		final ProgressDialogue startup = new ProgressDialogue();
		startup.start(primaryStage);
		
		//TODO Verify file write permissions and capability
		
		Task<Integer> dataThread = new Task<Integer>() {

			@Override
			protected Integer call() throws Exception {
				
				new Thread() {
					@Override
					public void run() {
						this.setName("Settings Initialization");
						Settings.initialize();
						
					}
				}.start();
				
				new Thread() {
					@Override
					public void run() {
						this.setName("Database Initialization");
						try {
							Database.initialize();
						} catch (SQLException e) {
							//TODO Notify user of failed database connection
							e.printStackTrace();
						}
					}
				}.start();
				
				this.updateMessage("Loading...");
				
				while (!Settings.isComplete() || !Database.isComplete()) {
					this.updateProgress((Settings.getProgress() / 2.0) + (Database.getProgress() / 2.0), 1.0);
					//this.updateMessage(Settings.getMessage());
				}
				
				this.updateMessage("Complete!");
				this.updateProgress(1.0, 1.0);
				
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
	
	protected void buildMainUI() {
			
		VBox root = new VBox();
		
		AnchorPane topBar = new AnchorPane();
		
		Text appName = new Text("Starbound Mod Manager");
		Text versionName = new Text(Settings.getVersion());
		AnchorPane.setTopAnchor(appName, 21.0);
		AnchorPane.setLeftAnchor(appName, 19.0);
		AnchorPane.setRightAnchor(versionName, 19.0);
		AnchorPane.setTopAnchor(versionName, 21.0);
		
		topBar.getChildren().addAll(appName, versionName);

		root.getChildren().add(topBar);
		
		AnchorPane tabsBar = new AnchorPane();
		Tabs pageTabs = new Tabs(this);
		HBox buttons = new HBox();
		
		buttons.getChildren().addAll(
				new Text("1"),
				new Text("2"),
				new Text("3"),
				new Text("4")
				);
		
		AnchorPane.setLeftAnchor(pageTabs, 19.0);
		AnchorPane.setTopAnchor(pageTabs, 19.0);
		
		AnchorPane.setRightAnchor(buttons, 19.0);
		AnchorPane.setTopAnchor(buttons, 19.0);
		
		tabsBar.getChildren().addAll(pageTabs, buttons);
		
		modListView = new ModListView(this);
		
		mainContentPane = new ScrollPane();
		mainContentPane.setFitToHeight(true);
		mainContentPane.setFitToWidth(true);
		
		showModList();
		
		VBox.setVgrow(mainContentPane, Priority.ALWAYS);
		
		root.getChildren().add(tabsBar);
		root.getChildren().add(mainContentPane);
		root.prefHeightProperty().bind(primaryStage.heightProperty());
		
		Scene scene = new Scene(root, 683, 700);
		primaryStage.setMinWidth(683);
		primaryStage.setMinHeight(700);
		primaryStage.setScene(scene);
		primaryStage.show();
		
	}
	
	protected void showModList() {
		log.debug("Setting view to mod list.");
		mainContentPane.setContent(modListView);
	}
	
	protected void showBackupList() {
		log.debug("Setting view to backup list.");
		//TODO Actual content
		mainContentPane.setContent(new VBox());
	}
	
	protected void showSettings() {
		log.debug("Setting view to settings.");
		//TODO Actual content
		mainContentPane.setContent(new VBox());
	}
	
	protected void showAbout() {
		log.debug("Setting view to about.");
		//TODO Actual content
		mainContentPane.setContent(new VBox());
	}
	
	protected Stage getStage() {
		return primaryStage;
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
}
