package net.krazyweb.starmodmanager.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class LicenseView {
	
	private LicenseViewController controller;

	private Stage stage;
	private String title;
	private String file;
	
	private VBox root;
	private TextArea licenseText;
	private Button closeButton;
	
	protected LicenseView(final String title, final String file) {
		this.title = title;
		this.file = file;
		controller = new LicenseViewController(this);
	}
	
	protected void build() throws IOException {
		
		stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.centerOnScreen();
		
		root = new VBox();
		
		licenseText = new TextArea();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(LicenseView.class.getClassLoader().getResourceAsStream(file)));
		
		String line;
		
		StringBuilder output = new StringBuilder();
		
		while ((line = reader.readLine()) != null) {
			output.append(line);
		}
		
		licenseText.setText(output.toString());
		
		closeButton = new Button();
		
		root.getChildren().addAll(
			licenseText,
			closeButton
		);
		
		Scene scene = new Scene(root);
		stage.setScene(scene);
		
		updateStrings();
		
	}
	
	private void updateStrings() {
		
	}
	
	protected void open() {
		stage.showAndWait();
	}
	
}