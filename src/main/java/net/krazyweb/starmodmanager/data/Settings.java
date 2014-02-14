package net.krazyweb.starmodmanager.data;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

public class Settings implements SettingsModelInterface {

	private static final Logger log = LogManager.getLogger(Settings.class);
	
	private static enum LoggerType {
		ALL, CONSOLE, FILE
	}
	
	private static final int VERSION_MAJOR = 2;
	private static final int VERSION_MINOR = 0;
	private static final int VERSION_PATCH = 0;
	private static final String VERSION_EXTRA = "a";
	private static final String VERSION_STRING = VERSION_MAJOR + "." + VERSION_MINOR + "." + VERSION_PATCH + VERSION_EXTRA;
	
	private static OS operatingSystem;
	private static String operatingSystemName;
	
	private Map<String, String> settings;
	private Properties defaultProperties;
	
	private Set<Observer> observers;

	private DatabaseModelInterface database;
	private DatabaseModelFactory databaseFactory;
	
	protected Settings(final DatabaseModelFactory databaseFactory) {
		observers = new HashSet<>();
		this.databaseFactory = databaseFactory;
	}
	
	/*
	 * Sets the appropriate logging levels for the logger based on launch conditions.
	 * If the program is launched from a .jar file, then the console logging is turned off.
	 * Otherwise, it is left on and file logging is turned off.
	 */
	@Override
	public Task<Void> getInitializeLoggerTask() {
		
		final Task<Void> task = new Task<Void>() {

			@Override
			protected Void call() throws Exception {

				this.updateMessage("Configuring Logger");
				this.updateProgress(0.0, 4.0);
				
				identifyOS();
				
				this.updateProgress(1.0, 4.0);
				
				//Print program information before adjusting loggers.
				setLoggerLevel(Level.TRACE, LoggerType.ALL);
				
				this.updateProgress(2.0, 4.0);

				log.info("[Application Launched]");
				log.info("Starbound Mod Manager - Version {}", VERSION_STRING);
				log.info("Running on {}", operatingSystemName);

				this.updateProgress(3.0, 4.0);

				if (Settings.class.getResource("Settings.class").toString().startsWith("jar:")) {
					setLoggerLevel(Level.OFF, LoggerType.CONSOLE);
					setLoggerLevel(Level.WARN, LoggerType.FILE);
				} else {
					setLoggerLevel(Level.DEBUG, LoggerType.CONSOLE);
					setLoggerLevel(Level.OFF, LoggerType.FILE);
				}

				this.updateProgress(4.0, 4.0);

				return null;
				
			}
			
		};
		
		task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(final WorkerStateEvent event) {
				notifyObservers("loggerconfigured");
			}
		});
		
		return task;
		
	}

	@Override
	public Task<Void> getLoadSettingsTask() {
		
		database = databaseFactory.getInstance();
		
		final Task<Void> task = new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				
				this.updateMessage("Loading Settings From Database");
				this.updateProgress(0.0, 3.0);

				settings = database.getProperties();

				this.updateProgress(1.0, 3.0);
				
				defaultProperties = new Properties();
				defaultProperties.load(Settings.class.getClassLoader().getResourceAsStream("defaultsettings.properties"));

				this.updateProgress(2.0, 3.0);

				if (Settings.class.getResource("Settings.class").toString().startsWith("jar:")) {
					setLoggerLevel(Level.OFF, LoggerType.CONSOLE);
					setLoggerLevel(getPropertyLevel("loggerlevel"), LoggerType.FILE);
				}
				
				if (Files.notExists(getPropertyPath("modsdir"))) {
					Files.createDirectories(getPropertyPath("modsdir"));
				}

				this.updateProgress(3.0, 3.0);
				
				log.debug("Settings Loaded");
				
				return null;
				
			}
			
		};
		
		task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(final WorkerStateEvent event) {
				notifyObservers("settingsloaded");
			}
		});
		
		task.setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(final WorkerStateEvent event) {
				log.error("", task.getException());
			}
		});

		return task;
		
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

	@Override
	public OS getOperatingSystem() {
		return operatingSystem;
	}

	@Override
	public void setLoggerLevel(final Level level) {
		if (Settings.class.getResource("Settings.class").toString().startsWith("jar:")) {
			setLoggerLevel(Level.OFF, LoggerType.CONSOLE);
			setLoggerLevel(level, LoggerType.FILE);
			setProperty("loggerlevel", level);
		}
	}
	
	private void setLoggerLevel(final Level level, final LoggerType loggerType) {
		
		if (loggerType == LoggerType.ALL) {
			
			System.setProperty("consolelevel", level.toString());
			System.setProperty("filelevel", level.toString());
			
		} else {
		
			if (loggerType == LoggerType.CONSOLE) {
				System.setProperty("consolelevel", level.toString());
			} else if (loggerType == LoggerType.FILE) {
				System.setProperty("filelevel", level.toString());
			}
		
		}
		
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		ctx.reconfigure();
		
	}

	@Override
	public String getPropertyString(final String key) {
		
		if (settings.containsKey(key)) {
			return settings.get(key);
		}
		
		if (defaultProperties.containsKey(key)) {
			log.debug("Property '{}' not found in database. Using default value: '{}'", key, defaultProperties.getProperty(key));
			return defaultProperties.getProperty(key);
		} else {
			log.warn("Could not find property: {}", key);
			return null;
		}
		
	}

	@Override
	public int getPropertyInt(final String key) {
		return Integer.parseInt(getPropertyString(key));
	}

	@Override
	public double getPropertyDouble(final String key) {
		return Double.parseDouble(getPropertyString(key));
	}

	@Override
	public boolean getPropertyBoolean(final String key) {
		return Boolean.parseBoolean(getPropertyString(key));
	}

	@Override
	public Path getPropertyPath(final String key) {
		return Paths.get(getPropertyString(key));
	}

	@Override
	public Level getPropertyLevel(final String key) {
		return Level.toLevel(getPropertyString(key));
	}

	@Override
	public void setProperty(final String key, final Object property) {
		settings.put(key, property.toString());
		notifyObservers("propertychanged:" + key);
		database.setProperty(key, property);
		log.debug("Property Changed: {} -- {}", key, property);
	}

	@Override
	public String getVersion() {
		return VERSION_STRING;
	}

	@Override
	public void addObserver(final Observer observer) {
		observers.add(observer);
	}

	@Override
	public void removeObserver(final Observer observer) {
		observers.remove(observer);
	}
	
	private final void notifyObservers(final Object message) {
		for (final Observer o : observers) {
			o.update(this, message);
		}
	}
	
}