package main.java.net.krazyweb.starmodmanager.view;

import org.apache.log4j.Logger;

public class ModListView /*implements Observer*/ {
	
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(ModListView.class);
	
	/*private Map<Mod, ModView> modViews;
	private VBox modsBox;
	private List<Mod> mods;
	private double y, mouseY, lastY;
	
	private VBox root;
	
	private ModListViewController controller;
	
	protected ModListView(final ModListViewController c) {
		this.controller = c;
		Localizer.getInstance().addObserver(this);
	}
	
	protected void buildUI(final ModList modList) {
		
		root.setSpacing(25.0);
		
		modsBox = new VBox();
		modsBox.setSpacing(25.0);
		
		Button addMod = new Button(Localizer.getMessage("modlistview.addmodsbutton"));
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
		
	}
	
	private void setDragAndDrop(final ModView modView) {
		
		modView.setOnMousePressed(new EventHandler<MouseEvent>() {

			@Override
			public void handle(final MouseEvent e) {
				lastY = y = modView.getLayoutY();
				mouseY = e.getSceneY();
				modView.toFront();
			}
			
		});
		
		modView.setOnMouseDragged(new EventHandler<MouseEvent>() {

			@Override
			public void handle(final MouseEvent e) {

				lastY = modView.getLayoutY();
				modView.setLayoutY(y + e.getSceneY() - mouseY);
				
				if (modView.getLayoutY() < modsBox.getLayoutY()) {
					modView.setLayoutY(modsBox.getLayoutY());
				}
				
				if (modView.getLayoutY() + modView.getHeight() > modsBox.getLayoutY() + modsBox.getHeight()) {
					modView.setLayoutY(modsBox.getLayoutY() + modsBox.getHeight() - modView.getHeight());
				}
				
				if (mods.indexOf(modView.mod) > 0 && modView.getLayoutY() < lastY) {
					
					final ModView mv = modViews.get(mods.get(mods.indexOf(modView.mod) - 1));
					
					if (!mv.moving && mv.getLayoutY() > modView.getLayoutY() - 16) { //16  = half the node height
						mv.moving = true;
						modList.moveMod(modView.mod, 1);
						getModList();
						animate(mv, 57);
					}
					
				}
				
				if (mods.indexOf(modView.mod) < mods.size() - 1 && modView.getLayoutY() > lastY) {
					
					final ModView mv = modViews.get(mods.get(mods.indexOf(modView.mod) + 1));
					
					if (!mv.moving && mv.getLayoutY() < modView.getLayoutY() + modView.getHeight() - 16) { //16  = half the node height
						mv.moving = true;
						modList.moveMod(modView.mod, -1);
						getModList();
						animate(mv, -57);
					}
					
				}
				
				e.consume();
				
			}
			
		});
		
		modView.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent e) {
				modView.setLayoutY(y);
				modList.requestUpdate();
			}
		});
		
	}
	
	private void removeDragAndDrop(final ModView modView) {
		
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
		
	}
	
	private void animate(final ModView modView, final int amount) {
		
		Timeline timeline = new Timeline();
		timeline.setAutoReverse(false);
		final KeyValue kv = new KeyValue(modView.layoutYProperty(), modView.getLayoutY() + amount); //Height + spacing
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
	
	protected void toggleLock() {
		
		if (modList.isLocked()) {
			modList.unlockList();
		} else {
			modList.lockList();
		}
		
	}
	
	private void updateStrings() {
		
	}*/
	
	/*@Override
	public void update(final Observable observable, final Object message) {
		
		if (observable instanceof Localizer && message.equals("localechanged")) {
			//updateStrings();
		}
		
	}*/

}