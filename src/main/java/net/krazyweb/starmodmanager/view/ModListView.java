package main.java.net.krazyweb.starmodmanager.view;

import java.util.List;

import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import main.java.net.krazyweb.starmodmanager.data.Mod;
import main.java.net.krazyweb.starmodmanager.data.ModList;

import org.apache.log4j.Logger;

public class ModListView extends VBox {
	
	private static final Logger log = Logger.getLogger(ModListView.class);
	
	private ModList modList;
	
	public ModListView() {
		
		modList = new ModList(this);
		
	}
	
	public void updateModList(final List<Mod> mods) {
		
		for (Mod mod : mods) {
			getChildren().add(new Text(mod.getInternalName()));
		}
		
	}

}