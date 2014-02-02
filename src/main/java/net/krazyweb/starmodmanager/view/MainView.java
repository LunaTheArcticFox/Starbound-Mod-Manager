package main.java.net.krazyweb.starmodmanager.view;

import java.util.Observable;
import java.util.Observer;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.DragEvent;
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
import main.java.net.krazyweb.starmodmanager.ModManager;
import main.java.net.krazyweb.starmodmanager.data.Localizer;
import main.java.net.krazyweb.starmodmanager.data.Settings;

import org.apache.log4j.Logger;

public class MainView implements Observer {
	
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(MainView.class);
	
	private MainViewController controller;

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
	
	protected MainView(final MainViewController c) {
		this.controller = c;
		Localizer.getInstance().addObserver(this);
	}
	
	protected void build() {
		
		appName = new Text();
		versionName = new Text();
		
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
		
		stackPane = new StackPane();
		stackPane.getChildren().add(root);
		
		final Scene scene = new Scene(stackPane, Settings.getInstance().getPropertyInt("windowwidth"), Settings.getInstance().getPropertyInt("windowheight"));
		Stage stage = ModManager.getPrimaryStage();
		
		stage.setScene(scene);
		
		setDragEvents(scene, stackPane, root);
		
		updateStrings();
		
	}
	
	private void buildTabs() {

		modListButton = new Button();
		modListButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				controller.modTabClicked();
			}
		});
		
		backupListButton = new Button();
		backupListButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				controller.backupsTabClicked();
			}
		});
		
		settingsButton = new Button();
		settingsButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				controller.settingsTabClicked();
			}
		});
		
		aboutButton = new Button();
		aboutButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				controller.aboutTabClicked();
			}
		});
		
	}
	
	private void buildActionButtons() {
		
		quickBackupButton = new Button();
		quickBackupButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				controller.backupButtonClicked();
			}
		});
		
		lockButton = new Button("Lock");
		lockButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				controller.lockButtonClicked();
			}
		});
		
		refreshButton = new Button("Refresh");
		refreshButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				controller.refreshButtonClicked();
			}
		});
		
		expandButton = new Button("Expand");
		expandButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				controller.expandButtonClicked();
			}
		});
		
	}
	
	private void setDragEvents(final Scene scene, final StackPane stackPane, final VBox root) {
		
		scene.setOnDragOver(new EventHandler<DragEvent>() {

			@Override
			public void handle(final DragEvent event) {
               controller.filesDraggedOver(event);
			}
			
		});
		
		scene.setOnDragExited(new EventHandler<DragEvent>() {

			@Override
			public void handle(DragEvent event) {
				controller.dragExited();
			}
			
		});
		
		scene.setOnDragDropped(new EventHandler<DragEvent>() {

			@Override
			public void handle(DragEvent event) {
				controller.filesDropped(event);
			}
			
		});
		
	}
	
	protected void showOverlay(final String message) {
		Text text = new Text(message);
		text.setFill(Color.WHITE);
		text.setFont(Font.font("Verdana", 32));
		stackPane.getChildren().addAll(new Rectangle(ModManager.getPrimaryStage().getWidth(), ModManager.getPrimaryStage().getHeight(), new Color(0.0, 0.0, 0.0, 0.8)), text);
	}
	
	protected void hideOverlay() {
		stackPane.getChildren().clear();
		stackPane.getChildren().add(root);
	}
	
	protected void show() {
		ModManager.getPrimaryStage().show();
	}
	
	protected void setContent(final Node content) {
		mainContentPane.setContent(content);
	}

	private void updateStrings() {
		
		ModManager.getPrimaryStage().setTitle(Localizer.getInstance().formatMessage("windowtitle", Settings.getInstance().getVersion()));
		
		appName.setText(Localizer.getInstance().getMessage("appname"));
		versionName.setText(Settings.getInstance().getVersion());

		modListButton.setText(Localizer.getInstance().getMessage("navbartabs.mods"));
		backupListButton.setText(Localizer.getInstance().getMessage("navbartabs.backups"));
		settingsButton.setText(Localizer.getInstance().getMessage("navbartabs.settings"));
		aboutButton.setText(Localizer.getInstance().getMessage("navbartabs.about"));
		
		//TODO Remove, these are for testing and will have no text later
		quickBackupButton.setText("No Function");
		lockButton.setText("No Function");
		refreshButton.setText("No Function");
		expandButton.setText("No Function");
		
	}
	
	@Override
	public void update(final Observable observable, final Object message) {
		
		if (observable instanceof Localizer && message.equals("localechanged")) {
			updateStrings();
		}
		
	}
	
}
