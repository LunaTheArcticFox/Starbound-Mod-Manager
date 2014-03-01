package net.krazyweb.starmodmanager.view;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javafx.concurrent.Task;
import net.krazyweb.starmodmanager.data.LocalizerModelInterface;
import net.krazyweb.starmodmanager.data.ModList;
import net.krazyweb.starmodmanager.data.SettingsFactory;
import net.krazyweb.starmodmanager.dialogue.MessageDialogue;
import net.krazyweb.starmodmanager.dialogue.MessageDialogue.MessageType;
import net.krazyweb.starmodmanager.dialogue.MessageDialogueConfirm;


public class ModViewController {
	
	private ModView view;
	
	private ModList modList;
	
	private LocalizerModelInterface localizer; 
	
	protected ModViewController(final ModView view, final ModList modList, final LocalizerModelInterface localizer) {
		this.view = view;
		this.modList = modList;
		this.localizer = localizer;
		this.view.build(new SettingsFactory().getInstance().getPropertyBoolean("modviewexpanded"));
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
	
	protected void moreInfoButtonClicked() {
		view.toggleMoreInfo();
	}
	
	protected void deleteButtonClicked() {
		
		MessageDialogueConfirm m = new MessageDialogueConfirm(
					localizer.formatMessage("modview.confirmdelete", view.getMod().getDisplayName()),
					localizer.getMessage("modview.confirmdeletetitle"),
					MessageType.CONFIRM,
					localizer
				);
		
		if (m.getResult() == MessageDialogue.DialogueAction.YES) {
			modList.deleteMod(view.getMod());
		}
		
	}
	
	protected void hideButtonClicked() {
		
		MessageDialogueConfirm m = new MessageDialogueConfirm(
				localizer.formatMessage("modview.confirmhide", view.getMod().getDisplayName()),
				localizer.getMessage("modview.confirmhidetitle"),
				MessageType.CONFIRM,
				localizer
			);
	
		if (m.getResult() == MessageDialogue.DialogueAction.YES) {
			modList.hideMod(view.getMod());
		}
		
	}
	
	protected void linkButtonClicked() {
		
		if (!view.getMod().getURL().isEmpty()) {
			
			try {
				openWebpage(view.getMod().getURL());
			} catch (URISyntaxException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
	
	private void openWebpage(final String url) throws URISyntaxException, IOException {
		openWebpage(new URL(url).toURI());
	}
	
	private void openWebpage(final URI uri) throws IOException {
		
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			desktop.browse(uri);
		}
		
	}
	
}