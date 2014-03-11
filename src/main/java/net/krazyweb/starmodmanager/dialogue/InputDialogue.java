package net.krazyweb.starmodmanager.dialogue;

import java.io.File;
import java.nio.file.Paths;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import net.krazyweb.helpers.CSSHelper;
import net.krazyweb.helpers.FXHelper;
import net.krazyweb.starmodmanager.data.LocalizerModelFactory;
import net.krazyweb.starmodmanager.data.LocalizerModelInterface;
import net.krazyweb.starmodmanager.view.SettingsView;

public class InputDialogue extends MessageDialogue {
	
	//TODO
	//An application close request MUST close all other dialogues
	//Otherwise the program doesn't actually close

	private Label pathTitle;
	private TextField pathField;
	private Button pathButton;
	
	public InputDialogue(final String message, final String title, final MessageType messageType, final LocalizerModelInterface localizer) {
		super(message, title, messageType, localizer);
	}
	
	public InputDialogue(final String message, final String title, final MessageType messageType, final LocalizerModelFactory localizerFactory) {
		super(message, title, messageType, localizerFactory);
	}
	
	protected void build(final String message, final String title, final MessageType messageType) {
		
		super.build(message, title, messageType);
		
		root.getChildren().remove(messageText);

		pathTitle = new Label();
		pathTitle.setText(message);
		pathTitle.setId("settings-view-text-large");
		pathTitle.setTranslateX(10);
		pathTitle.setAlignment(Pos.TOP_LEFT);
		pathTitle.setPrefHeight(25);
		pathField = new TextField();
		pathField.setPrefHeight(37);
		pathField.prefWidthProperty().set(450);
		pathButton = new Button();
		pathButton.setId("settings-path-button");
		pathButton.setPrefHeight(37);
		pathButton.setPrefWidth(36);
		pathButton.setGraphic(new ImageView(new Image(SettingsView.class.getClassLoader().getResourceAsStream("folder-icon.png"))));
		
		pathButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {

				DirectoryChooser chooser = new DirectoryChooser();
				chooser.setInitialDirectory(new File("").getParentFile());
				
				File output = chooser.showDialog(new Stage());
				
				if (output != null) {
					pathField.setText(Paths.get(output.getAbsolutePath()).toAbsolutePath().toString());
				}
				
			}
		});

		icon = new ImageView(new Image(MessageDialogueConfirm.class.getClassLoader().getResourceAsStream("large-folder-icon.png")));
		Color color = CSSHelper.getColor("file-browser-icon-color", settings.getPropertyString("theme"));
		FXHelper.setColor(pathButton.getGraphic(), color);
		
		GridPane pathContainer = new GridPane();
		pathContainer.add(pathTitle, 1, 1);
		pathContainer.add(pathField, 1, 2);
		pathContainer.add(pathButton, 2, 2);
		
		confirmButton.setText(localizer.getMessage("inputdialogue.select"));
		confirmButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				actionPerformed = DialogueAction.OK;
				stage.close();
			}
		});
		
		root.getChildren().remove(confirmButton);
		
		GridPane.setColumnSpan(confirmButton, 2);
		GridPane.setHalignment(confirmButton, HPos.CENTER);
		
		root.setVgap(25);
		root.add(confirmButton, 1, 2);
		root.add(pathContainer, 2, 1);
		
	}
	
	public String getInputData() {
		return pathField.getText();
	}
	
}
