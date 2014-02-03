package net.krazyweb.starmodmanager.view;

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
		modList.installMod(view.getMod());
	}
	
	protected void uninstallButtonClicked() {
		modList.uninstallMod(view.getMod());
	}
	
}