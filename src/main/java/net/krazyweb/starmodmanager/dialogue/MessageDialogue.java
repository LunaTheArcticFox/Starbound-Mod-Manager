package net.krazyweb.starmodmanager.dialogue;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.krazyweb.starmodmanager.data.LocalizerModelFactory;
import net.krazyweb.starmodmanager.data.LocalizerModelInterface;

public class MessageDialogue {
	
	//TODO Make generic
	//An application close request MUST close all other dialogues
	//Otherwise the program doesn't actually close
	
	public static enum MessageType {
		INFO, ERROR, CONFIRM
	}
	
	public static enum DialogueAction {
	    YES, NO, CANCEL, OK, CLOSED
	}
	
	protected Stage stage;
	protected GridPane root;

	protected String title;
	protected Text messageText;
	protected Button confirmButton;
	protected Text iconPlaceholder;
	
	protected DialogueAction actionPerformed;
	
	protected LocalizerModelInterface localizer;
	
	public MessageDialogue(final String message, final String title, final MessageType messageType, final LocalizerModelInterface localizer) {
		this.localizer = localizer;
		build(message, title, messageType);
		show();
	}
	
	public MessageDialogue(final String message, final String title, final MessageType messageType, final LocalizerModelFactory localizerFactory) {
		localizer = localizerFactory.getInstance();
		build(message, title, messageType);
		show();
	}
	
	protected void build(final String message, final String title, final MessageType messageType) {
		
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
	
	protected void show() {
		
		stage = new Stage();
		stage.setScene(new Scene(root));
		stage.setTitle(title);
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setResizable(false);
		
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				if (event.getEventType() == WindowEvent.WINDOW_CLOSE_REQUEST) {
					actionPerformed = DialogueAction.CLOSED;
					stage.close();
				}
			}
		});
		
		stage.showAndWait();
	}
	
	public DialogueAction getResult() {
		return actionPerformed;
	}
	
}
