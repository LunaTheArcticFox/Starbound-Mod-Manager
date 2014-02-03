package net.krazyweb.starmodmanager.view;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.krazyweb.starmodmanager.ModManager;
import net.krazyweb.starmodmanager.data.Localizer;
import net.krazyweb.starmodmanager.data.Mod;
import net.krazyweb.starmodmanager.data.ModList;

import org.apache.log4j.Logger;

public class ModListViewController {
	
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(ModListViewController.class);
	
	private ModList modList;

	private List<ModView> modViews;
	
	private ModListView view;
	
	private double y, lastY, mouseY;
	
	protected ModListViewController(final ModListView view, final ModList modList) {
		
		modViews = new ArrayList<>();
		
		this.view = view;
		this.modList = modList;
		this.view.build();
		
		for (final Mod mod : modList.getMods()) {
			ModView m = new ModView(mod);
			modViews.add(m);
			this.view.addMod(m);
		}
		
		/*
		 * The following essentially pre-renders the view,
		 * preventing delay when adding it to the main window
		 * for the first time. I haven't found a better way
		 * around this problem yet. If this isn't done, it
		 * takes a noticeable amount of time to load the view
		 * on the first click, which is jarring and should be
		 * avoided.
		 */
		Stage stage = new Stage();
		Scene scene = new Scene((VBox) this.view.getContent());
		stage.setScene(scene);
		stage.setOpacity(0);
		stage.initStyle(StageStyle.UTILITY);
		stage.show();
		ModManager.getPrimaryStage().toFront();
		stage.close();
		
	}
	
	protected void addModButtonClicked() {

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(Localizer.getInstance().getMessage("modlistview.modfilechoosertitle"));
		
		List<Path> paths = new ArrayList<>();
		List<File> files = fileChooser.showOpenMultipleDialog(ModManager.getPrimaryStage());
		
		if (files != null && !files.isEmpty()) {
		
			for (File file : files) {
				paths.add(file.toPath());
			}
			
			modList.addMods(paths);
		
		}
		
	}
	
	protected void modViewMousePressed(final ModView modView, final MouseEvent event) {
		lastY = y = modView.getContent().getLayoutY();
		mouseY = event.getSceneY();
		modView.getContent().toFront();		
	}
	
	protected void modViewMouseDragged(final ModView modView, final MouseEvent event) {

		lastY = modView.getContent().getLayoutY();
		modView.getContent().setLayoutY(y + event.getSceneY() - mouseY);
		
		if (modView.getContent().getLayoutY() < view.getModsBox().getLayoutY()) {
			modView.getContent().setLayoutY(view.getModsBox().getLayoutY());
		}
		
		if (modView.getContent().getLayoutY() + modView.getContent().getHeight() > view.getModsBox().getLayoutY() + view.getModsBox().getHeight()) {
			modView.getContent().setLayoutY(view.getModsBox().getLayoutY() + view.getModsBox().getHeight() - modView.getContent().getHeight());
		}
		
		if (modList.getMods().indexOf(modView.getMod()) > 0 && modView.getContent().getLayoutY() < lastY) {
			
			final ModView mv = getModViewByMod(modList.getMods().get(modList.getMods().indexOf(modView.getMod()) - 1));
			
			if (!mv.moving && mv.getContent().getLayoutY() > modView.getContent().getLayoutY() - 16) { //16  = half the node height
				mv.moving = true;
				modList.moveMod(modView.getMod(), 1);
				view.animate(mv, 57);
			}
			
		}
		
		if (modList.getMods().indexOf(modView.getMod()) < modList.getMods().size() - 1 && modView.getContent().getLayoutY() > lastY) {
			
			final ModView mv = getModViewByMod(modList.getMods().get(modList.getMods().indexOf(modView.getMod()) + 1));
			
			if (!mv.moving && mv.getContent().getLayoutY() < modView.getContent().getLayoutY() + modView.getContent().getHeight() - 16) { //16  = half the node height
				mv.moving = true;
				modList.moveMod(modView.getMod(), -1);
				view.animate(mv, -57);
			}
			
		}
		
	}
	
	private ModView getModViewByMod(final Mod mod) {
		for (final ModView modView : modViews) {
			if (modView.getMod() == mod) {
				return modView;
			}
		}
		return null;
	}
	
	protected void modViewMouseReleased(final ModView modView, final MouseEvent e) {
		modView.getContent().setLayoutY(modList.getMods().indexOf(modView.getMod()) * 57);
	}
	
}