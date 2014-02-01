package main.java.net.krazyweb.starmodmanager.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class Settings extends Observable implements Progressable {

	private static final Logger log = Logger.getLogger(Settings.class);
	
	private static final int VERSION_MAJOR = 2;
	private static final int VERSION_MINOR = 0;
	private static final int VERSION_PATCH = 0;
	private static final String VERSION_EXTRA = "a";
	private static final String VERSION_STRING = VERSION_MAJOR + "." + VERSION_MINOR + "." + VERSION_PATCH + VERSION_EXTRA;
	
	private static enum OS {
		WINDOWS, MACOS, LINUX32, LINUX64;
	}
	
	private static Path modsDirectory;
	private static Path modsInstallDirectory;
	
	private static OS operatingSystem;
	private static String operatingSystemName;
	
	//private static String message;
	//private static double progress;
	
	private static Settings instance;
	
	private Task<?> task;
	
	private ReadOnlyDoubleProperty progress;
	private ReadOnlyStringProperty message;
	
	private Map<String, String> settings;
	
	private Settings() {
		
	}
	
	public static Settings getInstance() {
		if (instance == null) {
			synchronized (Settings.class) {
				instance = new Settings();
			}
		}
		return instance;
	}
	
	private void setProgress(final ReadOnlyDoubleProperty progress) {
		this.progress = progress;
	}
	
	private void setMessage(final ReadOnlyStringProperty message) {
		this.message = message; 
	}

	@Override
	public ReadOnlyDoubleProperty getProgressProperty() {
		return progress;
	}

	@Override
	public ReadOnlyStringProperty getMessageProperty() {
		return message;
	}

	@Override
	public void processTask() {
		Thread thread = new Thread(task);
		thread.setName("Settings Task Thread");
		thread.setDaemon(true);
		thread.start();
	}
	
	/*
	 * Sets the appropriate logging levels for the logger based on launch conditions.
	 * If the program is launched from a .jar file, then the console logging is turned off.
	 * Otherwise, it is left on and file logging is turned off.
	 * TODO Add user configurable logging levels per appender.
	 */
	public void configureLogger() {
		
		task = new Task<Void>() {

			@Override
			protected Void call() throws Exception {

				this.updateMessage("Configuring Logger");
				this.updateProgress(0.0, 4.0);
				
				identifyOS();
				
				ConsoleAppender console = (ConsoleAppender) Logger.getRootLogger().getAppender("console");
				FileAppender file = (FileAppender) Logger.getRootLogger().getAppender("file");
				
				this.updateProgress(1.0, 4.0);
				
				//Print program information before adjusting loggers.
				console.setThreshold(Level.TRACE);
				file.setThreshold(Level.TRACE);
				
				this.updateProgress(2.0, 4.0);

				log.info("[Application Launched]");
				log.info("Starbound Mod Manager - Version " + VERSION_STRING);
				log.info("Running on " + operatingSystemName);

				this.updateProgress(3.0, 4.0);

				if (Settings.class.getResource("Settings.class").toString().startsWith("jar:")) {
					console.setThreshold(Level.OFF);
					file.setThreshold(Level.WARN);
				} else {
					console.setThreshold(Level.DEBUG);
					file.setThreshold(Level.OFF);
				}
				
				//TODO
				setModsDirectory(Paths.get("mods/"));
				setModsInstallDirectory(Paths.get("D:\\Games\\Steam\\steamapps\\common\\Starbound\\mods"));

				this.updateProgress(4.0, 4.0);

				return null;
				
			}
			
		};
		
		task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(final WorkerStateEvent event) {
				setChanged();
				notifyObservers("loggerconfigured");
			}
		});
		
		this.setProgress(task.progressProperty());
		this.setMessage(task.messageProperty());
		
	}
	
	public void load() {
		
		task = new Task<Void>() {

			@Override
			protected Void call() throws Exception {

				this.updateMessage("Loading Settings From Database");
				this.updateProgress(0.0, 1.0);

				Thread.sleep(1000);
				
				settings = Database.getInstance().getProperties();

				this.updateProgress(1.0, 1.0);

				Thread.sleep(1000);
				
				return null;
				
			}
			
			
			
		};
		
		task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(final WorkerStateEvent event) {
				setChanged();
				notifyObservers("settingsloaded");
			}
		});
		
		this.setProgress(task.progressProperty());
		this.setMessage(task.messageProperty());
		
	}
	
	/*
	 * TODO
	 * Clear log file
	 * Set logging level (But don't remove errors)
	 * Clean this file!
	 */
	
	/*public static void initialize() {
		
		complete = false;
		
		identifyOS();
		configureLogger();
		setModsDirectory(Paths.get("mods/"));
		setModsInstallDirectory(Paths.get("D:\\Games\\Steam\\steamapps\\common\\Starbound\\mods"));
		
		updateProgress(4, 4);
		updateMessage("Settings Initialized Successfully");
		complete = true;
		
	}*/
	
	private final void identifyOS() {
		
		
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

	public static Path getModsDirectory() {
		return modsDirectory;
	}

	public static void setModsDirectory(final Path modsDirectory) {

		
		Settings.modsDirectory = modsDirectory;
		if (Files.notExists(modsDirectory)) {
			try {
				Files.createDirectories(modsDirectory);
			} catch (IOException e) {
				log.error("Error creating mods directory.", e);
			}
		}
		
	}
	
	protected static Locale getLocale() {
		return new Locale(Database.getPropertyString("language", "en"), Database.getPropertyString("region", "US"));
	}

	public static OS getOperatingSystem() {
		return operatingSystem;
	}

	public static void setOperatingSystem(final OS operatingSystem) {
		Settings.operatingSystem = operatingSystem;
	}
	
	public static String getVersion() {
		return VERSION_STRING;
	}

	public static Path getModsInstallDirectory() {
		return modsInstallDirectory;
	}

	public static void setModsInstallDirectory(final Path modsInstallDirectory) {
		Settings.modsInstallDirectory = modsInstallDirectory;
	}
	
	public static int getWindowWidth() {
		return Database.getPropertyInt("windowwidth", 683);
	}
	
	public static int getWindowHeight() {
		return Database.getPropertyInt("windowheight", 700);
	}
	
}