package net.krazyweb.starmodmanager;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;

public class ModList {
	
	private boolean locked;
	
	private ArrayList<Mod> mods;
	
	public ModList() {
		//TODO Get locked status from settings.
		System.out.println("Mod list created.");
		try {
			mods = Database.getModList();
			
			for (Mod mod : mods) {
				mod.setOrder(mods.indexOf(mod));
				Database.updateMod(mod);
			}
			
			
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
	
	public void moveMod(final String name, final int amount) {
		
		if (locked) {
			return;
		}
		
		Mod mod = getModByName(name);
		
		if (mod == null) {
			return;
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