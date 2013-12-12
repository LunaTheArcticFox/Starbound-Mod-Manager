package application;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import application.Configuration.KeyValuePair;

public class ModManager extends Application {

	public ArrayList<Mod> mods = new ArrayList<Mod>();
	public Mod selectedMod = null;
	
	private Button installButton;
	private Button addNewModButton;
	private Button moveUpButton;
	private Button moveDownButton;
	private Button launchStarboundButton;
	private ScrollPane modListWrapper;
	private VBox modList;
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(final Stage primaryStage) {
		
		primaryStage.setTitle("Starbound Mod Manager - Version 1.3");
		
		Configuration.load(primaryStage);
		loadMods();
		
		BorderPane contents = new BorderPane();
		contents.setPadding(new Insets(20));
		
		Group left = new Group();
		GridPane leftButtons = new GridPane();
		Group right = new Group();
		modListWrapper = new ScrollPane();
		modListWrapper.setId("scrollpane");
		modListWrapper.setFitToWidth(true);
		modListWrapper.setHbarPolicy(ScrollBarPolicy.NEVER);
		modListWrapper.setVbarPolicy(ScrollBarPolicy.ALWAYS);
		
		modList = new VBox();
		modList.prefWidthProperty().bind(modListWrapper.widthProperty().subtract(16));
		modList.minHeightProperty().bind(modListWrapper.heightProperty().subtract(1));
		modList.setId("modlist");
		
		for (final Mod m : mods) {
			modList.getChildren().add(m.container);
			m.container.minWidthProperty().bind(modListWrapper.maxWidthProperty().subtract(16));
		}
		
		addNewModButton = new Button("Add New Mod");
		
		addNewModButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent e) {
				addNewMod(primaryStage);
			}
			
		});
		
		installButton = new Button("Install Mod");
		installButton.setDisable(true);
		
		moveUpButton = new Button("Up");
		moveUpButton.setTooltip(new Tooltip("Moves the mod priority higher.\nHigher priority mods will take precedence in the event of conflicts."));
		moveUpButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent e) {
				moveModUp();
			}
			
		});
		
		moveDownButton = new Button("Down");
		moveDownButton.setTooltip(new Tooltip("Moves the mod priority lower.\nHigher priority mods will take precedence in the event of conflicts."));
		moveDownButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent e) {
				moveModDown();
			}
			
		});
		
		launchStarboundButton = new Button("Launch Starbound");
		launchStarboundButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent e) {
				launchGame();
			}
			
		});
		
		leftButtons.add(installButton, 0, 0, 2, 1);
		leftButtons.add(addNewModButton, 0, 1, 2, 1);
		leftButtons.add(moveUpButton, 0, 2, 1, 1);
		leftButtons.add(moveDownButton, 1, 2, 1, 1);
		leftButtons.add(launchStarboundButton, 0, 3, 2, 1);
		
		leftButtons.setHgap(10);
		leftButtons.setVgap(10);
		
		ColumnConstraints col1 = new ColumnConstraints();
		col1.setPercentWidth(50);
		
		ColumnConstraints col2 = new ColumnConstraints();
		col2.setPercentWidth(50);
		
		installButton.prefWidthProperty().bind(leftButtons.widthProperty());
		addNewModButton.prefWidthProperty().bind(leftButtons.widthProperty());
		launchStarboundButton.prefWidthProperty().bind(leftButtons.widthProperty());
		moveUpButton.prefWidthProperty().bind(leftButtons.widthProperty().divide(2).subtract(5));
		moveDownButton.prefWidthProperty().bind(leftButtons.widthProperty().divide(2).subtract(5));
		
		leftButtons.setMinWidth(150);
		installButton.setMinHeight(75);
		addNewModButton.setMinHeight(40);
		moveUpButton.setMinHeight(40);
		moveDownButton.setMinHeight(40);
		launchStarboundButton.setMinHeight(80);

		leftButtons.getColumnConstraints().add(col1);
		leftButtons.getColumnConstraints().add(col2);

		left.getChildren().add(leftButtons);
		right.getChildren().add(modList);
		modListWrapper.setContent(right);
		
		contents.setLeft(left);
		contents.setCenter(modListWrapper);
		BorderPane.setMargin(modListWrapper, new Insets(0, 0, 0, 15));
		
		Scene s = new Scene(contents);
		s.getStylesheets().add(application.ModManager.class.getResource("styles.css").toExternalForm());
		
		primaryStage.setScene(s);
		primaryStage.setWidth(Integer.parseInt(Configuration.getProperty("width", "800")));
		primaryStage.setHeight(Integer.parseInt(Configuration.getProperty("height", "500")));
		primaryStage.setMinWidth(Integer.parseInt(Configuration.getProperty("width", "800")));
		primaryStage.setMinHeight(Integer.parseInt(Configuration.getProperty("height", "500")));
		primaryStage.show();
		
	}
	
	public void loadMods() {

		for (final KeyValuePair kvp : Configuration.getProperties("mods")) {
			
			final Mod m = new Mod(kvp.key, Boolean.parseBoolean(kvp.value));
			
			boolean modAlreadyExists = false;
			
			for (Mod mod : mods) {
				if (mod.name.equals(m.name)) {
					modAlreadyExists = true;
					System.out.println("Ignored duplicate mod '" + mod.name + "'. (" + m.file + ")");
				}
			}
			
			if (modAlreadyExists) {
				continue;
			}
			
			mods.add(m);
			
			m.container.setOnMouseClicked(new EventHandler<MouseEvent>() {
				
				@Override
				public void handle(MouseEvent e) {
					selectMod(m);
				}
				
			});
			
		}
		
		findConflicts();
		
	}
	
	private void findConflicts() {
		
		for (int i = 0; i < mods.size(); i++) {
			
			final Mod mod = mods.get(i);
			
			ArrayList<String> list1 = new ArrayList<String>(mod.filesModified);
			
			for (int ii = i - 1; ii >= 0; ii--) {
				
				if (ii < 0) {
					continue;
				}
				
				ArrayList<String> list2 = new ArrayList<String>(mods.get(ii).filesModified);
				list2.retainAll(list1);
				
				ArrayList<String> toRemove = new ArrayList<String>();
				
				for (String s : list2) {
					if (s.endsWith(".txt") || s.endsWith(".json")) {
						toRemove.add(s);
					}
				}
				
				list2.removeAll(toRemove);
				
				if (list2.size() > 0) {
					mod.setConflicted(true);
				}
				
			}
			
		}
		
		for (Mod m : mods) {
			m.updateStyles();
		}
		
	}
	
	private void addNewMod(Stage primaryStage) {
		
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select Mod File");
		
		final List<File> modFiles = fileChooser.showOpenMultipleDialog(primaryStage);
		
		if (modFiles == null) {
			return;
		}
		
		for (File f : modFiles) {
			
			if (f.getName().endsWith(".zip")) {
				
				File newFileLocation = new File(Configuration.modsFolder + File.separator + f.getName());
				
				try {
					FileHelper.copyFile(f, newFileLocation);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				final Mod m = new Mod(newFileLocation.getName(), false);

				modList.getChildren().add(m.container);
				m.container.minWidthProperty().bind(modListWrapper.maxWidthProperty().subtract(16));
				
				mods.add(m);
				
				Configuration.addProperty("mods", m.file, "false");
				
				m.container.setOnMouseClicked(new EventHandler<MouseEvent>() {
					
					@Override
					public void handle(MouseEvent e) {
						selectMod(m);
					}
					
				});
				
			}
			
			findConflicts();
			
		}
		
	}
	
	private void moveModUp() {
		
		int firstIndex = mods.indexOf(selectedMod);
		int secondIndex = firstIndex - 1;
		
		if (secondIndex >= 0) {
			
			Mod m1 = selectedMod;
			Mod m2 = mods.get(secondIndex);
			
			mods.set(secondIndex, m1);
			mods.set(firstIndex, m2);
			
		}
		
		modList.getChildren().clear();

		for (Mod m : mods) {
			m.setConflicted(false);
		}
		
		findConflicts();
		
		for (Mod m : mods) {
			modList.getChildren().add(m.container);
			Configuration.removeProperty(m.file);
			Configuration.addProperty("mods", m.file, m.installed + "");
		}
		
		ArrayList<Mod> installedMods = new ArrayList<Mod>();
		
		for (Mod mod : mods) {
			if (mod.installed) {
				installedMods.add(mod);
			}
		}
		
		Configuration.updateBootstrap(installedMods);
		
	}
	
	private void moveModDown() {
		
		int firstIndex = mods.indexOf(selectedMod);
		int secondIndex = firstIndex + 1;
		
		if (secondIndex < mods.size()) {
			
			Mod m1 = selectedMod;
			Mod m2 = mods.get(secondIndex);
			
			mods.set(secondIndex, m1);
			mods.set(firstIndex, m2);
			
		}
		
		modList.getChildren().clear();
		
		for (Mod m : mods) {
			modList.getChildren().add(m.container);
			Configuration.removeProperty(m.file);
			Configuration.addProperty("mods", m.file, m.installed + "");
		}
		
		findConflicts();
		
		ArrayList<Mod> installedMods = new ArrayList<Mod>();
		
		for (Mod mod : mods) {
			if (mod.installed) {
				installedMods.add(mod);
			}
		}
		
		Configuration.updateBootstrap(installedMods);
		
	}
	
	public void selectMod(Mod m) {
		
		for (Mod mod : mods) {
			if (m != mod) {
				mod.modName.getStyleClass().removeAll("selected");
			} else {
				selectedMod = mod;
				mod.modName.getStyleClass().add("selected");
			}
		}
		
		if (selectedMod.installed) {
			
			installButton.setText("Uninstall Mod");
			installButton.setDisable(false);
			
			installButton.setOnAction(new EventHandler<ActionEvent>() {
				
				@Override
				public void handle(ActionEvent e) {
					uninstallMod();
				}
				
			});
			
		} else if (selectedMod.hasConflicts) {
			
			installButton.setText("Install Mod");
			
		} else {
			
			installButton.setText("Install Mod");
			installButton.setDisable(false);
			
			installButton.setOnAction(new EventHandler<ActionEvent>() {
				
				@Override
				public void handle(ActionEvent e) {
					installMod();
				}
				
			});
			
		}
		
	}
	
	public void installMod() {
		
		uninstallMod();
		
		if (selectedMod == null) {
			return;
		}
		
		selectedMod.installed = true;
		
		ArrayList<Mod> installedMods = new ArrayList<Mod>();
		
		for (Mod mod : mods) {
			if (mod.installed) {
				installedMods.add(mod);
			}
		}
		
		selectedMod.install(installedMods);

		installButton.setText("Uninstall Mod");
		installButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent e) {
				uninstallMod();
			}
			
		});
		
		for (Mod mod : mods) {
			Configuration.removeProperty(mod.file);
			Configuration.addProperty("mods", mod.file, mod.installed + "");
		}

	}
	
	public void uninstallMod() {
		
		if (selectedMod == null) {
			return;
		}
		
		ArrayList<Mod> installedMods = new ArrayList<Mod>();
		
		for (Mod mod : mods) {
			if (mod.installed) {
				installedMods.add(mod);
			}
		}
		
		installedMods.remove(selectedMod);
		
		selectedMod.uninstall(installedMods);

		installButton.setText("Install Mod");
		installButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent e) {
				installMod();
			}
			
		});
		
		Configuration.updateBootstrap(installedMods);
		
	}
	
	public void launchGame() {
		
		try {
			FileHelper.deleteFile(new File(Configuration.backupFolder.getAbsolutePath() + File.separator + "player"));
			FileHelper.deleteFile(new File(Configuration.backupFolder.getAbsolutePath() + File.separator + "universe"));
			FileHelper.copyFolder(new File(Configuration.starboundFolder.getAbsolutePath() + File.separator + "player"), new File(Configuration.backupFolder.getAbsolutePath() + File.separator + "player"));
			FileHelper.copyFolder(new File(Configuration.starboundFolder.getAbsolutePath() + File.separator + "universe"), new File(Configuration.backupFolder.getAbsolutePath() + File.separator + "universe"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Runtime rt = Runtime.getRuntime() ;
		
		switch (Configuration.systemType) {
		
			case "Windows":
				
				try {
					rt.exec(Configuration.starboundFolder.getAbsolutePath() + File.separator + "win32" + File.separator + "Starbound.exe");
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				break;
		
			case "Linux (32-Bit)":
				
				try {
					rt.exec(Configuration.starboundFolder.getAbsolutePath() + File.separator + "linux32" + File.separator + "launch_starbound.sh");
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				break;
		
			case "Linux (64-Bit)":
				
				try {
					rt.exec(Configuration.starboundFolder.getAbsolutePath().replaceAll(" ", "_") + File.separator + "linux64" + File.separator + "launch_starbound.sh");
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				break;
		
			case "Mac OS":
				
				try {
					rt.exec("open " + Configuration.starboundFolder.getAbsolutePath().replaceAll(" ", "_") + File.separator + "Starbound.app");
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				break;
			
		
		}
		
	}
	
}
