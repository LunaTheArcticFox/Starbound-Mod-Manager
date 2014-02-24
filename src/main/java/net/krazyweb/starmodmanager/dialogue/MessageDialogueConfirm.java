package net.krazyweb.starmodmanager.dialogue;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.util.Duration;
import net.krazyweb.starmodmanager.data.LocalizerModelFactory;
import net.krazyweb.starmodmanager.data.LocalizerModelInterface;
import net.krazyweb.starmodmanager.data.SettingsFactory;
import net.krazyweb.starmodmanager.data.SettingsModelInterface;

public class MessageDialogueConfirm extends MessageDialogue {

	protected Button noButton;
	protected SettingsModelInterface settings;
	
	public MessageDialogueConfirm(String message, String title, MessageType messageType, LocalizerModelInterface localizer) {
		super(message, title, messageType, localizer);
	}

	public MessageDialogueConfirm(String message, String title, MessageType messageType, LocalizerModelFactory localizer) {
		super(message, title, messageType, localizer);
	}
	
	@Override
	protected void build(final String message, final String title, final MessageType messageType) {
		
		settings = new SettingsFactory().getInstance();
		
		this.title = title;
		
		root = new GridPane();
		
		iconPlaceholder = new Text("[PLACEHOLDER]"); //TODO Icon
		messageText = new Text(message);
		confirmButton = new Button(localizer.formatMessage("messagedialogueconfirm.yeswaiting", settings.getPropertyInt("confirmdelay")));
		noButton = new Button(localizer.getMessage("messagedialogueconfirm.no"));
		
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
		
		root.add(iconPlaceholder, 1, 1);
		root.add(messageText, 2, 1);
		root.add(confirmButton, 2, 2);
		root.add(noButton, 3, 2);
		
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
