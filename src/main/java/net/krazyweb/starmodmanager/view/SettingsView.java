package net.krazyweb.starmodmanager.view;

import java.util.Observable;
import java.util.Observer;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import net.krazyweb.starmodmanager.data.Localizer.Language;
import net.krazyweb.starmodmanager.data.Settings;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


public class SettingsView implements Observer {
	
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(SettingsView.class);

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
	
	private ComboBox<Level> loggerLevelSelector; //TODO
	private Text loggerLevelTitle;
	
	private SettingsViewController controller;
	
	protected SettingsView() {
		controller = new SettingsViewController(this);
		Localizer.getInstance().addObserver(this);
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
		
		ObservableList<Language> languageOptions = FXCollections.observableArrayList(Localizer.getInstance().getLanguages());
		languageSelector = new ComboBox<>(languageOptions);
		languageSelector.setValue(Localizer.getInstance().getCurrentLanguage());
		
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
		loggerLevelSelector.setValue(Settings.getInstance().getPropertyLevel("loggerlevel"));
		loggerLevelTitle = new Text();
		loggerLevelContainer.getChildren().addAll(loggerLevelTitle, loggerLevelSelector);
		
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
			checkVersionContainer,
			backupSavesOnLaunchContainer,
			confirmButtonDelayContainer
		);
		
		createListeners();
		updateStrings();
		
	}
	
	private void createListeners() {

		gamePathField.setText(Settings.getInstance().getPropertyPath("starboundpath").toAbsolutePath().toString());
		gamePathField.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
				if (!newValue) {
					controller.gamePathChanged(gamePathField.getText());
				}
			}
		});

		modsPathField.setText(Settings.getInstance().getPropertyPath("modsdir").toAbsolutePath().toString());
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

		checkVersionBox.selectedProperty().setValue(Settings.getInstance().getPropertyBoolean("checkversiononlaunch"));
		checkVersionBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> ov,	Boolean oldValue, Boolean newValue) {
				controller.checkVersionChanged(newValue);
			}
		});

		backupSavesOnLaunchBox.selectedProperty().setValue(Settings.getInstance().getPropertyBoolean("backuponlaunch"));
		backupSavesOnLaunchBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> ov,	Boolean oldValue, Boolean newValue) {
				controller.backupSavesOnLaunchChanged(newValue);
			}
		});

		confirmButtonDelayField.setMinValue(0);
		confirmButtonDelayField.setMaxValue(10);
		confirmButtonDelayField.setDefaultValue(0);
		confirmButtonDelayField.setText(Settings.getInstance().getPropertyString("confirmdelay"));
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
		
		gamePathTitle.setText(Localizer.getInstance().getMessage("settings.starboundpath"));
		gamePathButton.setText(">>"); //TODO Replace with image

		modsPathTitle.setText(Localizer.getInstance().getMessage("settings.modspath"));
		modsPathButton.setText(">>"); //TODO Replace with image
		
		loggerLevelTitle.setText(Localizer.getInstance().getMessage("settings.loggerlevel"));
		
		checkVersionTitle.setText(Localizer.getInstance().getMessage("settings.checkversion"));
		backupSavesOnLaunchTitle.setText(Localizer.getInstance().getMessage("settings.backuponlaunch"));
		confirmButtonDelayTitle.setText(Localizer.getInstance().getMessage("settings.confirmdelay"));
		
	}
	
	@Override
	public void update(final Observable observable, final Object message) {
		
		if (observable instanceof Localizer && message.equals("localechanged")) {
			updateStrings();
		}
		
	}
	
}
