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

public class ConfirmDialogue {

	//TODO Make generic
	
	private Stage stage;
	
	private Text message;
	private Button yesButton;
	private Button noButton;
	private Text placeholderImage;
	
	private boolean result = false;
	
	private Scene createDialogue() {
		
		GridPane pane = new GridPane();
		
		placeholderImage = new Text("[PLACEHOLDER]");
		message = new Text("[PLACEHOLDER]");
		
		yesButton = new Button(Localizer.getMessage("confirmdialogue.yes"));
		
		yesButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				result = true;
				stage.close();
			}
		});
		
		noButton = new Button(Localizer.getMessage("confirmdialogue.no"));
		
		noButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				result = false;
				stage.close();
			}
		});
		
		pane.add(placeholderImage, 1, 1);
		pane.add(message, 2, 1);
		pane.add(yesButton, 2, 2);
		pane.add(noButton, 3, 2);
		
		return new Scene(pane, 500, 200);
		
	}
	
	public void start(final String message, final String windowTitle) {
		stage = new Stage();
		stage.setScene(createDialogue());
		stage.setTitle(windowTitle);
		stage.initStyle(StageStyle.UTILITY);
		stage.showAndWait();
	}
	
	public boolean getResult() {
		return result;
	}
	
}
