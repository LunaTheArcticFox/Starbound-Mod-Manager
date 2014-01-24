package main.java.net.krazyweb.starmodmanager.data;

import java.io.File;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class Settings {

	private static final Logger log = Logger.getLogger(Settings.class);
	
	private static final int VERSION_MAJOR = 2;
	private static final int VERSION_MINOR = 0;
	private static final int VERSION_PATCH = 0;
	private static final String VERSION_EXTRA = "a";
	private static final String VERSION_STRING = VERSION_MAJOR + "." + VERSION_MINOR + "." + VERSION_PATCH + VERSION_EXTRA;
	
	private static enum OS {
		WINDOWS, MACOS, LINUX32, LINUX64;
	}
	
	private static File modsDirectory;
	
	private static OS operatingSystem;
	private static String operatingSystemName;
	
	private static String message;
	private static double progress;
	private static boolean complete = false;
	
	/*
	 * TODO
	 * Clear log file
	 * Set logging level (But don't remove errors)
	 * 
	 */
	
	public static void initialize() {
		
		complete = false;
		
		identifyOS();
		configureLogger();
		setModsDirectory(new File("mods/"));
		
		updateProgress(4, 4);
		updateMessage("Settings Initialized Successfully");
		complete = true;
		
	}
	
	private static final void identifyOS() {
		
		updateProgress(0, 4);
		updateMessage("Identifying Operating System");
		
		operatingSystemName = System.getProperty("os.name").toLowerCase();
		
		if (operatingSystemName.contains("win")) {
			setOperatingSystem(OS.WINDOWS);
		} else if (operatingSystemName.contains("mac")) {
			setOperatingSystem(OS.MACOS);
		} else if (operatingSystemName.contains("nix") || operatingSystemName.contains("nux") || operatingSystemName.contains("aix")) {
			
			if (System.getProperty("os.arch").contains("64")) {
				setOperatingSystem(OS.LINUX64);
			} else {
				setOperatingSystem(OS.LINUX32);
			}
			
		}
		
		operatingSystemName = System.getProperty("os.name");
		
	}
	
	/*
	 * Sets the appropriate logging levels for the logger based on launch conditions.
	 * If the program is launched from a .jar file, then the console logging is turned off.
	 * Otherwise, it is left on and file logging is turned off.
	 * TODO Add user configurable logging levels per appender.
	 */
	private static final void configureLogger() {
		
		updateProgress(1, 4);
		updateMessage("Starting Logger");
		
		ConsoleAppender console = (ConsoleAppender) Logger.getRootLogger().getAppender("console");
		FileAppender file = (FileAppender) Logger.getRootLogger().getAppender("file");
		
		//Print program information before muting loggers.
		console.setThreshold(Level.TRACE);
		file.setThreshold(Level.TRACE);
		
		log.info("======================");
		log.info("[Application Launched]");
		log.info("Starbound Mod Manager - Version " + VERSION_STRING);
		log.info("Running on " + operatingSystemName);
		log.info("----------------------");

		updateProgress(2, 4);
		updateMessage("Setting Logger Thresholds");
		
		if (Settings.class.getResource("Settings.class").toString().startsWith("jar:")) {
			console.setThreshold(Level.OFF);
			file.setThreshold(Level.WARN);
		} else {
			console.setThreshold(Level.TRACE);
			file.setThreshold(Level.OFF);
		}
		
	}

	public static File getModsDirectory() {
		return modsDirectory;
	}

	public static void setModsDirectory(File modsDirectory) {

		updateProgress(3, 4);
		updateMessage("Creating Mods Directory");
		
		Settings.modsDirectory = modsDirectory;
		if (!Settings.modsDirectory.exists()) {
			Settings.modsDirectory.mkdir();
		}
		
	}

	public static OS getOperatingSystem() {
		return operatingSystem;
	}

	public static void setOperatingSystem(OS operatingSystem) {
		Settings.operatingSystem = operatingSystem;
	}
	
	private static void updateProgress(final double amount, final double total) {
		progress = (double) amount / (double) total;
	}
	
	private static void updateMessage(final String m) {
		message = m;
	}

	public static double getProgress() {
		return progress;
	}

	public static String getMessage() {
		return message;
	}
	
	public static boolean isComplete() {
		return complete;
	}
	
	public static String getVersion() {
		return VERSION_STRING;
	}
	
}