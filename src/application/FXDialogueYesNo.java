package application;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class FXDialogueYesNo {
	
	private String text;
	
	public FXDialogueYesNo(String text) {
		this.text = text;
	}
	
	public boolean show() {
		
		final Stage dialogueStage = new Stage();
		dialogueStage.initModality(Modality.WINDOW_MODAL);
		
		VBox contents = new VBox();
		HBox buttons = new HBox();
		contents.setAlignment(Pos.CENTER);
		contents.setPadding(new Insets(25));
		contents.setSpacing(30);
		
		buttons.setAlignment(Pos.CENTER);
		buttons.setSpacing(50);
		
		Text message = new Text(text);
		message.setFont(new Font("Tahoma", 16));
		message.setTextAlignment(TextAlignment.CENTER);
		contents.getChildren().add(message);
		
		Button yesButton = new Button("Yes");
		Button noButton = new Button("No");
		
		yesButton.setPrefWidth(80);
		yesButton.setPrefHeight(35);
		noButton.setPrefWidth(80);
		noButton.setPrefHeight(35);
		
		final SimpleBooleanProperty selection = new SimpleBooleanProperty(false);
		
		yesButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent e) {
				selection.setValue(true);
				dialogueStage.close();
			}
			
		});
		
		noButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent e) {
				selection.setValue(false);
				dialogueStage.close();
			}
			
		});
		
		buttons.getChildren().add(yesButton);
		buttons.getChildren().add(noButton);
		
		contents.getChildren().add(buttons);
		
		dialogueStage.setScene(new Scene(contents));
		dialogueStage.showAndWait();
		
		return selection.getValue();
		
	}
	
}