package net.krazyweb.starbound.modmanager;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class FXDialogueDropdown {
	
	private String text;
	private String[] options;
	private int defaultOption = -1;
	
	public FXDialogueDropdown(String text, String[] options, int defaultOption) {
		this.text = text;
		this.options = options;
		this.defaultOption = defaultOption;
	}
	
	public String show() {
		
		final Stage dialogueStage = new Stage();
		dialogueStage.initModality(Modality.WINDOW_MODAL);
		
		VBox contents = new VBox();
		contents.setAlignment(Pos.CENTER);
		contents.setPadding(new Insets(25));
		contents.setSpacing(10);
		
		contents.getChildren().add(new Text(text));
		
		final ComboBox<String> comboBox = new ComboBox<String>(FXCollections.observableArrayList(options));
		
		if (defaultOption >= 0) {
			comboBox.setValue(options[defaultOption]);
		}
		
		Button confirmButton = new Button("OK");
		confirmButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent e) {
				dialogueStage.close();
			}
			
		});
		
		contents.getChildren().add(comboBox);
		contents.getChildren().add(confirmButton);
		
		dialogueStage.setScene(new Scene(contents));
		dialogueStage.showAndWait();
		
		return (String) comboBox.getValue();
		
	}
	
}