package net.krazyweb.starmodmanager.data;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Observable;
import java.util.Properties;

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
	
	private static OS operatingSystem;
	private static String operatingSystemName;
	
	private static Settings instance;
	
	private Task<?> task;
	
	private ReadOnlyDoubleProperty progress;
	private ReadOnlyStringProperty message;
	
	private Map<String, String> settings;
	private Properties defaultProperties;
	
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
				this.updateProgress(0.0, 3.0);

				settings = Database.getInstance().getProperties();

				this.updateProgress(1.0, 3.0);
				
				defaultProperties = new Properties();
				defaultProperties.load(Settings.class.getClassLoader().getResourceAsStream("defaultsettings.properties"));

				this.updateProgress(2.0, 3.0);

				if (Settings.class.getResource("Settings.class").toString().startsWith("jar:")) {
					ConsoleAppender console = (ConsoleAppender) Logger.getRootLogger().getAppender("console");
					FileAppender file = (FileAppender) Logger.getRootLogger().getAppender("file");
					console.setThreshold(Level.OFF);
					file.setThreshold(getPropertyLevel("loggerlevel"));
				}

				this.updateProgress(3.0, 3.0);
				
				log.debug("Settings Loaded");
				
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
		
		task.setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(final WorkerStateEvent event) {
				log.error("", task.getException());
			}
		});
		
		this.setProgress(task.progressProperty());
		this.setMessage(task.messageProperty());
		
	}
	
	private final void identifyOS() {
		
		operatingSystemName = System.getProperty("os.name").toLowerCase();
		
		if (operatingSystemName.contains("win")) {
			operatingSystem = OS.WINDOWS;
		} else if (operatingSystemName.contains("mac")) {
			operatingSystem = OS.MACOS;
		} else if (operatingSystemName.contains("nix") || operatingSystemName.contains("nux") || operatingSystemName.contains("aix")) {
			
			if (System.getProperty("os.arch").contains("64")) {
				operatingSystem = OS.LINUX64;
			} else {
				operatingSystem = OS.LINUX32;
			}
			
		}
		
		operatingSystemName = System.getProperty("os.name");
		
	}

	public OS getOperatingSystem() {
		return operatingSystem;
	}

	public void setLoggerLevel(final Level level) {
		if (Settings.class.getResource("Settings.class").toString().startsWith("jar:")) {
			FileAppender file = (FileAppender) Logger.getRootLogger().getAppender("file");
			file.setThreshold(level);
		}
	}
	
	public String getPropertyString(final String key) {
		
		if (settings.containsKey(key)) {
			return settings.get(key);
		}
		
		if (defaultProperties.containsKey(key)) {
			log.debug("Property '" + key + "' not found in database. Using default value: '" + defaultProperties.getProperty(key) + "'");
			return defaultProperties.getProperty(key);
		} else {
			log.warn("Could not find property: " + key);
			return null;
		}
		
	}
	
	public int getPropertyInt(final String key) {
		return Integer.parseInt(getPropertyString(key));
	}
	
	public double getPropertyDouble(final String key) {
		return Double.parseDouble(getPropertyString(key));
	}
	
	public boolean getPropertyBoolean(final String key) {
		return Boolean.parseBoolean(getPropertyString(key));
	}
	
	public Path getPropertyPath(final String key) {
		return Paths.get(getPropertyString(key));
	}
	
	public Level getPropertyLevel(final String key) {
		return Level.toLevel(getPropertyString(key));
	}
	
	public void setProperty(final String key, final Object property) {
		settings.put(key, property.toString());
		setChanged();
		notifyObservers("propertychanged:" + key);
		Database.setProperty(key, property);
		log.debug("Property Changed: " + key + " -- " + property);
	}
	
	public String getVersion() {
		return VERSION_STRING;
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
	
}