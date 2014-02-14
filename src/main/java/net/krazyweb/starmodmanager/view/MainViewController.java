package net.krazyweb.starmodmanager.view;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.WindowEvent;
import net.krazyweb.helpers.FileHelper;
import net.krazyweb.starmodmanager.data.HyperSQLDatabase;
import net.krazyweb.starmodmanager.data.Localizer;
import net.krazyweb.starmodmanager.data.ModList;
import net.krazyweb.starmodmanager.data.Settings;
import net.krazyweb.starmodmanager.dialogue.MessageDialogue;
import net.krazyweb.starmodmanager.dialogue.MessageDialogue.MessageType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainViewController extends Observable {
	
	private static final Logger log = LogManager.getLogger(MainViewController.class);

	private MainView view;
	private ModListView modListView;
	private SettingsView settingsView;
	private AboutView aboutView;
	
	private ModList modList;
	
	private boolean dragOver = false;
	
	protected MainViewController(final ModList modList) {
		
		view = new MainView(this);
		view.build();
		view.show();
		
		modListView = new ModListView(modList);
		settingsView = new SettingsView();
		aboutView = new AboutView();
		
		view.setContent(modListView.getContent());
		
		this.modList = modList;
		
	}
	
	protected void modTabClicked() {
		view.setContent(modListView.getContent());
	}
	
	protected void backupsTabClicked() {
		MessageDialogue m = new MessageDialogue("Test Message", "Test Title", MessageType.INFO);
		log.debug(m.getResult());
	}
	
	protected void settingsTabClicked() {
		view.setContent(settingsView.getContent());
	}
	
	protected void aboutTabClicked() {
		view.setContent(aboutView.getContent());
	}
	
	protected void backupButtonClicked() {
		
	}
	
	protected void lockButtonClicked() {
		
	}
	
	protected void refreshButtonClicked() {
		
	}
	
	protected void expandButtonClicked() {
		
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
			modList.addMods(toAdd);
		}
		
		event.setDropCompleted(success);
		event.consume();
		
		view.hideOverlay();
		
	}
	
	protected void dragExited() {
		dragOver = false;
		view.hideOverlay();
	}
	
	protected void closeRequested(final WindowEvent event) {
		
		Settings.getInstance().setProperty("windowwidth", view.getScene().getWidth());
		Settings.getInstance().setProperty("windowheight", view.getScene().getHeight());
		
		HyperSQLDatabase.getInstance().closeConnection();
		
	}
	
}
