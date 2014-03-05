package net.krazyweb.starmodmanager.view;


import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import net.krazyweb.helpers.CSSHelper;
import net.krazyweb.helpers.FXHelper;
import net.krazyweb.starmodmanager.data.Localizer;
import net.krazyweb.starmodmanager.data.LocalizerFactory;
import net.krazyweb.starmodmanager.data.LocalizerModelInterface;
import net.krazyweb.starmodmanager.data.ModList;
import net.krazyweb.starmodmanager.data.Observable;
import net.krazyweb.starmodmanager.data.Observer;
import net.krazyweb.starmodmanager.data.SettingsFactory;
import net.krazyweb.starmodmanager.data.SettingsModelInterface;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModListView implements Observer {
	
	@SuppressWarnings("unused")
	private static final Logger log = LogManager.getLogger(ModListView.class);
	
	private VBox root;
	private VBox modsBox;
	
	private AnchorPane addModPane;
	private Button addModButton;
	
	private ModListViewController controller;
	
	private LocalizerModelInterface localizer;
	private SettingsModelInterface settings;
	
	protected ModListView(final ModList modList) {
		settings = new SettingsFactory().getInstance();
		localizer = new LocalizerFactory().getInstance();
		localizer.addObserver(this);
		this.controller = new ModListViewController(this, modList);
	}
	
	protected void build() {
		
		root = new VBox();
		root.setSpacing(25.0);
		
		modsBox = new VBox();
		modsBox.setSpacing(10.0);
		
		addModButton = new Button();
		addModButton.prefWidthProperty().bind(root.widthProperty());
		addModButton.setGraphic(new ImageView(new Image(ModView.class.getClassLoader().getResourceAsStream("add-mods-plus.png"))));
		addModButton.setId("modlistview-add-mods-button");
		addModButton.setGraphicTextGap(15);
		
		addModPane = new AnchorPane();
		VBox.setVgrow(addModPane, Priority.ALWAYS);
		addModPane.getChildren().add(addModButton);
		addModButton.prefWidthProperty().bind(root.widthProperty().subtract(20));
		AnchorPane.setBottomAnchor(addModButton, 0.0);
		AnchorPane.setLeftAnchor(addModButton, 0.0);
		
		root.getChildren().addAll(
			modsBox,
			addModPane
		);
		
		createListeners();
		updateStrings();
		updateColors();
		
	}
	
	private void createListeners() {
		
		addModButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				controller.addModButtonClicked();
			}
		});
		
	}
	
	private void updateStrings() {
		
		addModButton.setText(localizer.getMessage("modlistview.addmodsbutton"));
				
	}
	
	private void updateColors() {
		FXHelper.setColor(addModButton.getGraphic(), CSSHelper.getColor("modlistview-add-mods-button-color", settings.getPropertyString("theme")));
	}
	
	protected VBox getContent() {
		return root;
	}
	
	protected void clearMods() {
		modsBox.getChildren().clear();
	}
	
	protected void addMod(final ModView modView) {
		createDragListeners(modView);
		modsBox.getChildren().add(modView.getContent());
	}
	
	protected void removeMod(final ModView modView) {
		modsBox.getChildren().remove(modView.getContent());
	}
	
	private void createDragListeners(final ModView modView) {
		
		modView.getContent().setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent e) {
				controller.modViewMousePressed(modView, e);
				e.consume();
			}
		});
		
		modView.getContent().setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent e) {
				controller.modViewMouseDragged(modView, e);
				e.consume();
			}
		});
		
		modView.getContent().setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent e) {
				controller.modViewMouseReleased(modView, e);
				e.consume();
			}
		});
		
	}
	
	protected void animate(final ModView modView, final int amount) {
		
		 TranslateTransition tt = new TranslateTransition(Duration.millis(80), modView.getContent());
	     tt.setByY(amount);
	     tt.setCycleCount(1);
	     tt.setAutoReverse(false);
	     tt.setOnFinished(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				modView.moving = false;
			}
		});
	 
	     tt.play();
		
	}
	
	protected VBox getModsBox() {
		return modsBox;
	}
	
	protected void toggleExpansion() {
		controller.toggleExpansion();
	}
	
	protected void toggleLock() {
		controller.toggleLock();
	}
	
	protected void getNewMods() {
		controller.getNewMods();
	}
	
	@Override
	public void update(final Observable observable, final Object message) {
		
		if (observable instanceof Localizer && message.equals("localechanged")) {
			updateStrings();
		}
		
	}

}