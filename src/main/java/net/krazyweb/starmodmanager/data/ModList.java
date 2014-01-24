package main.java.net.krazyweb.starmodmanager.data;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import main.java.net.krazyweb.starmodmanager.view.ModListView;

import org.apache.log4j.Logger;

public class ModList {
	
	private static final Logger log = Logger.getLogger(ModList.class);
	
	private boolean locked;
	
	private ModListView modListView;
	
	private List<Mod> mods;
	
	public ModList(final ModListView modListView) {
		
		this.modListView = modListView;
		
		//TODO Get locked status from settings.
		try {
			
			mods = Database.getModList();
			
			for (Mod mod : mods) {
				mod.setOrder(mods.indexOf(mod));
				Database.updateMod(mod);
			}
			
			updateView();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public void addMod(final File file) {
		
		Mod mod = Mod.load(file, mods.size());
		
		if (mod != null) {
			mods.add(mod);
		}
		
	}
	
	public void deleteMod(final String name) {
		
		Mod mod = getModByName(name);
		
		if (mod == null) {
			return;
		}
		
		try {
			Database.deleteMod(mod);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		mods.remove(mod);
		
	}
	
	public void installMod(final String name) {
		
		Mod mod = getModByName(name);
		
		if (mod == null) {
			return;
		}
		
		
		
	}
	
	public void uninstallMod(final String name) {
		
		Mod mod = getModByName(name);
		
		if (mod == null) {
			return;
		}
		
		
		
	}
	
	public void hideMod(final String name) {
		
		Mod mod = getModByName(name);
		
		if (mod == null) {
			return;
		}
		
		mod.setHidden(true);
		
	}
	
	private void updateView() {

		List<Mod> modListCopy = new ArrayList<>(mods);
		
		modListView.updateModList(modListCopy);
		
	}
	
	/*
	 * See: http://stackoverflow.com/questions/4938626/moving-items-around-in-an-arraylist
	 */
	public void moveMod(final String name, final int amount) {
		
		if (locked) {
			log.debug("Mod list locked; cannot move mod: " + name);
			return;
		}
		
		Mod mod = getModByName(name);
		
		if (mod == null) {
			System.out.println("Mod '" + name + "' not found.");
			return;
		}
		
		log.debug("=============\nPerforming rotation, results:");
		
		if (amount > 0) {
			
			if (mods.indexOf(mod) - amount > 0) {
				Collections.rotate(mods.subList(mods.indexOf(mod) - amount, mods.indexOf(mod) + 1), 1);
			} else {
				Collections.rotate(mods.subList(0, mods.indexOf(mod) + 1), 1);
			}
			
		} else {
			
			if (mods.indexOf(mod) - amount + 1 <= mods.size()) {
				Collections.rotate(mods.subList(mods.indexOf(mod), mods.indexOf(mod) - amount + 1), -1);
			} else {
				Collections.rotate(mods.subList(mods.indexOf(mod), mods.size()), -1);
			}
			
		}
		
		for (Mod m : mods) {
			m.setOrder(mods.indexOf(m));
			log.debug("[" + m.getOrder() + "] \t" + m.getInternalName());
			try {
				Database.updateMod(m);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public void lockList() {
		locked = true;
	}
	
	public void unlockList() {
		locked = false;
	}
	
	public void refreshMods() {
		try {
			Database.getModList();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private Mod getModByName(final String name) {
		
		for (Mod mod : mods) {
			if (mod.getInternalName().equals(name)) {
				return mod;
			}
		}
		
		return null;
		
	}
	
}