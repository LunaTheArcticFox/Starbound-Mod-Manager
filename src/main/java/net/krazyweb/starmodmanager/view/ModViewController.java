package net.krazyweb.starmodmanager.view;

import javafx.concurrent.Task;
import net.krazyweb.starmodmanager.data.ModList;


public class ModViewController {
	
	private ModView view;
	
	private ModList modList;
	
	protected ModViewController(final ModView view, final ModList modList) {
		this.view = view;
		this.modList = modList;
		this.view.build();
	}
	
	protected void installButtonClicked() {
		Task<Void> task = modList.getInstallModTask(view.getMod());
		BackgroundTaskProgressDialogue lview = new BackgroundTaskProgressDialogue();
		lview.build();
		lview.getProgressBar().progressProperty().bind(task.progressProperty());
		lview.getText().setText("Installing mod, please wait...");
		Thread thread = new Thread(task);
		thread.setDaemon(true);
		thread.setName("Install Mod Thread");
		thread.start();
		lview.start();
	}
	
	protected void uninstallButtonClicked() {
		modList.uninstallMod(view.getMod());
	}
	
}