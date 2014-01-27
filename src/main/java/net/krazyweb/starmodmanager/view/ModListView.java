package main.java.net.krazyweb.starmodmanager.view;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import main.java.net.krazyweb.starmodmanager.data.Mod;
import main.java.net.krazyweb.starmodmanager.data.ModList;

import org.apache.log4j.Logger;

public class ModListView extends VBox {
	
	private static final Logger log = Logger.getLogger(ModListView.class);
	
	private ModList modList;
	
	private Map<Mod, ModView> modViews;
	
	private VBox modsBox;
	
	private List<Mod> mods;
	
	private double y, mouseY, lastY;
	
	public ModListView(final MainView mainView) {
		
		setSpacing(25.0);
		
		modsBox = new VBox();
		modsBox.setSpacing(25.0);
		
		Button addMod = new Button("Add Mods");
		addMod.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent e) {
				
				FileChooser f = new FileChooser();
				f.setTitle("Select the mod to add.");
				
				List<Path> paths = new ArrayList<>();
				List<File> files = f.showOpenMultipleDialog(mainView.getStage());
				
				if (files != null && !files.isEmpty()) {
				
					for (File file : files) {
						paths.add(file.toPath());
					}
					
					modList.addMods(paths);
				
				}
				
			}
			
		});
		
		getChildren().addAll(modsBox, addMod);
		
		modList = new ModList(this);
		modList.requestUpdate();
		
	}
	
	public void addMod(final Path file) {
		
		log.info("Adding mod: " + file);
		
		List<Path> toAdd = new ArrayList<>();
		toAdd.add(file);
		
		modList.addMods(toAdd);
		
	}
	
	public void updateModList(final List<Mod> mods) {
		
		modsBox.getChildren().clear();
		
		this.mods = mods;
		
		if (modViews == null) {
			modViews = new HashMap<>();
		}
		
		for (final Mod mod : mods) {
			
			if (!modViews.containsKey(mod)) {
				ModView modView = new ModView(mod, modList);
				setDragAndDrop(modView);
				modViews.put(mod, modView);
			}
			
			ModView modView = modViews.get(mod);
			modView.update();
			
			modsBox.getChildren().add(modView);
			
		}
		
	}
	
	private void setDragAndDrop(final ModView modView) {
		
		modView.setOnMousePressed(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent e) {
				lastY = y = modView.getLayoutY();
				mouseY = e.getSceneY();
			}
			
		});
		
		modView.setOnMouseDragged(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent e) {

				lastY = modView.getLayoutY();
				modView.setLayoutY(y + e.getSceneY() - mouseY);
				
				if (modView.getLayoutY() < modsBox.getLayoutY()) {
					modView.setLayoutY(modsBox.getLayoutY());
				}
				
				if (modView.getLayoutY() + modView.getHeight() > modsBox.getLayoutY() + modsBox.getHeight()) {
					modView.setLayoutY(modsBox.getLayoutY() + modsBox.getHeight() - modView.getHeight());
				}
				
				if (modList.indexOf(modView.mod) > 0 && modView.getLayoutY() < lastY) {
					
					final ModView mv = modViews.get(mods.get(modList.indexOf(modView.mod) - 1));
					
					if (!mv.moving && mv.getLayoutY() > modView.getLayoutY() - 16) {
						mv.moving = true;
						modList.moveMod(modView.mod, 1);
						mods = modList.getMods();
						Timeline timeline = new Timeline();
						timeline.setAutoReverse(false);
						final KeyValue kv = new KeyValue(mv.layoutYProperty(), mv.getLayoutY() + 57); //Height + spacing
						final KeyFrame kf = new KeyFrame(Duration.millis(80), kv);
						timeline.getKeyFrames().add(kf);
						timeline.setOnFinished(new EventHandler<ActionEvent>() {
							@Override
							public void handle(ActionEvent e) {
								mv.moving = false;
							}
						});
						timeline.play();
					}
					
				}
				
				if (modList.indexOf(modView.mod) < mods.size() - 1 && modView.getLayoutY() > lastY) {
					
					final ModView mv = modViews.get(mods.get(modList.indexOf(modView.mod) + 1));
					
					if (!mv.moving && mv.getLayoutY() < modView.getLayoutY() + modView.getHeight() + 16) {
						mv.moving = true;
						modList.moveMod(modView.mod, -1);
						mods = modList.getMods();
						Timeline timeline = new Timeline();
						timeline.setAutoReverse(false);
						final KeyValue kv = new KeyValue(mv.layoutYProperty(), mv.getLayoutY() - 57); //Height + spacing
						final KeyFrame kf = new KeyFrame(Duration.millis(80), kv);
						timeline.getKeyFrames().add(kf);
						timeline.setOnFinished(new EventHandler<ActionEvent>() {
							@Override
							public void handle(ActionEvent e) {
								mv.moving = false;
							}
						});
						timeline.play();
					}
					
				}
				
				e.consume();
				
			}
			
		});
		
		modView.setOnMouseReleased(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent e) {
				
				/*if (modView.getLayoutY() - 20 > y) {
				} else if (modView.getLayoutY() + 20 < y) { 
					modList.moveMod(modView.mod, 1);
				} else {*/
					modView.setLayoutY(y);
				//}
				
				modList.requestUpdate();
				
			}
			
		});
		
	}

}