package net.krazyweb.starmodmanager.view;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
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
import javafx.stage.WindowEvent;
import net.krazyweb.starmodmanager.ModManager;
import net.krazyweb.starmodmanager.data.Localizer;
import net.krazyweb.starmodmanager.data.LocalizerFactory;
import net.krazyweb.starmodmanager.data.LocalizerModelInterface;
import net.krazyweb.starmodmanager.data.Observable;
import net.krazyweb.starmodmanager.data.Observer;
import net.krazyweb.starmodmanager.data.SettingsFactory;
import net.krazyweb.starmodmanager.data.SettingsModelInterface;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainView implements Observer {
	
	@SuppressWarnings("unused")
	private static final Logger log = LogManager.getLogger(MainView.class);
	
	private MainViewController controller;

	private Scene scene;
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

	private SettingsModelInterface settings;
	private LocalizerModelInterface localizer;
	
	protected MainView(final MainViewController c) {
		this.controller = c;
		settings = new SettingsFactory().getInstance();
		localizer = new LocalizerFactory().getInstance();
		localizer.addObserver(this);
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
		mainContentPane.setVbarPolicy(ScrollBarPolicy.ALWAYS);
		mainContentPane.setHbarPolicy(ScrollBarPolicy.NEVER);
		
		VBox.setVgrow(mainContentPane, Priority.ALWAYS);
		
		root.getChildren().add(tabsBar);
		root.getChildren().add(mainContentPane);
		
		stackPane = new StackPane();
		stackPane.getChildren().add(root);
		
		scene = new Scene(stackPane, settings.getPropertyDouble("windowwidth"), settings.getPropertyDouble("windowheight"));
		
		Stage stage = ModManager.getPrimaryStage();
		
		/*
		 * Similar to pre-rendering the views of each tab, to set the
		 * correct window size (including borders, making it an unknown value)
		 * to maintain a minimum canvas size (known), an empty scene with the
		 * wanted values must be rendered in a fully transparent window.
		 * The stage width/height is then the real minimum size that is
		 * desired, so that value is plugged in for future use.
		 * Once all that is done, hide the stage, change the opacity back to 1.0,
		 * then add in the real scene.
		 */
		stage.setOpacity(0.0);
		stage.setScene(new Scene(new VBox(), 683, 700));
		stage.show();
		
		stage.setMinWidth(stage.getWidth());
		stage.setMinHeight(stage.getHeight());
		
		stage.hide();
		stage.setOpacity(1.0);
		
		stage.setScene(scene);
		
		setSceneEvents(scene, stackPane, root);
		setStageEvents();
		
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
	
	private void setSceneEvents(final Scene scene, final StackPane stackPane, final VBox root) {
		
		scene.setOnDragOver(new EventHandler<DragEvent>() {

			@Override
			public void handle(final DragEvent event) {
               controller.filesDraggedOver(event);
			}
			
		});
		
		scene.setOnDragExited(new EventHandler<DragEvent>() {

			@Override
			public void handle(final DragEvent event) {
				controller.dragExited();
			}
			
		});
		
		scene.setOnDragDropped(new EventHandler<DragEvent>() {

			@Override
			public void handle(final DragEvent event) {
				controller.filesDropped(event);
			}
			
		});
		
	}
	
	private void setStageEvents() {
		
		ModManager.getPrimaryStage().setOnCloseRequest(new EventHandler<WindowEvent>() {

			@Override
			public void handle(final WindowEvent event) {
				controller.closeRequested(event);
			}
			
		});
		
	}
	
	protected void showOverlay(final String message) {
		Text text = new Text(message);
		text.setFill(Color.WHITE);
		text.setFont(Font.font("Verdana", 32));
		stackPane.getChildren().addAll(new Rectangle(root.getWidth(), root.getHeight(), new Color(0.0, 0.0, 0.0, 0.8)), text);
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
	
	protected ScrollPane getContent() {
		return mainContentPane;
	}
	
	protected Scene getScene() {
		return scene;
	}

	private void updateStrings() {
		
		ModManager.getPrimaryStage().setTitle(localizer.formatMessage("windowtitle", settings.getVersion()));
		
		appName.setText(localizer.getMessage("appname"));
		versionName.setText(settings.getVersion());

		modListButton.setText(localizer.getMessage("navbartabs.mods"));
		backupListButton.setText(localizer.getMessage("navbartabs.backups"));
		settingsButton.setText(localizer.getMessage("navbartabs.settings"));
		aboutButton.setText(localizer.getMessage("navbartabs.about"));
		
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
