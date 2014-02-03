package net.krazyweb.starmodmanager.view;


public class ModViewController {
	
	private ModView view;
	
	protected ModViewController(final ModView view) {
		this.view = view;
		this.view.build();
	}
	
}