package net.krazyweb.starmodmanager.dialogue;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.krazyweb.helpers.CSSHelper;
import net.krazyweb.helpers.FXHelper;
import net.krazyweb.starmodmanager.data.LocalizerModelFactory;
import net.krazyweb.starmodmanager.data.LocalizerModelInterface;
import net.krazyweb.starmodmanager.data.SettingsFactory;
import net.krazyweb.starmodmanager.data.SettingsModelInterface;

public class MessageDialogue {
	
	//TODO
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
	protected ImageView icon;
	
	protected DialogueAction actionPerformed;

	protected SettingsModelInterface settings;
	protected LocalizerModelInterface localizer;
	
	public MessageDialogue(final String message, final String title, final MessageType messageType, final LocalizerModelInterface localizer) {
		settings = new SettingsFactory().getInstance();
		this.localizer = localizer;
		build(message, title, messageType);
		show();
	}
	
	public MessageDialogue(final String message, final String title, final MessageType messageType, final LocalizerModelFactory localizerFactory) {
		settings = new SettingsFactory().getInstance();
		localizer = localizerFactory.getInstance();
		build(message, title, messageType);
		show();
	}
	
	protected void build(final String message, final String title, final MessageType messageType) {
		
		this.title = title;
		
		root = new GridPane();
		root.setPadding(new Insets(43, 50, 30, 20));
		root.setHgap(25);
		
		if (messageType == MessageType.CONFIRM) {
			icon = new ImageView(new Image(MessageDialogueConfirm.class.getClassLoader().getResourceAsStream("delete-file-icon.png")));
			Color color = CSSHelper.getColor("message-dialogue-confirm-warning-color", settings.getPropertyString("theme"));
			FXHelper.setColor(icon, color);
		} else if (messageType == MessageType.ERROR) {
			icon = new ImageView(new Image(MessageDialogueConfirm.class.getClassLoader().getResourceAsStream("error-icon.png")));
			Color color = CSSHelper.getColor("message-dialogue-confirm-error-color", settings.getPropertyString("theme"));
			FXHelper.setColor(icon, color);
		} else {
			icon = new ImageView(new Image(MessageDialogueConfirm.class.getClassLoader().getResourceAsStream("delete-file-icon.png")));
			Color color = CSSHelper.getColor("message-dialogue-confirm-info-color", settings.getPropertyString("theme"));
			FXHelper.setColor(icon, color);
		}
		
		messageText = new Text(message);
		messageText.setId("message-dialogue-text");
		messageText.setWrappingWidth(285);
		
		confirmButton = new Button(localizer.getMessage("messagedialogue.okay"));
		confirmButton.setId("message-dialogue-button");
		confirmButton.setPrefWidth(120);
		confirmButton.setPrefHeight(40);
		confirmButton.setAlignment(Pos.CENTER);
		
		confirmButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				actionPerformed = DialogueAction.OK;
				stage.close();
			}
		});

		HBox buttonBox = new HBox();
		buttonBox.getChildren().addAll(confirmButton);
		buttonBox.setSpacing(30);
		buttonBox.setAlignment(Pos.CENTER);
		
		root.add(icon, 1, 1);
		root.add(messageText, 2, 1);
		root.add(buttonBox, 2, 2);
		
	}
	
	protected void show() {
		
		Scene scene = new Scene(root);
		scene.getStylesheets().add(MessageDialogue.class.getClassLoader().getResource("theme_base.css").toExternalForm());
		scene.getStylesheets().add(MessageDialogue.class.getClassLoader().getResource(settings.getPropertyString("theme")).toExternalForm());
		
		stage = new Stage();
		stage.setScene(scene);
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
