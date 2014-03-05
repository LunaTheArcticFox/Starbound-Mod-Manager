package net.krazyweb.starmodmanager.view;

import java.io.IOException;

public class LicenseViewController {
	
	private LicenseView view;
	
	protected LicenseViewController(final LicenseView view) {
		this.view = view;
		try {
			this.view.build();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void close() {
		view.close();
	}

}
