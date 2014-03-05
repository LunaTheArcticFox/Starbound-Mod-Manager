package net.krazyweb.starmodmanager.view;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.input.DragEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
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
		appName.setId("topbar-title");
		versionName = new Text();
		versionName.setId("topbar-version");
		
		AnchorPane.setTopAnchor(appName, 15.0);
		AnchorPane.setBottomAnchor(appName, 15.0);
		AnchorPane.setLeftAnchor(appName, 19.0);
		AnchorPane.setRightAnchor(versionName, 19.0);
		AnchorPane.setTopAnchor(versionName, 15.0);

		AnchorPane topBar = new AnchorPane();
		topBar.getChildren().addAll(appName, versionName);
		topBar.setId("topbar");
		
		root = new VBox();
		root.getChildren().add(topBar);
		
		GridPane pageTabs = new GridPane();
		HBox buttons = new HBox();
		
		buildTabs();
		buildActionButtons();
		
		pageTabs.add(modListButton, 0, 0);
		pageTabs.add(backupListButton, 1, 0);
		pageTabs.add(settingsButton, 2, 0);
		pageTabs.add(aboutButton, 3, 0);
		pageTabs.setHgap(36);
		GridPane.setValignment(modListButton, VPos.CENTER);
		GridPane.setHalignment(modListButton, HPos.CENTER);
		GridPane.setValignment(backupListButton, VPos.CENTER);
		GridPane.setHalignment(backupListButton, HPos.CENTER);
		GridPane.setValignment(settingsButton, VPos.CENTER);
		GridPane.setHalignment(settingsButton, HPos.CENTER);
		GridPane.setValignment(aboutButton, VPos.CENTER);
		GridPane.setHalignment(aboutButton, HPos.CENTER);
		
		/*
		 * JavaFX's GridPane pushes around elements when the size of a column changes.
		 * When changing the styling of the text for highlighted buttons, this becomes
		 * a problem, as the whole layout shifts every click. To get around this, it's
		 * necessary to compute the pixel width of the text in each button, then
		 * constrain the columns to be the maximum size of the text-no more, no less.
		 * Accurate HGaps can then be applied and the elements will not move around.
		 * 
		 * To compute the actual size of the text in the scene, it's necessary to
		 * create an invisible stage, add a Text node with the font and size
		 * equivalent to the stylesheet's, then get the node's layout width.
		 * 
		 * TODO This must be done with every language change.
		 */
		ColumnConstraints col1 = new ColumnConstraints();
		ColumnConstraints col2 = new ColumnConstraints();
		ColumnConstraints col3 = new ColumnConstraints();
		ColumnConstraints col4 = new ColumnConstraints();

		Text test = new Text();
		test.setFont(Font.loadFont(MainView.class.getClassLoader().getResourceAsStream("Lato-Medium.ttf"), 18));
		test.setId("pagetab-selected");

		VBox t = new VBox();
		t.getChildren().add(test);
		
		Stage s = new Stage();
		s.setOpacity(0);
		s.setScene(new Scene(t, 500, 500));
		s.show();

		test.setText(localizer.getMessage("navbartabs.mods"));
		col1.setPrefWidth(test.getLayoutBounds().getWidth());
		test.setText(localizer.getMessage("navbartabs.backups"));
		col2.setPrefWidth(test.getLayoutBounds().getWidth());
		test.setText(localizer.getMessage("navbartabs.settings"));
		col3.setPrefWidth(test.getLayoutBounds().getWidth());
		test.setText(localizer.getMessage("navbartabs.about"));
		col4.setPrefWidth(test.getLayoutBounds().getWidth());
		
		s.close();
		
		pageTabs.getColumnConstraints().addAll(col1, col2, col3, col4);
		
		buttons.getChildren().addAll(
			quickBackupButton,
			lockButton,
			refreshButton,
			expandButton
		);
		
		AnchorPane.setLeftAnchor(pageTabs, 35.0);
		AnchorPane.setTopAnchor(pageTabs, 26.0);
		AnchorPane.setBottomAnchor(pageTabs, 21.0);
		
		AnchorPane.setRightAnchor(buttons, 21.0);
		AnchorPane.setTopAnchor(buttons, 26.0);

		AnchorPane tabsBar = new AnchorPane();
		tabsBar.setId("tabsbar");
		tabsBar.getChildren().addAll(pageTabs, buttons);
		
		mainContentPane = new ScrollPane();
		mainContentPane.setFitToHeight(true);
		mainContentPane.setFitToWidth(true);
		mainContentPane.setFocusTraversable(false);
		mainContentPane.setVbarPolicy(ScrollBarPolicy.ALWAYS);
		mainContentPane.setHbarPolicy(ScrollBarPolicy.NEVER);

		VBox v = new VBox();
		v.getChildren().add(mainContentPane);
		v.setPadding(new Insets(20, 20, 20, 1));
		
		VBox.setVgrow(mainContentPane, Priority.ALWAYS);
		VBox.setVgrow(v, Priority.ALWAYS);
		
		root.getChildren().add(tabsBar);
		root.getChildren().add(v);
		
		stackPane = new StackPane();
		stackPane.getChildren().add(root);
		
		scene = new Scene(stackPane,
				Math.max(settings.getPropertyDouble("windowwidth"), settings.getPropertyDouble("enforcedminwidth")),
				Math.max(settings.getPropertyDouble("windowheight"), settings.getPropertyDouble("enforcedminheight")));
		scene.getStylesheets().add(MainView.class.getClassLoader().getResource("theme_base.css").toString());
		scene.getStylesheets().add(MainView.class.getClassLoader().getResource(settings.getPropertyString("theme")).toString());
		
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
		stage.setScene(new Scene(new VBox(), settings.getPropertyDouble("enforcedminwidth"), settings.getPropertyDouble("enforcedminheight")));
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
		modListButton.setId("pagetab-selected");
		modListButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				modListButton.setId("pagetab-selected");
				backupListButton.setId("pagetab");
				settingsButton.setId("pagetab");
				aboutButton.setId("pagetab");
				controller.modTabClicked();
			}
		});
		
		backupListButton = new Button();
		backupListButton.setId("pagetab");
		backupListButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				modListButton.setId("pagetab");
				backupListButton.setId("pagetab-selected");
				settingsButton.setId("pagetab");
				aboutButton.setId("pagetab");
				controller.backupsTabClicked();
			}
		});
		
		settingsButton = new Button();
		settingsButton.setId("pagetab");
		settingsButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				modListButton.setId("pagetab");
				backupListButton.setId("pagetab");
				settingsButton.setId("pagetab-selected");
				aboutButton.setId("pagetab");
				controller.settingsTabClicked();
			}
		});
		
		aboutButton = new Button();
		aboutButton.setId("pagetab");
		aboutButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				modListButton.setId("pagetab");
				backupListButton.setId("pagetab");
				settingsButton.setId("pagetab");
				aboutButton.setId("pagetab-selected");
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
				e.consume();
			}
		});
		
		lockButton = new Button();
		lockButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				controller.lockButtonClicked();
				e.consume();
			}
		});
		
		refreshButton = new Button();
		refreshButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				controller.refreshButtonClicked();
				e.consume();
			}
		});
		
		expandButton = new Button();
		expandButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				controller.expandButtonClicked();
				e.consume();
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
		text.setFont(Font.font("Lato-Regular", 32));
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
		
		appName.setText(localizer.getMessage("appname").toUpperCase());
		versionName.setText(settings.getVersion());

		modListButton.setText(localizer.getMessage("navbartabs.mods"));
		backupListButton.setText(localizer.getMessage("navbartabs.backups"));
		settingsButton.setText(localizer.getMessage("navbartabs.settings"));
		aboutButton.setText(localizer.getMessage("navbartabs.about"));
		
		//TODO Remove, these are for testing and will have no text later
		quickBackupButton.setText("No Function");
		lockButton.setText("LCK");
		refreshButton.setText("REF");
		expandButton.setText("EXP");
		
	}
	
	@Override
	public void update(final Observable observable, final Object message) {
		
		if (observable instanceof Localizer && message.equals("localechanged")) {
			updateStrings();
		}
		
	}
	
}
