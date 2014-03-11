package net.krazyweb.starmodmanager.view;


import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import net.krazyweb.helpers.CSSHelper;
import net.krazyweb.helpers.FXHelper;
import net.krazyweb.jfx.controls.NumericTextField;
import net.krazyweb.starmodmanager.data.Localizer;
import net.krazyweb.starmodmanager.data.LocalizerFactory;
import net.krazyweb.starmodmanager.data.LocalizerModelInterface;
import net.krazyweb.starmodmanager.data.LocalizerModelInterface.Language;
import net.krazyweb.starmodmanager.data.Observable;
import net.krazyweb.starmodmanager.data.Observer;
import net.krazyweb.starmodmanager.data.SettingsFactory;
import net.krazyweb.starmodmanager.data.SettingsModelInterface;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class SettingsView implements Observer {
	
	@SuppressWarnings("unused")
	private static final Logger log = LogManager.getLogger(SettingsView.class);

	private VBox root;
	
	private Label gamePathTitle;
	private TextField gamePathField;
	private Button gamePathButton;
	
	private Label modsPathTitle;
	private TextField modsPathField;
	private Button modsPathButton;
	
	private ComboBox<Language> languageSelector;

	private Text checkVersionTitle;
	private CheckBox checkVersionBox;
	
	private Text backupSavesOnLaunchTitle;
	private CheckBox backupSavesOnLaunchBox;
	
	private Label confirmButtonDelayTitle;
	private NumericTextField confirmButtonDelayField;
	
	private ComboBox<Level> loggerLevelSelector;
	private Label loggerLevelTitle;
	
	private Button openLogButton;
	
	private SettingsViewController controller;

	private SettingsModelInterface settings;
	private LocalizerModelInterface localizer;
	
	protected SettingsView() {
		settings = new SettingsFactory().getInstance();
		localizer = new LocalizerFactory().getInstance();
		localizer.addObserver(this);
		controller = new SettingsViewController(this);
	}
	
	protected void build() {
		
		root = new VBox();
		root.setSpacing(15);
		
		gamePathTitle = new Label();
		gamePathTitle.setId("settings-view-text-large");
		gamePathTitle.setTranslateX(10);
		gamePathTitle.setAlignment(Pos.TOP_LEFT);
		gamePathTitle.setPrefHeight(25);
		gamePathField = new TextField();
		gamePathField.setPrefHeight(37);
		gamePathField.prefWidthProperty().bind(root.widthProperty().subtract(56));
		gamePathButton = new Button();
		gamePathButton.setId("settings-path-button");
		gamePathButton.setPrefHeight(37);
		gamePathButton.setPrefWidth(36);
		gamePathButton.setGraphic(new ImageView(new Image(SettingsView.class.getClassLoader().getResourceAsStream("folder-icon.png"))));

		GridPane gamePathContainer = new GridPane();
		gamePathContainer.add(gamePathTitle, 1, 1);
		gamePathContainer.add(gamePathField, 1, 2);
		gamePathContainer.add(gamePathButton, 2, 2);
		
		modsPathTitle = new Label();
		modsPathTitle.setId("settings-view-text-large");
		modsPathTitle.setTranslateX(10);
		modsPathTitle.setAlignment(Pos.TOP_LEFT);
		modsPathTitle.setPrefHeight(25);
		modsPathField = new TextField();
		modsPathField.setPrefHeight(37);
		modsPathField.prefWidthProperty().bind(root.widthProperty().subtract(56));
		modsPathButton = new Button();
		modsPathButton.setId("settings-path-button");
		modsPathButton.setPrefHeight(37);
		modsPathButton.setPrefWidth(36);
		modsPathButton.setGraphic(new ImageView(new Image(SettingsView.class.getClassLoader().getResourceAsStream("folder-icon.png"))));

		GridPane modInstallPathContainer = new GridPane();
		modInstallPathContainer.add(modsPathTitle, 1, 1);
		modInstallPathContainer.add(modsPathField, 1, 2);
		modInstallPathContainer.add(modsPathButton, 2, 2);
		
		ObservableList<Language> languageOptions = FXCollections.observableArrayList(localizer.getLanguages());
		languageSelector = new ComboBox<>(languageOptions);
		languageSelector.setValue(localizer.getCurrentLanguage());
		languageSelector.setFocusTraversable(false);
		languageSelector.setPrefWidth(300);
		languageSelector.setPrefHeight(37);

		openLogButton = new Button();
		openLogButton.setId("settings-standalone-button");
		openLogButton.setGraphic(new ImageView(new Image(SettingsView.class.getClassLoader().getResourceAsStream("log-icon.png"))));
		openLogButton.setPrefHeight(37);
		openLogButton.setGraphicTextGap(10);
		openLogButton.setAlignment(Pos.CENTER);
		
		ObservableList<Level> loggerLevelOptions = FXCollections.observableArrayList(
			Level.OFF,
			Level.FATAL,
			Level.ERROR,
			Level.WARN,
			Level.INFO,
			Level.DEBUG,
			Level.TRACE
		);
		HBox loggerLevelContainer = new HBox();
		loggerLevelContainer.setAlignment(Pos.CENTER_LEFT);
		loggerLevelContainer.setSpacing(15);
		loggerLevelSelector = new ComboBox<>(loggerLevelOptions);
		loggerLevelSelector.setValue(settings.getPropertyLevel("loggerlevel"));
		loggerLevelSelector.setPrefWidth(183);
		loggerLevelSelector.setPrefHeight(37);
		loggerLevelSelector.setFocusTraversable(false);
		loggerLevelTitle = new Label();
		loggerLevelTitle.setId("settings-view-text-large");
		loggerLevelContainer.getChildren().addAll(loggerLevelTitle, loggerLevelSelector, openLogButton);
		
		
		HBox checkVersionContainer = new HBox();
		checkVersionBox = new CheckBox();
		checkVersionTitle = new Text();
		checkVersionContainer.getChildren().addAll(checkVersionBox, checkVersionTitle);

		HBox backupSavesOnLaunchContainer = new HBox();
		backupSavesOnLaunchBox = new CheckBox();
		backupSavesOnLaunchTitle = new Text();
		backupSavesOnLaunchContainer.getChildren().addAll(backupSavesOnLaunchBox, backupSavesOnLaunchTitle);

		HBox confirmButtonDelayContainer = new HBox();
		confirmButtonDelayContainer.setAlignment(Pos.CENTER_LEFT);
		confirmButtonDelayContainer.setSpacing(15);
		confirmButtonDelayField = new NumericTextField();
		confirmButtonDelayField.setId("fully-rounded-input");
		confirmButtonDelayField.setPrefHeight(37);
		confirmButtonDelayField.setPrefWidth(43);
		confirmButtonDelayTitle = new Label();
		confirmButtonDelayTitle.setId("settings-view-text-large");
		confirmButtonDelayContainer.getChildren().addAll(confirmButtonDelayField, confirmButtonDelayTitle);
		
		root.getChildren().addAll(
			gamePathContainer,
			modInstallPathContainer,
			languageSelector,
			loggerLevelContainer,
			//checkVersionContainer,
			//backupSavesOnLaunchContainer,
			confirmButtonDelayContainer
		);
		
		createListeners();
		updateStrings();
		updateColors();
		
	}
	
	private void createListeners() {

		gamePathField.setText(settings.getPropertyPath("starboundpath").toAbsolutePath().toString());
		gamePathField.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
				if (!newValue) {
					controller.gamePathChanged(gamePathField.getText());
				}
			}
		});
		
		gamePathButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				controller.openFileBrowser(gamePathField);
			}
		});

		modsPathField.setText(settings.getPropertyPath("modsdir").toAbsolutePath().toString());
		modsPathField.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
				if (!newValue) {
					controller.modsPathChanged(modsPathField.getText());
				}
			}
		});

		modsPathButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				controller.openFileBrowser(modsPathField);
			}
		});
		
		languageSelector.valueProperty().addListener(new ChangeListener<Language>() {
			@Override
			public void changed(ObservableValue<? extends Language> ov, Language oldValue, Language newValue) {
				controller.languageChanged(newValue);
			}
		});
		
		loggerLevelSelector.valueProperty().addListener(new ChangeListener<Level>() {
			@Override
			public void changed(ObservableValue<? extends Level> ov, Level oldValue, Level newValue) {
				controller.loggerLevelChanged(newValue);
			}
		});
		
		openLogButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				controller.openLog();
			}
		});

		checkVersionBox.selectedProperty().setValue(settings.getPropertyBoolean("checkversiononlaunch"));
		checkVersionBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> ov,	Boolean oldValue, Boolean newValue) {
				controller.checkVersionChanged(newValue);
			}
		});

		backupSavesOnLaunchBox.selectedProperty().setValue(settings.getPropertyBoolean("backuponlaunch"));
		backupSavesOnLaunchBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> ov,	Boolean oldValue, Boolean newValue) {
				controller.backupSavesOnLaunchChanged(newValue);
			}
		});

		confirmButtonDelayField.setMinValue(0);
		confirmButtonDelayField.setMaxValue(10);
		confirmButtonDelayField.setDefaultValue(0);
		confirmButtonDelayField.setText(settings.getPropertyString("confirmdelay"));
		confirmButtonDelayField.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> ov,	Boolean oldValue, Boolean newValue) {
				if (!newValue) {
					controller.confirmButtonDelayChanged(confirmButtonDelayField.getText());
				}
			}
		});
		
	}
	
	protected Node getContent() {
		return root;
	}

	private void updateStrings() {
		
		gamePathTitle.setText(localizer.getMessage("settings.starboundpath"));

		modsPathTitle.setText(localizer.getMessage("settings.modspath"));
		
		loggerLevelTitle.setText(localizer.getMessage("settings.loggerlevel"));
		
		openLogButton.setText(localizer.getMessage("settings.openlog"));
		
		checkVersionTitle.setText(localizer.getMessage("settings.checkversion"));
		backupSavesOnLaunchTitle.setText(localizer.getMessage("settings.backuponlaunch"));
		confirmButtonDelayTitle.setText(localizer.getMessage("settings.confirmdelay"));
		
		//TODO Move this to a utility class
		Text test = new Text();
		test.setFont(Font.loadFont(ModView.class.getClassLoader().getResourceAsStream("Lato-Medium.ttf"), 12));
		test.setId("settings-standalone-button");

		VBox t = new VBox();
		t.getChildren().add(test);
		
		Stage s = new Stage();
		s.setOpacity(0);
		s.setScene(new Scene(t, 500, 500));
		s.show();

		test.setText(localizer.getMessage("settings.openlog"));
		int width = (int) (test.getLayoutBounds().getWidth() + 64);
		openLogButton.setPrefWidth(width);
		openLogButton.setMinWidth(width);
		
		s.close();
		
	}
	
	private void updateColors() {

		Color color = CSSHelper.getColor("file-browser-icon-color", settings.getPropertyString("theme"));
		
		FXHelper.setColor(gamePathButton.getGraphic(), color);
		FXHelper.setColor(modsPathButton.getGraphic(), color);
		
		color = CSSHelper.getColor("log-icon-color", settings.getPropertyString("theme"));
		FXHelper.setColor(openLogButton.getGraphic(), color);
		
	}
	
	@Override
	public void update(final Observable observable, final Object message) {
		
		if (observable instanceof Localizer && message.equals("localechanged")) {
			updateStrings();
		}
		
	}
	
}
