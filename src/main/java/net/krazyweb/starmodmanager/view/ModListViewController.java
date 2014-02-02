package main.java.net.krazyweb.starmodmanager.view;

import java.util.Observable;
import java.util.Observer;

import main.java.net.krazyweb.starmodmanager.data.Localizer;
import main.java.net.krazyweb.starmodmanager.data.ModList;

public class ModListViewController implements Observer {
	
	protected ModListViewController(final ModList modList) {
		modList.addObserver(this);
	}
	
	@Override
	public void update(final Observable observable, final Object message) {
		
		if (observable instanceof Localizer && message.equals("localechanged")) {
			//TODO Observe for modList changes
		}
		
	}
	
}