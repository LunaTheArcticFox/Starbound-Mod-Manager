package net.krazyweb.starmodmanager.view;

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

}
