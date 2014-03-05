package net.krazyweb.starmodmanager.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.krazyweb.starmodmanager.data.LocalizerFactory;
import net.krazyweb.starmodmanager.data.LocalizerModelInterface;

import com.sun.webpane.sg.prism.WCGraphicsPrismContext;

public class LicenseView {
	
	private LicenseViewController controller;

	private Stage stage;
	private String title;
	private String file;
	
	private VBox root;
	private Button closeButton;
	
	private LocalizerModelInterface localizer;
	
	protected LicenseView(final String title, final String file) {
		this.title = title;
		this.file = file;
		localizer = new LocalizerFactory().getInstance();
		controller = new LicenseViewController(this);
	}
	
	protected void build() throws IOException {
		
		stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.centerOnScreen();
		stage.setTitle(title);
		
		WebView v = new WebView();
		WebEngine e = v.getEngine();

		//Remove logging from WebView renderer. It messes things up if you don't.
		//If the logging is set to something higher than FINEST, which it appears to be by default,
		//WebView enters a debugging mode.
		java.util.logging.Logger.getLogger(WCGraphicsPrismContext.class.getName()).setLevel(java.util.logging.Level.OFF);  
		
		root = new VBox();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(LicenseView.class.getClassLoader().getResourceAsStream(file)));
		
		String line;
		
		StringBuilder output = new StringBuilder();
		
		while ((line = reader.readLine()) != null) {
			output.append(line);
			output.append(" ");
		}
		
		e.loadContent(output.toString());
		
		closeButton = new Button();
		closeButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent event) {
				controller.close();
			}
			
		});
		
		root.getChildren().addAll(
			v,
			closeButton
		);
		
		Scene scene = new Scene(root);
		stage.setScene(scene);
		
		updateStrings();
		
	}
	
	private void updateStrings() {
		closeButton.setText(localizer.getMessage("licenseview.close"));
	}
	
	protected void open() {
		stage.showAndWait();
	}
	
	protected void close() {
		stage.close();
	}
	
}