package net.krazyweb.starmodmanager.view;

import java.util.Observable;
import java.util.Observer;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import net.krazyweb.starmodmanager.data.Localizer;
import net.krazyweb.starmodmanager.data.ModList;

import org.apache.log4j.Logger;

public class ModListView implements Observer {
	
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(ModListView.class);
	
	private VBox root;
	private VBox modsBox;
	
	private Button addModButton;
	
	private ModListViewController controller;
	
	protected ModListView(final ModList modList) {
		this.controller = new ModListViewController(this, modList);
		Localizer.getInstance().addObserver(this);
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
		
		addModButton.setText(Localizer.getInstance().getMessage("modlistview.addmodsbutton"));
				
	}
	
	protected Node getContent() {
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
	
	/*protected void buildUI(final ModList modList) {
		
		root.setSpacing(25.0);
		
		modsBox = new VBox();
		modsBox.setSpacing(25.0);
		
		Button addMod = new Button();
		addMod.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(final ActionEvent e) {
				
				FileChooser f = new FileChooser();
				f.setTitle(Localizer.getMessage("modlistview.modfilechoosertitle"));
				
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
	
	public void addMods(final List<Path> files) {
		
		log.info("Adding mods: " + files);
		modList.addMods(files);
		
	}
	
	public void updateModList(final List<Mod> mods) {
		
		modsBox.getChildren().clear();
		
		this.mods = mods;
		
		if (modViews == null) {
			modViews = new HashMap<>();
		}
		
		final Set<Mod> toRemove = new HashSet<>();
		
		for (final Mod mod : mods) {
			
			if (mod.isHidden()) {
				toRemove.add(mod);
				continue;
			}
			
			if (!modViews.containsKey(mod)) {
				ModView modView = new ModView(mod, modList);
				modViews.put(mod, modView);
			}
			
			ModView modView = modViews.get(mod);
			modView.update();

			if (modList.isLocked()) {
				removeDragAndDrop(modView);
			} else {
				setDragAndDrop(modView);
			}
			
			modsBox.getChildren().add(modView);
			
		}
		
		this.mods.removeAll(toRemove);
		
	}
	
	private void getModList() {
		
		mods = modList.getMods();
		
		final Set<Mod> toRemove = new HashSet<>();

		for (final Mod mod : mods) {
			
			if (mod.isHidden()) {
				toRemove.add(mod);
			}
			
		}
		
		mods.removeAll(toRemove);
		
	}*/	
	/*private void removeDragAndDrop(final ModView modView) {
		
		modView.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent e) {
				e.consume();
				return;
			}
		});
		
		modView.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent e) {
				e.consume();
				return;
			}
		});
		
		modView.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent e) {
				e.consume();
				return;
			}
		});
		
	}*/
	
	protected void animate(final ModView modView, final int amount) {
		
		Timeline timeline = new Timeline();
		timeline.setAutoReverse(false);
		final KeyValue kv = new KeyValue(modView.getContent().layoutYProperty(), modView.getContent().getLayoutY() + amount); //Height + spacing
		final KeyFrame kf = new KeyFrame(Duration.millis(80), kv);
		timeline.getKeyFrames().add(kf);
		timeline.setOnFinished(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				modView.moving = false;
			}
		});
		timeline.play();
		
	}
	
	protected VBox getModsBox() {
		return modsBox;
	}
	
	/*protected void toggleLock() {
		
		if (modList.isLocked()) {
			modList.unlockList();
		} else {
			modList.lockList();
		}
		
	}
		
	}*/
	
	@Override
	public void update(final Observable observable, final Object message) {
		
		if (observable instanceof Localizer && message.equals("localechanged")) {
			updateStrings();
		}
		
	}

}