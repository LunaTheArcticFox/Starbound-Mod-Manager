package main.java.net.krazyweb.starmodmanager.view;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import main.java.net.krazyweb.helpers.FileHelper;
import main.java.net.krazyweb.starmodmanager.data.Localizer;
import main.java.net.krazyweb.starmodmanager.data.ModList;
import main.java.net.krazyweb.starmodmanager.data.Settings;

import org.apache.log4j.Logger;

public class MainViewController extends Observable {
	
	private static final Logger log = Logger.getLogger(MainViewController.class);
	
	private MainView view;
	
	private boolean dragOver = false;
	
	private SettingsView settingsView;
	
	protected MainViewController(final ModList modList) {
		
		view = new MainView(this);
		view.build();
		view.show();
		
		settingsView = new SettingsView();
		
	}
	
	protected void modTabClicked() {
		
	}
	
	protected void backupsTabClicked() {
		
	}
	
	protected void settingsTabClicked() {
		view.setContent(settingsView.getContent());
	}
	
	protected void aboutTabClicked() {
		
	}
	
	protected void backupButtonClicked() {
		
	}
	
	protected void lockButtonClicked() {
		Settings.getInstance().setProperty("locale", "fl-SB");
	}
	
	protected void refreshButtonClicked() {
		Settings.getInstance().setProperty("locale", "de-DE");
	}
	
	protected void expandButtonClicked() {
		Settings.getInstance().setProperty("locale", "en-US");
	}
	
	protected void filesDraggedOver(final DragEvent event) {
		
		 Dragboard db = event.getDragboard();
         
         if (db.hasFiles()) {

         	boolean filesAccepted = false;
         	String fileName = "\n";
         	
				for (File file : db.getFiles()) {
					if (FileHelper.verify(Paths.get(file.getPath()), dragOver)) {
						filesAccepted = true;
						fileName += Localizer.getInstance().formatMessage(dragOver, "inquotes", file.getName()) + "\n";
					}
				}
         	
				if (filesAccepted) {
					event.acceptTransferModes(TransferMode.COPY);
					if (!dragOver) {
						String message = Localizer.getInstance().formatMessage("mainview.addmods", db.getFiles().size(), fileName);
						view.showOverlay(message);
						dragOver = true;
					}
				} else {
					event.consume();
				}
             
         } else {
             event.consume();
         }
         
	}
	
	protected void filesDropped(final DragEvent event) {

		Dragboard db = event.getDragboard();
	   
		boolean success = false;
		
		if (db.hasFiles()) {
			success = true;
			List<Path> toAdd = new ArrayList<>();
			for (File file : db.getFiles()) {
				toAdd.add(file.toPath());
				log.debug("File '" + file.toPath() + "' dropped on Mod Manager.");
			}
			//modListView.addMods(toAdd); TODO
		}
		
		event.setDropCompleted(success);
		event.consume();
		
		view.hideOverlay();
		
	}
	
	protected void dragExited() {
		dragOver = false;
		view.hideOverlay();
	}
	
}
