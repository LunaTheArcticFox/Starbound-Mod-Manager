package main.java.net.krazyweb.starmodmanager.view;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import main.java.net.krazyweb.starmodmanager.data.Mod;
import main.java.net.krazyweb.starmodmanager.data.ModList;

import org.apache.log4j.Logger;

public class ModListView extends VBox {
	
	private static final Logger log = Logger.getLogger(ModListView.class);
	
	private ModList modList;
	
	private Map<Mod, ModView> modViews;
	
	private VBox modsBox;
	
	public ModListView(final MainView mainView) {
		
		setSpacing(25.0);
		
		modsBox = new VBox();
		modsBox.setSpacing(25.0);
		
		modList = new ModList(this);
		modList.requestUpdate();
		
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
		
	}
	
	public void addMod(final Path file) {
		
		log.info("Adding mod: " + file);
		
		List<Path> toAdd = new ArrayList<>();
		toAdd.add(file);
		
		modList.addMods(toAdd);
		
	}
	
	public void updateModList(final List<Mod> mods) {
		
		if (modViews == null) {
			modViews = new HashMap<>();
		}
		
		for (final Mod mod : mods) {
			
			if (!modViews.containsKey(mod)) {
				ModView modView = new ModView(mod, modList);
				modViews.put(mod, modView);
				modsBox.getChildren().add(modView);
			}
			
			ModView modView = modViews.get(mod);
			modView.update();
			
		}
		
	}

}