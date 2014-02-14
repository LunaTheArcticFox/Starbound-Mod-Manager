package net.krazyweb.starmodmanager.dialogue;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.krazyweb.starmodmanager.data.LocalizerModelFactory;
import net.krazyweb.starmodmanager.data.LocalizerModelInterface;

public class MessageDialogue {
	
	//TODO Make generic
	//An application close request MUST close all other dialogues
	//Otherwise the program doesn't actually close
	
	public static enum MessageType {
		INFO, ERROR
	}
	
	public static enum DialogueAction {
	    YES, NO, CANCEL, OK, CLOSED
	}
	
	private Stage stage;
	private GridPane root;

	private String title;
	private Text messageText;
	private Button confirmButton;
	private Text iconPlaceholder;
	
	private DialogueAction actionPerformed;
	
	private LocalizerModelInterface localizer;
	
	public MessageDialogue(final String message, final String title, final MessageType messageType, final LocalizerModelFactory localizerFactory) {
		build(message, title, messageType);
		show();
		localizer = localizerFactory.getInstance();
	}
	
	private void build(final String message, final String title, final MessageType messageType) {
		
		this.title = title;
		
		root = new GridPane();
		
		iconPlaceholder = new Text("[PLACEHOLDER]");
		messageText = new Text(message);
		confirmButton = new Button(localizer.getMessage("messagedialogue.okay"));
		
		confirmButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				actionPerformed = DialogueAction.OK;
				stage.close();
			}
		});
		
		root.add(iconPlaceholder, 1, 1);
		root.add(messageText, 2, 1);
		root.add(confirmButton, 2, 2);
		
	}
	
	private void show() {
		stage = new Stage();
		stage.setScene(new Scene(root));
		stage.setTitle(title);
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setResizable(false);
		stage.showAndWait();
	}
	
	public DialogueAction getResult() {
		return actionPerformed;
	}
	
}
