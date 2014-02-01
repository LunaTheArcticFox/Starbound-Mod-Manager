package main.java.net.krazyweb.starmodmanager.data;

import java.io.UnsupportedEncodingException;
import java.util.MissingResourceException;
import java.util.Observable;
import java.util.ResourceBundle;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

import org.apache.log4j.Logger;

import com.ibm.icu.text.MessageFormat;

public class Localizer extends Observable implements Progressable {
	
	private static final Logger log = Logger.getLogger(Localizer.class);
	
	private static Localizer instance;
	
	private Task<?> task;
	private ReadOnlyDoubleProperty progress;
	private ReadOnlyStringProperty message;
	
	private ResourceBundle bundle;
	
	private Localizer() {
		
	}
	
	public static Localizer getInstance() {
		if (instance == null) {
			synchronized (Localizer.class) {
				instance = new Localizer();
			}
		}
		return instance;
	}
	
	public void initialize() {
		
		task = new Task<Void>() {

			@Override
			protected Void call() throws Exception {

				this.updateMessage("Loading Localizer");
				this.updateProgress(0.0, 1.0);

				bundle = ResourceBundle.getBundle("strings", Settings.getLocale());
				
				this.updateProgress(1.0, 1.0);
				
				return null;
				
			}
			
		};
		
		task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(final WorkerStateEvent event) {
				setChanged();
				notifyObservers("localizerloaded");
			}
		});
		
		this.setProgress(task.progressProperty());
		this.setMessage(task.messageProperty());
		
	}
	
	public String getMessage(final String key, final boolean suppressLogging) {
		
		String output = "";
		
		try {
			output = bundle.getString(key.toLowerCase());
		} catch (final NullPointerException e) {
			if (!suppressLogging) {
				log.warn("Localization key is null.");
			}
			return "Localization key is null.";
		} catch (final MissingResourceException e) {
			if (!suppressLogging) {
				log.warn("Key '" + key + "' not found.");
			}
			return "Key '" + key + "' not found.";
		} catch (final ClassCastException e) {
			if (!suppressLogging) {
				log.warn("Value found for key '" + key + "' is not a String.");
			}
			return "Value found for key '" + key + "' is not a String.";
		}
		
		String formatted = "CHAR ENCODING ERROR";
		
		try {
			formatted = new String(output.getBytes("ISO-8859-1"), "UTF-8");
		} catch (final UnsupportedEncodingException e) {
			if (!suppressLogging) {
				log.error("Could not encode '" + output + "' to UTF-8.", e);
			}
		}

		if (!suppressLogging) {
			log.debug("String '" + output + "' converted to '" + formatted + "'.");
		}
		
		return formatted;
		
	}
	
	public String getMessage(final String key) {
		return getMessage(key, false);
	}
	
	public String formatMessage(final boolean suppressLogging, final String key, final Object... messageArguments) {
		
		MessageFormat formatter = null;
		
		try {
			formatter = new MessageFormat(bundle.getString(key.toLowerCase()), Settings.getLocale());
		} catch (final IllegalArgumentException e) {
			if (!suppressLogging) {
				log.warn("Could not parse pattern for '" + key.toLowerCase() + "':", e);
			}
		}
		
		if (formatter == null) {
			return "INVALID PROPERTY: " + key;
		}
		
		formatter.setLocale(Settings.getLocale());
		
		String formatted = "CHAR ENCODING ERROR";
		
		try {
			formatted = new String(formatter.format(messageArguments).getBytes("ISO-8859-1"), "UTF-8");
		} catch (final UnsupportedEncodingException e) {
			if (!suppressLogging) {
				log.error("Could not encode '" + formatter.format(messageArguments) + "' to UTF-8.", e);
			}
		}
		
		if (!suppressLogging) {
			log.debug("String '" + formatter.format(messageArguments) + "' encoded to '" + formatted + "'.");
		}
		
		return formatted;
		
	}
	
	public String formatMessage(final String key, final Object... messageArguments) {
		return formatMessage(false, key, messageArguments);
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
		thread.setName("Localizer Task Thread");
		thread.setDaemon(true);
		thread.start();
	}
	
}