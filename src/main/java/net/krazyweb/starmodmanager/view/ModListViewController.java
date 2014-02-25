package net.krazyweb.starmodmanager.view;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.krazyweb.starmodmanager.ModManager;
import net.krazyweb.starmodmanager.data.LocalizerFactory;
import net.krazyweb.starmodmanager.data.Mod;
import net.krazyweb.starmodmanager.data.ModList;
import net.krazyweb.starmodmanager.data.Observable;
import net.krazyweb.starmodmanager.data.Observer;
import net.krazyweb.starmodmanager.data.SettingsFactory;
import net.krazyweb.starmodmanager.data.SettingsModelInterface;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModListViewController implements Observer {
	
	@SuppressWarnings("unused")
	private static final Logger log = LogManager.getLogger(ModListViewController.class);
	
	private ModList modList;

	private List<ModView> modViews;
	private ModListView view;
	
	private double y, lastY, mouseY;
	
	private SettingsModelInterface settings;

	private boolean expanded, locked;

	protected ModListViewController(final ModListView view, final ModList modList) {
		
		settings = new SettingsFactory().getInstance();

		expanded = settings.getPropertyBoolean("modviewexpanded");
		locked = settings.getPropertyBoolean("modlistlocked");
		
		modList.addObserver(this);
		
		modViews = new ArrayList<>();
		
		this.view = view;
		this.modList = modList;
		this.view.build();
		
		for (final Mod mod : modList.getMods()) {
			ModView m = new ModView(mod, modList);
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
		fileChooser.setTitle(new LocalizerFactory().getInstance().getMessage("modlistview.modfilechoosertitle"));
		
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
		
		if (locked) {
			return;
		}
		
		y = modView.getContent().getTranslateY();
		lastY = y + modView.getContent().getLayoutY();
		mouseY = event.getSceneY();
		modView.getContent().toFront();
		
	}
	
	protected void modViewMouseDragged(final ModView modView, final MouseEvent event) {
		
		if (locked) {
			return;
		}

		modView.getContent().setTranslateY((int) (y + event.getSceneY() - mouseY));
		
		double position = modView.getContent().getLayoutY() + modView.getContent().getTranslateY();
		
		if (position <= 0) {
			modView.getContent().setTranslateY(0 - modView.getContent().getLayoutY());
		}
		
		if (position + modView.getContent().getHeight() >= view.getModsBox().getHeight()) {
			modView.getContent().setTranslateY(view.getModsBox().getHeight() - modView.getContent().getLayoutY() - modView.getContent().getHeight());
		}
		
		if (modList.getMods().indexOf(modView.getMod()) > 0 && position < lastY) {
			
			final ModView otherModView = getModViewByMod(modList.getMods().get(modList.getMods().indexOf(modView.getMod()) - 1));
			
			double otherModViewPos = otherModView.getContent().getTranslateY() + otherModView.getContent().getLayoutY();
			
			if (!otherModView.moving && otherModViewPos > position - 16) { //16  = half the node height
				otherModView.moving = true;
				modList.moveMod(modView.getMod(), 1);
				view.animate(otherModView, 57);
			}
			
		}
		
		if (modList.getMods().indexOf(modView.getMod()) < modList.getMods().size() - 1 && position > lastY) {
			
			final ModView otherModView = getModViewByMod(modList.getMods().get(modList.getMods().indexOf(modView.getMod()) + 1));
			
			double otherModViewPos = otherModView.getContent().getTranslateY() + otherModView.getContent().getLayoutY();
			
			if (!otherModView.moving && otherModViewPos < position + modView.getContent().getHeight() - 16) { //16  = half the node height
				otherModView.moving = true;
				modList.moveMod(modView.getMod(), -1);
				view.animate(otherModView, -57);
			}
			
		}
		
		lastY = position;
		
	}
	
	protected void modViewMouseReleased(final ModView modView, final MouseEvent e) {
		
		if (locked) {
			return;
		}
		
		modView.getContent().setTranslateY(modList.getMods().indexOf(modView.getMod()) * 57 - modView.getContent().getLayoutY());
		updateListView();
		
	}
	
	private ModView getModViewByMod(final Mod mod) {
		for (final ModView modView : modViews) {
			if (modView.getMod() == mod) {
				return modView;
			}
		}
		return null;
	}
	
	private void updateListView() {
		
		/*
		 * For some stupid reason that I can't figure out, the mod panes will not retain
		 * their positions on screen unless they're removed and re-added to the 
		 * mod list container.
		 * 
		 * Additionally, the list must be sorted into the correct load order
		 * and the layout/translation data set back to 0. 
		 */
		view.clearMods();
		
		Collections.sort(modViews, new Comparator<ModView>(){

			@Override
			public int compare(ModView mv1, ModView mv2) {
				return mv1.getMod().getOrder() - mv2.getMod().getOrder();
			}
			
		});
		
		for (ModView mv : modViews) {
			mv.getContent().setTranslateY(0);
			mv.getContent().setLayoutY(0);
			view.addMod(mv);
		}
		
	}
	
	protected void toggleExpansion() {
		
		expanded = !expanded;
		settings.setProperty("modviewexpanded", expanded);
		
		for (ModView modView : modViews) {
			modView.expand(expanded);
		}
		
		updateListView();
		
	}
	
	protected void toggleLock() {

		locked = !locked;
		settings.setProperty("modlistlocked", locked);
		
	}
	
	protected void getNewMods() {
		
		Thread t = new Thread(modList.getNewModsTask());
		t.setName("Get Mods Task");
		t.setDaemon(true);
		t.start();
		
	}
	
	@Override
	public void update(final Observable observable, final Object message) {
		
		if (observable instanceof ModList && message instanceof Object[]) {
			
			Object[] args = (Object[]) message;
			
			if (args[0].equals("modadded")) {
				
				ModView newModView = new ModView((Mod) args[1], modList);
				modViews.add(newModView);
				
				updateListView();
				
			} else if (args[0].equals("moddeleted") || args[0].equals("modhidden")) {

				modViews.remove(getModViewByMod((Mod) args[1]));
				
				updateListView();
				
			}
			
		}
		
	}
	
}