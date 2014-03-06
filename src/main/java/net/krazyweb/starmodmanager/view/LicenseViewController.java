package net.krazyweb.starmodmanager.view;

import java.io.IOException;

import net.krazyweb.starmodmanager.data.LocalizerFactory;
import net.krazyweb.starmodmanager.data.LocalizerModelInterface;
import net.krazyweb.starmodmanager.dialogue.MessageDialogue;
import net.krazyweb.starmodmanager.dialogue.MessageDialogue.MessageType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LicenseViewController {
	
	private static final Logger log = LogManager.getLogger(LicenseViewController.class);
	
	private LicenseView view;
	
	protected LicenseViewController(final LicenseView view) {
		
		LocalizerModelInterface localizer = new LocalizerFactory().getInstance();
		
		this.view = view;
		try {
			this.view.build();
		} catch (IOException e) {
			log.error("", e);
			MessageDialogue dialogue = new MessageDialogue(localizer.getMessage("licenseviewcontroller.fileerror"), localizer.getMessage("licenseviewcontroller.fileerror.title"), MessageType.ERROR, new LocalizerFactory());
			dialogue.getResult();
		}
	}
	
	protected void close() {
		view.close();
	}

}
