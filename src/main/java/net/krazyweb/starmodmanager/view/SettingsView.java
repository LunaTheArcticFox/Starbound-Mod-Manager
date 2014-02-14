package net.krazyweb.starmodmanager.view;


import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
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
	
	private Text gamePathTitle;
	private TextField gamePathField;
	private Button gamePathButton;
	
	private Text modsPathTitle;
	private TextField modsPathField;
	private Button modsPathButton;
	
	private ComboBox<Language> languageSelector;

	private Text checkVersionTitle;
	private CheckBox checkVersionBox;
	
	private Text backupSavesOnLaunchTitle;
	private CheckBox backupSavesOnLaunchBox;
	
	private Text confirmButtonDelayTitle;
	private NumericTextField confirmButtonDelayField;
	
	private ComboBox<Level> loggerLevelSelector;
	private Text loggerLevelTitle;
	
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
		
		gamePathTitle = new Text();
		gamePathField = new TextField();
		gamePathButton = new Button();

		GridPane gamePathContainer = new GridPane();
		gamePathContainer.add(gamePathTitle, 1, 1);
		gamePathContainer.add(gamePathField, 1, 2);
		gamePathContainer.add(gamePathButton, 2, 2);
		
		modsPathTitle = new Text();
		modsPathField = new TextField();
		modsPathButton = new Button();

		GridPane modInstallPathContainer = new GridPane();
		modInstallPathContainer.add(modsPathTitle, 1, 1);
		modInstallPathContainer.add(modsPathField, 1, 2);
		modInstallPathContainer.add(modsPathButton, 2, 2);
		
		ObservableList<Language> languageOptions = FXCollections.observableArrayList(localizer.getLanguages());
		languageSelector = new ComboBox<>(languageOptions);
		languageSelector.setValue(localizer.getCurrentLanguage());
		
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
		loggerLevelSelector = new ComboBox<>(loggerLevelOptions);
		loggerLevelSelector.setValue(settings.getPropertyLevel("loggerlevel"));
		loggerLevelTitle = new Text();
		loggerLevelContainer.getChildren().addAll(loggerLevelTitle, loggerLevelSelector);
		
		openLogButton = new Button();
		
		HBox checkVersionContainer = new HBox();
		checkVersionBox = new CheckBox();
		checkVersionTitle = new Text();
		checkVersionContainer.getChildren().addAll(checkVersionBox, checkVersionTitle);

		HBox backupSavesOnLaunchContainer = new HBox();
		backupSavesOnLaunchBox = new CheckBox();
		backupSavesOnLaunchTitle = new Text();
		backupSavesOnLaunchContainer.getChildren().addAll(backupSavesOnLaunchBox, backupSavesOnLaunchTitle);

		HBox confirmButtonDelayContainer = new HBox();
		confirmButtonDelayField = new NumericTextField();
		confirmButtonDelayTitle = new Text();
		confirmButtonDelayContainer.getChildren().addAll(confirmButtonDelayField, confirmButtonDelayTitle);
		
		root.getChildren().addAll(
			gamePathContainer,
			modInstallPathContainer,
			languageSelector,
			loggerLevelContainer,
			openLogButton,
			checkVersionContainer,
			backupSavesOnLaunchContainer,
			confirmButtonDelayContainer
		);
		
		createListeners();
		updateStrings();
		
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

		modsPathField.setText(settings.getPropertyPath("modsdir").toAbsolutePath().toString());
		modsPathField.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
				if (!newValue) {
					controller.modsPathChanged(modsPathField.getText());
				}
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
		gamePathButton.setText(">>"); //TODO Replace with image

		modsPathTitle.setText(localizer.getMessage("settings.modspath"));
		modsPathButton.setText(">>"); //TODO Replace with image
		
		loggerLevelTitle.setText(localizer.getMessage("settings.loggerlevel"));
		
		openLogButton.setText(localizer.getMessage("settings.openlog"));
		
		checkVersionTitle.setText(localizer.getMessage("settings.checkversion"));
		backupSavesOnLaunchTitle.setText(localizer.getMessage("settings.backuponlaunch"));
		confirmButtonDelayTitle.setText(localizer.getMessage("settings.confirmdelay"));
		
	}
	
	@Override
	public void update(final Observable observable, final Object message) {
		
		if (observable instanceof Localizer && message.equals("localechanged")) {
			updateStrings();
		}
		
	}
	
}
