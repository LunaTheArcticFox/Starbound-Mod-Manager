package net.krazyweb.starmodmanager.view;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BackupViewController {
	
	@SuppressWarnings("unused")
	private static final Logger log = LogManager.getLogger(BackupViewController.class);
	
	private BackupListView view;
	
	protected BackupViewController(final BackupListView view) {
		
		this.view = view;
		this.view.build();
		
	}

}
