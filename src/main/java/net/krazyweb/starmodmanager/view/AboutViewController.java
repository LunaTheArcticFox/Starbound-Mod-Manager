package net.krazyweb.starmodmanager.view;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import net.krazyweb.starmodmanager.data.LocalizerFactory;
import net.krazyweb.starmodmanager.data.LocalizerModelInterface;
import net.krazyweb.starmodmanager.dialogue.MessageDialogue;
import net.krazyweb.starmodmanager.dialogue.MessageDialogue.MessageType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AboutViewController {
	
	@SuppressWarnings("unused")
	private static final Logger log = LogManager.getLogger(AboutViewController.class);
	
	private AboutView view;
	
	protected AboutViewController(final AboutView view) {
		
		this.view = view;
		this.view.build();
		
	}
	
	protected void openWebpage(final String url) throws URISyntaxException, IOException {
		openWebpage(new URL(url).toURI());
	}
	
	protected void openWebpage(final URI uri) throws IOException {
		
		LocalizerModelInterface localizer = new LocalizerFactory().getInstance(); 
		
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			desktop.browse(uri);
		} else {
			MessageDialogue dialogue = new MessageDialogue(localizer.getMessage("modviewcontroller.nodesktop"), localizer.getMessage("modviewcontroller.nodesktop.title"), MessageType.ERROR, new LocalizerFactory());
			dialogue.getResult();
		}
		
	}

}
