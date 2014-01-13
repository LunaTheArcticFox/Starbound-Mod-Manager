package net.krazyweb.starmodmanager;

import java.io.File;

public class Settings {
	
	private static File modsDirectory;
	
	public static void initialize() {
		setModsDirectory(new File("mods/"));
	}

	public static File getModsDirectory() {
		return modsDirectory;
	}

	public static void setModsDirectory(File modsDirectory) {
		Settings.modsDirectory = modsDirectory;
		if (!Settings.modsDirectory.exists()) {
			Settings.modsDirectory.mkdir();
		}
	}
	
}