package net.krazyweb.starmodmanager.dialogue;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import net.krazyweb.helpers.CSSHelper;
import net.krazyweb.helpers.FXHelper;
import net.krazyweb.starmodmanager.data.LocalizerModelFactory;
import net.krazyweb.starmodmanager.data.LocalizerModelInterface;

public class MessageDialogueConfirm extends MessageDialogue {

	protected Button noButton;
	
	public MessageDialogueConfirm(String message, String title, MessageType messageType, LocalizerModelInterface localizer) {
		super(message, title, messageType, localizer);
	}

	public MessageDialogueConfirm(String message, String title, MessageType messageType, LocalizerModelFactory localizer) {
		super(message, title, messageType, localizer);
	}
	
	@Override
	protected void build(final String message, final String title, final MessageType messageType) {
		
		this.title = title;
		
		root = new GridPane();
		root.setPadding(new Insets(43, 50, 30, 20));
		root.setHgap(25);
		
		if (messageType == MessageType.CONFIRM) {
			icon = new ImageView(new Image(MessageDialogueConfirm.class.getClassLoader().getResourceAsStream("delete-file-icon.png")));
		} else if (messageType == MessageType.ERROR) {
			icon = new ImageView(new Image(MessageDialogueConfirm.class.getClassLoader().getResourceAsStream("error-icon.png")));
		} else {
			icon = new ImageView(new Image(MessageDialogueConfirm.class.getClassLoader().getResourceAsStream("delete-file-icon.png")));
		}
		
		Color color = CSSHelper.getColor("message-dialogue-confirm-warning-color", settings.getPropertyString("theme"));
		FXHelper.setColor(icon, color);
		
		messageText = new Text(message);
		messageText.setId("message-dialogue-text");
		messageText.setWrappingWidth(285);
		
		HBox buttonBox = new HBox();
		confirmButton = new Button(localizer.formatMessage("messagedialogueconfirm.yeswaiting", settings.getPropertyInt("confirmdelay")));
		confirmButton.setId("message-dialogue-button");
		confirmButton.setPrefWidth(120);
		confirmButton.setPrefHeight(40);
		confirmButton.setAlignment(Pos.CENTER);
		noButton = new Button(localizer.getMessage("messagedialogueconfirm.no"));
		noButton.setId("message-dialogue-button");
		noButton.setPrefWidth(120);
		noButton.setPrefHeight(40);
		noButton.setAlignment(Pos.CENTER);
		
		confirmButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				actionPerformed = DialogueAction.YES;
				stage.close();
			}
		});
		
		confirmButton.setDisable(true);
		
		noButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				actionPerformed = DialogueAction.NO;
				stage.close();
			}
		});
		
		buttonBox.getChildren().addAll(confirmButton, noButton);
		buttonBox.setSpacing(30);
		buttonBox.setAlignment(Pos.CENTER);
		
		root.add(icon, 1, 1);
		root.add(messageText, 2, 1);
		root.add(buttonBox, 2, 2);
		
	}
	
	@Override
	protected void show() {
		
		if (settings.getPropertyInt("confirmdelay") > 0) {
		
			Timeline confirmDelay = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
				
				int count = settings.getPropertyInt("confirmdelay");
				
			    @Override
			    public void handle(ActionEvent event) {
			    	if (count > 1) {
			    		confirmButton.setText(localizer.formatMessage("messagedialogueconfirm.yeswaiting", --count));
			    	} else {
						confirmButton.setText(localizer.getMessage("messagedialogueconfirm.yes"));
						confirmButton.setDisable(false);
			    	}
			    }
			    
			}));
			confirmDelay.setCycleCount(settings.getPropertyInt("confirmdelay"));
			confirmDelay.play();
		
		} else {
			confirmButton.setText(localizer.getMessage("messagedialogueconfirm.yes"));
			confirmButton.setDisable(false);
		}
		
		super.show();
		
	}
	
}
