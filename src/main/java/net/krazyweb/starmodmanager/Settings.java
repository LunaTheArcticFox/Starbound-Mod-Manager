package main.java.net.krazyweb.starmodmanager;

import java.io.File;

import org.apache.log4j.Logger;

public class Settings {
	
	private static Logger log;
	private static File modsDirectory;
	
	public static void initialize() {
		
		log = Logger.getLogger(Settings.class);
		
		setLoggerLevels();
		
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
	
	private static final void setLoggerLevels() {
		
		if (Settings.class.getResource("Settings.class").toString().startsWith("jar:")) {
			log.debug("Jar!");
		} else {
			log.debug("Not jar!");
		}
		
	}
	
}