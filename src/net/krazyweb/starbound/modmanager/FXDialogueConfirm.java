package net.krazyweb.starbound.modmanager;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class FXDialogueConfirm {
	
	private String text;
	
	public FXDialogueConfirm(String text) {
		this.text = text;
	}
	
	public void show() {
		
		final Stage dialogueStage = new Stage();
		dialogueStage.initModality(Modality.WINDOW_MODAL);
		
		VBox contents = new VBox();
		contents.setAlignment(Pos.CENTER);
		contents.setPadding(new Insets(25));
		contents.setSpacing(10);
		
		contents.getChildren().add(new Text(text));
		
		Button confirmButton = new Button("OK");
		confirmButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent e) {
				dialogueStage.close();
			}
			
		});
		
		contents.getChildren().add(confirmButton);
		
		dialogueStage.setScene(new Scene(contents));
		dialogueStage.showAndWait();
		
	}
	
}