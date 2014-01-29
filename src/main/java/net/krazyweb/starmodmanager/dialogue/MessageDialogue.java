package main.java.net.krazyweb.starmodmanager.dialogue;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import main.java.net.krazyweb.starmodmanager.data.Localizer;

public class MessageDialogue {
	
	public static enum MessageType {
		INFO, ERROR
	}
	
	private Stage stage;
	
	private Text message;
	private Button confirmButton;
	private Text placeholderImage;
	
	private Scene createDialogue() {
		
		GridPane pane = new GridPane();
		
		placeholderImage = new Text("[PLACEHOLDER]");
		message = new Text("[PLACEHOLDER]");
		confirmButton = new Button(Localizer.getMessage("messagedialogue.okay"));
		
		confirmButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				stage.close();
			}
		});
		
		pane.add(placeholderImage, 1, 1);
		pane.add(message, 2, 1);
		pane.add(confirmButton, 2, 2);
		
		return new Scene(pane, 500, 200);
		
	}
	
	public void start(final String message, final String windowTitle, final MessageType type) {
		stage = new Stage();
		stage.setScene(createDialogue());
		stage.setTitle(windowTitle);
		stage.initStyle(StageStyle.UTILITY);
		stage.showAndWait();
	}
	
}
