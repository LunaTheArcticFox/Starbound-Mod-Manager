package main.java.net.krazyweb.starmodmanager.view;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import main.java.net.krazyweb.helpers.FileHelper;
import main.java.net.krazyweb.starmodmanager.data.Database;
import main.java.net.krazyweb.starmodmanager.data.Localizer;
import main.java.net.krazyweb.starmodmanager.data.Settings;
import main.java.net.krazyweb.starmodmanager.dialogue.ConfirmDialogue;
import main.java.net.krazyweb.starmodmanager.dialogue.MessageDialogue;
import main.java.net.krazyweb.starmodmanager.dialogue.MessageDialogue.MessageType;
import main.java.net.krazyweb.starmodmanager.dialogue.ProgressDialogue;

import org.apache.log4j.Logger;

public class MainView extends Application {
	
	private static final Logger log = Logger.getLogger(MainView.class);
	
	private Stage primaryStage;
	
	private ModListView modListView;
	private BackupListView backupListView;
	private SettingsView settingsView;
	private AboutView aboutView;
	
	private ScrollPane mainContentPane;
	
	private boolean dragOver = false;
	
	@Override
	public void start(final Stage primaryStage) {
		
		this.primaryStage = primaryStage;
		
		initialize();
		
	}
	
	private void initialize() {
		
		/*
		 * Currently there's no good way that I can think of to localize the initial
		 * loading progress dialogue. If someone thinks up a way, please add it.
		 */
		final ProgressDialogue startup = new ProgressDialogue();
		startup.start("Starbound Mod Manager - Loading");
		
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
							log.error("", e);
						}
					}
				}.start();
				
				this.updateMessage("Initializing...");
				
				while (!Settings.isComplete() || !Database.isComplete()) {
					this.updateProgress((Settings.getProgress() / 2.0) + (Database.getProgress() / 2.0), 1.0);
				}

				Localizer.initialize();
				
				this.updateMessage("Complete!");
				this.updateProgress(1.0, 1.0);
				
				return 1;
				
			}
		};
		
		dataThread.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent t) {
				startup.close();
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
		
		/* 
		 * TODO Add this to the progress bar for loading the program.
		 * This will require lots of refactoring
		 * This method was getting way too long anyway
		 */
		
		final VBox root = new VBox();
		
		AnchorPane topBar = new AnchorPane();
		
		Text appName = new Text(Localizer.getMessage("appName"));
		Text versionName = new Text(Settings.getVersion());
		AnchorPane.setTopAnchor(appName, 21.0);
		AnchorPane.setLeftAnchor(appName, 19.0);
		AnchorPane.setRightAnchor(versionName, 19.0);
		AnchorPane.setTopAnchor(versionName, 21.0);
		
		topBar.getChildren().addAll(appName, versionName);

		root.getChildren().add(topBar);
		
		AnchorPane tabsBar = new AnchorPane();
		NavBarTabs pageTabs = new NavBarTabs(this);
		HBox buttons = new HBox();
		
		buttons.getChildren().add(new NavBarButtons(this));
		
		AnchorPane.setLeftAnchor(pageTabs, 19.0);
		AnchorPane.setTopAnchor(pageTabs, 19.0);
		
		AnchorPane.setRightAnchor(buttons, 19.0);
		AnchorPane.setTopAnchor(buttons, 19.0);
		
		tabsBar.getChildren().addAll(pageTabs, buttons);
		
		modListView = new ModListView(this);
		backupListView = new BackupListView();
		settingsView = new SettingsView();
		aboutView = new AboutView();
		
		mainContentPane = new ScrollPane();
		mainContentPane.setFitToHeight(true);
		mainContentPane.setFitToWidth(true);
		
		showModList();
		
		VBox.setVgrow(mainContentPane, Priority.ALWAYS);
		
		root.getChildren().add(tabsBar);
		root.getChildren().add(mainContentPane);
		root.prefHeightProperty().bind(primaryStage.heightProperty());
		
		final StackPane stackPane = new StackPane();
		stackPane.getChildren().add(root);
		
		final Scene scene = new Scene(stackPane, Settings.getWindowWidth(), Settings.getWindowHeight());
		primaryStage.initStyle(StageStyle.DECORATED);
		primaryStage.setMinWidth(683);
		primaryStage.setMinHeight(700);
		primaryStage.setScene(scene);
		primaryStage.setTitle(Localizer.formatMessage("windowTitle", Settings.getVersion()));
		primaryStage.show();
		
		scene.setOnDragOver(new EventHandler<DragEvent>() {

			@Override
			public void handle(DragEvent event) {
				
                Dragboard db = event.getDragboard();
                
                if (db.hasFiles()) {

                	boolean filesAccepted = false;
                	String fileName = "\n";
                	
					for (File file : db.getFiles()) {
						if (FileHelper.verify(Paths.get(file.getPath()), dragOver)) {
							filesAccepted = true;
							fileName += Localizer.formatMessage(dragOver, "inquotes", file.getName()) + "\n";
						}
					}
                	
					if (filesAccepted) {
						event.acceptTransferModes(TransferMode.COPY);
						if (!dragOver) {
							Text text = new Text(Localizer.formatMessage("mainview.addmods", db.getFiles().size(), fileName));
							text.setFill(Color.WHITE);
							text.setFont(Font.font("Verdana", 32));
							stackPane.getChildren().addAll(new Rectangle(683, 700, new Color(0.0, 0.0, 0.0, 0.8)), text);
							dragOver = true;
						}
					} else {
						event.consume();
					}
                    
                } else {
                    event.consume();
                }
				
			}
			
		});
		
		scene.setOnDragExited(new EventHandler<DragEvent>() {

			@Override
			public void handle(DragEvent event) {
				stackPane.getChildren().clear();
				stackPane.getChildren().add(root);
				dragOver = false;
			}
			
		});
		
		scene.setOnDragDropped(new EventHandler<DragEvent>() {

			@Override
			public void handle(DragEvent event) {
				
				Dragboard db = event.getDragboard();
			   
				boolean success = false;
				
				if (db.hasFiles()) {
					success = true;
					List<Path> toAdd = new ArrayList<>();
					for (File file : db.getFiles()) {
						toAdd.add(file.toPath());
						log.debug("File '" + file.toPath() + "' dropped on Mod Manager.");
					}
					modListView.addMods(toAdd);
				}
				
				event.setDropCompleted(success);
				event.consume();
				
				stackPane.getChildren().clear();
				stackPane.getChildren().add(root);
				
			}
			
		});
		
		new MessageDialogue().start("TEMP", "NOTIFICATION", MessageType.INFO);
		ConfirmDialogue dialogue = new ConfirmDialogue();
		dialogue.start("Really PLACEHOLDER?", "PLACEHOLDER");
		
		if (dialogue.getResult()) {
			Database.setProperty("windowwidth", 1000);
			log.debug("TRUE");
		}
		
	}
	
	protected void toggleLockModList() {
		log.debug("(Un)Locking mod list!");
		modListView.toggleLock();
	}
	
	protected void showModList() {
		log.debug("Setting view to mod list.");
		mainContentPane.setContent(modListView);
	}
	
	protected void showBackupList() {
		log.debug("Setting view to backup list.");
		mainContentPane.setContent(backupListView);
	}
	
	protected void showSettings() {
		log.debug("Setting view to settings.");
		mainContentPane.setContent(settingsView);
	}
	
	protected void showAbout() {
		log.debug("Setting view to about.");
		mainContentPane.setContent(aboutView);
	}
	
	protected Stage getStage() {
		return primaryStage;
	}
	
	public static void main(String[] args) {
		try {
			launch(args);
		} catch (final Exception e) {
			log.error("Initializing Mod Manager OR Uncaught Exception", e);
		}
	}
	
}
