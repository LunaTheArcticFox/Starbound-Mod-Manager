package main.java.net.krazyweb.starmodmanager.view;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
import main.java.net.krazyweb.helpers.FileHelper;
import main.java.net.krazyweb.starmodmanager.controllers.MainViewController;
import main.java.net.krazyweb.starmodmanager.controllers.ModManager;
import main.java.net.krazyweb.starmodmanager.data.Localizer;
import main.java.net.krazyweb.starmodmanager.data.Settings;

import org.apache.log4j.Logger;

public class MainView implements Observer {
	
	private static final Logger log = Logger.getLogger(MainView.class);
	
	private MainViewController controller;
	private boolean dragOver = false;

	private VBox root;
	private StackPane stackPane;
	private ScrollPane mainContentPane;
	
	private Text appName;
	private Text versionName;
	
	private Button modListButton;
	private Button backupListButton;
	private Button settingsButton;
	private Button aboutButton;
	
	private Button quickBackupButton;
	private Button lockButton;
	private Button refreshButton;
	private Button expandButton;
	
	public MainView(final MainViewController c) {
		this.controller = c;
		Localizer.getInstance().addObserver(this);
	}
	
	public void build() {
		
		appName = new Text(Localizer.getInstance().getMessage("appName"));
		versionName = new Text(Settings.getInstance().getVersion());
		
		AnchorPane.setTopAnchor(appName, 21.0);
		AnchorPane.setLeftAnchor(appName, 19.0);
		AnchorPane.setRightAnchor(versionName, 19.0);
		AnchorPane.setTopAnchor(versionName, 21.0);

		AnchorPane topBar = new AnchorPane();
		topBar.getChildren().addAll(appName, versionName);

		root = new VBox();
		root.getChildren().add(topBar);
		
		HBox pageTabs = new HBox();
		HBox buttons = new HBox();
		
		buildTabs();
		buildActionButtons();
		
		pageTabs.getChildren().addAll(
			modListButton,
			backupListButton,
			settingsButton,
			aboutButton
		);
		
		buttons.getChildren().addAll(
			quickBackupButton,
			lockButton,
			refreshButton,
			expandButton
		);
		
		AnchorPane.setLeftAnchor(pageTabs, 19.0);
		AnchorPane.setTopAnchor(pageTabs, 19.0);
		
		AnchorPane.setRightAnchor(buttons, 19.0);
		AnchorPane.setTopAnchor(buttons, 19.0);

		AnchorPane tabsBar = new AnchorPane();
		tabsBar.getChildren().addAll(pageTabs, buttons);
		
		mainContentPane = new ScrollPane();
		mainContentPane.setFitToHeight(true);
		mainContentPane.setFitToWidth(true);
		
		VBox.setVgrow(mainContentPane, Priority.ALWAYS);
		
		root.getChildren().add(tabsBar);
		root.getChildren().add(mainContentPane);
		root.prefHeightProperty().bind(ModManager.getPrimaryStage().heightProperty());
		
		stackPane = new StackPane();
		stackPane.getChildren().add(root);
		
		final Scene scene = new Scene(stackPane, Settings.getInstance().getPropertyInt("windowwidth"), Settings.getInstance().getPropertyInt("windowheight"));
		Stage stage = ModManager.getPrimaryStage();
		
		stage.setScene(scene);
		stage.setTitle(Localizer.getInstance().formatMessage("windowTitle", Settings.getInstance().getVersion()));
		
		setDragEvents(scene, stackPane, root);
		
	}
	
	private void buildTabs() {

		modListButton = new Button(Localizer.getInstance().getMessage("navbartabs.mods"));
		modListButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				//TODO Functionality
			}
		});
		
		backupListButton = new Button(Localizer.getInstance().getMessage("navbartabs.backups"));
		backupListButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				//TODO Functionality
			}
		});
		
		settingsButton = new Button(Localizer.getInstance().getMessage("navbartabs.settings"));
		settingsButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				//TODO Functionality
			}
		});
		
		aboutButton = new Button(Localizer.getInstance().getMessage("navbartabs.about"));
		aboutButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				//TODO Functionality
			}
		});
		
	}
	
	private void buildActionButtons() {
		
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
				//TODO Functionality
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
		
	}
	
	private void setDragEvents(final Scene scene, final StackPane stackPane, final VBox root) {
		
		scene.setOnDragOver(new EventHandler<DragEvent>() {

			@Override
			public void handle(final DragEvent event) {
				
				
                Dragboard db = event.getDragboard();
                
                if (db.hasFiles()) {

                	boolean filesAccepted = false;
                	String fileName = "\n";
                	
					for (File file : db.getFiles()) {
						if (FileHelper.verify(Paths.get(file.getPath()), dragOver)) {
							filesAccepted = true;
							fileName += Localizer.getInstance().formatMessage(dragOver, "inquotes", file.getName()) + "\n";
						}
					}
                	
					if (filesAccepted) {
						event.acceptTransferModes(TransferMode.COPY);
						if (!dragOver) {
							Text text = new Text(Localizer.getInstance().formatMessage("mainview.addmods", db.getFiles().size(), fileName));
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
					//modListView.addMods(toAdd); TODO
				}
				
				event.setDropCompleted(success);
				event.consume();
				
				stackPane.getChildren().clear();
				stackPane.getChildren().add(root);
				
			}
			
		});
		
	}
	
	public void show() {
		ModManager.getPrimaryStage().show();
	}

	@Override
	public void update(final Observable observable, final Object message) {
		//TODO
	}
	
}
