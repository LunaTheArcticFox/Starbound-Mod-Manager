package net.krazyweb.starmodmanager.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.krazyweb.helpers.FileHelper;
import net.krazyweb.starmodmanager.data.LocalizerFactory;
import net.krazyweb.starmodmanager.data.LocalizerModelInterface;
import net.krazyweb.starmodmanager.data.SettingsFactory;
import net.krazyweb.starmodmanager.data.SettingsModelInterface;
import net.krazyweb.starmodmanager.dialogue.MessageDialogue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.webpane.sg.prism.WCGraphicsPrismContext;

public class LicenseView {
	
	@SuppressWarnings("unused")
	private static final Logger log = LogManager.getLogger(LicenseView.class);
	
	private LicenseViewController controller;

	private Stage stage;
	private String title;
	private String file;
	
	private VBox root;
	private Button closeButton;
	
	private WebView webView;
	
	private SettingsModelInterface settings;
	private LocalizerModelInterface localizer;
	
	protected LicenseView(final String title, final String file) {
		this.title = title;
		this.file = file;
		settings = new SettingsFactory().getInstance();
		localizer = new LocalizerFactory().getInstance();
		controller = new LicenseViewController(this);
	}
	
	protected void build() throws IOException {
		
		stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.centerOnScreen();
		stage.setTitle(title);
		
		webView = new WebView();
		WebEngine e = webView.getEngine();

		//Remove logging from WebView renderer. It messes things up if you don't.
		//If the logging is set to something higher than FINEST, which it appears to be by default,
		//WebView enters a debugging mode.
		java.util.logging.Logger.getLogger(WCGraphicsPrismContext.class.getName()).setLevel(java.util.logging.Level.OFF);  
		
		root = new VBox();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(LicenseView.class.getClassLoader().getResourceAsStream(file)));
		
		String line;
		
		StringBuilder output = new StringBuilder();
		
		Font.loadFont(
			LicenseView.class.getClassLoader().getResource("Lato-Regular.ttf").toExternalForm(), 
			12
		);
		
		while ((line = reader.readLine()) != null) {
			if (line.startsWith("<head>")) {
				String style = FileHelper.fileToString(LicenseView.class.getClassLoader().getResourceAsStream("theme_web_base.css")) + "\n" +
							   FileHelper.fileToString(LicenseView.class.getClassLoader().getResourceAsStream("theme_web_green.css")); //TODO Theme from settings
				output.append(line.replace("<head>", "<head><style>" + style + "</style>"));
			} else {
				output.append(line);
			}
			output.append(" ");
		}
		
		e.loadContent(output.toString());
		
		closeButton = new Button();
		closeButton.setId("message-dialogue-button");
		closeButton.setPrefWidth(120);
		closeButton.setMinHeight(40);
		closeButton.setPrefHeight(40);
		closeButton.setAlignment(Pos.CENTER);
		closeButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent event) {
				controller.close();
			}
			
		});
		
		root.getChildren().addAll(
			webView,
			closeButton
		);
		
		root.setAlignment(Pos.CENTER);
		
		VBox.setMargin(closeButton, new Insets(25, 0, 20, 0));
		
		Scene scene = new Scene(root);
		scene.getStylesheets().add(MessageDialogue.class.getClassLoader().getResource("theme_base.css").toExternalForm());
		scene.getStylesheets().add(MessageDialogue.class.getClassLoader().getResource(settings.getPropertyString("theme")).toExternalForm());
		stage.setScene(scene);
		stage.setWidth(700);
		stage.setMinWidth(700);
		stage.setMinHeight(600);
		
		updateStrings();
		
	}
	
	private void updateStrings() {
		closeButton.setText(localizer.getMessage("licenseview.close"));
	}
	
	protected void open() {
		//Remove logging from WebView renderer. It messes things up if you don't.
		//If the logging is set to something higher than FINEST, which it appears to be by default,
		//WebView enters a debugging mode.
		java.util.logging.Logger.getLogger(WCGraphicsPrismContext.class.getName()).setLevel(java.util.logging.Level.OFF);
		stage.showAndWait();
	}
	
	protected void close() {
		stage.close();
	}
	
}