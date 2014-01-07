package net.krazyweb.starmodmanager;

import java.util.HashSet;

public class ModList {
	
	private boolean locked;
	
	private HashSet<Mod> mods;
	
	public ModList() {
		//TODO Get locked status from settings.
		mods = new HashSet<Mod>();
	}
	
	public void addMod(final String path) {
		mods.add(new Mod(path));
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
		
		mod.hide();
		
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
		Database.getModList();
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