package main.java.net.krazyweb.starmodmanager.view;

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
import main.java.net.krazyweb.starmodmanager.data.Localizer;
import main.java.net.krazyweb.starmodmanager.data.Localizer.Language;
import main.java.net.krazyweb.starmodmanager.data.Settings;

import org.apache.log4j.Logger;


public class SettingsView implements Observer {
	
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(SettingsView.class);

	private VBox root;
	
	private Text gamePathTitle;
	private TextField gamePathField;
	private Button gamePathButton;
	
	private Text modInstallPathTitle;
	private TextField modInstallPathField;
	private Button modInstallPathButton;
	
	private ComboBox<Language> languages;

	private Text checkVersionTitle;
	private CheckBox checkVersionBox;
	
	private Text backupSavesOnLaunchTitle;
	private CheckBox backupSavesOnLaunchBox;
	
	private Text confirmButtonDelayTitle;
	private TextField confirmButtonDelayField;
	
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
		
		modInstallPathTitle = new Text();
		modInstallPathField = new TextField();
		modInstallPathButton = new Button();

		GridPane modInstallPathContainer = new GridPane();
		modInstallPathContainer.add(modInstallPathTitle, 1, 1);
		modInstallPathContainer.add(modInstallPathField, 1, 2);
		modInstallPathContainer.add(modInstallPathButton, 2, 2);
		
		ObservableList<Language> options = FXCollections.observableArrayList(Localizer.getInstance().getLanguages());
		
		languages = new ComboBox<>(options);
		languages.setValue(Localizer.getInstance().getCurrentLanguage());
		
		languages.valueProperty().addListener(new ChangeListener<Language>() {
			@Override
			public void changed(ObservableValue<? extends Language> ov,	Language oldValue, Language newValue) {
				controller.languageChanged(newValue);
			}
		});
		
		HBox checkVersionContainer = new HBox();
		checkVersionBox = new CheckBox();
		checkVersionTitle = new Text();
		checkVersionContainer.getChildren().addAll(checkVersionBox, checkVersionTitle);

		HBox backupSavesOnLaunchContainer = new HBox();
		backupSavesOnLaunchBox = new CheckBox();
		backupSavesOnLaunchTitle = new Text();
		backupSavesOnLaunchContainer.getChildren().addAll(backupSavesOnLaunchBox, backupSavesOnLaunchTitle);

		HBox confirmButtonDelayContainer = new HBox();
		confirmButtonDelayField = new TextField();
		confirmButtonDelayTitle = new Text();
		confirmButtonDelayContainer.getChildren().addAll(confirmButtonDelayField, confirmButtonDelayTitle);
		
		root.getChildren().addAll(
			gamePathContainer,
			modInstallPathContainer,
			languages,
			checkVersionContainer,
			backupSavesOnLaunchContainer,
			confirmButtonDelayContainer
		);
		
		updateStrings();
		
	}
	
	protected Node getContent() {
		return root;
	}

	private void updateStrings() {
		
		gamePathTitle.setText(Localizer.getInstance().getMessage("settings.starboundpath"));
		gamePathField.setText(Settings.getInstance().getPropertyString("starboundpath"));
		gamePathButton.setText(">>");

		modInstallPathTitle.setText(Localizer.getInstance().getMessage("settings.modsinstallpath"));
		modInstallPathField.setText(Settings.getInstance().getPropertyString("modsinstalldir"));
		modInstallPathButton.setText(">>");
		
		checkVersionTitle.setText(Localizer.getInstance().getMessage("settings.checkversion"));
		
		backupSavesOnLaunchTitle.setText(Localizer.getInstance().getMessage("settings.backuponlaunch"));
		
		confirmButtonDelayTitle.setText(Localizer.getInstance().getMessage("settings.confirmdelay"));
		confirmButtonDelayField.setText(Settings.getInstance().getPropertyString("confirmdelay"));
		
	}
	
	@Override
	public void update(final Observable observable, final Object message) {
		
		if (observable instanceof Localizer && message.equals("localechanged")) {
			updateStrings();
		} else if (observable instanceof Settings && message.equals("propertychanged:starboundpath")) {
			
		}
		
	}
	
}
