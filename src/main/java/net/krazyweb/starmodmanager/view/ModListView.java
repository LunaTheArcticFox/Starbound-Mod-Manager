package net.krazyweb.starmodmanager.view;


import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import net.krazyweb.starmodmanager.data.Localizer;
import net.krazyweb.starmodmanager.data.LocalizerFactory;
import net.krazyweb.starmodmanager.data.LocalizerModelInterface;
import net.krazyweb.starmodmanager.data.ModList;
import net.krazyweb.starmodmanager.data.Observable;
import net.krazyweb.starmodmanager.data.Observer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModListView implements Observer {
	
	@SuppressWarnings("unused")
	private static final Logger log = LogManager.getLogger(ModListView.class);
	
	private VBox root;
	private VBox modsBox;
	
	private Button addModButton;
	
	private ModListViewController controller;
	
	private LocalizerModelInterface localizer;
	
	protected ModListView(final ModList modList) {
		localizer = new LocalizerFactory().getInstance();
		localizer.addObserver(this);
		this.controller = new ModListViewController(this, modList);
	}
	
	protected void build() {
		
		root = new VBox();
		root.setSpacing(25.0);
		
		modsBox = new VBox();
		modsBox.setSpacing(25.0);
		
		addModButton = new Button();
		
		root.getChildren().addAll(
			modsBox,
			addModButton
		);
		
		createListeners();
		updateStrings();
		
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
	
	@Override
	public void update(final Observable observable, final Object message) {
		
		if (observable instanceof Localizer && message.equals("localechanged")) {
			updateStrings();
		}
		
	}

}