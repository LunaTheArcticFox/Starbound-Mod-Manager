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
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import org.tmatesoft.sqljet.core.SqlJetException;

import application.Configuration.KeyValuePair;

public class ModManager extends Application {
	
	public static final int MAJOR_VERSION = 1;
	public static final int MINOR_VERSION = 6;
	public static final int PATCH_VERSION = 0;
	
	public static final String VERSION_STRING = MAJOR_VERSION + "." + MINOR_VERSION + "." + PATCH_VERSION;;
	
	public ArrayList<Mod> mods = new ArrayList<Mod>();
	public Mod selectedMod = null;
	
	private Button installButton;
	private Button addNewModButton;
	private Button removeModButton;
	private Button moveUpButton;
	private Button moveDownButton;
	private Button launchStarboundButton;
	private ScrollPane modListWrapper;
	private VBox modList;
	private Text modInformation;
	
	public static void main(String[] args) {
		try {
			launch(args);
		} catch (Exception e) {
			Configuration.printException(e, "Uncaught Exception");
			System.exit(-1);
		}
	}

	@Override
	public void start(final Stage primaryStage) {
		
		try {
			Database.connect();
		} catch (SqlJetException e1) {
			new FXDialogueConfirm("Could not open the database.\nPlease report this bug alongside the errors.log file inside your mod manager's folder.");
			Configuration.printException(e1);
		}
		
		primaryStage.setTitle("Starbound Mod Manager - Version " + VERSION_STRING);
		
		Configuration.load(primaryStage);
		
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
            	Configuration.addProperty("windowsettings", "width", "" + (int) primaryStage.getWidth());
            	Configuration.addProperty("windowsettings", "height", "" + (int) primaryStage.getHeight());
            	Database.closeConnection();
            	primaryStage.close();
                event.consume();
            }
        });
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
		
		removeModButton = new Button("Delete Mod");
		removeModButton.setDisable(true);
		
		removeModButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent e) {
				deleteMod(primaryStage);
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
		leftButtons.add(removeModButton, 0, 3, 2, 1);
		leftButtons.add(launchStarboundButton, 0, 4, 2, 1);
		
		leftButtons.setHgap(10);
		leftButtons.setVgap(10);
		
		ColumnConstraints col1 = new ColumnConstraints();
		col1.setPercentWidth(50);
		
		ColumnConstraints col2 = new ColumnConstraints();
		col2.setPercentWidth(50);
		
		installButton.prefWidthProperty().bind(leftButtons.widthProperty());
		addNewModButton.prefWidthProperty().bind(leftButtons.widthProperty());
		removeModButton.prefWidthProperty().bind(leftButtons.widthProperty());
		launchStarboundButton.prefWidthProperty().bind(leftButtons.widthProperty());
		moveUpButton.prefWidthProperty().bind(leftButtons.widthProperty().divide(2).subtract(5));
		moveDownButton.prefWidthProperty().bind(leftButtons.widthProperty().divide(2).subtract(5));
		
		leftButtons.setMinWidth(150);
		installButton.setMinHeight(75);
		addNewModButton.setMinHeight(40);
		removeModButton.setMinHeight(40);
		moveUpButton.setMinHeight(40);
		moveDownButton.setMinHeight(40);
		launchStarboundButton.setMinHeight(80);

		leftButtons.getColumnConstraints().add(col1);
		leftButtons.getColumnConstraints().add(col2);

		left.getChildren().add(leftButtons);
		right.getChildren().add(modList);
		modListWrapper.setContent(right);
		
		VBox temp = new VBox();
		modInformation = new Text("Mod information will appear here when a mod is selected.");
		temp.getChildren().add(modInformation);
		VBox.setMargin(modInformation, new Insets(15, 0, 0, 15));
		temp.prefWidthProperty().bind(leftButtons.widthProperty().multiply(1.5));
		temp.setId("modInformation");
		modInformation.setWrappingWidth(220);
		
		contents.setLeft(left);
		contents.setCenter(modListWrapper);
		contents.setRight(temp);
		BorderPane.setMargin(modListWrapper, new Insets(0, 15, 0, 15));
		
		Scene s = new Scene(contents);
		s.getStylesheets().add(application.ModManager.class.getResource("styles.css").toExternalForm());
		
		primaryStage.setScene(s);
		primaryStage.setWidth(Integer.parseInt(Configuration.getProperty("width", "950")));
		primaryStage.setHeight(Integer.parseInt(Configuration.getProperty("height", "550")));
		primaryStage.setMinWidth(950);
		primaryStage.setMinHeight(550);
		primaryStage.show();
		
	}
	
	public void loadMods() {
		
		try {
			for (final KeyValuePair kvp : Database.getModList()) {
				
				boolean installed = false;
				
				if (kvp.value.equals("1")) {
					installed = true;
				}
				
				final Mod m = Mod.loadMod(kvp.key, installed);
			
				if (m == null) {
					continue;
				}
				
				boolean modAlreadyExists = false;
				
				for (Mod mod : mods) {
					if (mod.internalName.equals(m.internalName)) {
						modAlreadyExists = true;
						System.out.println("Ignored duplicate mod '" + mod.internalName + "'. (" + m.file + ")");
						break;
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
		} catch (SqlJetException e) {
			new FXDialogueConfirm("An error occurred while loading mods.\nPlease report this bug alongside the errors.log file inside your mod manager's folder.");
			Configuration.printException(e);
		}
		
		findConflicts();

		ArrayList<Mod> installedMods = new ArrayList<Mod>();
		
		for (Mod mod : mods) {
			if (mod.installed) {
				installedMods.add(mod);
			}
		}
			
	}
	
	private void findConflicts() {
		
		for (int i = 0; i < mods.size(); i++) {
			
			final Mod mod = mods.get(i);
			
			mod.conflicts = "";
			
			ArrayList<String> list1 = new ArrayList<String>(mod.filesModified);
			
			for (int ii = i - 1; ii >= 0; ii--) {
				
				if (ii < 0) {
					continue;
				}
				
				ArrayList<String> list2 = new ArrayList<String>(mods.get(ii).filesModified);
				list2.retainAll(list1);
				
				ArrayList<String> toRemove = new ArrayList<String>();
				
				for (String s : list2) {
					if (s.endsWith(".modinfo") || s.toLowerCase().equals("readme.txt")) {
						toRemove.add(s);
					}
				}
				
				list2.removeAll(toRemove);
				
				if (list2.size() > 0) {
					mod.setConflicted(true);
					mod.conflicts += " - " + mods.get(ii).displayName + "\n";
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
		
		String initDir = Configuration.getProperty("modChooserFile", "null");
		
		if (!initDir.equals("null")) {
			File f = new File(initDir);
			if (f.exists()) {
				fileChooser.setInitialDirectory(f);
			}
		}
		
		final List<File> modFiles = fileChooser.showOpenMultipleDialog(primaryStage);
		
		if (modFiles == null || modFiles.size() == 0) {
			return;
		}
		
		Configuration.addProperty("paths", "modChooserFile", modFiles.get(0).getParent());
		
		for (File f : modFiles) {
			
			if (f.getName().endsWith(".zip") || f.getName().endsWith(".rar") || f.getName().endsWith(".7z") || f.getName().endsWith(".pak")) {
				
				File newFileLocation = new File(Configuration.modsFolder + File.separator + f.getName());
				
				try {
					FileHelper.copyFile(f, newFileLocation);
				} catch (IOException e) {
					new FXDialogueConfirm("An error occurred while adding mods.\nPlease report this bug alongside the errors.log file inside your mod manager's folder.");
					Configuration.printException(e, "Copying file: " + f.getAbsolutePath() + " to " + newFileLocation.getAbsolutePath());
				}
				
				Mod tempMod = null;
				
				tempMod = Mod.loadMod(newFileLocation.getName(), false);
				
				final Mod m = tempMod;
				
				if (newFileLocation.getAbsolutePath().endsWith(".pak")) {
					try {
						FileHelper.deleteFile(newFileLocation);
					} catch (IOException e) {
						new FXDialogueConfirm("The mod manager could not delete temporary file: " + newFileLocation.getAbsolutePath());
						Configuration.printException(e);
					}
				}
				
				if (m == null) {
					try {
						if (newFileLocation.getAbsolutePath().endsWith(".pak")) {
							FileHelper.deleteFile(newFileLocation.getAbsolutePath().replace(".pak", ".zip"));
						} else {
							FileHelper.deleteFile(newFileLocation);
						}
					} catch (IOException e1) {
						new FXDialogueConfirm("The mod manager could not delete temporary file: " + newFileLocation.getAbsolutePath());
						Configuration.printException(e1, "Deleting a mod's archive when it's not valid when adding.");
					}
					continue;
				}

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
	
	private void deleteMod(Stage primaryStage) {
		
		if (selectedMod == null) {
			return;
		}
		
		if (!new FXDialogueYesNo("Really delete \"" + selectedMod.displayName + "\"?\nThis action cannot be undone.").show()) {
			return;
		}

		try {
			Database.removeMod(selectedMod.file);
		} catch (SqlJetException e1) {
			new FXDialogueConfirm("An error occurred while removing a mod from the database.\nPlease report this bug alongside the errors.log file inside your mod manager's folder.");
			Configuration.printException(e1);
		}
		
		try {
			FileHelper.deleteFile(Configuration.modsFolder + File.separator + selectedMod.file);
		} catch (IOException e) {
			new FXDialogueConfirm("An error occurred while deleting a mod.\nPlease report this bug alongside the errors.log file inside your mod manager's folder.");
			Configuration.printException(e, "Deleting a mod.");
		}

		ArrayList<Mod> installedMods = new ArrayList<Mod>();
		
		for (Mod mod : mods) {
			if (mod.installed) {
				installedMods.add(mod);
			}
		}
		
		if (selectedMod.installed || selectedMod.patched) {
			installedMods.remove(selectedMod);
			selectedMod.uninstall(installedMods);
		}
		
		modList.getChildren().remove(selectedMod.container);
		mods.remove(selectedMod);
		
		findConflicts();
		
		selectedMod = null;
		
		installButton.setDisable(true);
		removeModButton.setDisable(true);
		
	}
	
	private void moveModUp() {
		
		if (selectedMod == null) {
			return;
		}
		
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
			try {
				Database.updateMod(m);
			} catch (SqlJetException e) {
				new FXDialogueConfirm("An error occurred while updating a mod in the database.\nPlease report this bug alongside the errors.log file inside your mod manager's folder.");
				Configuration.printException(e);
			}
		}
		
	}
	
	private void moveModDown() {
		
		if (selectedMod == null) {
			return;
		}
		
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
			m.setConflicted(false);
		}
		
		findConflicts();
		
		for (Mod m : mods) {
			modList.getChildren().add(m.container);
			try {
				Database.updateMod(m);
			} catch (SqlJetException e) {
				new FXDialogueConfirm("An error occurred while updating a mod in the database.\nPlease report this bug alongside the errors.log file inside your mod manager's folder.");
				Configuration.printException(e);
			}
		}
		
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
		
		removeModButton.setDisable(false);
		
		if (selectedMod.installed) {
			
			installButton.setText("Uninstall Mod");
			installButton.setDisable(false);
			
			installButton.setOnAction(new EventHandler<ActionEvent>() {
				
				@Override
				public void handle(ActionEvent e) {
					uninstallMod();
					updateSelectedModInfo();
				}
				
			});
			
		} else {
			
			installButton.setText("Install Mod");
			installButton.setDisable(false);
			
			installButton.setOnAction(new EventHandler<ActionEvent>() {
				
				@Override
				public void handle(ActionEvent e) {
					installMod();
					updateSelectedModInfo();
				}
				
			});
			
		}
		
		updateSelectedModInfo();
		
	}
	
	public void updateSelectedModInfo() {
		
		String modInfoText = "";
		
		modInfoText += "Description:\n" + selectedMod.description + "\n";
		
		if (selectedMod.hasConflicts) {
			modInfoText += "\n\n\nThis mod has conflicts with the following mods:\n\n" + selectedMod.conflicts;
			modInfoText += "\n\nThis mod may still be installed and the mod manager will attempt to merge the mods automatically.\n\nThis results in a successful merge most of the time and you should try it.";
		}

		if (selectedMod.installed) {
			modInfoText += "\n\nThis mod is currently installed.";
		} else {
			modInfoText += "\n\nThis mod has not been installed.";
		}
		
		modInformation.setText(modInfoText);
		
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
				updateSelectedModInfo();
			}
			
		});
		
		for (Mod mod : mods) {
			try {
				Database.updateMod(mod);
			} catch (SqlJetException e) {
				new FXDialogueConfirm("An error occurred while updating a mod in the database.\nPlease report this bug alongside the errors.log file inside your mod manager's folder.");
				Configuration.printException(e);
			}
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
				updateSelectedModInfo();
			}
			
		});
		
	}
	
	public void launchGame() {
		
		try {
			FileHelper.deleteFile(new File(Configuration.backupFolder.getAbsolutePath() + File.separator + "player"));
			FileHelper.deleteFile(new File(Configuration.backupFolder.getAbsolutePath() + File.separator + "universe"));
			FileHelper.copyFolder(new File(Configuration.starboundFolder.getAbsolutePath() + File.separator + "player"), new File(Configuration.backupFolder.getAbsolutePath() + File.separator + "player"));
			FileHelper.copyFolder(new File(Configuration.starboundFolder.getAbsolutePath() + File.separator + "universe"), new File(Configuration.backupFolder.getAbsolutePath() + File.separator + "universe"));
		} catch (IOException e) {
			new FXDialogueConfirm("An error occurred while backing up save data.\nPlease report this bug alongside the errors.log file inside your mod manager's folder.");
			Configuration.printException(e, "Backing up save data.");
		}
		
		Runtime rt = Runtime.getRuntime() ;
		
		switch (Configuration.systemType) {
		
			case "Windows":
				
				try {
					rt.exec(new String[] { "\"" + Configuration.starboundFolder.getAbsolutePath() + File.separator + "win32" + File.separator + "launcher" + File.separator + "launcher.exe\"" });
				} catch (IOException e) {
					new FXDialogueConfirm("Could not launch the game.\nPlease report this bug alongside the errors.log file inside your mod manager's folder.");
					Configuration.printException(e, "Launching game on Windows.");
				}
				
				break;
		
			case "Linux (32-Bit)":
				
				try {
					rt.exec(Configuration.starboundFolder.getAbsolutePath() + File.separator + "linux32" + File.separator + "launch_starbound.sh");
				} catch (IOException e) {
					new FXDialogueConfirm("Could not launch the game.\nPlease report this bug alongside the errors.log file inside your mod manager's folder.");
					Configuration.printException(e, "Launching game on Linux (32-Bit).");
				}
				
				break;
		
			case "Linux (64-Bit)":
				
				try {
					rt.exec(Configuration.starboundFolder.getAbsolutePath().replaceAll(" ", "_") + File.separator + "linux64" + File.separator + "launch_starbound.sh");
				} catch (IOException e) {
					Configuration.printException(e, "Launching game on Linux (64-Bit).");
				}
				
				break;
		
			case "Mac OS":
				
				try {
					rt.exec("open " + Configuration.starboundFolder.getAbsolutePath().replaceAll(" ", "_") + File.separator + "Starbound.app");
				} catch (IOException e) {
					new FXDialogueConfirm("Could not launch the game.\nPlease report this bug alongside the errors.log file inside your mod manager's folder.");
					Configuration.printException(e, "Launching game on Mac OS.");
				}
				
				break;
			
		
		}
		
	}
	
}
