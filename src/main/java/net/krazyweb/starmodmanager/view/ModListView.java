package main.java.net.krazyweb.starmodmanager.view;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import main.java.net.krazyweb.starmodmanager.data.Mod;
import main.java.net.krazyweb.starmodmanager.data.ModList;

import org.apache.log4j.Logger;

public class ModListView extends VBox {
	
	private static final Logger log = Logger.getLogger(ModListView.class);
	
	private MainView mainView;
	private ModList modList;
	
	public ModListView(final MainView mainView) {
		
		this.mainView = mainView;
		
		modList = new ModList(this);
		
		setSpacing(25.0);
		
	}
	
	public void addMod(final Path file) {
		
		log.info("Adding mod: " + file);
		
		List<Path> toAdd = new ArrayList<>();
		toAdd.add(file);
		
		modList.addMods(toAdd);
		
	}
	
	public void updateModList(final List<Mod> mods) {
		
		getChildren().clear();
		
		for (final Mod mod : mods) {
			
			GridPane modPane = new GridPane();
			modPane.setHgap(25.0);
			modPane.add(new Text(mod.getDisplayName()), 1, 1);
			modPane.add(new Text(mod.isInstalled() ? "Installed" : "Not Installed"), 2, 1);
			modPane.add(new Text(mod.getModVersion()), 2, 2);
			
			Button installButton = new Button("Install");
			installButton.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent e) {
					modList.installMod(mod);
				}
			});
			
			modPane.add(installButton, 3, 1);
			
			final Button expandButton = new Button("↓");
			expandButton.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent e) {
					expandButton.setText("↑");
				}
			});
			
			modPane.add(expandButton, 4, 1);
			
			GridPane.setRowSpan(modPane.getChildren().get(0), 2);
			GridPane.setRowSpan(installButton, 2);
			GridPane.setRowSpan(expandButton, 2);
			
			getChildren().add(modPane);
			
		}
		
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
		
		getChildren().add(addMod);
		
	}

}